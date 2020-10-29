/*
 * Util 2011-2011 Jaebong Lee
 * novaever@gmail.com
 * http://jbong.net/
 */

package net.jbong.Util;

import android.graphics.BitmapFactory;
import android.graphics.Rect;

public final class PointAndRect {
	public static Rect getRectWidthAspectRatio(int width, int height, int wRatio, int hRatio) {
		final int rWidth = height * wRatio / hRatio;
		
		if (rWidth <= width)
			return new Rect(0, 0, rWidth, height);
		else
			return new Rect(0, 0, width, width * hRatio / wRatio);
	}
	
	public static Rect getSizeOfBitmap(String pathName) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		
		return (new Rect(0, 0, options.outWidth, options.outHeight));
	}
}