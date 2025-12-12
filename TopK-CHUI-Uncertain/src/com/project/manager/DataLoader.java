package com.project.manager;

import com.project.model.Dataset;
import com.project.model.Item;
import com.project.model.Transaction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static Dataset load(String path) {
        Dataset dataset = new Dataset();
        int maxItemId = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("%") || line.startsWith("#")) continue;
                
                // Format: Items:TU:Utilities:Probabilities
                String[] parts = line.split(":");
                
                if (parts.length < 4) {
                    System.err.println("Skipping malformed line (missing parts): " + line);
                    continue; 
                }

                // Parse các phần
                String[] itemStr = parts[0].trim().split(" ");
                double transactionUtility = Double.parseDouble(parts[1]); 
                String[] utilStr = parts[2].trim().split(" ");
                String[] probStr = parts[3].trim().split(" ");

                // Check số lượng phải khớp nhau
                if (itemStr.length != utilStr.length || itemStr.length != probStr.length) {
                    System.err.println("Skipping malformed line (length mismatch): " + line);
                    continue;
                }

                List<Item> items = new ArrayList<>();
                for (int i = 0; i < itemStr.length; i++) {
                    int itemId = Integer.parseInt(itemStr[i]);
                    double utility = Double.parseDouble(utilStr[i]);
                    double probability = Double.parseDouble(probStr[i]);
                    
                    items.add(new Item(itemId, utility, probability));
                    
                    if (itemId > maxItemId) {
                        maxItemId = itemId;
                    }
                }

                Transaction t = new Transaction(items, transactionUtility, 1.0);
                dataset.addTransaction(t);
            }
            
            dataset.setMaxItemId(maxItemId); 

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number in file: " + e.getMessage());
        }
        
        System.out.println("Dataset loaded. Transactions: " + dataset.getTransactions().size() + ", Max ItemID: " + maxItemId);
        return dataset;
    }
}