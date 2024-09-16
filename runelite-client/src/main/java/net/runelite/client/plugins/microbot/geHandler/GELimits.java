package net.runelite.client.plugins.microbot.geHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

public class GELimits {
    private static final Map<String, ItemLimit> itemLimits = new HashMap<>();
    private static final Map<String, Integer> buyLimits = new HashMap<>();



    private static class ItemLimit {
        int remainingLimit;
        Instant lastBuyTime;

        ItemLimit(int limit) {
            this.remainingLimit = limit;
            this.lastBuyTime = Instant.now();
        }
    }

    public static void setRemainingBuyLimit(String itemName1, int boughtQuantity) {
        String itemName = itemName1.toLowerCase();
        itemLimits.computeIfAbsent(itemName, k -> new ItemLimit(getDefaultLimit(itemName)));
        ItemLimit limit = itemLimits.get(itemName);

        // Reset limit if 4 hours have passed
        if (Instant.now().minusSeconds(4 * 3600).isAfter(limit.lastBuyTime)) {
            limit.remainingLimit = getDefaultLimit(itemName);
        }

        limit.remainingLimit = Math.max(0, limit.remainingLimit - boughtQuantity);
        limit.lastBuyTime = Instant.now();
    }

    public static int getRemainingBuyLimit(String itemName1) {
        String itemName = itemName1.toLowerCase();
        itemLimits.computeIfAbsent(itemName, k -> new ItemLimit(getDefaultLimit(itemName)));
        ItemLimit limit = itemLimits.get(itemName);

        // Reset limit if 4 hours have passed
        if (Instant.now().minusSeconds(4 * 3600).isAfter(limit.lastBuyTime)) {
            limit.remainingLimit = getDefaultLimit(itemName);
            limit.lastBuyTime = Instant.now();
        }

        return limit.remainingLimit;
    }

    public static void printUpdatedBuyLimits() {
        System.out.println("Updated Buy Limits:");
        for (Map.Entry<String, ItemLimit> entry : itemLimits.entrySet()) {
            String itemName = entry.getKey();
            ItemLimit limit = entry.getValue();
            int defaultLimit = getDefaultLimit(itemName);

            // Check if the current time is within 4 hours of the last buy
            boolean isWithinFourHours = Instant.now().minusSeconds(4 * 3600).isBefore(limit.lastBuyTime);

            // Only print if the remaining limit is less than the default and within 4 hours of last buy
            if (limit.remainingLimit < defaultLimit && isWithinFourHours) {
                System.out.printf("%-30s: %d/%d%n", itemName, limit.remainingLimit, defaultLimit);
            }
        }
    }

    private static int getDefaultLimit(String itemName) {
        return buyLimits.getOrDefault(itemName, 10000); // Default to 10000 if not specified
    }
    private static void loadBuyLimits() {
        try (InputStream inputStream = GELimits.class.getResourceAsStream("/buy_limits.txt");
             BufferedReader reader = inputStream != null ? new BufferedReader(new InputStreamReader(inputStream)) : null) {

            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: /buy_limits.txt");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String itemName = parts[0].trim().toLowerCase();
                    int limit = Integer.parseInt(parts[1].trim());
                    buyLimits.put(itemName, limit);
                } else {
                    System.err.println("Invalid line format: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        loadBuyLimits();
    }
}