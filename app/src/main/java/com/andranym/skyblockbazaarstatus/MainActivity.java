package com.andranym.skyblockbazaarstatus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 45782;
    public static final String CHANNEL_ID = "Skyblock Bazaar Price Change";
    GoogleSignInClient mGoogleSignInClient;
    Button btnViewPrices;
    Button btnViewFavorites;
    Button btnTestJson;
    Button btnSettings;
    Button btnChallenges;
    Button btnArbitrage;
    Button btnBAZAARFLIP;
    Button btnNPCFLIP;
    Button btnMinionOptimizer;
    Button btnGoogleSignIn;
    Button btnCredits;
    Button btnPriceHistory;
    ImageButton btnLeaderboard;
    TextView txtMinutesSince;
    TextView txtWarnData;
    TextView txtWelcome;
    ProgressDialog pdStoring;
    ProgressDialog pd;

    boolean agreed = false;
    boolean solved1 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Main Menu");

        //regionCreate notification channel so this app can send notifications if the user desires
        createNotificationChannel();
        //endregion

        //region Declare all UI elements
        btnViewPrices = (Button) findViewById(R.id.btnViewPrices);
        btnTestJson = (Button) findViewById(R.id.btnUpdateJson);
        btnViewFavorites = (Button)findViewById(R.id.btnViewFavorites);
        btnMinionOptimizer = (Button)findViewById(R.id.btnMinionOptimizer);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnChallenges = (Button) findViewById(R.id.btnChallenges);
        btnArbitrage = (Button) findViewById(R.id.btnArbitrage);
        btnNPCFLIP = (Button) findViewById(R.id.btnNPCFLIP);
        btnBAZAARFLIP = (Button) findViewById(R.id.btnBAZAARFLIP);
        btnPriceHistory = (Button)findViewById(R.id.btnPriceHistory);
        txtMinutesSince = (TextView) findViewById(R.id.txtMinutesSince);
        txtWarnData = (TextView) findViewById(R.id.txtWarnData);
        txtWelcome = (TextView)findViewById(R.id.txtWelcomeMessage);
        pdStoring = new ProgressDialog(MainActivity.this);
        btnGoogleSignIn = (Button)findViewById(R.id.btnGoogleSignIn);
        btnCredits = (Button)findViewById(R.id.btnCredits);
        btnLeaderboard = (ImageButton) findViewById(R.id.btnLeaderboard);
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
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean agreed = sharedPref.getBoolean("agreedToTerms",false);
        //this part used to be further down, but we need to put it here for a quick fix
        final String[] priceData = {null};
        if(!agreed){
            //regionThe first ever time the app starts, it will crash if there's no data, this fixes it
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
            //endregion

            //region grab a bit of data for the price history, so it looks better for the first time
            OneTimeWorkRequest dataRetrieval =  new OneTimeWorkRequest.Builder(RetrieveAndStoreDataAndNotify.class)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(dataRetrieval);
            //endregion

            //if the user hasn't agreed in the past, make them agree
            Intent goAgree = new Intent(this,AgreementActivity.class);
            startActivity(goAgree);
        }
        //endregion

        //regionCheck for update
        if(sharedPref.getBoolean("needToShowUpdateScreen8",true)){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("needToShowUpdateScreen8",false);
            editor.commit();
            startActivity(new Intent(this,UpdateActivity.class));
        }
        //endregion

        //region The Hypixel website where you get all the data from
        //used to be more complicated...
        String informationURL = getString(R.string.url_beginning);
        //endregion

        //regionFetch data as soon as the app is opened
        //Only do so if a connection is found, and you are not on mobile data
        new Thread(){
            @Override
            public void run() {
                super.run();
                if (isConnected[0] && !isMetered) {
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
                } else{
                    //If no connection, fetch the data from before
                    priceData[0] = sharedPref.getString("currentData",null);
                }
            }
        }.start();
        //endregion

        //regionDisplay visit counter and color if needed
        final int[] visitCount = {sharedPref.getInt("visitCounter", 1)};
        if(isConnected[0] && !showMobileWarning[0]) {
            try {
                //Retrieve the previous score, and if its higher, replace the current score
                LeaderboardsClient mLeaderboardsClient = Games.getLeaderboardsClient(MainActivity.this, GoogleSignIn.getLastSignedInAccount(this));
                mLeaderboardsClient.loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard_most_active_users), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                        .addOnSuccessListener(this, new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                            @Override
                            public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                                long score = 0;
                                if (leaderboardScoreAnnotatedData != null) {
                                    if (leaderboardScoreAnnotatedData.get() != null) {
                                        score = leaderboardScoreAnnotatedData.get().getRawScore();
                                        if (score > visitCount[0]) {
                                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putInt("visitCounter", (int) score + 1);
                                            editor.apply();
                                        }
                                    }
                                }
                            }
                        });
                mLeaderboardsClient.loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard_notifications_received), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                        .addOnSuccessListener(this, new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                            @Override
                            public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                                if (leaderboardScoreAnnotatedData != null) {
                                    if (leaderboardScoreAnnotatedData.get() != null) {
                                        long notificationsPrevious = leaderboardScoreAnnotatedData.get().getRawScore();
                                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        int oldNotifications = sharedPref.getInt("numberOfNotificationsSent",0);
                                        if (notificationsPrevious > oldNotifications) {
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putInt("numberOfNotificationsSent", (int) notificationsPrevious);
                                            editor.apply();
                                        }
                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                //do nothing
            }
        }

        boolean displayWelcome = sharedPref.getBoolean("displayWelcome",false);
        if(displayWelcome){
            txtWelcome.setVisibility(View.VISIBLE);
        }
        boolean useDifferentColor = sharedPref.getBoolean("useDifferentColor",false);
        if (useDifferentColor) {
            try {
                String colorString = sharedPref.getString("welcomeColor", "#8a000000");
                int color = Color.parseColor(colorString);
                txtWelcome.setTextColor(color);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "That is an invalid color", Toast.LENGTH_SHORT).show();
            }
        }
        String visitMessage = "Welcome back! Number of times opened: " + visitCount[0];
        txtWelcome.setText(visitMessage);
        ++visitCount[0];
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("visitCounter", visitCount[0]);
        editor.apply();

        //endregion

        //regionUpload usage count if signed in
        if (isConnected[0] && !showMobileWarning[0]) {
            //Honestly I added this because google tells me how many times people ping for sign in, and I'm somewhat
            //curious how much people use my app, this way I get a ping each time someone opens the app, I'll remove it if it gets excessive, or limit it heavily
            try {
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .submitScore(getString(R.string.leaderboard_most_active_users), visitCount[0]);
            } catch(Exception e){
                //do nothing
            }
        }

        //endregion

        //region Code for what happens when you press the VIEW CURRENT BAZAAR PRICES button
        final Intent intentViewPrices = new Intent(this, ViewPricesNoScrollActivity.class);
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
        //see which one to display
        boolean useOptimizedArbitrage = sharedPref.getBoolean("useOptimizedArbitrage",false);
        if(useOptimizedArbitrage) {
            btnArbitrage.setVisibility(View.GONE);
        } else {
            btnBAZAARFLIP.setVisibility(View.GONE);
            btnNPCFLIP.setVisibility(View.GONE);
        }
        final Intent intentArbitrage = new Intent(this, ArbitrageMenu.class);
        btnArbitrage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentArbitrage);
            }
        });

        final Intent intentNPC = new Intent(this,NPCFlipActivity.class);
        btnNPCFLIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intentNPC);
            }
        });

        final Intent intentBazaar = new Intent(this,BazaarFlipActivity.class);
        btnBAZAARFLIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intentBazaar);
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
        final int minionTrials = sharedPref.getInt("minionFreeTrials",5);
        final Toast goSolve = Toast.makeText(this,"Complete the first challenge in order to access this feature.",Toast.LENGTH_LONG);
        btnMinionOptimizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solved1 || minionTrials > 0) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (!solved1) {
                        int tempTrials = minionTrials - 1;
                        editor.putInt("minionFreeTrials", tempTrials);
                        editor.commit();
                        Toast.makeText(getApplicationContext(),"You have can use this feature " + tempTrials + " more times.\nThen you must complete Challenge 1 to continue to use this feature",Toast.LENGTH_LONG).show();
                    }
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

