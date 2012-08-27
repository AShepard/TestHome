package com.title50.testhome;

import android.content.Context;
import android.view.View;
import android.widget.GridView;

/*
 * Each time you create a home screen:
 * 	Need a grid view (this holds the apps/delete box)
 * 	Need a drag controller to drag
 */
public class HomeScreen {
	private int m_content_view_id;
	private GridView m_grid_view;
	private View m_content_view;
	
	public HomeScreen(View home_view, Context context) {
		m_content_view = home_view;
		m_grid_view = (GridView)m_content_view.findViewById(R.id.image_grid_view);
		
		m_grid_view.setAdapter (new ImageCellAdapter(context));
	}
	
	public GridView getGrid() {
		
		return m_grid_view;
	}
	
	public View getContentView() {
		return m_content_view;
	}
}
