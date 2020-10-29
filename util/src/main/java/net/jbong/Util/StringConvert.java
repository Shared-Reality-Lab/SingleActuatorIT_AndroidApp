/*
 * Util 2011-2011 Jaebong Lee
 * novaever@gmail.com
 * http://jbong.net/
 */

package net.jbong.Util;

import android.graphics.Bitmap;

public final class StringConvert {
	public static String msToString(int ms) {
		final int sec = ms / 1000;
		final int min = sec / 60;
		final int hour = min / 60;

		return String.format("%01d:%02d:%02d.%03d", hour, min % 60, sec % 60, ms % 1000);
	}
	
	public static String getFileNameFromPath(String str) {
		return str.substring(str.lastIndexOf('/') + 1);
	}
	
	/* - How To Use (Example) -
	 * Log.i("SoleMovieStudio", "rb: " +
	 *         StringConvert.bitmapPixelString(BitmapFactory.decodeResource(getResources(),
	 *         R.drawable.element_border_normal_rb)));
	 */
	public static String bitmapPixelString(Bitmap bitmap) {
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		String str = "";

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++)
				str += String.format("0x%08x, ", bitmap.getPixel(j, i));
		}
		
		return "(" + width + "x" + height + ") " + str;
	}
}
