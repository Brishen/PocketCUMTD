package com.rmathur.cumtd.data;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false,
        includes = {
                DataModule.class,
        })
public class CUMTDModule {

    private Application app;

    public CUMTDModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }
}
