package com.andranym.skyblockbazaarstatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public class CreditsActivity extends AppCompatActivity {

    Button donate;
    Button licenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        setTitle("Credits");

        donate = findViewById(R.id.btnDonate);
        licenses = findViewById(R.id.btnLicenseInformation);

        //Let the buttons launch the code required
        final Intent goDonate = new Intent(this,DonateActivity.class);
        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(goDonate);
            }
        });

        licenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), OssLicensesMenuActivity.class));
            }
        });
    }

}