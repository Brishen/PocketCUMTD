package com.rmathur.cumtd.ui.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rmathur.cumtd.R;
import com.rmathur.cumtd.data.StopDataSource;
import com.rmathur.cumtd.data.model.Stop;
import com.rmathur.cumtd.ui.adapters.list.FavoritesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FavoritesFragment extends Fragment {

    @InjectView(R.id.lstFavoriteStops)
    ListView lstStopList;

    private StopDataSource dataSource;
    private FavoritesAdapter mainAdapter;

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.inject(this, view);

        lstStopList = (ListView) view.findViewById(R.id.lstFavoriteStops);
        getActivity().setTitle("Favorites");
        hideKeyboard();

        fetchFavorites();

        return view;
    }

    @Override
    public void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
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

    public void fetchFavorites() {
        dataSource = new StopDataSource(getActivity());
        dataSource.open();

        ArrayList<Stop> values = dataSource.getAllStops();

        mainAdapter = new FavoritesAdapter(values, this.getActivity().getApplicationContext());
        lstStopList.setAdapter(mainAdapter);

        final List<Stop> finalValues = values;

        lstStopList.setItemsCanFocus(false);
        lstStopList.setDividerHeight(40);

        lstStopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Stop selectedStop = finalValues.get(position);

                ResultsFragment newFragment = new ResultsFragment();

                Bundle bundle = new Bundle();
                bundle.putString("StopId", selectedStop.getStopId());
                bundle.putString("StopName", selectedStop.getStopName());
                newFragment.setArguments(bundle);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                transaction.replace(R.id.main_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}
