package com.rmathur.cumtd.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.rmathur.cumtd.R;
import com.rmathur.cumtd.data.StopDataSource;
import com.rmathur.cumtd.data.model.DrawerItem;
import com.rmathur.cumtd.ui.adapters.drawer.MainDrawerAdapter;
import com.rmathur.cumtd.ui.fragments.main.AboutFragment;
import com.rmathur.cumtd.ui.fragments.main.FavoritesFragment;
import com.rmathur.cumtd.ui.fragments.main.MapFragment;
import com.rmathur.cumtd.ui.fragments.main.NearbyFragment;
import com.rmathur.cumtd.ui.fragments.main.SearchFragment;
import com.rmathur.cumtd.ui.fragments.main.SettingsFragment;
import com.rmathur.cumtd.ui.fragments.main.TripPlannerFragment;
import com.rmathur.cumtd.ui.services.FloatingService;
import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    public int currentDrawerItem;
    @InjectView(R.id.main_drawer)
    DrawerLayout drawerLayout;
    @InjectView(R.id.main_drawer_list)
    ListView drawerList;
    @InjectView(R.id.main_toolbar)
    Toolbar toolbar;
    SharedPreferences sharedPreferences;
    boolean floatingEnabled;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<DrawerItem> items;
    private StopDataSource dataSource;

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        LeakCanary.install(this.getApplication());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        items = new ArrayList<>();

        items.add(new DrawerItem(R.drawable.favorites, "Favorites"));
        items.add(new DrawerItem(R.drawable.search, "Search"));
        items.add(new DrawerItem(R.drawable.nearby, "Nearby"));
        items.add(new DrawerItem(R.drawable.map, "Map"));
        //items.add(new DrawerItem(R.drawable.planner, "Trip Planner"));
        items.add(new DrawerItem(R.drawable.settings, "Settings"));
        items.add(new DrawerItem(R.drawable.about, "About"));

        MainDrawerAdapter adapter = new MainDrawerAdapter(this, items);
        drawerList.setAdapter(adapter);
        setSupportActionBar(toolbar);

        dataSource = new StopDataSource(this);
        dataSource.open();
        if (dataSource.getAllStops().isEmpty()) {
            // if there are no stops, load search
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, SearchFragment.newInstance())
                    .commit();
            currentDrawerItem = 1;
            setTitle("Search");
        } else {
            // if there are stops, load favorites
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, FavoritesFragment.newInstance())
                    .commit();
            currentDrawerItem = 0;
            setTitle("Favorites");
        }
        dataSource.close();

        taskDescriptionSet();
        loadSettings();
        if (floatingEnabled) {
            startFloating();
        }
    }

    public void startFloating() {
        startService(new Intent(this, FloatingService.class));
    }

    private void taskDescriptionSet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String title = getString(R.string.app_name);
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            int color = getResources().getColor(R.color.black_primary);

            // The reflected method call
            try {
                Class<?> clazz = Class.forName("android.app.ActivityManager$TaskDescription");
                Constructor<?> cons = clazz.getConstructor(String.class, Bitmap.class, int.class);
                Object taskDescription = cons.newInstance(title, icon, color);
                Method method = ((Object) MainActivity.this).getClass().getMethod("setTaskDescription", clazz);
                method.invoke(this, taskDescription);
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
        }
    }

    @OnItemClick(R.id.main_drawer_list)
    void drawerItemClick(int position) {
        DrawerItem item = items.get(position);
        if (position != currentDrawerItem) {
            if (item.getLabel().equals("Favorites")) {
                switchToFragment(FavoritesFragment.newInstance());
            } else if (item.getLabel().equals("Search")) {
                switchToFragment(SearchFragment.newInstance());
            } else if (item.getLabel().equals("Nearby")) {
                switchToFragment(NearbyFragment.newInstance());
            } else if (item.getLabel().equals("Map")) {
                switchToFragment(MapFragment.newInstance());
//            } else if (item.getLabel().equals("Trip Planner")) {
//                switchToFragment(TripPlannerFragment.newInstance());
            } else if (item.getLabel().equals("Settings")) {
                switchToFragment(SettingsFragment.newInstance());
            } else if (item.getLabel().equals("About")) {
                switchToFragment(AboutFragment.newInstance());
            }

            setTitle(item.getLabel());
            currentDrawerItem = position;
        }
        drawerLayout.closeDrawer(Gravity.START);
    }

    private void switchToFragment(Fragment fragment) {
        this.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.main_container, fragment)
                .commit();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Activate the navigation drawer toggle
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadSettings() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        floatingEnabled = sharedPreferences.getBoolean("floating", false);
    }
}
