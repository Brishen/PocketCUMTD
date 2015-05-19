package com.rmathur.cumtd;

import android.app.Application;

import com.rmathur.cumtd.data.CUMTDModule;
import com.rmathur.cumtd.data.DataModule;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public class CUMTDApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return Arrays.asList(
                new DataModule(),
                new CUMTDModule(this));
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }
}
