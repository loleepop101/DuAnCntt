# Object-Oriented Solution for Mining Top-K Closed High Expected Utility Itemsets from uncertain transaction databases

## 📋 Tổng quan Dự án

- **Tên dự án**: U-TOPK-CLOSED-MINER
- **Mục tiêu**: So sánh hiệu năng của 3 thuật toán khai phá Top-K Closed HUIs từ uncertain databases
- **Ngôn ngữ lập trình**: Java (JDK 17+)
- **Design Patterns**: Strategy Pattern + Template Method Pattern
- **Các thuật toán đã cài đặt**:
  1. **U-TKU** - Uncertain Tree-based (Two-Phase với UP-Tree)
  2. **U-TKO** - Uncertain Top-K One-phase (Utility-List based)
  3. **U-EFIM** - Uncertain Efficient Itemset Mining (Projection-based)

## 🎯 Khái niệm Cốt lõi

### Uncertain Data Mining
- Mỗi item có **xác suất tồn tại** (existential probability): 0 < p ≤ 1
- **Expected Utility**: `EU(itemset, tid) = [Σ utility] × [Π probability]`
- **Expected Support**: Tổng xác suất của các transaction chứa itemset

### Top-K Mining
- **Không có ngưỡng cố định** - Tìm chính xác K itemsets có expected utility cao nhất
- **Dynamic threshold raising**: minUtility được cập nhật khi tìm thấy pattern tốt hơn
- Sử dụng **Priority Queue (Min-Heap)** để duy trì top-K candidates

### Closed Itemsets
- Loại bỏ dư thừa bằng cách chỉ giữ **closed patterns**
- Itemset X là closed nếu ∀Y ⊃ X: support(Y) ≠ support(X)
- Giảm kích thước kết quả mà không mất thông tin

## 📂 Cấu trúc Dự án

```
TopK-CHUI-Uncertain/
│
├── src/com/project/
│   │
│   ├── model/                      # [Data Layer] Các thực thể dữ liệu cơ bản
│   │   ├── Item.java               # Lưu itemId, utility, probability
│   │   ├── Transaction.java        # Danh sách items + transaction utility
│   │   ├── Dataset.java            # Toàn bộ cơ sở dữ liệu transaction
│   │   └── Itemset.java            # Kết quả khai phá (items, support, EU)
│   │
│   ├── algorithms/                 # [Logic Layer] Ba thuật toán khai phá
│   │   │
│   │   ├── base/                   # Các lớp trừu tượng dùng chung
│   │   │   ├── MiningAlgorithm.java # Strategy interface (Template Method)
│   │   │   ├── Stats.java          # Thống kê hiệu năng
│   │   │   └── TopKQueue.java      # Priority Queue quản lý Top-K
│   │   │
│   │   ├── utku/                   # [Algo 1] Tree-based (Baseline)
│   │   │   ├── UTKU_Miner.java     # Logic chính - Two-phase mining
│   │   │   ├── UPTree.java         # Cấu trúc cây UP-Tree
│   │   │   └── UPNode.java         # Node của cây
│   │   │
│   │   ├── utko/                   # [Algo 2] Utility-List based
│   │   │   ├── UTKO_Miner.java     # Logic chính - One-phase vertical mining
│   │   │   ├── UtilityList.java    # Cấu trúc dữ liệu dọc
│   │   │   └── Element.java        # Phần tử list (tid, iutil, rutil)
│   │   │
│   │   └── uefim/                  # [Algo 3] Projection-based 
│   │       ├── UEFIM_Miner.java    # Logic chính - DFS 
│   │
│   ├── manager/                    # [IO Layer] Xử lý Input/Output
│   │   ├── DataLoader.java         # Parse file datasets
│   │   └── ResultWriter.java       # Ghi kết quả CSV
│   │
│   ├── utils/                      # [Utilities] Các class hỗ trợ
│   │   ├── MemoryLogger.java       # Đo RAM sử dụng
│   │   └── MathUtils.java          # So sánh số thực (epsilon-based)
│   │
│   └── MainTest.java               # [Runner] Điều phối thực nghiệm
│
├── data/                           # Input datasets
│   ├── foodmart.txt                # Dataset bán lẻ
│   └── liquor.txt                  # Dataset cửa hàng rượu
│
└── output/                         # Kết quả thực nghiệm
    └── experiments_result.csv      # So sánh hiệu năng CSV
```

## 📄 Định dạng Dữ liệu Đầu vào

**Format**: `Items:TU:Utilities:Probabilities`

**Ví dụ một dòng**:
```
1 3:10:5 2:0.9 0.6
```

**Giải thích**:
- **Items**: {1, 3}
- **Transaction Utility (TU)**: 10
- **Item Utilities**: item1=5, item3=2
- **Probabilities**: P(item1)=0.9, P(item3)=0.6

## 🚀 Bắt đầu

### Yêu cầu
- **Java JDK 17+**
- Command line hoặc IDE (VS Code, IntelliJ IDEA, Eclipse)

### Biên dịch

Từ thư mục gốc dự án:
```bash
cd TopK-CHUI-Uncertain
javac -d bin -sourcepath src src/com/project/MainTest.java
```

### Chạy Thực nghiệm

#### Chạy mặc định (Tất cả thuật toán, nhiều giá trị K)
```bash
java -cp bin com.project.MainTest
```

Chương trình sẽ:
- Load `data/foodmart.txt`
- Chạy U-TKU, U-TKO, U-EFIM
- Test với K = [10, 50, 100, 500]
- Xuất kết quả ra `output/experiments_result.csv`

