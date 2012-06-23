package com.title50.testhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

/**
 * Google's
 * 
 * When a drawable is attached to a View, the View gives the Drawable its dimensions
 * by calling Drawable.setBounds(). In this application, the View that draws the
 * wallpaper has the same size as the screen. However, the wallpaper might be larger
 * that the screen which means it will be automatically stretched. Because stretching
 * a bitmap while drawing it is very expensive, we use a ClippedDrawable instead.
 * This drawable simply draws another wallpaper but makes sure it is not stretched
 * by always giving it its intrinsic dimensions. If the wallpaper is larger than the
 * screen, it will simply get clipped but it won't impact performance.
 */

public class ClippedDrawable extends Drawable {
	private final Drawable mWallpaper;

    public ClippedDrawable(Drawable wallpaper) {
        mWallpaper = wallpaper;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        // Ensure the wallpaper is as large as it really is, to avoid stretching it
        // at drawing time
        mWallpaper.setBounds(left, top, left + mWallpaper.getIntrinsicWidth(),
                top + mWallpaper.getIntrinsicHeight());
    }

    public void draw(Canvas canvas) {
        mWallpaper.draw(canvas);
    }

    public void setAlpha(int alpha) {
        mWallpaper.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        mWallpaper.setColorFilter(cf);
    }

    public int getOpacity() {
        return mWallpaper.getOpacity();
    }
}
