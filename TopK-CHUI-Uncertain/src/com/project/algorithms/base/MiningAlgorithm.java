package com.project.algorithms.base;

import com.project.model.Dataset;
import com.project.model.Itemset;

public abstract class MiningAlgorithm {
    protected TopKQueue topKBuffer;
    protected double minUtility = 0;

    // Abstract method 
    public abstract Stats runAlgorithm(Dataset db, int k);

    /**
     * Hàm này được gọi khi tìm thấy một tập Closed tiềm năng.
     * Tự động quản lý Top-K queue và cập nhật minUtility.
     */
    protected void savePattern(Itemset itemset) {
        // 1. Kiểm tra tính đóng 
        if (!isClosed(itemset)) {
            return;
        }

        // 2. Thêm vào Queue 
        topKBuffer.add(itemset);

        // 3. Cập nhật ngưỡng cắt tỉa (Pruning threshold)
        if (topKBuffer.isFull()) {
            minUtility = topKBuffer.peek().getUtility();
        }
    }

    protected boolean isClosed(Itemset x) {
        return true; 
    }
}