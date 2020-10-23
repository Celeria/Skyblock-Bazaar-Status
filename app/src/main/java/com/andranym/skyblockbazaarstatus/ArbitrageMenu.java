package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ArbitrageMenu extends AppCompatActivity {

    Button btnBazaarFlip;
    Button btnBazaarFlipHelp;
    Button btnNPCFlipHelp;
    Button btnNPCFlip;
    TextView txtHelpBazaar;
    TextView txtHelpNPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arbitrage_menu);

        setTitle("Arbitrage Menu");

        //region Add back button
        
        //endregion

        //regionInitialize UI elements
        btnBazaarFlipHelp = (Button)findViewById(R.id.btnBazaarFlipHelp);
        btnNPCFlipHelp = (Button)findViewById(R.id.btnNPCFlipHelp);
        btnBazaarFlip = (Button)findViewById(R.id.btnBazaarFlip);
        btnNPCFlip = (Button)findViewById(R.id.btnNPCFlip);
        txtHelpBazaar = (TextView)findViewById(R.id.txtHelpBazaar);
        txtHelpNPC = (TextView)findViewById(R.id.txtHelpNPC);
        //endregion

        //region Display help after clicking buttons with ?
        btnBazaarFlipHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtHelpBazaar.setVisibility(View.VISIBLE);
                btnBazaarFlipHelp.setVisibility(View.INVISIBLE);
            }
        });

        btnNPCFlipHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtHelpNPC.setVisibility(View.VISIBLE);
                btnNPCFlipHelp.setVisibility(View.INVISIBLE);
            }
        });
        //endregion

        //regionNavigate to proper activities
        final Intent goToBazaar = new Intent(this,BazaarFlipActivity.class);
        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final boolean solved2 = data.getBoolean("solvedChallenge2",false);
        final Toast fail = Toast.makeText(this,"You need to complete challenge 2 in order to use this feature.",Toast.LENGTH_LONG);
        btnBazaarFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solved2) {
                    startActivity(goToBazaar);
                } else {
                    fail.show();
                }
            }
        });

        final Intent goToNPCFlip = new Intent(this,NPCFlipActivity.class);
        btnNPCFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(goToNPCFlip);
            }
        });
        //endregion
    }
}