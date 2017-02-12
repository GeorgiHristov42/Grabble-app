package com.grabble.grabble;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Georgi on 1/18/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private Integer[] mThumbIds;

    public ImageAdapter(Context c, ArrayList<String> l) {
        mContext = c;
        String[] arr = l.toArray(new String[l.size()]);
        Integer[] images = prepareGridViewImages(arr);
        mThumbIds = images;
    }

    public ImageAdapter(Context c, String[] l) {
        mContext = c;
        Integer[] images = prepareGridViewImages(l);
        mThumbIds = images;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            int width = metrics.widthPixels / 8;
            int height = metrics.heightPixels / 10;
            imageView.setLayoutParams(new GridView.LayoutParams(width, height));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    public Integer[] prepareGridViewImages(String[] arr) {
        Integer[] images = new Integer[arr.length];

        for(int i = 0; i < arr.length ;i++)
        {
            switch(arr[i]){
                case "A":
                    images[i] = R.drawable.a;
                    break;
                case "B":
                    images[i] = R.drawable.b;
                    break;
                case "C":
                    images[i] = R.drawable.c;
                    break;
                case "D":
                    images[i] = R.drawable.d;
                    break;
                case "E":
                    images[i] = R.drawable.e;
                    break;
                case "F":
                    images[i] = R.drawable.f;
                    break;
                case "G":
                    images[i] = R.drawable.g;
                    break;
                case "H":
                    images[i] = R.drawable.h;
                    break;
                case "I":
                    images[i] = R.drawable.i;
                    break;
                case "J":
                    images[i] = R.drawable.j;
                    break;
                case "K":
                    images[i] = R.drawable.k;
                    break;
                case "L":
                    images[i] = R.drawable.l;
                    break;
                case "M":
                    images[i] = R.drawable.m;
                    break;
                case "N":
                    images[i] = R.drawable.n;
                    break;
                case "O":
                    images[i] = R.drawable.o;
                    break;
                case "P":
                    images[i] = R.drawable.p;
                    break;
                case "Q":
                    images[i] = R.drawable.q;
                    break;
                case "R":
                    images[i] = R.drawable.r;
                    break;
                case "S":
                    images[i] = R.drawable.s;
                    break;
                case "T":
                    images[i] = R.drawable.t;
                    break;
                case "U":
                    images[i] = R.drawable.u;
                    break;
                case "V":
                    images[i] = R.drawable.v;
                    break;
                case "W":
                    images[i] = R.drawable.w;
                    break;
                case "X":
                    images[i] = R.drawable.x;
                    break;
                case "Y":
                    images[i] = R.drawable.y;
                    break;
                case "Z":
                    images[i] = R.drawable.z;
                    break;
                case "empty":
                    images[i] = R.drawable.empty;
                    break;
            }
        }
        return images;
    }

    // references to our images
//    private Integer[] mThumbIds = {
//            R.drawable.a, R.drawable.b,
//            R.drawable.c, R.drawable.d,
//            R.drawable.e, R.drawable.f,
//            R.drawable.g, R.drawable.h,
//            R.drawable.i, R.drawable.j,
//            R.drawable.k, R.drawable.l,
//            R.drawable.m, R.drawable.n,
//            R.drawable.o, R.drawable.p,
//            R.drawable.q, R.drawable.r,
//            R.drawable.s, R.drawable.t,
//            R.drawable.u, R.drawable.v,
//            R.drawable.w, R.drawable.x,
//            R.drawable.y, R.drawable.z
//    };
}