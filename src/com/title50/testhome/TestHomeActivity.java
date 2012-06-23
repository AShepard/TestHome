/**
 * Basics of getting Home Screen functionality obtained from "Copyright (C) 2007 The Android Open Source Project"
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

package com.title50.testhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class TestHomeActivity extends Activity {
	
	//key for restoring state of grid
	private static final String KEY_SAVE_GRID_OPENED = "grid.opened";
	
	//Grid showing applications
	private GridView m_app_grid;
	
	private static ArrayList<ApplicationInfo> m_applications;
	
	
	/*
	 * Broadcast receivers for wallpaper and application calls
	 */
	private final BroadcastReceiver m_wallpaper_receiver = new WallpaperIntentReceiver();
    private final BroadcastReceiver m_applications_receiver = new ApplicationsIntentReceiver();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        registerIntentReceivers();
        
        loadApplications(true);
        bindApplications();
        
        showApplications();
        
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the callback for the cached drawables or we leak
        // the previous Home screen on orientation change
        final int count = m_applications.size();
        for (int i = 0; i < count; i++) {
        	m_applications.get(i).icon.setCallback(null);
        }

        unregisterReceiver(m_wallpaper_receiver);
        unregisterReceiver(m_applications_receiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        //below is for storing recent apps launched
        //bindRecents();
    }
    
    //TODO: when are these called?
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        final boolean opened = state.getBoolean(KEY_SAVE_GRID_OPENED, false);
        if (opened) {
            showApplications();
        }
    }

    //TODO: when are these called?
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SAVE_GRID_OPENED, m_app_grid.getVisibility() == View.VISIBLE);
    }
    
    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
        registerReceiver(m_wallpaper_receiver, filter);

        filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(m_applications_receiver, filter);
    }
    
    //display applications
    private void showApplications() {
    	m_app_grid.setVisibility(View.VISIBLE);
    }
    
    /**
     * Google's
     * Receives intents from other applications to change the wallpaper.
     */
    private class WallpaperIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getWindow().setBackgroundDrawable(new ClippedDrawable(getWallpaper()));
        }
    }
    
    
    /**
     * Google's
     * Loads the list of installed applications in mApplications.
     */
    private void loadApplications(boolean isLaunching) {
        if (isLaunching && m_applications != null) {
            return;
        }

        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            if (m_applications == null) {
            	m_applications = new ArrayList<ApplicationInfo>(count);
            }
            m_applications.clear();

            for (int i = 0; i < count; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.setActivity(new ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(manager);

                m_applications.add(application);
            }
        }
    }
    
    /**
     * Creates a new applications adapter for the grid view and registers it.
     */
    private void bindApplications() {
        if (m_app_grid == null) {
        	m_app_grid = (GridView) findViewById(R.id.all_apps);
        }
        m_app_grid.setAdapter(new ApplicationsAdapter(this, m_applications));
        m_app_grid.setSelection(0);

        
    }

    /**
     * Google's
     * Receives notifications when applications are added/removed.
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadApplications(false);
            bindApplications();
        }
    }
    
    /**
     * Google's
     * GridView adapter to show the list of all installed applications.
     */
    private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
        private Rect mOldBounds = new Rect();

        public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ApplicationInfo info = m_applications.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.application, parent, false);
            }

            Drawable icon = info.icon;

            if (!info.filtered) {
                //final Resources resources = getContext().getResources();
                int width = 42;//(int) resources.getDimension(android.R.dimen.app_icon_size);
                int height = 42;//(int) resources.getDimension(android.R.dimen.app_icon_size);

                final int iconWidth = icon.getIntrinsicWidth();
                final int iconHeight = icon.getIntrinsicHeight();

                if (icon instanceof PaintDrawable) {
                    PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                }

                if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
                    final float ratio = (float) iconWidth / iconHeight;

                    if (iconWidth > iconHeight) {
                        height = (int) (width / ratio);
                    } else if (iconHeight > iconWidth) {
                        width = (int) (height * ratio);
                    }

                    final Bitmap.Config c =
                            icon.getOpacity() != PixelFormat.OPAQUE ?
                                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                    final Bitmap thumb = Bitmap.createBitmap(width, height, c);
                    final Canvas canvas = new Canvas(thumb);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
                    // Copy the old bounds to restore them later
                    // If we were to do oldBounds = icon.getBounds(),
                    // the call to setBounds() that follows would
                    // change the same instance and we would lose the
                    // old bounds
                    mOldBounds.set(icon.getBounds());
                    icon.setBounds(0, 0, width, height);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = info.icon = new BitmapDrawable(thumb);
                    info.filtered = true;
                }
            }

            final TextView textView = (TextView) convertView.findViewById(R.id.label);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            textView.setText(info.title);

            return convertView;
        }
    }
}