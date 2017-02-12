package com.grabble.grabble;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Game opened");


    }


    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);

    }


    public void openMaps(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void openLetterbox(View view) {
        Intent intent = new Intent(this, LetterboxActivity.class);
        startActivity(intent);
    }

    public void openStats(View view) {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }
}
