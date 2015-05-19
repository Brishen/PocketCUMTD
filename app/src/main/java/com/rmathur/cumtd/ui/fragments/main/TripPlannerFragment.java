package com.rmathur.cumtd.ui.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.rmathur.cumtd.R;

import butterknife.ButterKnife;

public class TripPlannerFragment extends Fragment {

    public static TripPlannerFragment newInstance() {
        return new TripPlannerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tripplanner, container, false);
        ButterKnife.inject(this, view);

        getActivity().setTitle("Trip Planner");
        hideKeyboard();

        return view;
    }

    @Override
    public void onResume() {
        hideKeyboard();
        super.onResume();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            Log.e("Error:", e.toString());
        }

    }
}
