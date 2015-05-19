package com.rmathur.cumtd.data;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false,
        library = true)
public class DataModule {

    @Provides
    @Singleton
    public Picasso providePicasso(Application app) {
        // By default Picasso uses 1/7 of usable memory
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        return new Picasso.Builder(app)
                .memoryCache(new LruCache((int) (maxMemory / 3)))
                .loggingEnabled(true)
                .build();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder().create();
    }
}
