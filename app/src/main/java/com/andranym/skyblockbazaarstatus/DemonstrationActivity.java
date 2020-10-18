package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DemonstrationActivity extends AppCompatActivity {
    Button onlybutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demonstration);

        onlybutton = (Button)findViewById(R.id.btnLeaveDemo);

        //Leave this activity
        final Intent intent = new Intent(this,MainActivity.class);
        onlybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }
}