//        //region Block people from stealing this app
        boolean usedBefore = sharedPref.getBoolean("copyWriteDetection",false);
        if (!usedBefore) {
            editor.putBoolean("copyWriteDetection",true);
            editor.putLong("timeAccessed",System.currentTimeMillis());
            editor.commit();
        } else {
            long timeAccessed = sharedPref.getLong("timeAccessed",0);
            if ((double)(System.currentTimeMillis() - timeAccessed)/1000/60/60/24 > 3) {
                Intent blockUser = new Intent(this,BannedActivity.class);
                startActivity(blockUser);
            }
        }
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
                        SystemClock.sleep(1000);
                        //Take a break so this thread isn't too resource intensive
                    }
                }
            }.start();
        }
        //endregion

        //regionCode to sign in to Google
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Didn't get this to work, not deleting in case I need it later
        signInSilently();

        //Part where I actually sign in.
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        //endregion

        //regionCode for credits
        final Intent goCredits = new Intent(this,CreditsActivity.class);
        btnCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(goCredits);
            }
        });
        //endregion

        //regionCode for price history
        btnPriceHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getApplicationContext(),PriceHistoryMenuActivity.class));
                startActivity(new Intent(getApplicationContext(),PriceHistoryMenuActivity.class));
            }
        });
        //endregion

        //regionCode to show leaderboards
        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLeaderboard();
            }
        });
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
                                    }
                                }
                            });
        }
    }

    private static final int RC_LEADERBOARD_UI = 9004;

    private void showLeaderboard() {
        boolean showActive = new Random().nextBoolean();
        String leaderboard;

        if(showActive){
            leaderboard = getString(R.string.leaderboard_most_active_users);
        } else {
            leaderboard = getString(R.string.leaderboard_notifications_received);
        }

        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent(leaderboard)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Reset the ability to use minion optimizer/terms agreed
        agreed = sharedPref.getBoolean("agreedToTerms",false);
        solved1 = sharedPref.getBoolean("solvedChallenge1",false);
        //Reset the arbitrage settings
        boolean useOptimizedArbitrage = sharedPref.getBoolean("useOptimizedArbitrage",false);
        if(useOptimizedArbitrage) {
            btnArbitrage.setVisibility(View.GONE);
            btnBAZAARFLIP.setVisibility(View.VISIBLE);
            btnNPCFLIP.setVisibility(View.VISIBLE);
        } else {
            btnBAZAARFLIP.setVisibility(View.GONE);
            btnNPCFLIP.setVisibility(View.GONE);
            btnArbitrage.setVisibility(View.VISIBLE);
        }
        //Display welcome if desired
        boolean displayWelcome = sharedPref.getBoolean("displayWelcome",false);
        if(displayWelcome){
            txtWelcome.setVisibility(View.VISIBLE);
        }
        //change color if desired
        boolean useDifferentColor = sharedPref.getBoolean("useDifferentColor",false);
        if (useDifferentColor) {
            try {
                String colorString = sharedPref.getString("welcomeColor", "#8a000000");
                int color = Color.parseColor(colorString);
                txtWelcome.setTextColor(color);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "That is an invalid color", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    @Override
//    protected void onStart() {
//        // Check for existing Google Sign In account, if the user is already signed in
//        // the GoogleSignInAccount will be non-null.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if(account == null) {
//            startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
//        }
//        super.onStart();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //Set up the notification channel so the app can send notifications
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bazaar Information Update";
            String description = "Receive notifications if Skyblock Bazaar prices change enough for you to care.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Sign In Error", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this, "Could not sign in.\nMake sure you have the Google Play Games app, and that you are signed in on there.", Toast.LENGTH_LONG).show();
        }
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
}
