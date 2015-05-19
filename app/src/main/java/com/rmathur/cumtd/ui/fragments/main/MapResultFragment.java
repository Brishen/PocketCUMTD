package com.rmathur.cumtd.ui.fragments.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rmathur.cumtd.R;
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

import butterknife.ButterKnife;

public class MapResultFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    MapView mapView;
    GoogleMap map;
    double busLat;
    double busLon;
    String busName;
    String minsLeft;
    String shapeid;
    String colorRoute;
    drawRoute drawRouteTask = null;

    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    double latitude;
    double longitude;

    public static MapResultFragment newInstance() {
        return new MapResultFragment();
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

        fetchParameters();

        mapView = (MapView) view.findViewById(R.id.mapview);

        mapView.onCreate(savedInstanceState);
        mapView.onResume(); //without this, map showed but was empty

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        map.setInfoWindowAdapter(new PopupAdapter(this.getActivity().getLayoutInflater()));

        MapsInitializer.initialize(this.getActivity());

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        map.addMarker(new MarkerOptions()
                        .position(new LatLng(busLat, busLon))
                        .title(busName)
                        .snippet(minsLeft)
                        .draggable(false)
        );

        shapeid = shapeid.replace(" ", "+");
        drawRouteTask = new drawRoute(this.getActivity());
        drawRouteTask.execute(shapeid);

        return view;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Populates parameters with lat/lon information
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            zoomMap();
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
        if (drawRouteTask != null) {
            drawRouteTask.cancel(true);
        }
        drawRouteTask = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    public void fetchParameters() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("Latitude") && args.containsKey("Latitude") && args.containsKey("busName") && args.containsKey("minsLeft")) {
            busLat = args.getDouble("Latitude");
            busLon = args.getDouble("Longitude");
            busName = args.getString("busName");
            minsLeft = args.getString("minsLeft");
            shapeid = args.getString("shape");
            colorRoute = args.getString("color");
        } else {
            busLat = 0;
            busLon = 0;
            busName = "";
            minsLeft = "";
            shapeid = "";
            colorRoute = "";
        }
    }

    public void zoomMap() {
        double centerLat, centerLon;
        int zoomLevel = 16;

        centerLat = (busLat + latitude) / 2;
        centerLon = (busLon + longitude) / 2;

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(centerLat, centerLon), zoomLevel);
        map.animateCamera(cameraUpdate);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class drawRoute extends AsyncTask<String, Void, String> {

        public Activity activity;
        ProgressDialog pd;

        public drawRoute(Activity a) {
            this.activity = a;
        }

        protected void onPreExecute() {
            pd = new ProgressDialog(activity);
            pd.setMessage("Loading route...");
            pd.setProgress(0);
            pd.show();
            super.onPreExecute();
        }

        protected String doInBackground(String... strings) {
            if (!isAdded()) {
                return "";
            }

            // Some long-running task like downloading an image.
            String method = "GetShape";
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getString(R.string.mainAPI_URL) + method + "?shape_id=" + strings[0] + "&key=" + getString(R.string.apiKey));
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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (builder.toString().equals("") || builder.toString() == null)
                Toast.makeText(getActivity(), getString(R.string.internetError), Toast.LENGTH_SHORT).show();
            return builder.toString();
        }

        protected void onPostExecute(String ending) {
            if (!isAdded()) {
                return;
            }

            pd.setProgress(50);

            PolylineOptions busRoute = new PolylineOptions();
            PolylineOptions busRouteOutline = new PolylineOptions();

            try {
                JsonParser jsonParser = new JsonParser();
                JsonArray results = jsonParser.parse(ending).getAsJsonObject().getAsJsonArray("shapes");
                for (JsonElement result : results) {
                    if (result != null) {
                        double pointlat = result.getAsJsonObject().get("shape_pt_lat").getAsDouble();
                        double pointlon = result.getAsJsonObject().get("shape_pt_lon").getAsDouble();
                        busRoute.add(new LatLng(pointlat, pointlon));
                        busRouteOutline.add(new LatLng(pointlat, pointlon));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            busRouteOutline.width(30f).color(Color.BLACK);
            map.addPolyline(busRouteOutline);
            busRoute.width(27f).color(Color.parseColor("#" + colorRoute));
            map.addPolyline(busRoute);

            pd.setProgress(100);
            if (pd != null) {
                pd.dismiss();
            }

            drawRouteTask = null;
        }
    }
}