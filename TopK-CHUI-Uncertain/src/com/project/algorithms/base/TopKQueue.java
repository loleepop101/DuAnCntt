package com.project.algorithms.base;

import com.project.model.Itemset;
import java.util.PriorityQueue;

/**
 * A specialized priority queue for maintaining the Top-K high utility itemsets.
 * Automatically handles eviction of the lowest utility itemset when the capacity is reached.
 */
public class TopKQueue {
    private PriorityQueue<Itemset> queue;
    private int k;

    public TopKQueue(int k) {
        this.k = k;
        this.queue = new PriorityQueue<>(); 
    }

    /**
     * Attempts to add an itemset to the queue.
     * @param itemset The candidate itemset to add.
     * @return The evicted itemset if one was removed to make room, null otherwise.
     */
    public Itemset add(Itemset itemset) {
        if (queue.size() < k) {
            queue.add(itemset);
            return null;
        } else {
            Itemset bottom = queue.peek();
            
            // If new item is better than the worst in Top-K
            if (itemset.compareTo(bottom) > 0) {
                Itemset evicted = queue.poll();
                queue.add(itemset);
                return evicted;
            }
        }
        return null;
    }

    public boolean remove(Itemset itemset) {
        return queue.remove(itemset);
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
