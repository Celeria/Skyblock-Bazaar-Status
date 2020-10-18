package com.andranym.skyblockbazaarstatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class MinionHelpMessage extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("I have no clue what's going on here please help.")
                .setMessage("First, select your upgrades you'll be using in both slots.\n" +
                        "What's with the two customs? Well in case Hypixel adds another upgrade that speeds up the minion, you can add the percentage yourself.\n" +
                        "Minion expanders work by increasing speed by 5%, which is different from how the flycatcher works, which adds 20% to the fuel.\n" +
                        "You may think that's the same thing, but trust me, its not.\n" +
                        "As an engineer, I have included the functionality, because I make my apps future proof.\n" +
                        "For fuels, there's 2 types, normal ones like coal and lava, which increases the speed of the minion, and catalysts, which multiply drops.\n" +
                        "After selecting the fuel type, enter the percentage boost or the multiplier of the fuel.\n" +
                        "For example, enter 25 for the percentage boost of Enchanted Lava Bucket, and 3 for the multiplier of a catalyst.\n" +
                        "I could have just added a drop down menu for all the fuels, but that would mean I would have to update the app each time Hypixel releases a new fuel, and " +
                        "there's no guarantee that I'll get to that in time, so how about you just read the number of the fuel you'll be using.\n" +
                        "Finally, select any crystals you have active. I'm just going to assume you are smart enough to make sure your minions" +
                        " are actually in the crystal range, if not, that's really on you.\n" +
                        "Though if Hypixel actually adds another crystal I WILL have to update this app, and I'll be annoyed for at least 2 hours, because" +
                        "coding actually involves a lot more than you might think. For example, the code that displays this message is like 50 lines long.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }
}
