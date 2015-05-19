package com.rmathur.cumtd.ui.fragments.main;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.rmathur.cumtd.R;

import java.util.Arrays;
import java.util.List;

class PopupAdapter implements InfoWindowAdapter {
    private View popup = null;
    private LayoutInflater inflater = null;

    PopupAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return (null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (popup == null) {
            popup = inflater.inflate(R.layout.popup, null);
        }

        TextView title = (TextView) popup.findViewById(R.id.title);
        TextView busNames = (TextView) popup.findViewById(R.id.busName);
        TextView minsLeft = (TextView) popup.findViewById(R.id.minsLeft);

        title.setText(marker.getTitle());

        String info = marker.getSnippet();
        List<String> departureList = Arrays.asList(info.split(","));

        busNames.setText("");
        minsLeft.setText("");

        for (int i = 0; i < (departureList.size() - 2); i += 2) {
            if (i == departureList.size() - 4) {
                busNames.setText(busNames.getText() + departureList.get(i));
                minsLeft.setText(minsLeft.getText() + departureList.get(i + 1));
            } else {
                busNames.setText(busNames.getText() + departureList.get(i) + "\n");
                minsLeft.setText(minsLeft.getText() + departureList.get(i + 1) + "\n");
            }
        }

        return (popup);
    }
}