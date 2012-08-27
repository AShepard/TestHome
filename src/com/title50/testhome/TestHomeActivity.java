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
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestHomeActivity extends Activity
	implements View.OnLongClickListener, View.OnClickListener,
	View.OnTouchListener
{
	
	//key for restoring state of grid
	private static final String KEY_SAVE_GRID_OPENED = "grid.opened";
	
	//Grid showing applications
	private GridView m_app_grid;
	
	private static ArrayList<ApplicationInfo> m_applications;
	
	//Google
	// Identifiers for option menu items
    private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
    private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
    private static final int MENU_SETTINGS = MENU_SEARCH + 1;
	
	/*
	 * Broadcast receivers for wallpaper and application calls
	 */
	private final BroadcastReceiver m_wallpaper_receiver = new WallpaperIntentReceiver();
    private final BroadcastReceiver m_applications_receiver = new ApplicationsIntentReceiver();
	
    
    /*
     * Variables for Dragging
     * code taken from open source project: http://blahti.wordpress.com/2011/10/03/drag-drop-for-android-gridview/
     */

    private static final int HIDE_TRASHCAN_MENU_ID = Menu.FIRST;
    private static final int SHOW_TRASHCAN_MENU_ID = Menu.FIRST + 1;
    private static final int ADD_OBJECT_MENU_ID = Menu.FIRST + 2;
    private static final int CHANGE_TOUCH_MODE_MENU_ID = Menu.FIRST + 3;

    private DragController mDragController;   // Object that handles a drag-drop sequence. It intersacts with DragSource and DropTarget objects.
    private HomeScreenList m_screen_list;
    private int m_cur_screen;
    
    private DragLayer mDragLayer;             // The ViewGroup within which an object can be dragged.
    private DeleteZone mDeleteZone;           // A drop target that is used to remove objects from the screen.
    private int mImageCount = 0;              // The number of images that have been added to screen.
    private ImageCell mLastNewCell = null;    // The last ImageCell added to the screen when Add Image is clicked.
    private boolean mLongClickStartsDrag = true;   // If true, it takes a long click to start the drag operation.
                                                    // Otherwise, any touch event starts a drag.
    private View m_staged_icon = null;
    private boolean m_is_staging = false;
    
    
    public static final boolean Debugging = false;   // Use this to see extra toast messages.
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.home);
        
        
        createScreens();
        
        m_cur_screen = 0;
        setScreen(0);
       
        /*
         * Set wallpaper
         * TODO find out what else needs to be added when first set (different screens?)
         * should this just be in on create?
         */
        setCurrentWallpaper();
        
        //set default apps
        
        
        /* TODO: Load apps into screens AFTER drag works
         //Might be able to get rid of all of this
        registerIntentReceivers();
        
        loadApplications(true);
        bindApplications();
        bindButtons();
        
        showApplications();
        */
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    /*
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.main, menu);
    	/*
        menu.add(0, MENU_WALLPAPER_SETTINGS, 0, "Set Wallpaper")//R.string.menu_wallpaper)
                 .setIcon(android.R.drawable.ic_menu_gallery)
                 .setAlphabeticShortcut('W');
        menu.add(0, MENU_SEARCH, 0, "Search")//R.string.menu_search)
                .setIcon(android.R.drawable.ic_search_category_default)
                .setAlphabeticShortcut(SearchManager.MENU_KEY);
        menu.add(0, MENU_SETTINGS, 0, "Settings")//R.string.menu_settings)
                .setIcon(android.R.drawable.ic_menu_preferences)
                .setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS));
       */
       // menu.add(0, MENU_SETTINGS, 0, "All Apps")//R.string.menu_settings)
        //.setIcon(android.R.drawable.ic_menu_preferences)
       // .setIntent(new Intent());
        
        //return 
    	//return true;
    //}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_app:
            	//TODO remove View arg
            	onClickAddImage(null);
                break;
            case R.id.menu_wallpaper:
            	toast("TODO: Set Wallpaper");
            	break;
            case R.id.menu_settings:
            	toast("TODO: Settings");
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void changeScreen() {
    	/*
    	GridView grid;
    	if(cur_screen == 0) {
    		grid =
    		cur_screen = 1;
    	} else {
    		grid = 
    		cur_screen = 0; 
    	}
    	*/
    }
    
    /*
     * Create as many home screens as we need
     */
    public void createScreens() {
    	m_screen_list = new HomeScreenList();
    	
    	for(int i=0; i<2; i++) {
    		View new_view = createView();
    		HomeScreen new_screen = new HomeScreen(new_view, this);
    		m_screen_list.addHomeScreen(new_screen);
    	}
    }
    
    public View createView() {
    	
    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DragLayer new_layout = (DragLayer) inflater.inflate(R.layout.home_screen,
                null);

        //View custom = inflater.inflate(R.layout.demo_1, null);
        
        //Button tv = (Button) new_layout.findViewById(R.id.button_add_default);
       // tv.setText("Custom View");
        
        
      //  new_layout.addView(tv);
        

        return new_layout;
    }
    
    public void setCurrentWallpaper() {
    	//This will get users current wallpaper
    	WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
    	Drawable wallpaperDrawable = wallpaperManager.getDrawable();
    	
    	
    	setWallpaper(wallpaperDrawable);
    }
    
    public void setWallpaper(Drawable wallpaper) {
    	//TODO change how to set wallpaper for when multiple screens
    	RelativeLayout main_screen = (RelativeLayout) findViewById(R.id.main_screen);
    	main_screen.setBackgroundDrawable(wallpaper);
    	
    }
    
    public void setScreen(int screen_id) {
    	if(screen_id > m_screen_list.getNumScreens()) {
    		toast("Error: Grid out of range");
    	}
    	
    	//get home screen
    	HomeScreen screen = m_screen_list.getHomeScreen(screen_id);
    	View content_view = screen.getContentView();
    	GridView grid = screen.getGrid();
    	
    	//set content view to home screen
    	setContentView(content_view);
    	
    	//set up dragging ability
    	setUpDragging(grid);
    }
     
    /*
     *  Drag functions below 
     */
    
    public void setUpDragging(GridView gridView) {
    	
    	
        if (gridView == null) toast ("Unable to find GridView");
        
        mDragController = new DragController(this);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mDragLayer.setDragController (mDragController);
        mDragLayer.setGridView (gridView);

        mDragController.setDragListener (mDragLayer);
        // mDragController.addDropTarget (mDragLayer);

        mDeleteZone = (DeleteZone) findViewById (R.id.delete_zone_view);

        // Give the user a little guidance.
        Toast.makeText (getApplicationContext(), 
                        getResources ().getString (R.string.instructions),
                        Toast.LENGTH_LONG).show ();
        
        ApplicationInfo app = getAppInfo(5);
    	
    	if(mDragLayer == null) {
    		//error
    		toast("Error: No drag Layer!");
    	}
    	mDragLayer.setAppAt(app, 4);
        
    }
    
    //TODO load apps into gridview
    
    public void onAddDefaultApps(View v) {
    //TODO change back
    	
    	if(m_cur_screen==0)
    		m_cur_screen++;
    	else {
    		m_cur_screen--;
    	}
    	
    	setScreen(m_cur_screen);
    	/*
    	ApplicationInfo app = getAppInfo(5);
    	
    	if(mDragLayer == null) {
    		//error
    		toast("Error: No drag Layer!");
    	}
    	mDragLayer.setAppAt(app, 4);
    	*/
    }
    /**
     * Will loop through applications available on device
     * 
     * @param resourceId int - the resource id of the image to be added
     * 
     * TODO: need to add image over grid
     */

    public void addNewImageToScreen (ApplicationInfo app)
    {
        if (mLastNewCell != null) mLastNewCell.setVisibility (View.GONE);

       
        
        FrameLayout imageHolder = (FrameLayout) findViewById (R.id.image_source_frame);
        if (imageHolder != null) {
           FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams (LayoutParams.WRAP_CONTENT, 
                                                                       LayoutParams.WRAP_CONTENT, 
                                                                       Gravity.CENTER);
           ImageCell newView = new ImageCell (this);
           newView.setImageDrawable(app.icon);
           newView.setAppInfo(app);
           imageHolder.addView (newView, lp);
           newView.mEmpty = false;
           newView.mCellNumber = -1;
           mLastNewCell = newView;
           mImageCount++;

           // Have this activity listen to touch and click events for the view.
           newView.setOnClickListener(this);
           newView.setOnLongClickListener(this);
           newView.setOnTouchListener (this);
           
           m_is_staging = true;
           m_staged_icon = newView;
           
           GridView staging_area = (GridView) findViewById(R.id.staging_area);
           staging_area.setVisibility(View.VISIBLE);
           
        } else {
        	toast("Error: No image holder");
        }
    }

    /**
     * Add one of the images to the screen so the user has a new image to move around. 
     * See addImageToScreen.
     *
     */    

    public void addNewImageToScreen ()
    {
        int resourceId = R.drawable.hello;
        
        /*
         * Get app to add to screen
         */
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
        int count = -1;
        if (apps != null) {
             count = apps.size();
        } else {
        	toast("Error: No apps to add!");
        	return;
        }
        
        int app_id = mImageCount % count;
        
        ApplicationInfo application = getAppInfo(app_id);
        
        addNewImageToScreen (application);
    }
    
    public ApplicationInfo getAppInfo(int app_id) {
    	
    	PackageManager manager = getPackageManager();
    	
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
    	final List<ResolveInfo> app_list = manager.queryIntentActivities(mainIntent, 0);
    			
        if (app_list == null) {
        	toast("Error: No apps to add!");
        	return null;
        }
        
        ApplicationInfo application = new ApplicationInfo();
        ResolveInfo info = app_list.get(app_id);

        
        String app_name = (String) info.loadLabel(manager);
        
        //if(app_name.compareToIgnoreCase("MoneySaver") == 0) {
        	application.title = info.loadLabel(manager);
        	application.setActivity(new ComponentName(
                     info.activityInfo.applicationInfo.packageName,
                     info.activityInfo.name),
                     Intent.FLAG_ACTIVITY_NEW_TASK
                     | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            application.icon = info.activityInfo.loadIcon(manager);
            
           return application;
    }
    /**
     * Handle a click on a view.
     *
     */    

    public void onClick(View v) 
    {
    	/*
        if (mLongClickStartsDrag) {
           // Tell the user that it takes a long click to start dragging.
           toast ("Press and hold to drag an image.");
        }*/
    	
    	ImageCell app_view = (ImageCell) v;
    	ApplicationInfo app = app_view.getAppInfo();
        startActivity(app.intent);
    }

    /**
     * TODO eventually remove this
     * Handle a click of the Add Image button
     *
     */    

    public void onClickAddImage (View v) 
    {
        addNewImageToScreen ();
    }
    
    /**
     * Handle a click of an item in the grid of cells.
     * 
     */

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
    {
        ImageCell i = (ImageCell) v;
        trace ("onItemClick in view: " + i.mCellNumber);
    }

    /**
     * Handle a long click.
     * If mLongClick only is true, this will be the only way to start a drag operation.
     *
     * @param v View
     * @return boolean - true indicates that the event was handled
     */    

    public boolean onLongClick(View v) 
    {
    	
        if (mLongClickStartsDrag) {
           
            //trace ("onLongClick in view: " + v + " touchMode: " + v.isInTouchMode ());

            // Make sure the drag was started by a long press as opposed to a long click.
            // (Note: I got this from the Workspace object in the Android Launcher code. 
            //  I think it is here to ensure that the device is still in touch mode as we start the drag operation.)
            if (!v.isInTouchMode()) {
               toast ("isInTouchMode returned false. Try touching the view again.");
               return false;
            }
            
            //convert to imagecell and pass
            ImageCell imagecell = (ImageCell) v;
            
            return startDrag (imagecell);
        }

        // If we get here, return false to indicate that we have not taken care of the event.
        return false;
    }


    /**
     * TODO change this description, maybe rethink
     * This is the starting point for a drag operation if mLongClickStartsDrag is false.
     * It looks for the down event that gets generated when a user touches the screen.
     * Only that initiates the drag-drop sequence.
     *
     */    

    public boolean onTouch (View v, MotionEvent ev) 
    {
        // If we are configured to start only on a long click, we are not going to handle any events here.
        if (!m_is_staging) return false;
        
        GridView staging_area = (GridView)findViewById(R.id.staging_area);
        staging_area.setVisibility(View.GONE);
        
        if(m_staged_icon == v) {
        	startDrag((ImageCell)m_staged_icon);
        }
        
        /* TODO: dont need?
        boolean handledHere = false;

        final int action = ev.getAction();

        // In the situation where a long click is not needed to initiate a drag, simply start on the down event.
        if (action == MotionEvent.ACTION_DOWN) {
           //handledHere = startDrag (v);
        }
        */
        return true;
    }   

    /**
     * Start dragging a view.
     */    
    public boolean startDrag (ImageCell imagecell)
    {
        DragSource dragSource = (DragSource) imagecell;

        // We are starting a drag. Let the DragController handle it.
        mDragController.startDrag (imagecell, dragSource, dragSource, DragController.DRAG_ACTION_MOVE);

        return true;
    }

    /**
     * Show a string on the screen via Toast.
     * 
     * @param msg String
     * @return void
     */

    public void toast (String msg)
    {
        Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_SHORT).show ();
    } // end toast

    /**
     * Send a message to the debug log. Also display it using Toast if Debugging is true.
     */

    public void trace (String msg) 
    {
        Log.d ("DragActivity", msg);
        if (!Debugging) return;
        toast (msg);
    }
    
    /*
     * Functions for applications below
     */
    
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
    protected void onPause() {
    	super.onPause();
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

    //called when launching an app
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(KEY_SAVE_GRID_OPENED, m_app_grid.getVisibility() == View.VISIBLE);
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

                
                String app_name = (String) info.loadLabel(manager);
                
                //if(app_name.compareToIgnoreCase("MoneySaver") == 0) {
                	application.title = info.loadLabel(manager);
                	application.setActivity(new ComponentName(
                             info.activityInfo.applicationInfo.packageName,
                             info.activityInfo.name),
                             Intent.FLAG_ACTIVITY_NEW_TASK
                             | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    application.icon = info.activityInfo.loadIcon(manager);
                
                    insertApplicationIcon(application, i);
                   
               // }
                m_applications.add(application);
            }
            updateGrid();
        }
    }
    
    /**
     * Creates a new applications adapter for the grid view and registers it.
     */
    private void bindApplications() {
    	if(m_app_grid == null) {
    		m_app_grid = (GridView)findViewById(R.id.all_apps);
    	}
        m_app_grid.setAdapter(new ApplicationsAdapter(this, m_applications));
        m_app_grid.setSelection(0);
    }
    
    
    /*
     * TODO: insert item into position p in array
     * then reset grid
     */
    private void insertApplicationIcon(ApplicationInfo app_icon, int index) {
    	if(m_applications.contains(app_icon)) {
    		return;
    	}
    	
    	//TODO will need to change this later for when we remove apps
    	int num_elements = m_applications.size();
    	if(index > num_elements) {
    		index = num_elements;
    	}
    	m_applications.add(index, app_icon);
    	
    	
    }
    
    /*
     * TODO - change to select grid
     * Update grid
     */
    private void updateGrid() {
    	if (m_app_grid == null) {
        	m_app_grid = (GridView) findViewById(R.id.all_apps);
        }
    	m_app_grid.setAdapter(new ApplicationsAdapter(this, m_applications));
    }
    
    /**
     * Binds actions to the various buttons.
     */
    private void bindButtons() {
    	m_app_grid.setOnItemClickListener(new ApplicationLauncher());
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
    
    /**
     * Google's
     * Starts the selected activity/application in the grid view.
     */
    private class ApplicationLauncher implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
            startActivity(app.intent);
        }
    }
    
    
   
}