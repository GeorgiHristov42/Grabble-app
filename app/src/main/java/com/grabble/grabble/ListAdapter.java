package com.grabble.grabble;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Georgi on 1/22/2017.
 */

public class ListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] words;
    private final Integer[] scores;

    public ListAdapter(Context context, String[] words, Integer[] scores) {
        super(context, R.layout.listview_layout, words);
        this.context = context;
        this.words = words;
        this.scores = scores;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_layout, parent, false);
        TextView wordTextView = (TextView) rowView.findViewById(R.id.text_left);
        TextView scoreTextView = (TextView) rowView.findViewById(R.id.text_right);
        wordTextView.setText(words[position]);
        scoreTextView.setText(scores[position].toString() + " points");

        return rowView;
    }
}
