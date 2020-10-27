package com.andranym.skyblockbazaarstatus;

public class Stonk {
    public Stonk(String productName, int itemsOwned,String orderHistory) {
        this.productName = productName;
        this.itemsOwned = itemsOwned;
        this.orderHistory = orderHistory;
    }

    private String productName;
    private int itemsOwned;
    private String orderHistory;

    public String getProductName() {
        return productName;
    }

    public int getItemsOwned() {
        return itemsOwned;
    }

    public String getOrderHistory() {
        return orderHistory;
    }
}