#### Dataset tùy chỉnh
Chỉnh sửa [`MainTest.java`](src/com/project/MainTest.java):
```java
String[] datasets = {"data/foodmart.txt", "data/liquor.txt"};
int[] kList = {10, 50, 100};
```

## 🧪 Chi tiết Thuật toán

### 1. U-TKU (Tree-based - Baseline)
* **Chiến lược cốt lõi:** Two-Phase Mining (Sinh ứng viên & Kiểm tra).
* **Cơ chế hoạt động:**
    * **Phase 1 (Construction):** Xây dựng cấu trúc **UP-Tree** (Uncertain Pattern Tree). Mỗi node lưu trữ thông tin để ước lượng cận trên (TWU/Estimated Utility).
    * **Mining:** Duyệt cây theo phương pháp đệ quy để sinh ra các tập ứng viên tiềm năng (Potential HUIs - PHUIs).
    * **Phase 2 (Verification):** Với mỗi tập ứng viên, quay lại quét Database gốc để tính toán Utility chính xác.
* **Đặc điểm:**
    * ✅ Cấu trúc cây giúp nén dữ liệu tốt nếu các giao dịch lặp lại nhiều.
    * ❌ **Bùng nổ tổ hợp** khi gặp dataset thưa (Sparse) hoặc giá trị K lớn, dẫn đến thời gian chạy cực lâu.
    * ❌ Tốn nhiều bộ nhớ do lưu trữ dữ liệu dưới dạng Object phức tạp (Node, Link).
* **Thích hợp cho:** Dataset kích thước nhỏ hoặc trung bình, dữ liệu dày đặc.

---

### 2. U-TKO (Utility-List based - Trung bình)
* **Chiến lược cốt lõi:** One-Phase Vertical Mining (Khai thác theo chiều dọc).
* **Cơ chế hoạt động:**
    * **Cấu trúc dữ liệu:** Sử dụng **Utility-List**. Mỗi tập mục $X$ được gắn với một danh sách các phần tử chứa `{tid, iutil, rutil}`.
    * **Join Logic:** Sử dụng công thức hợp nhất chính xác để loại bỏ phần Utility bị tính trùng lặp:
        $$U(X \cup Y) = U(X) + U(Y) - U(X \cap Y)$$
    * **Pruning:** Cắt tỉa nhánh tìm kiếm ngay lập tức nếu `sum(iutil + rutil) < minUtility`.
* **Đặc điểm:**
    * ✅ Không cần quét lại toàn bộ DB nhiều lần.
    * ✅ Tốc độ nhanh hơn U-TKU trên hầu hết các bộ dữ liệu.
    * ✅ Cắt tỉa không gian tìm kiếm hiệu quả nhờ cập nhật ngưỡng `minUtility` liên tục từ Top-K Buffer.
* **Thích hợp cho:** Dataset kích thước trung bình đến lớn.

---

### 3. U-EFIM (Projection-based - Tốt nhất)
* **Chiến lược cốt lõi:** Depth-First Search trên **Primitive Arrays**.
* **Cơ chế hoạt động:**
    * **Database Projection:** Thay vì quét toàn bộ DB, thuật toán tạo ra các "Database chiếu" ảo (chỉ gồm các index `int[]`) chứa các giao dịch liên quan đến item đang xét.
    * **Pruning:** Sử dụng kỹ thuật **Sub-tree Utility** và **Local Utility** để cắt tỉa không gian tìm kiếm .
* **Đặc điểm:**
    * ✅ **Tốc độ nhanh nhất** (Vượt trội hoàn toàn trên dataset lớn, thưa và nhiều items).
    * ✅ **Bộ nhớ thấp nhất** (Do không tốn overhead cho Object Header của Java).
    * ✅ Khả năng mở rộng (Scalability) tốt nhất khi K tăng.
* **Thích hợp cho:** Mọi loại Dataset.

## 📊 Output mẫu

### File CSV (`output/experiments_result.csv`)

| Algorithm | Dataset | K | Runtime(ms) | Memory(MB) | PatternCount | MinUtilityThreshold |
|-----------|---------|---|-------------|------------|--------------|---------------------|
| U-TKU | foodmart.txt | 100 | 519 | 50.84 | 100 | 11870.83000 |
| U-TKO | foodmart.txt | 100 | 350 | 65.98 | 100 | 11870.83000 |
| U-EFIM | foodmart.txt | 100 | 107 | 7.82 | 100 | 11870.83000 |


## 🔍 So sánh Hiệu năng & Đặc tả Kỹ thuật

| Chỉ số | U-TKU (Baseline) | U-TKO | U-EFIM |
| :--- | :--- | :--- | :--- |
| **Tốc độ** | Chậm nhất (đặc biệt khi K lớn) | Trung bình / Nhanh | **Nhanh nhất** (Ổn định nhất) |
| **Bộ nhớ (RAM)** | Cao | Trung bình | **Thấp nhất**  |
| **Cấu trúc dữ liệu** | UP-Tree | Utility-List | Primitive Arrays |
| **Số lần quét DB** | 2 lần (Phase 1 & 2) | 1 lần (Dựng List) | 1 lần (Load Mảng) |
| **Chiến lược Pruning** | TWU | Sum(iutil + rutil) | **Sub-tree Utility** |
| **Cơ chế đặc thù** | Two-Phase Mining | Join & Intersection | **Database Projection** |
| **Độ chính xác** | Thấp | Khá Cao | Cao |

**Cập nhật lần cuối**: Tháng 12, 2025