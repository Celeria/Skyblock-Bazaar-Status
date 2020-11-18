package com.andranym.skyblockbazaarstatus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnSuccessListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class ChallengeActivity extends AppCompatActivity {

    Button btnShowAchievements;
    Button btnSolve1;
    Button btnSolve2;
    Button btnSolve3;
    Button btnSolve5;
    Button btnSolve7;
    Button btnSolve8;
    Button btnSolve9;
    Button btnStartStonks;
    Button btnTryChallenge4;
    Button btnChangeColor;
    TextView txtSolved1;
    TextView txtSolved2;
    TextView txtSolved3;
    TextView txtSolved4;
    TextView txtSolved5;
    TextView txtSolved7;
    TextView txtSolved8;
    TextView txtSolved9;
    TextView txtChallenge1;
    TextView txtChallenge3;
    TextView txtChallenge7;
    TextView txtChallenge8;
    TextView txtChallenge9;
    EditText editNumChallenge1;
    EditText editTextC2;
    EditText editNumC3;
    EditText editTextC5;
    EditText editNumC7;
    EditText editNumC8;
    EditText editNumC9;
    EditText editColor;
    Switch switchFancyTimer;
    Switch switchArbitrageShortcut;
    Switch switchShowWelcome;

    final int RC_ACHIEVEMENT_UI = 9003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        setTitle("Challenges");

        //regionDeclare UI elements
        btnShowAchievements = findViewById(R.id.btnShowAchievements);
        btnSolve1 = findViewById(R.id.btnSolve1);
        btnSolve2 = findViewById(R.id.btnSolve2);
        btnSolve3 = findViewById(R.id.btnSolve3);
        btnSolve5 = findViewById(R.id.btnSolve5);
        btnSolve7 = findViewById(R.id.btnSolve7);
        btnSolve8 = findViewById(R.id.btnSolve8);
        btnSolve9 = findViewById(R.id.btnSolve9);
        btnTryChallenge4 = findViewById(R.id.btnTryChallenge4);
        btnStartStonks = findViewById(R.id.btnStartStonks);
        btnChangeColor = findViewById(R.id.btnChangeColor);
        txtSolved1 = findViewById(R.id.txtSolved1);
        txtSolved2 = findViewById(R.id.txtSolved2);
        txtSolved3 = findViewById(R.id.txtSolved3);
        txtSolved4 = findViewById(R.id.txtSolved4);
        txtSolved5 = findViewById(R.id.txtSolved5);
        txtSolved7 = findViewById(R.id.txtSolved7);
        txtSolved8 = findViewById(R.id.txtSolved8);
        txtSolved9 = findViewById(R.id.txtSolved9);
        txtChallenge1 = findViewById(R.id.txtChallenge1);
        txtChallenge3 = findViewById(R.id.txtChallenge3);
        txtChallenge7 = findViewById(R.id.txtChallenge7);
        txtChallenge8 = findViewById(R.id.txtChallenge8);
        txtChallenge9 = findViewById(R.id.txtChallenge9);
        editNumChallenge1 = findViewById(R.id.editNumC1);
        editTextC2 = findViewById(R.id.editTextC2);
        editNumC3 = findViewById(R.id.editNumC3);
        editNumC7 = findViewById(R.id.editNumC7);
        editTextC5 = findViewById(R.id.editTextC5);
        editNumC7 = findViewById(R.id.editNumC7);
        editNumC8 = findViewById(R.id.editNumC8);
        editNumC9 = findViewById(R.id.editNumC9);
        editColor = findViewById(R.id.editColor);
        switchFancyTimer = findViewById(R.id.switchFancyTimer);
        switchArbitrageShortcut = findViewById(R.id.switchArbitrageShortcut);
        switchShowWelcome = findViewById(R.id.switchShowWelcome);

        //endregion

        //regionRetrieve Data
        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final boolean solved1 = data.getBoolean("solvedChallenge1",false);
        final boolean solved2 = data.getBoolean("solvedChallenge2",false);
        final boolean solved3 = data.getBoolean("solvedChallenge3",false);
        final boolean solved4 = data.getBoolean("solvedChallenge4",false);
        final boolean solved5 = data.getBoolean("solvedChallenge5",false);
        final boolean solved6 = data.getBoolean("solvedChallenge6",false);
        final boolean solved7 = data.getBoolean("solvedChallenge7",false);
        final boolean solved8 = data.getBoolean("solvedChallenge8",false);
        final boolean solved9 = data.getBoolean("solvedChallenge9",false);

        final boolean solved1display = data.getBoolean("solvedChallenge1display",false);
        final boolean solved2display = data.getBoolean("solvedChallenge2display",false);
        final boolean solved3display = data.getBoolean("solvedChallenge3display",false);
        final boolean solved4display = data.getBoolean("solvedChallenge4display",false);
        final boolean solved5display = data.getBoolean("solvedChallenge5display",false);
        final boolean solved6display = data.getBoolean("solvedChallenge6display",false);
        final boolean solved7display = data.getBoolean("solvedChallenge7display",false);
        final boolean solved8display = data.getBoolean("solvedChallenge8display",false);
        final boolean solved9display = data.getBoolean("solvedChallenge9display",false);
        //endregion

        //Make random number generator
        final Random randMaker = new Random();

        //region Check for achievements
        if (solved1) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_simple_geometry));
                if (solved1display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge1display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Not signed in to Google Play Games. \nDo so on the main menu if you want the achievement.",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved2) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_civics_101));
                if (solved2display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge2display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved3) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_birthday_paradox));
                if (solved3display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge3display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved4) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_everything_else_is_accelerating_up));
                if (solved4display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge4display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved5) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_life_not_as_we_know_it));
                if (solved5display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge5display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved6) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_market_manipulation));
                if (solved6display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge6display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved7) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_no_luck_required));
                if (solved7display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge7display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved8) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_pattern_recognizer));
                if (solved8display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge8display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }

        if (solved9) {
            try {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_order_in_chaos));
                if (solved9display) {
                    //For the first time unlocking, show the achievements
                    showAchievements();
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge9display",false);
                    editor.commit();
                }
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Achievements did not work, perhaps not signed in?",Toast.LENGTH_SHORT).show();
            }
        }
        //endregion

        //regionButton to show achievements if the user wants
        btnShowAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showAchievements();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"Not signed in.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //endregion

        //OH HELLO IF YOU ARE LOOKING AT THIS FOR THE RIGHT ANSWERS. GOOD JOB, THIS IS AN INTENDED FEATURE,
        //I WANT TO GIVE AN ADVANTAGE TO FELLOW CODERS

        //region Challenge 1
        if(!solved1) {
            txtSolved1.setVisibility(View.GONE);
        }
        final int[] hemisphereVolume = {1 + randMaker.nextInt(40)};
        String question1 = "You have a hemisphere with a volume of " + hemisphereVolume[0] + ". " +
                "What is the surface area of a sphere with the same diameter? Round your answer to the nearest thousandth.";
        txtChallenge1.setText(question1);

        btnSolve1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editNumChallenge1.getText().toString().equals("")){
                    double answer1 = 4 * Math.PI * Math.pow((hemisphereVolume[0])/((2.0/3.0)*Math.PI),(2.0/3.0));
                    double userAnswer = Double.parseDouble(editNumChallenge1.getText().toString());
                    if (Math.abs(userAnswer - answer1) < 0.01) {
                        txtSolved1.setVisibility(View.VISIBLE);
                        SharedPreferences.Editor editor = data.edit();
                        editor.putBoolean("solvedChallenge1", true);
                        editor.putBoolean("solvedChallenge1display",true);
                        editor.commit();
                        Toast.makeText(getApplicationContext(),"Correct.",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast fail = Toast.makeText(getApplicationContext(),"No, the correct answer was " + answer1,Toast.LENGTH_LONG);
                        fail.show();
                        hemisphereVolume[0] = 1 + randMaker.nextInt(40);
                        String question1 = "You have a hemisphere with a volume of " + hemisphereVolume[0] + ". " +
                                "What is the surface area of a sphere with the same diameter? Round your answer to the nearest thousandth.";
                        txtChallenge1.setText(question1);
                    }
                }
            }
        });
        //endregion

        //region Challenge 2
        if (!solved2) {
            txtSolved2.setVisibility(View.GONE);
        }
        btnSolve2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userAnswer = editTextC2.getText().toString();
                userAnswer = userAnswer.toLowerCase();
                if ((userAnswer.contains("equal protection clause") || userAnswer.contains("fourteenth amendment") || userAnswer.contains("14th amendment")) &&
                        (userAnswer.contains("commerce"))) {
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge2", true);
                    editor.putBoolean("solvedChallenge2display",true);
                    editor.commit();
                    txtSolved2.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(),"Correct.",Toast.LENGTH_SHORT).show();
                } else {
                    Toast fail = Toast.makeText(getApplicationContext(),"That answer is not what I was looking for.",Toast.LENGTH_LONG);
                    fail.show();
                }
            }
        });
        //endregion

        //region Challenge 3
        if(!solved3) {
            switchFancyTimer.setVisibility(View.GONE);
            txtSolved3.setVisibility(View.GONE);
        }
        final int[] alienPopulation = {100 + randMaker.nextInt(100)};
        String question3 = "Imagine a planet called Tologon. This planet is arid, and its crust is has high amounts of sulfur in it. " +
                "Although you would think this would make this planet a desert planet, planets tend to contain more than just 1 biome, unlike " +
                "what some movies would have you believe. In any case, there is a variety of plant life, such as a plant that resembles the bamboo" +
                "that we all know about. It has a solid brick-like texture, and the only intelligent species of life on this planet, the Tolgonians, " +
                "often use the plant to fortify their subterranean homes. Just like our Earth, this planet's species have a concept of the birthday. " +
                "However, for their planet, the year has 10,000 days in it, which are split into 10 1000 day \"months\". Tologonian birthdays are " +
                "often celebrated using a cake made from a special " +
                "kind of mycelium that links the majority of the roots of the planet's other plant life. The fungus that creates the mycelium web actually " +
                "uses a novel form of sulfur metabolism that is energized by the sunlight through a unique series of chemical reactions that do not occur " +
                "naturally here on earth. " + alienPopulation[0] + " Tologonians have gathered in a room together. Birthdays on this planet are uniformly " +
                "distributed. What is the probability that at least two Tologonians in this room share the same birthday? An exact answer is not required, " +
                "as long as you are within 0.01 of the correct answer, your phone will accept it.\n" +
                "Also, be patient, this question is hard, your phone might take a couple seconds to calculate the right answer.";

        txtChallenge3.setText(question3);

        //region Where the user actually solves the problem
            btnSolve3.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    String userAnswer = editNumC3.getText().toString();
                    //make sure its not empty
                    if (!userAnswer.equals("")) {
                        double answer = Double.parseDouble(userAnswer);
                        //Remind user how probabilities work
                        if (answer > 1 || answer < 0) {
                            Toast fail = Toast.makeText(getApplicationContext(), "The answer is between 0 and 1. This is your only hint.", Toast.LENGTH_LONG);
                            fail.show();
                        } else {
                            //I don't intend to solve the birthday paradox properly
                            int successes = 0;

                            //Instead, simulate this situation 100,000 times to get the approx answer
                            for (int i = 0; i < 10000; ++i) {

                                //Fill array with possible birthdays
                                int[] currentTest = new int[alienPopulation[0]];
                                for (int j = 0; j < alienPopulation[0]; ++j) {
                                    currentTest[j] = randMaker.nextInt(10000);
                                }

                                //Sets cannot contain duplicates, if one is added, tell the program a duplicate has been found
                                boolean foundDuplicate = false;
                                ArraySet<Integer> testForDuplicates = new ArraySet<>();
                                for (int j : currentTest) {
                                    if (testForDuplicates.contains(j)) {
                                        foundDuplicate = true;
                                        break;
                                    }
                                    testForDuplicates.add(j);
                                }

                                if (foundDuplicate) {
                                    successes++;
                                }
                            }

                            double realAnswer = (double)successes / 10000;

                            if (Math.abs(answer - realAnswer) <= 0.01) {
                                SharedPreferences.Editor editor = data.edit();
                                editor.putBoolean("solvedChallenge3", true);
                                editor.putBoolean("solvedChallenge3display",true);
                                Toast.makeText(getApplicationContext(),"Correct.",Toast.LENGTH_SHORT).show();
                                editor.commit();
                                txtSolved3.setVisibility(View.VISIBLE);
                                switchFancyTimer.setVisibility(View.VISIBLE);
                            } else {
                                alienPopulation[0] = 100 + randMaker.nextInt(100);
                                Toast fail = Toast.makeText(getApplicationContext(), "Wrong answer. I won't hold your hand through this, figure it out yourself.", Toast.LENGTH_LONG);
                                fail.show();
                                String question3 = "Imagine a planet called Tologon. This planet is arid, and its crust is has high amounts of sulfur in it. " +
                                        "Although you would think this would make this planet a desert planet, planets tend to contain more than just 1 biome, unlike " +
                                        "what some movies would have you believe. In any case, there is a variety of plant life, such as a plant that resembles the bamboo" +
                                        "that we all know about. It has a solid brick-like texture, and the only intelligent species of life on this planet, the Tolgonians, " +
                                        "often use the plant to fortify their subterranean homes. Just like our Earth, this planet's species have a concept of the birthday. " +
                                        "However, for their planet, the year has 10,000 days in it, which are split into 10 1000 day \"months\". Tologonian birthdays are " +
                                        "often celebrated using a cake made from a special" +
                                        "kind of mycelium that links the majority of the roots of the planet's other plant life. The fungus that creates the mycelium web actually " +
                                        "uses a novel form of sulfur metabolism that is energized by the sunlight through a unique series of chemical reactions that do not occur " +
                                        "naturally here on earth. " + alienPopulation[0] + " Tologonians have gathered in a room together. Birthdays on this planet are uniformly " +
                                        "distributed. What is the probability that at least two Tologonians in this room share the same birthday? An exact answer is not required, " +
                                        "as long as you are within 0.01 of the correct answer, your phone will accept it.\n" +
                                        "Also, be patient, this question is hard, your phone might take a couple seconds to calculate the right answer.";

                                txtChallenge3.setText(question3);
                            }
                        }
                    }
                }
            });
        //endregion

        //Give user the option to enable fancy timer once they complete it
        boolean checkFancy = data.getBoolean("useFancyClock",false);
        if (checkFancy) {
            switchFancyTimer.setChecked(true);
        }
        switchFancyTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = data.edit();
                if (switchFancyTimer.isChecked()) {
                    editor.putBoolean("useFancyClock", true);
                } else {
                    editor.putBoolean("useFancyClock", false);
                }
                editor.commit();
            }
        });

        //endregion

        //regionChallenge 4
        if (!solved4) {
            txtSolved4.setVisibility(View.GONE);
        }
        final Intent goAccel = new Intent(this,InertialFrameActivity.class);
        btnTryChallenge4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(goAccel);
            }
        });
        //endregion

        //regionChallenge 5
        if(!solved5){
            txtSolved5.setVisibility(View.GONE);
        }
        final int[] pityCounter = {0};
        btnSolve5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userAnswer = editTextC5.getText().toString().toUpperCase();
                if (userAnswer.contains("TTTATCCATCCATCCATCCATCCATCAT") ||
                userAnswer.contains("TCTATCCATCCATCCATCCATCCATCAT") ||
                userAnswer.contains("TTCATCCATCCATCCATCCATCCATCAT")) {
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge5", true);
                    editor.putBoolean("solvedChallenge5display",true);
                    editor.commit();
                    txtSolved5.setVisibility(View.VISIBLE);
                } else {
                    if (pityCounter[0] < 15) {
                        Toast fail = Toast.makeText(getApplicationContext(), "That answer is not what I was looking for.", Toast.LENGTH_LONG);
                        fail.show();
                        ++pityCounter[0];
                    } else {
                        if (userAnswer.length() < 20) {
                            Toast semiFail = Toast.makeText(getApplicationContext(), "Did you account for the start and stop codon? This is your only hint by the way, and only because I see you've spent some time on this.", Toast.LENGTH_LONG);
                            semiFail.show();
                        } else {
                            Toast fail = Toast.makeText(getApplicationContext(), "That answer is not what I was looking for.", Toast.LENGTH_LONG);
                            fail.show();
                        }
                    }
                }
                if (solved5 && userAnswer.contains("I can't do the the multiplier one because my accelerometer is garbage, let me " +
                        "use this easter egg to unlock it.")) {
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge4", true);
                    editor.putBoolean("solvedChallenge4display",true);
                    editor.commit();
                    Toast.makeText(getApplicationContext(),"Read the source code, didn't you? Sneaky sneaky.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //endregion

        //region Challenge 6
        final Intent startStonk = new Intent(this,StonkActivity.class);
        btnStartStonks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(startStonk);
            }
        });
        //endregion

        //regionChallenge 7
        if (!solved7) {
            txtSolved7.setVisibility(View.GONE);
            switchArbitrageShortcut.setVisibility(View.GONE);
        }

        boolean useOptimizedArbitrage = data.getBoolean("useOptimizedArbitrage",false);
        if (useOptimizedArbitrage) {
            switchArbitrageShortcut.setChecked(true);
        }

        switchArbitrageShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = data.edit();
                if (switchArbitrageShortcut.isChecked()) {
                    editor.putBoolean("useOptimizedArbitrage", true);
                } else {
                    editor.putBoolean("useOptimizedArbitrage", false);
                }
                editor.commit();
            }
        });

        final int[] guess = {1 + randMaker.nextInt(1000000)};
        //So the user can find it
        Log.e("NUMBER_THINKING_OF",Integer.toString(guess[0]));
        final String display = "Your phone has come up with a number from 1 to 1,000,000." +
                "\nRead its mind and enter it below. You could also be stupid about it and guess," +
                "but I have limited you to 1,000 guesses, use them wisely.";
        txtChallenge7.setText(display);
        btnSolve7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int guessesRemaining = data.getInt("guessesRemaining",1000);
                if (guessesRemaining > 1 && !editNumC7.getText().toString().equals("")) {
                    if(guess[0] == Integer.parseInt(editNumC7.getText().toString())) {
                        //If the guess is equal to the guess the user entered
                        SharedPreferences.Editor editor = data.edit();
                        editor.putBoolean("solvedChallenge7", true);
                        editor.putBoolean("solvedChallenge7display",true);
                        editor.commit();
                        Toast success = Toast.makeText(getApplicationContext(),"Congrats, you got it. Hopefully by reading log data, and not from dumb luck.",Toast.LENGTH_LONG);
                        success.show();
                        txtSolved7.setVisibility(View.GONE);
                        switchArbitrageShortcut.setVisibility(View.GONE);
                    } else {
                        //come up with a new guess and decrement amount remaining
                        Toast fail = Toast.makeText(getApplicationContext(),"The answer was " + guess[0] + " try again.",Toast.LENGTH_SHORT);
                        fail.show();
                        guess[0] = randMaker.nextInt(1000000);
                        Log.e("NUMBER_THINKING_OF",Integer.toString(guess[0]));
                        guessesRemaining -= 1;
                        SharedPreferences.Editor editor = data.edit();
                        editor.putInt("guessesRemaining", guessesRemaining);
                        editor.commit();
                        String display = "Your phone has come up with a number from 1 to 1,000,000." +
                                "\nRead its mind and enter it below. You could also be stupid about it and guess, " +
                                "but I have limited you to 1,000 guesses, use them wisely.";
                        display = display + "\nYou now have " + guessesRemaining + " guesses left.";
                        txtChallenge7.setText(display);
                    }
                } else {
                    if (guessesRemaining < 1) {
                        String display = "Your phone has come up with a number from 1 to 1,000,000." +
                                "\nRead its mind and enter it below. You could also be stupid about it and guess," +
                                "but I have limited you to 1,000 guesses, use them wisely.";
                        display = display + "\nYou have been banned from trying this challenge, clearly you don't get it after trying it 1000 times." +
                                "\n Waste your life elsewhere.";
                        txtChallenge7.setText(display);
                    }
                }
            }
        });
        //endregion

        //region Challenge 8
        if (!solved8) {
            txtSolved8.setVisibility(View.INVISIBLE);
            switchShowWelcome.setVisibility(View.INVISIBLE);
        }

        //switch code
        boolean checkWelcome = data.getBoolean("displayWelcome",false);
        if (checkWelcome) {
            switchShowWelcome.setChecked(true);
        }
        switchShowWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = data.edit();
                if (switchShowWelcome.isChecked()) {
                    editor.putBoolean("displayWelcome", true);
                } else {
                    editor.putBoolean("displayWelcome", false);
                }
                editor.commit();
            }
        });

        boolean acceptableValue = false;
        int seedValue = 10;
        while (!acceptableValue) {
            seedValue = 10 + randMaker.nextInt(90);
            if(seedValue % 10 != 0) {
                acceptableValue = true;
            }
        }
        ArrayList<String> patternValues = new ArrayList<>();
        BigDecimal previousValue = BigDecimal.valueOf(seedValue);
        for (int i = 0; i < 8; ++i) {
            String prevPattern = previousValue.toPlainString();
            StringBuilder prevPattern1 = new StringBuilder();
            prevPattern1.append(prevPattern);
            prevPattern1 = prevPattern1.reverse();
            String newNumber = prevPattern1.toString();
            BigDecimal value = new BigDecimal(newNumber);
            BigDecimal newValue = value.multiply(value);
            patternValues.add(newValue.toPlainString());
            previousValue = newValue;
        }
        StringBuilder display8 = new StringBuilder("Find the next number.");
        for (int i = patternValues.size()-1; i > -1; --i) {
            display8.append("\n\n").append(patternValues.get(i));
        }
        txtChallenge8.setText(display8.toString());
        final int[] finalSeedValue = {seedValue};
        btnSolve8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String userAnswer = editNumC8.getText().toString();
                    int answer = Integer.parseInt(userAnswer);
                    if (answer == finalSeedValue[0]) {
                        SharedPreferences.Editor editor = data.edit();
                        editor.putBoolean("solvedChallenge8", true);
                        editor.putBoolean("solvedChallenge8display",true);
                        editor.commit();
                        txtSolved8.setVisibility(View.VISIBLE);
                        switchShowWelcome.setVisibility(View.VISIBLE);
                    } else {
                        //Generate new puzzle
                        boolean acceptableValue = false;
                        int seedValue = 10;
                        while (!acceptableValue) {
                            seedValue = 10 + randMaker.nextInt(90);
                            if(seedValue % 10 != 0) {
                                acceptableValue = true;
                            }
                        }
                        ArrayList<String> patternValues = new ArrayList<>();
                        BigDecimal previousValue = BigDecimal.valueOf(seedValue);
                        for (int i = 0; i < 8; ++i) {
                            String prevPattern = previousValue.toPlainString();
                            StringBuilder prevPattern1 = new StringBuilder();
                            prevPattern1.append(prevPattern);
                            prevPattern1 = prevPattern1.reverse();
                            String newNumber = prevPattern1.toString();
                            BigDecimal value = new BigDecimal(newNumber);
                            BigDecimal newValue = value.multiply(value);
                            patternValues.add(newValue.toPlainString());
                            previousValue = newValue;
                        }
                        StringBuilder display8 = new StringBuilder("Find the next number.");
                        for (int i = patternValues.size()-1; i > -1; --i) {
                            display8.append("\n\n").append(patternValues.get(i));
                        }
                        txtChallenge8.setText(display8.toString());
                        finalSeedValue[0] = seedValue;
                        Toast.makeText(getApplicationContext(),"Nope, try again. No hints for this.",Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e) {
                    //Probably because the thing was blank, do nothing
                }
            }
        });

        //endregion

        //regionChallenge 9
        if(!solved9){
            txtSolved9.setVisibility(View.GONE);
            editColor.setVisibility(View.GONE);
            btnChangeColor.setVisibility(View.GONE);
        }

        String beginning = "Welcome to the final boss of questions.\nEven I, the creator of this app, don't know how to solve this one.\nI mean " +
                "I have some idea, but I'm not APT level good.\nBe the first to solve this one, and I'll literally include your name here in this message.\n" +
                "Anyway, as for the actual question, there is a sequence of numbers below.\nFind the next 20 numbers:\n";
        StringBuilder sequence = new StringBuilder();
        for(int i = 0;i<400;++i) {
            if(i%20==0){
                sequence.append("\n");
            }
            sequence.append(randMaker.nextInt(10));
        }
        final StringBuilder answer = new StringBuilder();
        for(int i = 0;i<20;++i){
            answer.append(randMaker.nextInt());
        }
        String display9 = beginning + sequence.toString();
        txtChallenge9.setText(display9);
        btnSolve9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userAnswer = editNumC9.getText().toString();
                if(userAnswer.equals(answer.toString())){
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("solvedChallenge9", true);
                    editor.putBoolean("solvedChallenge9display",true);
                    editor.commit();
                    txtSolved9.setVisibility(View.VISIBLE);
                    btnChangeColor.setVisibility(View.VISIBLE);
                    editColor.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(),"That is incorrect",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String color = editColor.getText().toString();
                SharedPreferences.Editor editor = data.edit();
                editor.putString("welcomeColor",color);
                editor.putBoolean("useDifferentColor",true);
                editor.commit();
            }
        });

        //endregion
    }

    private void showAchievements(){
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                    }
                });
    }
}