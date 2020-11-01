package com.andranym.skyblockbazaarstatus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 45782;
    Button btnViewPrices;
    Button btnViewFavorites;
    Button btnTestJson;
    Button btnSettings;
    Button btnChallenges;
    Button btnArbitrage;
    Button btnMinionOptimizer;
    Button btnGoogleSignIn;
    TextView txtMinutesSince;
    TextView txtWarnData;
    ProgressDialog pdStoring;
    ProgressDialog pd;

    boolean agreed = false;
    boolean solved1 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Main Menu");

        //region Declare all UI elements
        btnViewPrices = (Button) findViewById(R.id.btnViewPrices);
        btnTestJson = (Button) findViewById(R.id.btnUpdateJson);
        btnViewFavorites = (Button)findViewById(R.id.btnViewFavorites);
        btnMinionOptimizer = (Button)findViewById(R.id.btnMinionOptimizer);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnChallenges = (Button) findViewById(R.id.btnChallenges);
        btnArbitrage = (Button) findViewById(R.id.btnArbitrage);
        txtMinutesSince = (TextView) findViewById(R.id.txtMinutesSince);
        txtWarnData = (TextView) findViewById(R.id.txtWarnData);
        pdStoring = new ProgressDialog(MainActivity.this);
        btnGoogleSignIn = (Button)findViewById(R.id.btnGoogleSignIn);
        pdStoring.setMessage("Your phone is processing the data... If you see this your device is so, so slow.");
        pdStoring.setCancelable(false);
        //endregion

        //region Check for internet connection
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean[] isConnected = {activeNetwork != null && activeNetwork.isConnectedOrConnecting()};
        final boolean isMetered = cm.isActiveNetworkMetered();

        //regionCheck if we are using mobile data or disconnected
        final boolean[] showMobileWarning = new boolean[1];
        final boolean checkMobileData = true;
        new Thread(){
            @Override
            public void run() {
                while (checkMobileData) {
                    ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                    final boolean isMetered = cm.isActiveNetworkMetered();
                    if (isMetered) {
                        showMobileWarning[0] = true;
                    } else {
                        showMobileWarning[0] = false;
                    }
                    SystemClock.sleep(5000);
                }
            }
        }.start();
        //endregion

        //endregion

        //region Check to see if the Hypixel Skyblock API key has already been provided, if not, open the activity that lets the user provide it
        // NO LONGER NEEDED DUE TO API UPDATE
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//        final String apiKey = sharedPref.getString(getString(R.string.preference_api_key),null);
//
//        //The Hypixel website where you get all the data from
//        String informationURL = getString(R.string.url_beginning);
//
//        if (apiKey != null) {
//            informationURL = informationURL + apiKey;
//        } else {
//            //open KeyActivity in order to supply the proper key
//            Intent intent = new Intent(this, KeyActivity.class);
//            startActivity(intent);
//        }
        //endregion

        //regionAgree to conditions
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean agreed = sharedPref.getBoolean("agreedToTerms",false);
        //if the user hasn't agreed in the past, make them agree
        if(!agreed){

        }
        //endregion

        //region The Hypixel website where you get all the data from
        //used to be more complicated...
        String informationURL = getString(R.string.url_beginning);
        //endregion

        //regionFetch data as soon as the app is opened
        //Only do so if a connection is found, and you are not on mobile data
        final String[] priceData = {null};
        if (isConnected[0] && !isMetered) {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Retrieving data from Hypixel.\n" +
                    "If it\'s down this won\'t work either.\n" +
                    "Its like half a megabyte of data, if your phone is trash, it might take a moment to process as well.\n" +
                    "Honestly if you had time to read this whole message either your internet or your phone is probably trash\n" +
                    "Or maybe you are a speed reader, that's also possible.\n" +
                    "I\'m honestly just putting stuff here so you have something to look at, you are welcome.");
            pd.setCancelable(false);
            pd.show();
            try {
                priceData[0] = new RetrieveData().execute(getString(R.string.url_beginning)).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Store it in sharedPreferences for use by other activities
            Thread t = new Thread() {
                @Override
                public void run() {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("currentData", priceData[0]);
                    editor.commit();
                }
            };
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (pd.isShowing()) {
                pd.dismiss();
            }
        } else{
            //If no connection, fetch the data from before
            priceData[0] = sharedPref.getString("currentData",null);
        }
        //endregion

        //region Code for what happens when you press the VIEW CURRENT BAZAAR PRICES button
        final Intent intentViewPrices = new Intent(this, ViewPricesNoScrollActivity.class);
        //intentViewPrices.putExtra("priceData",txtJson.getText().toString());
        btnViewPrices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prevents this button from being spammed if it doesn't work immediately
                btnViewPrices.setEnabled(false);
                Timer buttonTimer = new Timer();
                buttonTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnViewPrices.setEnabled(true);
                            }
                        });
                    }
                }, 300);
                startActivity(intentViewPrices);
            }
        });
        //endregion

        //regionCode for the ARBITRAGE button
        final Intent intentArbitrage = new Intent(this, ArbitrageMenu.class);
        //intentArbitrage.putExtra("priceData", priceData[0]);
        btnArbitrage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentArbitrage);
            }
        });
        //endregion

        //region Code for what happens when you press the SETTINGS button
        final Intent intentSettings = new Intent(this,SettingsActivity.class);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentSettings);
            }
        });
        //endregion

        //region Code for what happens when you press the CHALLENGES button
        final Intent intentChallenges = new Intent(this,ChallengeActivity.class);
        btnChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentChallenges);
            }
        });
        //endregion

        //region Code for what happens when you press the VIEW FAVORITES button
        final Intent intentFavorites = new Intent(this,FavoriteActivity.class);
        btnViewFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentFavorites);
            }
        });
        //endregion

        //regionCode for the Minion Optimizer button
        final Intent intentMinionOptimizer = new Intent(this,MinionOptimizerActivity.class);
        solved1 = sharedPref.getBoolean("solvedChallenge1",false);
        final Toast goSolve = Toast.makeText(this,"Complete the first challenge in order to access this feature.",Toast.LENGTH_LONG);
        btnMinionOptimizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solved1) {
                    startActivity(intentMinionOptimizer);
                } else {
                    goSolve.show();
                }
            }
        });
        //endregion

        //region Update the data manually
        final long[] timeUpdatedFancy = {0};
        btnTestJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //First check if there is internet
                ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    btnTestJson.setVisibility(View.GONE);
                    txtMinutesSince.setText("NOW");
                    txtMinutesSince.setTextColor(Color.parseColor("#4CAF50"));
                    pd = new ProgressDialog(MainActivity.this);
                    pd.setMessage("Retrieving data from Hypixel.\n" +
                            "If it\'s down this won\'t work either.\n" +
                            "Its like half a megabyte of data, if your phone is trash, it might take a moment to process as well.\n" +
                            "Honestly if you had time to read this whole message either your internet or your phone is probably trash\n" +
                            "Or maybe you are a speed reader, that's also possible.\n" +
                            "I\'m honestly just putting stuff here so you have something to look at, you are welcome.");
                    pd.setCancelable(false);
                    pd.show();
                    try {
                        priceData[0] = new RetrieveData().execute(getString(R.string.url_beginning)).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Store it in sharedPreferences for use by other activities
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("currentData", priceData[0]);
                            editor.commit();
                        }
                    };
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }

                    //region For fancy time
                        JSONObject priceDataJSON = null;
                        timeUpdatedFancy[0] = 0;
                        if (priceData[0] != null) {
                            try {
                                priceDataJSON = new JSONObject(priceData[0]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (priceDataJSON != null) {
                            try {
                                timeUpdatedFancy[0] = priceDataJSON.getLong("lastUpdated");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    //endregion
                } else{
                    Toast toast = Toast.makeText(getApplicationContext(),"NO INTERNET CONNECTION",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        //endregion

        //region Block people from stealing this app
//        boolean usedBefore = sharedPref.getBoolean("copyWriteDetection",false);
//        if (!usedBefore) {
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putBoolean("copyWriteDetection",true);
//            editor.putLong("timeAccessed",System.currentTimeMillis());
//            editor.commit();
//        } else {
//            long timeAccessed = sharedPref.getLong("timeAccessed",0);
//            if ((double)(System.currentTimeMillis() - timeAccessed)/1000/60/60/24 > 3) {
//                Intent blockUser = new Intent(this,BannedActivity.class);
//                startActivity(blockUser);
//            }
//        }
        //endregion

        //region Displays how long its been since the data was last updated
        boolean useFancy = sharedPref.getBoolean("useFancyClock",false);
        if (useFancy) {
            new Thread() {
                @Override
                public void run() {
                    Boolean checkTime = true; //always true
                    //Get time updated from the string from earlier
                    JSONObject priceDataJSON = null;
                    timeUpdatedFancy[0] = 0;
                    boolean stopWaiting = false;
                    while (!stopWaiting) {
                        if (priceData[0] != null) {
                            try {
                                priceDataJSON = new JSONObject(priceData[0]);
                                stopWaiting = true;
                            } catch (JSONException e) {
                                SystemClock.sleep(100);
                                e.printStackTrace();
                            }
                        }
                    }
                    if (priceDataJSON != null) {
                        try {
                            timeUpdatedFancy[0] = priceDataJSON.getLong("lastUpdated");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    while (checkTime) {
                        //Call the task that can actually access UI elements
                        new checkTimeFancy().execute(timeUpdatedFancy[0]);
                        new showMobileWarning().execute(showMobileWarning[0]);
                        SystemClock.sleep(20);
                        //Take a break so this thread isn't too resource intensive
                    }
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    Boolean checkTime = true; //always true
                    while (checkTime) {
                        //Get time updated from the string from earlier
                        JSONObject priceDataJSON = null;
                        long unixTimeDataUpdated = 0;
                        if (priceData[0] != null) {
                            try {
                                priceDataJSON = new JSONObject(priceData[0]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (priceDataJSON != null) {
                            try {
                                unixTimeDataUpdated = priceDataJSON.getLong("lastUpdated");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //Call the task that can actually access UI elements
                        new checkTime().execute(unixTimeDataUpdated);
                        new showMobileWarning().execute(showMobileWarning[0]);
                        SystemClock.sleep(5000);
                        //Take a break so this thread isn't too resource intensive
                    }
                }
            }.start();
        }
        //endregion

        //regionCode to sign in to Google
        signInSilently();
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignInIntent();
            }
        });
        //endregion
    }

    private void signInSilently() {
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
            GoogleSignInAccount signedInAccount = account;
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);
            signInClient
                    .silentSignIn()
                    .addOnCompleteListener(
                            this,
                            new OnCompleteListener<GoogleSignInAccount>() {
                                @Override
                                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                    if (task.isSuccessful()) {
                                        // The signed in account is stored in the task's result.
                                        GoogleSignInAccount signedInAccount = task.getResult();
                                        int e = 0;
                                    } else {
                                        // Player will need to sign-in explicitly using via UI.
                                        // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                                        // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                                        // Interactive Sign-in.
                                        final Toast weird = Toast.makeText(getApplicationContext(),"something strange",Toast.LENGTH_SHORT);
                                        weird.show();
                                    }
                                }
                            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //signInSilently();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        agreed = sharedPref.getBoolean("agreedToTerms",false);
        solved1 = sharedPref.getBoolean("solvedChallenge1",false);
    }

    private class checkTimeFancy extends AsyncTask<Long,Void,Integer> {

        @Override
        protected Integer doInBackground(Long... longs) {
            //Extract the UNIX timestamp from the JSON object
            long unixTimeDataUpdated = longs[0];
            long currentUnixTime = System.currentTimeMillis();
            //This gives the time passed in milliseconds
            return (int)(currentUnixTime - unixTimeDataUpdated);
        }

        @Override
        protected void onPostExecute(Integer millisecondsPassed) {
            double minutesPassed = (double)millisecondsPassed/1000/60;
            super.onPostExecute(millisecondsPassed);
            double exactTime = (double)millisecondsPassed / 1000;
            if (minutesPassed < 1) {
                String setTextTo = exactTime + " SECONDS AGO";
                txtMinutesSince.setText(setTextTo);
                btnTestJson.setVisibility(View.VISIBLE);
            }
            if (minutesPassed > 1 && minutesPassed <= 5){
                String setTextTo = exactTime + " SECONDS AGO";
                txtMinutesSince.setText(setTextTo);
                btnTestJson.setVisibility(View.VISIBLE);
            }
            if (minutesPassed > 5 && minutesPassed <= 60) {
                String setTextTo = exactTime + " SECONDS AGO";
                txtMinutesSince.setText(setTextTo);
                //Set to Orange
                txtMinutesSince.setTextColor(Color.parseColor("#ff8519"));
                btnTestJson.setVisibility(View.VISIBLE);
            }
            if (minutesPassed > 60) {
                String setTextTo = exactTime + " SECONDS AGO";
                //Set to Red
                txtMinutesSince.setTextColor(Color.parseColor("#ed1818"));
                txtMinutesSince.setText(setTextTo);
                btnTestJson.setVisibility(View.VISIBLE);
            }
        }
    }

    private class checkTime extends AsyncTask<Long,Void,Integer> {

        @Override
        protected Integer doInBackground(Long... longs) {
            //Extract the UNIX timestamp from the JSON object
            long unixTimeDataUpdated = longs[0];
            long currentUnixTime = System.currentTimeMillis();
            //This gives the minutes passed
            return (int)(currentUnixTime - unixTimeDataUpdated)/1000/60;
        }

        @Override
        protected void onPostExecute(Integer minutesPassed) {
            super.onPostExecute(minutesPassed);
            if (minutesPassed == 1) {
                String setTextTo = "1 MINUTE AGO";
                txtMinutesSince.setText(setTextTo);
                btnTestJson.setVisibility(View.VISIBLE);
            }
            if (minutesPassed > 1 && minutesPassed <= 5){
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtMinutesSince.setText(setTextTo);
                btnTestJson.setVisibility(View.VISIBLE);
            }
            if (minutesPassed > 5 && minutesPassed <= 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtMinutesSince.setText(setTextTo);
                //Set to Orange
                txtMinutesSince.setTextColor(Color.parseColor("#ff8519"));
                btnTestJson.setVisibility(View.VISIBLE);
            }
            if (minutesPassed > 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                //Set to Red
                txtMinutesSince.setTextColor(Color.parseColor("#ed1818"));
                txtMinutesSince.setText(setTextTo);
                btnTestJson.setVisibility(View.VISIBLE);
            }
        }
    }

    private class showMobileWarning extends AsyncTask<Boolean,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            if (booleans[0]) {
                return false;
            } else{
                return true;
            }
        }
        @Override

        //Kind of confusing, but when mobile data is being used it shows the warning, and when regular network is on it removes the message.
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);
            if(connected){
                txtWarnData.setVisibility(View.GONE);
            } else{
                txtWarnData.setVisibility(View.VISIBLE);
            }
        }
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = "You did not sign in.";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }
}
