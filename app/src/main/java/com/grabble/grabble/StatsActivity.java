package com.grabble.grabble;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class StatsActivity extends AppCompatActivity {

    private ArrayList<String> userWords;
    private Integer[] scores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        String userWordsRaw = readUserWords(this);
        userWords = parseWords(userWordsRaw,7);

        int totalScore = 0;
        scores = new Integer[userWords.size()];
        for(int i=0; i<userWords.size(); i++){
            scores[i] = countPoints(userWords.get(i));
            totalScore += countPoints(userWords.get(i));
        }

        String[] userWordsArray = userWords.toArray(new String[0]);

        Set<String> uniqueWords = new HashSet<String>(userWords);
        Log.i("Stats","Words list length: " + userWords.size());
        Log.i("Stats","Words set length: " + uniqueWords.size());


        ListView allWordsLV = (ListView) findViewById(R.id.wordsListView);
        allWordsLV.setAdapter(new ListAdapter(this, userWordsArray,scores));

        if(userWords.size() != 0) {
            int maxIndex = findMax(scores);
            String[] bestWord = {userWords.get(maxIndex)};
            Integer[] bestScore = {scores[maxIndex]};

            ListView bestWordLV = (ListView) findViewById(R.id.bestWordListView);
            bestWordLV.setAdapter(new ListAdapter(this, bestWord, bestScore));

            totalScore = totalScore + 20 * uniqueWords.size();
            TextView txtTotalScore = (TextView) findViewById(R.id.txtTotalScore2);
            txtTotalScore.setText(totalScore + " points!");
        }
    }

    public static int findMax(Integer[] array) {
        int largest = array[0], index = 0;
        for (int i = 1; i < array.length; i++) {
            if ( array[i] > largest ) {
                largest = array[i];
                index = i;
            }
        }
        return index;
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

    private static ArrayList<String> parseWords(String string, int partitionSize) {
        ArrayList<String> parts = new ArrayList<String>();
        int len = string.length();
        for (int i=0; i<len; i+=partitionSize)
        {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    private static int countPoints(String word){
        int points = 0;
        ArrayList<String> lettersList = parseWords(word,1);
        String[] letters = lettersList.toArray(new String[0]);
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
