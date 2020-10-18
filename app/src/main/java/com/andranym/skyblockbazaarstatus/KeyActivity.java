package com.andranym.skyblockbazaarstatus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// =========================================================================================
//  WHOLE ACTIVITY IS NOW NO LONGER NEEDED
//===========================================================================
public class KeyActivity extends AppCompatActivity {

    Button btnEnterKey;
    EditText editTextApiKey;
    ProgressDialog pd;
    public boolean found_key = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);

        btnEnterKey = (Button)findViewById(R.id.btnEnterKey);
        editTextApiKey = (EditText)findViewById(R.id.apiKey);

        //Navigate back to the MainActivity once a successful key is stored
        final Intent intent = new Intent(this, MainActivity.class);

        //Navigate to DemonstrationActivity if you are a special person
        final Intent demoIntent = new Intent(this,DemonstrationActivity.class);

        btnEnterKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //The reason for pressing the button twice on success, is first to let the user know it worked, second because
                //I couldn't figure out how to start new activity from inside
                if (!found_key) {
                    String possible_key = editTextApiKey.getText().toString();
                    if (possible_key != null && possible_key.length() > 5) {
                        if (possible_key.equals("demonstrationplease")) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = sharedPref.edit();
                            //Uses my own API key since this should be a limited application
                            editor.putString(getString(R.string.preference_api_key), "notactuallymykey");
                            editor.commit();
                            startActivity(demoIntent);
                        } else {
                            new JsonTask().execute(getString(R.string.url_beginning) + possible_key);
                        }
                    } else {
                        btnEnterKey.setText(getString(R.string.button_fail));
                    }
                } else{
                    startActivity(intent);
                }
            }
        });
    }

    //Code I found online that retrieves JSON information from a URL
    //I modified it so that it would be able to check the bazaar data properly
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(KeyActivity.this);
            pd.setMessage("Checking your API key");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            if (result != null) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                //Uses my own API key since this should be a limited application
                String possible_key = editTextApiKey.getText().toString();
                editor.putString(getString(R.string.preference_api_key),possible_key);
                editor.commit();
                found_key = true;
                btnEnterKey.setText(getString(R.string.button_success));
            } else {
                btnEnterKey.setText(R.string.button_fail);
            }
        }
    }
}
