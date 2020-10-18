package com.andranym.skyblockbazaarstatus;

public class Favorite {
    public Favorite(String itemTitle, String itemDesc) {
        this.itemTitle = itemTitle;
        this.itemPrices = itemDesc;
    }

    private String itemTitle;
    private String itemPrices;

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemDesc() {
        return itemPrices;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public void setItemDesc(String itemDesc) {
        this.itemPrices = itemDesc;
    }
}
