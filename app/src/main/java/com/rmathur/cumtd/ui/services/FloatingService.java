package com.rmathur.cumtd.ui.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.rmathur.cumtd.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FloatingService extends Service {

    private WindowManager windowManager;
    private List<View> bubbles;
    private LayoutInflater inflater;
    private GestureDetector gestureDetector;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = LayoutInflater.from(this);
        bubbles = new ArrayList<View>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final View bubble = inflater.inflate(R.layout.bubble, null);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;

        bubble.findViewById(R.id.bubble).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (gestureDetector.onTouchEvent(event)) {
                    Log.e("OMG", "clicked yo");
                    return true;
                } else {
                    Log.e("OMG", "dragged yo");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(bubble, params);
                            return true;
                    }
                    return false;
                }
            }
        });

        addBubble(bubble, params);

        return super.onStartCommand(intent, flags, startId);
    }

    public void addBubble(View bubble, LayoutParams params) {
        bubbles.add(bubble);
        windowManager.addView(bubble, params);
    }

    public void removeBubble(View bubble) {
        bubbles.remove(bubble);
        windowManager.removeView(bubble);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (View bubble : bubbles) {
            removeBubble(bubble);
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
}