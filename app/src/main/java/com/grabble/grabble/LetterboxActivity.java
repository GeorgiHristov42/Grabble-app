package com.grabble.grabble;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LetterboxActivity extends AppCompatActivity {


    private ArrayList<String> inventory = new ArrayList<String>();
    private ArrayList<String> dictionary;
    private ArrayList<String> userWords;

    private GridView gvLetterboxes;
    //Settings
    private boolean audioEnabled;
    private boolean vibrationEnabled;
    private boolean hardModeEnabled;

    private Vibrator vib;
    private MediaPlayer mps;
    private MediaPlayer mpf;

    private String[] letterboxes = new String[7];

    public static final String TAG = "LeterboxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letterbox);

        //Get settings
        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.MY_SETTINGS_PREF), MODE_PRIVATE);
        audioEnabled = settings.getBoolean("isAudioEnabled", true);
        vibrationEnabled = settings.getBoolean("isVibrationEnabled", true);
        hardModeEnabled = settings.getBoolean("isHardModeEnabled", false);
        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mps = MediaPlayer.create(this, R.raw.word_success);
        mpf = MediaPlayer.create(this, R.raw.word_fail);

        final Button btnFormWord = (Button) findViewById(R.id.btnFormWord);
        btnFormWord.setEnabled(false);

        for(int i=0; i < 7;i++){
            letterboxes[i]="empty";
        }

        // load collected letters from preference
        SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = inventoryPref.getString("inventoryJson", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        if(json != null) {
            inventory = gson.fromJson(json, type);
        }

        //load dictionary
        String dictionaryRaw = readDictionary(this);
        dictionary = parseWords(dictionaryRaw,7);

        String userWordsRaw = readUserWords(this);
        userWords = parseWords(userWordsRaw,7);


        //Initiate GridViews
        final GridView gvInventory = (GridView) findViewById(R.id.InventoryGrid);
        gvInventory.setAdapter(new ImageAdapter(this,inventory));

        gvLetterboxes = (GridView)  findViewById(R.id.LetterboxGridView);
        gvLetterboxes.setAdapter(new ImageAdapter(this,letterboxes));

        gvInventory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                for(int i=0;i < 7;i++){
                    if(letterboxes[i] == "empty"){
                        letterboxes[i] = inventory.remove(position);
                        break;
                    }
                }
                if(!Arrays.asList(letterboxes).contains("empty")){
                    btnFormWord.setEnabled(true);
                }

                Log.i(TAG,"Letterboxes array list: " + letterboxes);

                gvInventory.setAdapter(new ImageAdapter(LetterboxActivity.this,inventory));
                gvLetterboxes.setAdapter(new ImageAdapter(LetterboxActivity.this,letterboxes));
            }
        });

        //Set onLtemLongClickListener for deleting letters
        gvInventory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean  onItemLongClick(AdapterView<?> parent, View v,
                                            final int position, long id) {

                if(vibrationEnabled) {
                    vib.vibrate(100);
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        LetterboxActivity.this);

                alertDialogBuilder.setMessage("Delete letter: " + inventory.get(position) + " ?");
                alertDialogBuilder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                inventory.remove(position);
                                gvInventory.setAdapter(new ImageAdapter(LetterboxActivity.this,inventory));
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            }
        });

        gvLetterboxes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                if(letterboxes[position] != "empty") {
                    inventory.add(letterboxes[position]);
                    letterboxes[position] = "empty";

                    btnFormWord.setEnabled(false);

                    gvInventory.setAdapter(new ImageAdapter(LetterboxActivity.this, inventory));
                    gvLetterboxes.setAdapter(new ImageAdapter(LetterboxActivity.this, letterboxes));
                }
            }
        });
    }


    public void formWord(View view) {
        boolean wordFound = false;
        StringBuilder strBuilderNewWord = new StringBuilder();
        for (int i = 0; i < letterboxes.length; i++) {
            strBuilderNewWord.append(letterboxes[i]);
        }
        String word = strBuilderNewWord.toString();
        Log.i(TAG,"Formed word: " + word);
        Log.i(TAG,"Found match: " + dictionary.size());
        for (String s : dictionary){
//            Log.i(TAG,"Word: " + s);
//            if(s.equals("echidna")){
//                Log.i(TAG,"Test word: "+ s + " formed word " + word);
//            }
            if (s.equalsIgnoreCase(word)){
                wordFound = true;
                Log.i(TAG,"Found match: " + word);
                int points = countPoints(letterboxes);

                StringBuilder strBuilderUserWords = new StringBuilder();
                for (String w : userWords) {
                    strBuilderUserWords.append(w);

                }
                strBuilderUserWords.append(word);
                String data = strBuilderUserWords.toString();

                writeToUserWords(data,this);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);

                if(!userWords.contains(word)) {

                    if(vibrationEnabled) {
                        vib.vibrate(100);
                    }
                    if(audioEnabled) {
                        mps.start();
                    }
                    alertDialogBuilder.setMessage("Good job! You formed the word: " +  word + " which is " + points + " points + 20 bonus points for a new word");
                    alertDialogBuilder.setPositiveButton("Nice!",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    for(int i=0; i < 7;i++){
                                        letterboxes[i]="empty";
                                    }
                                    // Update letter inventory with new letter
                                    SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
                                    SharedPreferences.Editor editorInventory = inventoryPref.edit();
                                    Gson gson = new Gson();
                                    String json = gson.toJson(inventory);
                                    editorInventory.putString("inventoryJson", json);
                                    editorInventory.commit();

                                    gvLetterboxes.setAdapter(new ImageAdapter(LetterboxActivity.this, letterboxes));
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    break;
                }
                else {
                    if(vibrationEnabled) {
                        vib.vibrate(100);
                    }
                    if(audioEnabled) {
                        mps.start();
                    }
                    alertDialogBuilder.setMessage("Good job! You formed the word: " +  word + " which is " + points + " points!");
                    alertDialogBuilder.setPositiveButton("Nice!",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    for(int i=0; i < 7;i++){
                                        letterboxes[i]="empty";
                                    }
                                    // Update letter inventory with new letter
                                    SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
                                    SharedPreferences.Editor editorInventory = inventoryPref.edit();
                                    Gson gson = new Gson();
                                    String json = gson.toJson(inventory);
                                    editorInventory.putString("inventoryJson", json);
                                    editorInventory.commit();
                                    gvLetterboxes.setAdapter(new ImageAdapter(LetterboxActivity.this, letterboxes));
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    break;
                }
            }
        }
        if(!wordFound) {
            if(vibrationEnabled) {
                vib.vibrate(100);
            }
            if(audioEnabled) {
                mpf.start();
            }

            Toast.makeText(LetterboxActivity.this, "No such word",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private String readDictionary(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.grabble);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    private String readUserWords(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("userWords.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    private void writeToUserWords(String data,Context context) {
        try {
           // File file=new File("userWords.txt");
            Log.i(TAG, "Word written down: " + data);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("userWords.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private static ArrayList<String> parseWords(String string, int partitionSize) {
        ArrayList<String> parts = new ArrayList<String>();
        int len = string.length();
        for (int i=0; i<len; i+=partitionSize)
        {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    private static int countPoints(String[] letters){
        int points = 0;
        for(int i = 0; i < letters.length ;i++)
        {
            switch(letters[i]){
                case "A":
                    points = points + 3;
                    break;
                case "B":
                    points = points + 20;
                    break;
                case "C":
                    points = points + 13;
                    break;
                case "D":
                    points = points + 10;
                    break;
                case "E":
                    points = points + 1;
                    break;
                case "F":
                    points = points + 15;
                    break;
                case "G":
                    points = points + 18;
                    break;
                case "H":
                    points = points + 9;
                    break;
                case "I":
                    points = points + 5;
                    break;
                case "J":
                    points = points + 25;
                    break;
                case "K":
                    points = points + 22;
                    break;
                case "L":
                    points = points + 11;
                    break;
                case "M":
                    points = points + 14;
                    break;
                case "N":
                    points = points + 6;
                    break;
                case "O":
                    points = points + 4;
                    break;
                case "P":
                    points = points + 19;
                    break;
                case "Q":
                    points = points + 24;
                    break;
                case "R":
                    points = points + 8;
                    break;
                case "S":
                    points = points + 7;
                    break;
                case "T":
                    points = points + 2;
                    break;
                case "U":
                    points = points + 12;
                    break;
                case "V":
                    points = points + 21;
                    break;
                case "W":
                    points = points + 17;
                    break;
                case "X":
                    points = points + 23;
                    break;
                case "Y":
                    points = points + 16;
                    break;
                case "Z":
                    points = points + 26;
                    break;
                case "empty":
                    points = R.drawable.empty;
                    break;
            }
        }
        return points;
    }


}

