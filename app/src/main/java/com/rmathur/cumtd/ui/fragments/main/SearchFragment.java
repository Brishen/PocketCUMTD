package com.rmathur.cumtd.ui.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rmathur.cumtd.R;
import com.rmathur.cumtd.ui.RepeatedSafeToast;
import com.rmathur.cumtd.ui.activities.MainActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchFragment extends Fragment {

    ArrayList<String> results = new ArrayList<String>();

    @InjectView(R.id.edtSearchStops)
    EditText edtSearchStops;

    @InjectView(R.id.lstStops)
    ListView lstStopList;

    @InjectView(R.id.delete)
    ImageButton delete;

    private MyAsyncTask myAsyncTask = null;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);
        getActivity().setTitle("Search");

        edtSearchStops = (EditText) view.findViewById(R.id.edtSearchStops);
        lstStopList = (ListView) view.findViewById(R.id.lstStops);
        delete = (ImageButton) view.findViewById(R.id.delete);

        Drawable d = getResources().getDrawable(android.R.drawable.ic_delete);
        ImageView image = (ImageView) view.findViewById(R.id.delete);
        image.setImageDrawable(d);
        delete.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (edtSearchStops.length() > 0) {
                    edtSearchStops.getText().clear();
                }
            }
        });

        showKeyboard();

        edtSearchStops.requestFocus();
        edtSearchStops.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String userText = edtSearchStops.getText().toString();
                    if (userText.equals("isr")) {
                        userText = "Illinois Street Residence Hall";
                    }
                    userText = userText.replace(" ", "+").toLowerCase();
                    if (!userText.equals("")) {
                        myAsyncTask = new MyAsyncTask(getCurrentActivity());
                        myAsyncTask.execute(userText);
                    } else {
                        results.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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

    @Override
    public void onResume() {
        edtSearchStops.requestFocus();
        showKeyboard();
        super.onResume();
    }

    public void showKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearchStops, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    public Activity getCurrentActivity() {
        return this.getActivity();
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
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getString(R.string.autoAPI_URL) + strings[0]);
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
            return builder.toString();
        }

        protected void onPostExecute(String result) {
            if (!isAdded()) {
                return;
            }

            results.clear();

            try {
                JSONArray jsonArray = new JSONArray(result);
                HashMap<String, String> wipStopList = new HashMap<String, String>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    String stop_id = jsonArray.getJSONObject(i).getString("i");
                    String name = jsonArray.getJSONObject(i).getString("n");
                    wipStopList.put(name, stop_id);
                    results.add(name);
                }

                final HashMap<String, String> stopList = wipStopList;

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

            myAsyncTask = null;
        }
    }
}