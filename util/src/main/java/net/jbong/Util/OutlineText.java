/*
 * Util 2011-2011 Jaebong Lee
 * novaever@gmail.com
 * http://jbong.net/
 */

package net.jbong.Util;

import android.graphics.Canvas;
import android.graphics.Paint;

public class OutlineText {
	private String mText;
	
	private int mWidth = 0;
	private int mHeight = 0;
	
	private Paint mPaintFill;
	private Paint mPaintOutline;
	
	public OutlineText(int textSize, int fillColor, int outlineColor, int stokeWidth, String text) {
		mPaintFill = new Paint();
		mPaintFill.setAntiAlias(true);
		mPaintFill.setStyle(Paint.Style.FILL);
		mPaintFill.setTextSize(textSize);
		mPaintFill.setColor(fillColor);

		mPaintOutline = new Paint();
		mPaintOutline.setAntiAlias(true);
		mPaintOutline.setStyle(Paint.Style.STROKE);
		mPaintOutline.setStrokeWidth(stokeWidth);
		mPaintOutline.setTextSize(textSize);
		mPaintOutline.setColor(outlineColor);
		
		setText(text);
	}
	
	public void draw(Canvas canvas, int x, int y) {
		final int padding = (int)mPaintFill.getStrokeWidth();
		
		final int dy = ((int)-mPaintFill.ascent()) + padding;
		
		canvas.drawText(mText, x + padding, y + dy, mPaintOutline);
		canvas.drawText(mText, x + padding, y + dy, mPaintFill);
	}
	
	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}
	
	public String getText() {
		return mText;
	}
	
	public void setText(CharSequence text) {
		if (text == null)
			text = "";
		
		setText(text.toString());
	}

	public void setText(String text) {
		if (text == null)
			mText = "";
		else
			mText = text;

		updateWidthAndHeight();
	}
	
	public void setTextSize(int textSize) {
		mPaintFill.setTextSize(textSize);
		mPaintOutline.setTextSize(textSize);
		
		updateWidthAndHeight();
	}
	
	public void setTextColor(int fillColor, int outlineColor) {
		mPaintFill.setColor(fillColor);
		mPaintOutline.setColor(outlineColor);
	}
	
	private void updateWidthAndHeight() {
		final int textLength = mText.length();
		
		final int padding = (int)mPaintFill.getStrokeWidth();
		
		final float[] widths = new float[textLength];
		mPaintFill.getTextWidths(mText, widths);
		
		mWidth = 0;
		for (int i = 0; i < textLength; i++)
			mWidth += widths[i];
		mWidth += padding * 2;
		mHeight = (int)(mPaintFill.descent() - mPaintFill.ascent()) + padding * 2;
	}
}