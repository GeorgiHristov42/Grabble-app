package com.grabble.grabble;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Georgi on 12/5/2016.
 */

public class Placemark {
    public final int name;
    public final String description;
    public final LatLng point;

    public Placemark(int name, String description, LatLng point) {
        this.name = name;
        this.description = description;
        this.point = point;
    }

}
