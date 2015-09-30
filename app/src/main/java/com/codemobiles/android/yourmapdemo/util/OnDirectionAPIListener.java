package com.codemobiles.android.yourmapdemo.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by maboho_retina on 3/14/15 AD.
 */
public interface OnDirectionAPIListener {
    public void onFinished(DirectionsAPI api, ArrayList<LatLng> points);
}
