package com.project.algorithms.utku;

import com.project.algorithms.base.MiningAlgorithm;
import com.project.algorithms.base.Stats;
import com.project.algorithms.base.TopKQueue;
import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Itemset;
import com.project.model.Transaction;
import com.project.utils.MemoryLogger;

import java.util.*;

public class UTKU_Miner extends MiningAlgorithm {

    private Map<Integer, Double> mapItemToETWU; // Dùng để sort item
    private Dataset database; // Lưu tham chiếu DB để verify (Phase 2)

    @Override
    public Stats runAlgorithm(Dataset db, int k) {
        Stats stats = new Stats("U-TKU");
        this.database = db; 
        MemoryLogger.getInstance().reset();
        long start = System.currentTimeMillis();
        
        this.topKBuffer = new TopKQueue(k);
        this.minUtility = 0;
        this.mapItemToETWU = new HashMap<>();

        // Bước 1: Tính Global ETWU & Lọc các item rác
        for (Transaction t : db.getTransactions()) {
            double tu = t.getTransactionUtility();
            for (Item item : t.getItems()) {
                double contribution = tu * item.getProbability();
                mapItemToETWU.put(item.getItemId(), 
                    mapItemToETWU.getOrDefault(item.getItemId(), 0.0) + contribution);
            }
        }

        // Bước 2: Xây dựng UP-Tree Gốc (Global Tree)
        UPTree tree = new UPTree();
        
        for (Transaction t : db.getTransactions()) {
            // Lọc và Sắp xếp item theo ETWU giảm dần
            List<Item> sortedItems = new ArrayList<>();
            for (Item item : t.getItems()) {
                // Pruning sơ bộ: Chỉ giữ item có tiềm năng > minUtil (nếu minUtil > 0)
                if (mapItemToETWU.get(item.getItemId()) >= minUtility) {
                    sortedItems.add(item);
                }
            }
            // Sort Descending by ETWU
            sortedItems.sort((a, b) -> Double.compare(
                mapItemToETWU.get(b.getItemId()), 
                mapItemToETWU.get(a.getItemId())
            ));

            tree.addTransaction(sortedItems);
        }

        // Bước 3: Mining Đệ quy
        // Bắt đầu đệ quy với prefix rỗng
        mine(tree, new ArrayList<>());

        MemoryLogger.getInstance().checkMemory();
        long end = System.currentTimeMillis();
        
        // Tổng hợp thống kê
        stats.setRuntime(end - start);
        stats.setMemory(MemoryLogger.getInstance().getMaxMemory());
        stats.setPatternCount(topKBuffer.size());
        stats.setMinUtilThreshold(minUtility);
        
        return stats;
    }

    /**
     * Hàm đệ quy khai thác mẫu từ cây UP-Tree
     * @param tree Cây hiện tại (Global hoặc Conditional)
     * @param prefix Mẫu tiền tố (Itemset đang xét)
     */
    private void mine(UPTree tree, List<Integer> prefix) {
        // 1. Duyệt Header Table từ dưới lên trên (Bottom-Up)
        List<Integer> items = new ArrayList<>(tree.getHeaderTable().keySet());
        
        // Sort items theo ETWU tăng dần 
        // Lúc xây cây sort Giảm Dần, giờ duyệt ngược lại là Tăng Dần 
        items.sort((a, b) -> Double.compare(
            mapItemToETWU.get(a), 
            mapItemToETWU.get(b)
        ));

        for (Integer itemId : items) {
            // Tạo mẫu mới: Prefix cũ + Item hiện tại
            List<Integer> newPattern = new ArrayList<>(prefix);
            newPattern.add(itemId);

            // Tính tổng Utility ước lượng của mẫu này trên cây hiện tại
            double estimatedUtility = calculateEstimatedUtility(tree, itemId);

            // Pruning: Nếu ước lượng còn nhỏ hơn minUtility cắt
            if (estimatedUtility >= minUtility) {
                
                // Kiểm tra ứng viên với dữ liệu gốc
                verifyAndAddResult(newPattern);

                // Xây dựng cây điều kiện cho mẫu mới
                UPTree conditionalTree = buildConditionalTree(tree, itemId);

                // Nếu cây con không rỗng, tiếp tục đệ quy mở rộng mẫu
                if (conditionalTree.getRoot().getChildren().size() > 0) {
                    mine(conditionalTree, newPattern);
                }
            }
        }
    }

