package com.andranym.skyblockbazaarstatus;

public class GetEnchantedName {

    public String normal(String unenchanted) {
        String returnThis;
        String possible_correction = new FixBadNames().fix(unenchanted);
        if (possible_correction != null) {
            returnThis = "ENCHANTED_" + possible_correction;
        } else {
            returnThis = "ENCHANTED_" + unenchanted;
        }
        return returnThis;
    }

    public String codeName(String unenchanted) {
        String returnThis;
        String possible_correction = new FixBadNames().fix(unenchanted);
        if (possible_correction != null) {
            returnThis = "ENCHANTED_" + possible_correction;
        } else {
            returnThis = "ENCHANTED_" + unenchanted;
        }
        switch (returnThis) {
            case "ENCHANTED_INK_SACK:4":
                returnThis = "ENCHANTED_LAPIS_LAZULI";
                break;
            case "ENCHANTED_COCOA_BEANS":
                returnThis = "ENCHANTED_COCOA";
                break;
            case "ENCHANTED_CARROT_ITEM":
                returnThis = "ENCHANTED_CARROT";
                break;
            case "ENCHANTED_POTATO_ITEM":
                returnThis = "ENCHANTED_POTATO";
                break;
            case "ENCHANTED_NETHER_WART":
                returnThis = "ENCHANTED_NETHER_STALK";
                break;
            case "ENCHANTED_SALMON":
                returnThis = "ENCHANTED_RAW_SALMON";
                break;
            case "ENCHANTED_OAK_WOOD":
                returnThis = "ENCHANTED_OAK_LOG";
                break;
            case "ENCHANTED_SPRUCE_WOOD":
                returnThis = "ENCHANTED_SPRUCE_LOG";
                break;
            case "ENCHANTED_BIRCH_WOOD":
                returnThis = "ENCHANTED_BIRCH_LOG";
                break;
            case "ENCHANTED_DARK_OAK_WOOD":
                returnThis = "ENCHANTED_DARK_OAK_LOG";
                break;
            case "ENCHANTED_ACACIA_WOOD":
                returnThis = "ENCHANTED_ACACIA_LOG";
                break;
            case "ENCHANTED_JUNGLE_WOOD":
                returnThis = "ENCHANTED_JUNGLE_LOG";
                break;
            case "ENCHANTED_SNOW_BALL":
                returnThis = "ENCHANTED_SNOW_BLOCK";
                break;
            case "ENCHANTED_IRON_INGOT":
                returnThis = "ENCHANTED_IRON";
                break;
            case "ENCHANTED_GOLD_INGOT":
                returnThis = "ENCHANTED_GOLD";
                break;
            case "ENCHANTED_WHEAT":
                returnThis = "ENCHANTED_HAY_BLOCK";
                break;
            case "ENCHANTED_SUGAR_CANE":
                returnThis = "ENCHANTED_SUGAR";
                break;
            case "ENCHANTED_BLAZE_ROD":
                returnThis = "ENCHANTED_BLAZE_POWDER";
                break;
            case "ENCHANTED_CACTUS":
                returnThis = "ENCHANTED_CACTUS_GREEN";
                break;


        }
        return returnThis;
    }
}
