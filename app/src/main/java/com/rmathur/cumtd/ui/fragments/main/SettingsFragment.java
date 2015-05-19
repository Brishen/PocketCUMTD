package com.rmathur.cumtd.ui.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.rmathur.cumtd.R;
import com.rmathur.cumtd.ui.services.FloatingService;

import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {

    SharedPreferences sharedPreferences;
    boolean floatingEnabled;
    private Switch mySwitch;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pref, container, false);
        ButterKnife.inject(this, view);

        getActivity().setTitle("Settings");
        hideKeyboard();

        mySwitch = (Switch) view.findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveSettings(true);
                    startFloating();
                } else {
                    saveSettings(false);
                }
            }
        });

        loadSettings();

        if (floatingEnabled) {
            mySwitch.setChecked(true);
        } else {
            mySwitch.setChecked(false);
        }

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
            // nothing
        }
    }

    public void saveSettings(boolean floating) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("floating", floating);
        editor.commit();
    }

    public void loadSettings() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        floatingEnabled = sharedPreferences.getBoolean("floating", false);
    }

    public void startFloating() {
        this.getActivity().startService(new Intent(this.getActivity(), FloatingService.class));
    }
}






