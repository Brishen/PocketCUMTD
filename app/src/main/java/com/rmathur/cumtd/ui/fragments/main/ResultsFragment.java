package com.rmathur.cumtd.ui.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rmathur.cumtd.R;
import com.rmathur.cumtd.data.StopDataSource;
import com.rmathur.cumtd.data.model.Departure;
import com.rmathur.cumtd.ui.RepeatedSafeToast;
import com.rmathur.cumtd.ui.activities.MainActivity;
import com.rmathur.cumtd.ui.adapters.list.ResultsAdapter;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ResultsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.results_list)
    ListView resultsList;

    @InjectView(R.id.results_swipe)
    SwipeRefreshLayout swipeLayout;

    @InjectView(R.id.results_progress_bar)
    ProgressBarCircularIndeterminate progressBar;
    String stopId = "";
    String stopName = "";
    private ResultsAdapter adapter;
    private List<Departure> departures = new ArrayList<Departure>();
    private StopDataSource dataSource;
    private Menu menu;
    private Timer myTimer;

    private MyAsyncTask myAsyncTask = null;

    public static ResultsFragment newInstance() {
        return new ResultsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_results, container, false);
        ButterKnife.inject(this, view);

        hideKeyboard();

        swipeLayout.setColorSchemeColors(R.color.black_primary);
        swipeLayout.setOnRefreshListener(this);

        resultsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeLayout.setEnabled(true);
                else
                    swipeLayout.setEnabled(false);
            }
        });

        setHasOptionsMenu(true);
        dataSource = new StopDataSource(getActivity());

        fetchParameters();

        // initially load results
        onRefresh();
        adapter = new ResultsAdapter(this.getActivity());
        resultsList.setAdapter(adapter);

        getActivity().setTitle(stopName);

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRefresh();
            }
        }, 0, 60000);

        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                MapResultFragment newFragment = new MapResultFragment();

                Bundle bundle = new Bundle();
                bundle.putDouble("Latitude", departures.get(position).getLatitude());
                bundle.putDouble("Longitude", departures.get(position).getLongitude());
                bundle.putString("busName", departures.get(position).getBusName());
                bundle.putString("minsLeft", departures.get(position).getMinsLeft());
                bundle.putString("shape", departures.get(position).getShape());
                bundle.putString("color", departures.get(position).getColor());
                newFragment.setArguments(bundle);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                transaction.replace(R.id.main_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }

    private void updateList(List<Departure> departures) {
        adapter.setData(departures);
    }

    @Override
    public void onRefresh() {
        myAsyncTask = new MyAsyncTask(getCurrentActivity());
        myAsyncTask.execute(stopId);
    }

    @Override
    public void onPause() {
        myTimer.cancel();
        myTimer = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRefresh();
            }
        }, 0, 60000);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (myAsyncTask != null) {
            myAsyncTask.cancel(true);
        }
        myTimer.cancel();
        myAsyncTask = null;
        super.onDestroy();
    }

    public Activity getCurrentActivity() {
        return this.getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        dataSource.open();
        if (dataSource.stopExists(stopName)) {
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav_actionbarfilled));
        }
        dataSource.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_favorite) {
            dataSource.open();
            if (dataSource.stopExists(stopName)) {
                dataSource.findStopByName(stopName);
                RepeatedSafeToast.show(this.getActivity(), getString(R.string.removedFavorites));
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav_actionbar));
            } else {
                dataSource.createStop(stopId, stopName);
                RepeatedSafeToast.show(this.getActivity(), getString(R.string.addedFavorites));
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav_actionbarfilled));
            }

            dataSource.close();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void fetchParameters() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("StopId") && args.containsKey("StopName")) {
            stopId = args.getString("StopId");
            stopName = args.getString("StopName");
        } else {
            stopId = "";
            stopName = "";
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {

        public Activity activity;

        public MyAsyncTask(Activity a) {
            this.activity = a;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... strings) {
            if (!isAdded()) {
                return "";
            }

            // Some long-running task like downloading an image.
            String method = "GetDeparturesByStop";
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getString(R.string.mainAPI_URL) + method + "?stop_id=" + strings[0] + "&key=" + getString(R.string.apiKey) + "&pt=60");
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

            departures.clear();

            try {
                JsonParser jsonParser = new JsonParser();
                JsonArray results = jsonParser.parse(ending).getAsJsonObject().getAsJsonArray("departures");
                for (JsonElement result : results) {
                    if (result != null) {
                        String nameOfBus = result.getAsJsonObject().get("headsign").getAsString();
                        String expectedTime = result.getAsJsonObject().get("expected_mins").getAsString();
                        String routeColor = result.getAsJsonObject().get("route").getAsJsonObject().get("route_color").getAsString();
                        String long_name = result.getAsJsonObject().get("trip").getAsJsonObject().get("trip_headsign").getAsString();
                        double latitude = result.getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsDouble();
                        double longitude = result.getAsJsonObject().get("location").getAsJsonObject().get("lon").getAsDouble();
                        String shape = result.getAsJsonObject().get("trip").getAsJsonObject().get("shape_id").getAsString();

                        Departure newBus = new Departure(nameOfBus, long_name, expectedTime, routeColor, latitude, longitude, shape);
                        departures.add(newBus);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            updateList(departures);
            swipeLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            if (departures.isEmpty()) {
                RepeatedSafeToast.show(activity, getString(R.string.noBuses));
            }

            myAsyncTask = null;
        }
    }
}
