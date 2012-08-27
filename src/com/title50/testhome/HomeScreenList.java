package com.title50.testhome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.widget.GridView;

public class HomeScreenList {
	private List<HomeScreen> m_screen_list;
	
	public HomeScreenList() {
		m_screen_list = new ArrayList<HomeScreen>();
			
	}
	
	public int getNumScreens() {
		return m_screen_list.size();
	}
	
	public void addHomeScreen(HomeScreen new_screen) {
		m_screen_list.add(new_screen);
	}
	
	public HomeScreen getHomeScreen(int index) {
		if(index >= m_screen_list.size()) {
			//error
			return null;
		}
		
		return m_screen_list.get(index);
	}
	
	public GridView getGrid(int index) {
		HomeScreen screen = getHomeScreen(index);
		
		return screen.getGrid();
	}
}
