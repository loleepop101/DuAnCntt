package com.project.algorithms.base;

import com.project.model.Itemset;
import java.util.PriorityQueue;

public class TopKQueue {
    private PriorityQueue<Itemset> queue;
    private int k;

    public TopKQueue(int k) {
        this.k = k;
        this.queue = new PriorityQueue<>(); 
    }

    public void add(Itemset itemset) {
        if (queue.size() < k) {
            queue.add(itemset);
        } else {
            // Chỉ thêm nếu tốt hơn phần tử tệ nhất trong Top-K hiện tại
            Itemset bottom = queue.peek();
            
            // So sánh utility: Nếu itemset mới tốt hơn đáy của Top-K
            if (itemset.getUtility() > bottom.getUtility()) {
                queue.poll(); // Loại bỏ phần tử nhỏ nhất
                queue.add(itemset); // Thêm phần tử mới
            }
        }
    }

    public Itemset peek() {
        return queue.peek();
    }
    
    public boolean isFull() {
        return queue.size() >= k;
    }

    public int size() {
        return queue.size();
    }

    public PriorityQueue<Itemset> getQueue() {
        return queue;
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}