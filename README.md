# Giải pháp Hướng đối tượng Khai phá Top-K Closed High Expected Utility Itemsets từ Cơ sở dữ liệu Giao dịch Không chắc chắn

##  Tổng quan Dự án
- **Tên dự án**: U-TOPK-CLOSED-MINER
- **Mục tiêu**: Nghiên cứu và so sánh hiệu năng của 3 thuật toán tiên tiến trong việc khai phá các tập mục hữu dụng cao đóng (Closed High Utility Itemsets) trên dữ liệu không chắc chắn (Uncertain Data).
- **Ngôn ngữ**: Java (JDK 17+)
- **Kiến trúc**: Áp dụng Design Patterns (Strategy, Template Method) để đảm bảo tính mở rộng và dễ bảo trì.

##  Khái niệm Cốt lõi

### 1. Khai phá Dữ liệu Không chắc chắn (Uncertain Data Mining)
Khác với dữ liệu truyền thống, mỗi item trong một giao dịch đi kèm với một **xác suất tồn tại** ($0 < p \le 1$).
- **Expected Utility (EU)**: Độ hữu dụng kỳ vọng của tập mục $X$ trong giao dịch $T$ được tính bằng công thức:
  $$EU(X, T) = \left( \sum_{i \in X} u(i, T) \right) \times \left( \prod_{i \in X} P(i, T) \right)$$
- **Expected Support**: Tổng xác suất tồn tại của tập mục trên toàn bộ cơ sở dữ liệu.

### 2. Khai phá Top-K
Thay vì yêu cầu người dùng nhập ngưỡng hữu dụng tối thiểu (`minUtil`) khó xác định, thuật toán sẽ tìm **K tập mục** có độ hữu dụng kỳ vọng cao nhất. Ngưỡng `minUtil` sẽ được tự động cập nhật tăng dần trong quá trình khai phá.

### 3. Tập mục Đóng (Closed Itemsets)
Một tập mục được gọi là **đóng** nếu không tồn tại tập siêu (superset) nào của nó có cùng giá trị support. Việc khai phá tập đóng giúp giảm đáng kể số lượng kết quả dư thừa mà không làm mất thông tin quan trọng.

##  Cấu trúc Mã nguồn

```
TopK-CHUI-Uncertain/
 src/com/project/
    model/              # Lớp dữ liệu (Item, Transaction, Dataset, Itemset)
    algorithms/         # Logic các thuật toán
       base/           # Lớp cơ sở, quản lý Top-K Queue và Closed Constraint
       utku/           # Thuật toán U-TKU (Dựa trên cây UP-Tree)
       utko/           # Thuật toán U-TKO (Dựa trên Utility-List)
       uefim/          # Thuật toán U-EFIM (Dựa trên Database Projection)
    manager/            # Xử lý đọc file (DataLoader) và ghi kết quả (ResultWriter)
    utils/              # Tiện ích đo bộ nhớ, so sánh số thực epsilon
    MainTest.java       # Lớp điều phối thực nghiệm
 data/                   # Các bộ dữ liệu mẫu (foodmart, liquor)
 output/                 # Kết quả thực nghiệm (CSV)
```

##  Hướng dẫn Chạy

### Biên dịch
```bash
javac -d bin -sourcepath src src/com/project/MainTest.java
```

### Thực thi
```bash
java -cp bin com.project.MainTest
```

##  Phân tích Thuật toán & Hiệu năng

Dựa trên kết quả thực nghiệm mới nhất tại `output/experiments_result.csv`:

| Thuật toán | Đặc điểm kỹ thuật | Hiệu năng thực tế |
| :--- | :--- | :--- |
| **U-TKU** | Sử dụng cấu trúc cây **UP-Tree** và quy trình 2 pha (Candidate Generation & Verification). | Chạy nhanh trên dataset nhỏ (`foodmart`), nhưng cực chậm trên dataset lớn (`liquor` mất >160s cho K=500). Tiết kiệm RAM nhất. |
| **U-TKO** | Khai phá 1 pha dựa trên **Utility-List**. Cắt tỉa dựa trên tổng `iutil + rutil`. | Hiệu năng ổn định, nhanh hơn U-TKU nhiều lần trên dữ liệu lớn (chỉ mất ~9s cho `liquor` K=500). |
| **U-EFIM** | Sử dụng mảng nguyên thủy (**Primitive Arrays**) và kỹ thuật chiếu DB (**Projection**). | **Tốc độ nhanh nhất** trong mọi trường hợp (chỉ ~5s cho `liquor` K=500). Tuy nhiên, tiêu tốn RAM hơn do cấu trúc mảng chiếu. |

### Kết quả mẫu (Dataset: liquor.txt, K=500)
- **U-TKU**: 164,406 ms | 67.80 MB
- **U-TKO**: 9,042 ms | 43.82 MB
- **U-EFIM**: 5,144 ms | 124.44 MB

##  Định dạng Dữ liệu
File đầu vào phải tuân thủ định dạng: `Items:TU:Utilities:Probabilities`
Ví dụ: `1 3:10:5 2:0.9 0.6`
- Items: 1 và 3
- TU (Transaction Utility): 10
- Utilities: item 1 có u=5, item 3 có u=2
- Probabilities: item 1 có p=0.9, item 3 có p=0.6

---
**Cập nhật**: 30/12/2025.
b-tree Utility** |
| **Cơ chế đặc thù** | Two-Phase Mining | Join & Intersection | **Database Projection** |
| **Độ chính xác** | Thấp | Khá Cao | Cao |

**Cập nhật lần cuối**: Tháng 12, 2025