    /**
     * Xây dựng Cây Điều Kiện (Conditional UP-Tree) cho một item cụ thể
     */
    private UPTree buildConditionalTree(UPTree tree, int itemId) {
        UPTree condTree = new UPTree();
        
        // Bắt đầu từ node đầu tiên trong HeaderList của item đó
        UPNode currentNode = tree.getHeaderTable().get(itemId);
        
        // Duyệt qua tất cả các node cùng itemId trong cây (theo chiều ngang - NodeLink)
        while (currentNode != null) {
            // 1. Truy vết đường đi từ node này ngược lên gốc (Path to Root)
            List<Item> path = new ArrayList<>();
            UPNode parent = currentNode.getParent();
            
            while (parent.getItemId() != -1) { 
                double pathVal = currentNode.getNodeUtility(); 
                
                path.add(0, new Item(parent.getItemId(), pathVal, 1.0)); 
                
                parent = parent.getParent();
            }

            // 2. Thêm path này vào cây con
            if (!path.isEmpty()) {
                condTree.addTransaction(path);
            }

            // Di chuyển sang node tiếp theo cùng loại item
            currentNode = currentNode.getNodeLink();
        }
        
        return condTree;
    }

    /**
     * Tính tổng Utility ước lượng của item trong cây hiện tại (duyệt hàng ngang)
     */
    private double calculateEstimatedUtility(UPTree tree, int itemId) {
        double sum = 0;
        UPNode node = tree.getHeaderTable().get(itemId);
        while (node != null) {
            sum += node.getNodeUtility();
            node = node.getNodeLink();
        }
        return sum;
    }

    /**
     * Kiểm tra ứng viên (Candidate) với dữ liệu gốc để lấy Exact Utility và check Closed
     */
    private void verifyAndAddResult(List<Integer> candidateItems) {
        // 1. Tính toán Exact Utility & Support trong DB gốc
        double actualExpectedUtility = 0;
        int support = 0;

        // Duyệt toàn bộ DB 
        for (Transaction t : database.getTransactions()) {
            if (transactionContainsAll(t, candidateItems)) {
                // Tính utility của tập mục trong giao dịch này
                double u = calculateUtilityInTransaction(t, candidateItems);
                actualExpectedUtility += u;
                support++;
            }
        }

        // 2. Kiểm tra điều kiện Top-K
        if (actualExpectedUtility >= minUtility) {
            Itemset newItemset = new Itemset(candidateItems, actualExpectedUtility, support);
            
            // 3. Kiểm tra tính đóng 
            if (isClosed(newItemset)) {
                savePattern(newItemset); // Add vào Queue & Update minUtility
            }
        }
    }

    // Check transaction chứa itemset
    private boolean transactionContainsAll(Transaction t, List<Integer> items) {
        List<Integer> tItems = new ArrayList<>();
        for(Item i : t.getItems()) tItems.add(i.getItemId());
        return tItems.containsAll(items);
    }

    // Tính Utility thực tế (Sum(u * p))
    private double calculateUtilityInTransaction(Transaction t, List<Integer> items) {
        double sum = 0;
        for (Item tItem : t.getItems()) {
            if (items.contains(tItem.getItemId())) {
                sum += tItem.getExpectedUtility();
            }
        }
        return sum;
    }

    /**
     * Kiểm tra xem itemset mới tìm được (child) có bị bao bởi itemset đã có (parent) không.
     */
    @Override
    protected boolean isClosed(Itemset newSet) {
        // Duyệt queue hiện tại
        for (Itemset existing : topKBuffer.getQueue()) {
            // Nếu existing là tập cha của newSet 
            // và support bằng nhau -> newSet không phải closed.
            if (existing.getItems().containsAll(newSet.getItems()) 
                && existing.getItems().size() > newSet.getItems().size()
                && existing.getSupport() == newSet.getSupport()) {
                return false;
            }
        }
        return true;
    }
}