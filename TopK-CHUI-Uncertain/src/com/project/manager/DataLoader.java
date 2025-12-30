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
                // Ignore comments and empty lines
                if (line.trim().isEmpty() || line.startsWith("%") || line.startsWith("#")) continue;
                
                // Format: Items:TU:Utilities:Probabilities
                String[] parts = line.split(":");
                
                if (parts.length < 4) {
                    continue; 
                }

                try {
                    // Parse Items
                    String[] itemStr = parts[0].trim().split("\\s+");
                    // Parse Transaction Utility (TU)
                    double transactionUtility = Double.parseDouble(parts[1]); 
                    // Parse Item Utilities
                    String[] utilStr = parts[2].trim().split("\\s+");
                    // Parse Probabilities
                    String[] probStr = parts[3].trim().split("\\s+");

                    // Validation: All arrays must be same length
                    if (itemStr.length != utilStr.length || itemStr.length != probStr.length) {
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

                    // Create Transaction and add to dataset
                    Transaction t = new Transaction(items, transactionUtility);
                    dataset.addTransaction(t);
                    
                } catch (NumberFormatException e) {
                   // Skip specific malformed lines without crashing
                   System.err.println("Skipping malformed line: " + line);
                }
            }
            
            dataset.setMaxItemId(maxItemId); 

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return dataset;
    }
}