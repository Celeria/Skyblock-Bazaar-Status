package com.andranym.skyblockbazaarstatus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public class ChallengeActivity extends AppCompatActivity {

    Button btnSolve1;
    Button btnSolve2;
    Button btnSolve3;
    Button btnTryChallenge4;
    TextView txtSolved1;
    TextView txtSolved2;
    TextView txtSolved3;
    TextView txtSolved4;
    TextView txtChallenge1;
    TextView txtChallenge3;
    EditText editNumChallenge1;
    EditText editTextC2;
    EditText editNumC3;
    Switch switchFancyTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        setTitle("Challenges");

        //regionDeclare UI elements
        btnSolve1 = findViewById(R.id.btnSolve1);
        btnSolve2 = findViewById(R.id.btnSolve2);
        btnSolve3 = findViewById(R.id.btnSolve3);
        btnTryChallenge4 = findViewById(R.id.btnTryChallenge4);
        txtSolved1 = findViewById(R.id.txtSolved1);
        txtSolved2 = findViewById(R.id.txtSolved2);
        txtSolved3 = findViewById(R.id.txtSolved3);
        txtSolved4 = findViewById(R.id.txtSolved4);
        txtChallenge1 = findViewById(R.id.txtChallenge1);
        txtChallenge3 = findViewById(R.id.txtChallenge3);
        editNumChallenge1 = findViewById(R.id.editNumC1);
        editTextC2 = findViewById(R.id.editTextC2);
        editNumC3 = findViewById(R.id.editNumC3);
        switchFancyTimer = findViewById(R.id.switchFancyTimer);

        //endregion

        //regionRetrieve Data
        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final boolean solved1 = data.getBoolean("solvedChallenge1",false);
        final boolean solved2 = data.getBoolean("solvedChallenge2",false);
        final boolean solved3 = data.getBoolean("solvedChallenge3",false);
        final boolean solved4 = data.getBoolean("solvedChallenge4",false);
        //endregion

        //Make random number generator
        final Random randMaker = new Random();

        //region Check for achievements
        if (solved1) {
            Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                    .unlock("CgkI-4n1zrYYEAIQAA");
        }
        //endregion

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
                        editor.commit();
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
                    editor.commit();
                    txtSolved2.setVisibility(View.VISIBLE);
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
                "often celebrated using a cake made from a special" +
                "kind of mycelium that links the majority of the roots of the planet's other plant life. The fungus that creates the mycelium web actually" +
                "uses a novel form of sulfur metabolism that is energized by the sunlight through a unique series of chemical reactions that do not occur" +
                "naturally here on earth. " + alienPopulation[0] + " Tologonians have gathered in a room together. Birthdays on this planet are uniformly " +
                "distributed. What is the probability that at least two Tologonians in this room share the same birthday? An exact answer is not required," +
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
                            for (int i = 0; i < 100000; ++i) {

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

                            double realAnswer = (double)successes / 100000;

                            if (Math.abs(answer - realAnswer) <= 0.01) {
                                SharedPreferences.Editor editor = data.edit();
                                editor.putBoolean("solvedChallenge3", true);
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
                                        "kind of mycelium that links the majority of the roots of the planet's other plant life. The fungus that creates the mycelium web actually" +
                                        "uses a novel form of sulfur metabolism that is energized by the sunlight through a unique series of chemical reactions that do not occur" +
                                        "naturally here on earth. " + alienPopulation[0] + " Tologonians have gathered in a room together. Birthdays on this planet are uniformly " +
                                        "distributed. What is the probability that at least two Tologonians in this room share the same birthday? An exact answer is not required," +
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
    }
}