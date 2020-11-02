package com.andranym.skyblockbazaarstatus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.BreakIterator;

public class InertialFrameActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    public float x = 10;
    public float y = 10;
    public float z = 10;

    TextView movement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inertial_frame);

        movement = findViewById(R.id.txtInertialInfo);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final float[] second_x = {10};
        final float[] second_y = {10};
        final float[] second_z = {10};

        final double[] previousMovement = {10};

        new Thread() {
            @Override
            public void run() {
                boolean keepRunning = true;
                while (keepRunning) {
                    second_x[0] = x;
                    second_y[0] = y;
                    second_z[0] = z;

                    double currentMovement = Math.sqrt((second_x[0]) * (second_x[0]) +
                            (second_y[0]) * (second_y[0]) +
                            (second_z[0]) * (second_z[0]));

                    String display = "Place your phone in an inertial frame of reference:\nCurrent acceleration experienced: " + Round4(currentMovement);
                    new update().execute(display);

                    if (previousMovement[0] < 0.2 && currentMovement < 0.2) {
                        display = "You have completed this challenge.\nPress back button to exit.";
                        new update().execute(display);
                        SharedPreferences.Editor editor = data.edit();
                        editor.putBoolean("solvedChallenge4", true);
                        editor.putBoolean("solvedChallenge4display",true);
                        editor.commit();
                        keepRunning = false;
                    }
                    previousMovement[0] = currentMovement;
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    class update extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            movement.setText(s);
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float ax = event.values[0];
        float ay = event.values[1];
        float az = event.values[2];
        x = ax;
        y = ay;
        z = az;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //Quick method for removing pesky floating point imprecision decimals
    public double Round4(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
