package com.grabble.grabble;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class SettingsActivity extends AppCompatActivity {

    private ArrayList<String> inventory = new ArrayList<String>();

    private boolean isAudioChecked;
    private boolean isVibrationChecked;
    private boolean isHardModeChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.MY_SETTINGS_PREF), MODE_PRIVATE);

        isAudioChecked = settings.getBoolean("isAudioEnabled", true);
        Log.i("SettingsActivity","Audio mode: " + isAudioChecked);
        Switch switchAudio = (Switch)findViewById(R.id.switchAudio);
        switchAudio.setChecked(isAudioChecked);

        isVibrationChecked = settings.getBoolean("isVibrationEnabled", true);
        Switch switchVibration = (Switch)findViewById(R.id.switchVibration);
        switchVibration.setChecked(isVibrationChecked);

        isHardModeChecked = settings.getBoolean("isHardModeEnabled", false);
        ToggleButton switchHardMode = (ToggleButton)findViewById(R.id.toggleRegular);
        switchHardMode.setChecked(isHardModeChecked);

        // load collected letters from preference
        SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = inventoryPref.getString("inventoryJson", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        if(json != null) {
            inventory = gson.fromJson(json, type);
        }

    }

    public void toggleAudio(View view) {

        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.MY_SETTINGS_PREF), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Switch switchAudio = (Switch)findViewById(R.id.switchAudio);

        editor.putBoolean("isAudioEnabled", switchAudio.isChecked() );
        editor.commit();

    }

    public void toggleVibration(View view) {
        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.MY_SETTINGS_PREF), MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Switch switchVibration = (Switch)findViewById(R.id.switchVibration);

        editor.putBoolean("isVibrationEnabled", switchVibration.isChecked() );
        editor.commit();
    }

    public void toggleDifficulty(View view) {

        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.MY_SETTINGS_PREF), MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        Log.i("SettingsActivity","Hard mode: " + isHardModeChecked);

        if (!isHardModeChecked) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    SettingsActivity.this);

            alertDialogBuilder.setMessage("Switching to Hard Mode will limit your inventory to 25 letters. If you have more than" +
                    " that you will lose those collected letters. Are you sure you want to continue?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Update letter inventory with new letter
                            if(inventory.size() >= 25) {
                                ArrayList<String> truncatedInventory = new ArrayList<String>();
                                for (int i = 0; i < 25; i++) {
                                    truncatedInventory.add(inventory.get(i));
                                }
                                inventory = truncatedInventory;

                                Log.i("SettingsActivity", "Inventory size: " + inventory.size());

                                SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
                                SharedPreferences.Editor editorInventory = inventoryPref.edit();
                                Gson gson = new Gson();
                                String json = gson.toJson(inventory);
                                editorInventory.putString("inventoryJson", json);
                                editorInventory.commit();
                            }

                            ToggleButton toggleDiff = (ToggleButton) findViewById(R.id.toggleRegular);
                            isHardModeChecked = true;
                            editor.putBoolean("isHardModeEnabled", toggleDiff.isChecked());
                            editor.commit();
                            Log.i("SettingsActivity","Hard mode: " + isHardModeChecked);
                        }
                    });
            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ToggleButton toggleDiff = (ToggleButton) findViewById(R.id.toggleRegular);
                            toggleDiff.setChecked(false);
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else {

            ToggleButton toggleDiff = (ToggleButton) findViewById(R.id.toggleRegular);
            isHardModeChecked = false;
            editor.putBoolean("isHardModeEnabled", toggleDiff.isChecked());
            editor.commit();
        }
    }
}
