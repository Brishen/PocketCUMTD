package com.rmathur.cumtd.ui.fragments.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NearbyFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @InjectView(R.id.lstNearbyStops)
    ListView lstStopList;

    double latitude;
    double longitude;
    ArrayList<String> results = new ArrayList<String>();

    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    private NearbyStops myAsyncTask = null;

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.inject(this, view);
        lstStopList = (ListView) view.findViewById(R.id.lstNearbyStops);
        getActivity().setTitle("Nearby");

        hideKeyboard();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myAsyncTask != null) {
            myAsyncTask.cancel(true);
        }
        myAsyncTask = null;
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Populates parameters with lat/lon information
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            myAsyncTask = new NearbyStops(latitude, longitude, this.getActivity());
            myAsyncTask.execute();
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

    private class NearbyStops extends AsyncTask<String, Void, String> {

        ProgressDialog pd;
        Activity a;
        private double asynclat;
        private double asynclon;

        public NearbyStops(double lat, double lon, Activity a) {
            this.asynclat = lat;
            this.asynclon = lon;
            this.a = a;
        }

        protected void onPreExecute() {
            pd = new ProgressDialog(a);
            pd.setMessage("Loading nearby stops...");
            pd.setProgress(0);
            pd.show();
            super.onPreExecute();
        }

        protected String doInBackground(String... strings) {
            if (!isAdded()) {
                return "";
            }

            // Some long-running task like downloading an image.
            String method = "GetStopsByLatLon";
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getString(R.string.mainAPI_URL) + method + "?lat=" + asynclat + "&lon=" + asynclon + "&key=" + getString(R.string.apiKey));
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
            return builder.toString();
        }

        protected void onPostExecute(String ending) {
            if (!isAdded()) {
                return;
            }

            pd.setProgress(50);

            try {
                JsonParser jsonParser = new JsonParser();
                JsonArray newresults = jsonParser.parse(ending).getAsJsonObject().getAsJsonArray("stops");
                HashMap<String, String> wipStopList = new HashMap<String, String>();

                for (JsonElement result : newresults) {
                    String nameOfStop = result.getAsJsonObject().get("stop_name").getAsString();
                    String idOfStop = result.getAsJsonObject().get("stop_id").getAsString();

                    wipStopList.put(nameOfStop, idOfStop);
                    results.add(nameOfStop);
                }

                final HashMap<String, String> stopList = wipStopList;

                if (results.isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.noStopsNearby), Toast.LENGTH_SHORT).show();
                }

                if (!results.isEmpty() || results != null) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, results) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view.findViewById(android.R.id.text1);
                            textView.setTextColor(Color.BLACK);
                            return view;
                        }
                    };
                    lstStopList.setAdapter(arrayAdapter);
                }
                pd.setProgress(100);

                lstStopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    // argument position gives the index of item which is clicked
                    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                        String selectedStop = stopList.get(results.get(position));

                        ResultsFragment newFragment = new ResultsFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString("StopId", selectedStop);
                        bundle.putString("StopName", results.get(position));
                        newFragment.setArguments(bundle);

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                        transaction.replace(R.id.main_container, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (pd != null) {
                pd.dismiss();
            }
            myAsyncTask = null;
        }
    }
}
