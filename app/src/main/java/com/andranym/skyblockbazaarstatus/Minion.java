package com.andranym.skyblockbazaarstatus;

public class Minion {
    public Minion(String title, String tierProfits, String products, String itemsPerAction, String NPCPrices,String bazaarPrices, boolean fuelType, int fuelNumber, double additionalMultiplier, String upgrade1, String upgrade2, double petBoost, double miscBoost1, double miscBoost2, String warnings, double rankings) {
        this.title = title;
        this.tierProfits = tierProfits;
        this.products = products;
        this.bazaarPrices = bazaarPrices;
        this.NPCPrices = NPCPrices;
        this.fuelType = fuelType;
        this.fuelNumber = fuelNumber;
        this.additionalMultiplier = additionalMultiplier;
        this.upgrade1 = upgrade1;
        this.upgrade2 = upgrade2;
        this.petBoost = petBoost;
        this.miscBoost1 = miscBoost1;
        this.miscBoost2 = miscBoost2;
        this.warnings = warnings;
        this.rankings = rankings;
        this.itemsPerAction = itemsPerAction;
    }

    private String title;
    private String tierProfits;
    private String products;
    private String bazaarPrices;
    private String NPCPrices;
    private String itemsPerAction;
    //True for regular fuel, false for catalyst
    private boolean fuelType;
    private int fuelNumber;
    private double additionalMultiplier;
    private String upgrade1;
    private String upgrade2;
    private double petBoost;
    private double miscBoost1;
    private double miscBoost2;
    private String warnings;
    private double rankings;

    //region Getters
    public String getProducts() {
        return products;
    }

    public String getItemsPerAction() {
        return itemsPerAction;
    }

    public String getBazaarPrices() {
        return bazaarPrices;
    }

    public String getNPCPrices() {
        return NPCPrices;
    }

    public String getTitle() {
        return title;
    }

    public String getTierProfits() {
        return tierProfits;
    }

    public boolean isFuelType() {
        return fuelType;
    }

    public int getFuelNumber() {
        return fuelNumber;
    }

    public double getAdditionalMultiplier() {
        return additionalMultiplier;
    }

    public String getUpgrade1() {
        return upgrade1;
    }

    public String getUpgrade2() {
        return upgrade2;
    }

    public double getPetBoost() {
        return petBoost;
    }

    public double getMiscBoost1(){
        return miscBoost1;
    }

    public double getMiscBoost2(){
        return miscBoost2;
    }

    public String getWarnings() {
        return warnings;
    }

    public double getRankings() {
        return rankings;
    }
    //endregion
}
