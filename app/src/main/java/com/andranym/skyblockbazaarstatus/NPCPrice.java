package com.andranym.skyblockbazaarstatus;

public class NPCPrice {
    public double getPrice(String item){
        switch (item){
            case "DIAMOND":
                return 8.0;
            case "COAL":
                return 2.0;
            default:
                return 0;
        }
    }
}
