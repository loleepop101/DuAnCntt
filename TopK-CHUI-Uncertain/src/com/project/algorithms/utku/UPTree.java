package com.project.algorithms.utku;

import com.project.model.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPTree {
    private UPNode root;
    
    // Lưu node đầu tiên của mỗi item
    private Map<Integer, UPNode> headerTable; 
    
    // Map phụ để trỏ tới node cuối cùng của mỗi item 
    private Map<Integer, UPNode> lastNodeLink; 

    public UPTree() {
        this.root = new UPNode(-1, 0);
        this.headerTable = new HashMap<>();
        this.lastNodeLink = new HashMap<>();
    }

    /**
     * Chèn một giao dịch (đã được sắp xếp) vào cây
     */
    public void addTransaction(List<Item> sortedItems) {
        UPNode currentNode = root;

        for (Item item : sortedItems) {
            int itemId = item.getItemId();
            double expectedUtil = item.getExpectedUtility();

            UPNode child = currentNode.getChild(itemId);

            if (child == null) {
                // Tạo node mới
                child = new UPNode(itemId, expectedUtil);
                currentNode.addChild(child);

                // Cập nhật Header Table & Node Links
                updateHeaderLink(child);
            } else {
                // Node đã tồn tại -> Cộng dồn utility
                child.increaseUtility(expectedUtil);
            }

            currentNode = child; // Đi xuống
        }
    }

    private void updateHeaderLink(UPNode newNode) {
        int itemId = newNode.getItemId();
        if (headerTable.containsKey(itemId)) {
            // Nối vào đuôi của node cuối cùng
            UPNode last = lastNodeLink.get(itemId);
            last.setNodeLink(newNode);
            lastNodeLink.put(itemId, newNode);
        } else {
            // Node đầu tiên của item này
            headerTable.put(itemId, newNode);
            lastNodeLink.put(itemId, newNode);
        }
    }

    public UPNode getRoot() { return root; }
    public Map<Integer, UPNode> getHeaderTable() { return headerTable; }
}