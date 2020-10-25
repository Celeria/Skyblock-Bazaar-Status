package com.andranym.skyblockbazaarstatus;

import android.util.Pair;

import java.util.ArrayList;

public class FixBadNames {

    public String fix(String badName) {
        //Create Arraylist to Store
        ArrayList <Pair<String,String>> names = new ArrayList<>();
        //Add all the currently known terrible names
        names.add(new Pair<>("ENDER_STONE", "ENDSTONE"));
        names.add(new Pair<>("HUGE_MUSHROOM_1", "BROWN_MUSHROOM_BLOCK"));
        names.add(new Pair<>("HUGE_MUSHROOM_2", "RED_MUSHROOM_BLOCK"));
        names.add(new Pair<> ("ENCHANTED_HUGE_MUSHROOM_1","ENCHANTED_RED_MUSHROOM_BLOCK"));
        names.add(new Pair<> ("ENCHANTED_HUGE_MUSHROOM_2","ENCHANTED_BROWN_MUSHROOM_BLOCK"));
        names.add(new Pair<> ("LOG","OAK_WOOD"));
        names.add(new Pair<> ("LOG:1","SPRUCE_WOOD"));
        names.add(new Pair<> ("LOG:2","BIRCH_WOOD"));
        names.add(new Pair<> ("LOG:3","JUNGLE_WOOD"));
        names.add(new Pair<> ("LOG_2","ACACIA_WOOD"));
        names.add(new Pair<> ("LOG_2:1","DARK_OAK_WOOD"));
        names.add(new Pair<> ("RAW_FISH:1","SALMON"));
        names.add(new Pair<> ("RAW_FISH:2","CLOWNFISH"));
        names.add(new Pair<> ("RAW_FISH:3","PUFFERFISH"));
        names.add(new Pair<> ("WATER_LILY","LILY_PAD"));
        names.add(new Pair<> ("ENCHANTED_WATER_LILY","ENCHANTED_LILY_PAD"));
        names.add(new Pair<> ("INK_SACK:3","COCOA_BEANS"));
        names.add(new Pair<> ("INK_SACK:4","LAPIS_LAZULI"));
        names.add(new Pair<> ("SULPHUR","GUNPOWDER"));
        names.add(new Pair<> ("NETHER_STALK","NETHER_WART"));
        names.add(new Pair<> ("ENCHANTED_NETHER_STALK","ENCHANTED_NETHER_WART"));
        names.add(new Pair<> ("ENCHANTED_GLOWSTONE","ENCHANTED_GLOWSTONE_BLOCK"));

        for (int index = 0; index < names.size();++index) {
            Pair namePair = names.get(index);
            final Object first = namePair.first;
            String key = first.toString();

            final Object second = namePair.second;
            String value = second.toString();

            if (badName.equals(key)) {
                return value;
            }
        }
        return null;
    }

    public String unfix(String goodName) {
        //Create Arraylist to Store
        ArrayList <Pair<String,String>> names = new ArrayList<>();
        //Add all the currently known terrible names
        names.add(new Pair<>("ENDER_STONE", "ENDSTONE"));
        names.add(new Pair<>("HUGE_MUSHROOM_1", "BROWN_MUSHROOM_BLOCK"));
        names.add(new Pair<>("HUGE_MUSHROOM_2", "RED_MUSHROOM_BLOCK"));
        names.add(new Pair<> ("ENCHANTED_HUGE_MUSHROOM_1","ENCHANTED_RED_MUSHROOM_BLOCK"));
        names.add(new Pair<> ("ENCHANTED_HUGE_MUSHROOM_2","ENCHANTED_BROWN_MUSHROOM_BLOCK"));
        names.add(new Pair<> ("LOG","OAK_WOOD"));
        names.add(new Pair<> ("LOG:1","SPRUCE_WOOD"));
        names.add(new Pair<> ("LOG:2","BIRCH_WOOD"));
        names.add(new Pair<> ("LOG:3","JUNGLE_WOOD"));
        names.add(new Pair<> ("LOG_2","ACACIA_WOOD"));
        names.add(new Pair<> ("LOG_2:1","DARK_OAK_WOOD"));
        names.add(new Pair<> ("RAW_FISH:1","SALMON"));
        names.add(new Pair<> ("RAW_FISH:2","CLOWNFISH"));
        names.add(new Pair<> ("RAW_FISH:3","PUFFERFISH"));
        names.add(new Pair<> ("WATER_LILY","LILY_PAD"));
        names.add(new Pair<> ("ENCHANTED_WATER_LILY","ENCHANTED_LILY_PAD"));
        names.add(new Pair<> ("INK_SACK:3","COCOA_BEANS"));
        names.add(new Pair<> ("INK_SACK:4","LAPIS_LAZULI"));
        names.add(new Pair<> ("SULPHUR","GUNPOWDER"));
        names.add(new Pair<> ("NETHER_STALK","NETHER_WART"));
        names.add(new Pair<> ("ENCHANTED_NETHER_STALK","ENCHANTED_NETHER_WART"));
        names.add(new Pair<> ("ENCHANTED_GLOWSTONE","ENCHANTED_GLOWSTONE_BLOCK"));

        for (int index = 0; index < names.size();++index) {
            Pair namePair = names.get(index);
            final Object first = namePair.first;
            String bad = first.toString();

            final Object second = namePair.second;
            String good = second.toString();

            if (goodName.equals(good)) {
                return bad;
            }
        }
        return null;
    }
}
