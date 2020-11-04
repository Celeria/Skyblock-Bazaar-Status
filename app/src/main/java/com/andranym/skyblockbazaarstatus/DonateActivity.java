package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;
    Button btnDonate099;
    Button btnDonate299;
    Button btnDonate549;
    Button btnDonate999;
    Button btnDonateTooMuch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        setTitle("Donate");

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhT+g0ic1Bt6LQog0qxA6lGKN+uzSDqdzrRKjz0huuGTpHr2jtbQIz29bZmR7vJ7hQCEN/S76faVCB3P1Ui13StSYs4BVcVoZCyE+HXHar4v3hFzoEz4cheDk9zGO5GYx3lUWbV0BqfvbLTXQKdEEGg23xL/rQFvEqRhWUSka1/QOHvyeglEr930+JdISefysRzPX0jh/WZUqYQ4PK+5+7kwP4xXWDiyRu/tfPyQeKfNovqnDfYduPrjXJpJ4MjZGD0J89nIw+kvQTGyAkEQLcFUYC00BsV9A0bwrQOBduxKNs2zXXjE7YuiNmzE0qz8/Ld6BQYFnw9RkofmoKMT0ywIDAQAB", this);
        bp.initialize();
        // or bp = BillingProcessor.newBillingProcessor(this, "YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE", this);
        // See below on why this is a useful alternative

        btnDonate099 = findViewById(R.id.btnDonate099);
        btnDonate299 = findViewById(R.id.btnDonate299);
        btnDonate549 = findViewById(R.id.btnDonate549);
        btnDonate999 = findViewById(R.id.btnDonate999);
        btnDonateTooMuch = findViewById(R.id.btnDonateTooMuch);

        btnDonate099.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this,"unlicensedlabcoatdonate0.99");
                bp.consumePurchase("unlicensedlabcoatdonate0.99");
            }
        });

        btnDonate299.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this,"unlicensedlabcoatdonate3.00");
                bp.consumePurchase("unlicensedlabcoatdonate3.00");
            }
        });

        btnDonate549.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this,"unlicensedlabcoatdonate5.01");
                bp.consumePurchase("unlicensedlabcoatdonate5.01");
            }
        });

        btnDonate999.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this,"unlicensedlabcoatdonate10.00");
                bp.consumePurchase("unlicensedlabcoatdonate10.00");
            }
        });

        btnDonateTooMuch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonateActivity.this,"unlicensedlabcoatdonate99.99");
                //If you actually buy this, I'm removing the option to buy it again, since that would be ridiculous.
            }
        });
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}