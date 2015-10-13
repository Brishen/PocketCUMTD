package com.rmathur.cumtd.ui.fragments.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rmathur.cumtd.R;
import com.rmathur.cumtd.data.CSVFile;
import com.rmathur.cumtd.data.model.Stop;
import com.rmathur.cumtd.ui.RepeatedSafeToast;
import com.rmathur.cumtd.ui.activities.MainActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    MapView mapView;
    GoogleMap map;
    double latitude;
    double longitude;
    HashMap<Marker, String> stopDb = new HashMap<Marker, String>();

    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    private DeparturesFromStop departureAsync = null;
    private MyTask loadMap = null;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.inject(this, view);
        getActivity().setTitle("Map");
        hideKeyboard();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

        mapView = (MapView) view.findViewById(R.id.mapview);

        mapView.onCreate(savedInstanceState);
        mapView.onResume(); //without this, map showed but was empty

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
        map.setInfoWindowAdapter(new PopupAdapter(this.getActivity().getLayoutInflater()));
        MapsInitializer.initialize(this.getActivity());

        loadMap = new MyTask(this.getActivity());
        loadMap.execute();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                String stopId = stopDb.get(arg0);
                departureAsync = new DeparturesFromStop(arg0, getCurrentActivity());
                departureAsync.execute(stopId);
                return true;
            }
        });

        return view;
    }

    public Activity getCurrentActivity() {
        return this.getActivity();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Populates parameters with lat/lon information
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            double unionLatitude = 40.109243;
            double unionLongitude = -88.227253;

            // Updates the location and zoom of the MapView
            CameraUpdate cameraUpdate;
            if (40.047427 < latitude && latitude < 40.151161 && -88.304157 < longitude && longitude < -88.171978) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(unionLatitude, unionLongitude), 16);
            }
            map.moveCamera(cameraUpdate);
        } else {
            Log.e("Error", "Failed to get location");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("Error", "Failed to get location");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Error", "Connection suspended");
    }

    @Override
    public void onResume() {
        hideKeyboard();
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        if (departureAsync != null) {
            departureAsync.cancel(true);
        }
        if (loadMap != null) {
            loadMap.cancel(true);
        }
        departureAsync = null;
        loadMap = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    class MyTask extends AsyncTask<String, Void, ArrayList<Stop>> {
        ProgressDialog pd;
        Activity a;

        public MyTask(Activity activity) {
            this.a = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(a);
            pd.setMessage("Loading map...");
            pd.setProgress(0);
            pd.show();
        }

        @Override
        protected ArrayList<Stop> doInBackground(String... strings) {
            InputStream inputStream = getResources().openRawResource(R.raw.stops);
            CSVFile csvFile = new CSVFile(inputStream);
            ArrayList<Stop> stops = csvFile.read();
            return stops;
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops) {
            // long one
            pd.setProgress(50);
            for (int i = 0; i < stops.size(); i++) {
                Marker marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(stops.get(i).getLatitude(), stops.get(i).getLongitude()))
                                .title(stops.get(i).getStopName())
                                .snippet("")
                                .draggable(false)
                );
                stopDb.put(marker, stops.get(i).getStopId());
            }
            pd.setProgress(100);
            if (pd != null) {
                pd.dismiss();
            }
            loadMap = null;
        }
    }

    private class DeparturesFromStop extends AsyncTask<String, Void, String> {

        private Marker marker;
        private String stopId;
        public Activity activity;

        public DeparturesFromStop(Marker arg0, Activity a) {
            this.marker = arg0;
            this.activity = a;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... strings) {
            if (!isAdded()) {
                return "";
            }

            String method = "GetDeparturesByStop";
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            stopId = strings[0];
            HttpGet httpGet = new HttpGet(getString(R.string.mainAPI_URL) + method + "?stop_id=" + stopId + "&key=" + getString(R.string.apiKey) + "&pt=60");
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e(MainActivity.class.toString(), "Failed to get JSON object");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            RepeatedSafeToast.show(activity, getString(R.string.internetError));
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        RepeatedSafeToast.show(activity, getString(R.string.internetError));
                    }
                });
            }
            if (builder.toString().equals("") || builder.toString() == null)
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        RepeatedSafeToast.show(activity, getString(R.string.internetError));
                    }
                });
            return builder.toString();
        }

        protected void onPostExecute(String result) {

            if (!marker.isInfoWindowShown()) {
                if (!isAdded()) {
                    return;
                }
                String snippetText = "";
                marker.setSnippet(snippetText);
                try {
                    JsonParser newJsonParser = new JsonParser();
                    int counter = 0;
                    JsonArray otherResults = newJsonParser.parse(result).getAsJsonObject().getAsJsonArray("departures");
                    for (JsonElement individualResult : otherResults) {
                        String nameOfBus = individualResult.getAsJsonObject().get("headsign").getAsString();
                        String expectedTime = individualResult.getAsJsonObject().get("expected_mins").getAsString();

                        if (nameOfBus.equals("120E Teal Orchard Downs"))
                            nameOfBus = "120E Teal";
                        else if (nameOfBus.equals("100S Yellow First & Greg"))
                            nameOfBus = "100S Yellow";
                        else if (nameOfBus.equals("1S YellowHOPPER Gerty"))
                            nameOfBus = "1S YellowHOPPER";
                        else if (nameOfBus.equals("1S YellowHOPPER E-14"))
                            nameOfBus = "1S YellowHOPPER";
                        else if (nameOfBus.equals("100S Yellow E14"))
                            nameOfBus = "100S Yellow";
                        else if (nameOfBus.equals("12E Teal Orchard Downs"))
                            nameOfBus = "12E Teal";
                        else if (nameOfBus.equals("12E Teal PAR"))
                            nameOfBus = "12E Teal";

                        if (expectedTime.equals("0"))
                            expectedTime = "DUE";
                        else if (expectedTime.equals("1"))
                            expectedTime += " min";
                        else
                            expectedTime += " mins";

                        counter++;
                        if (counter == 5) {
                            snippetText += nameOfBus + "," + expectedTime;
                            break;
                        } else {
                            snippetText += nameOfBus + "," + expectedTime + ",";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                this.activity = null;
                departureAsync = null;
                marker.setSnippet(snippetText);
                marker.showInfoWindow();
            } else {
                marker.hideInfoWindow();
                marker.setSnippet("");
            }
            return;
        }
    }
}