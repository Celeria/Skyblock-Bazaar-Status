package com.andranym.skyblockbazaarstatus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.preference.PreferenceManager;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class ViewPricesActivity extends AppCompatActivity {

    TextView txtTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        txtTime = (TextView)findViewById(R.id.txtTime);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String apiKey = sharedPref.getString(getString(R.string.preference_api_key),null);

        //Get the string version of the JSON data acquired in main activity
        Intent priceInfo = getIntent();
        String priceDataString = priceInfo.getStringExtra("priceData");

        //Convert it into an actual JSON object for future use
        try {
            JSONObject priceData = new JSONObject(priceDataString);
            //One field is a UNIX time of what time the data is accurate to, I used a built in method to convert it
            //to something that the user can easily understand
            Date timeUpdated = new Date(priceData.getLong("lastUpdated"));
            String timeString = timeUpdated.toString();
            //Actually set the text
            txtTime.setText(getString(R.string.current_as_of)+timeString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}