package de.m19r.cim.ctrl.impl;

import java.io.InputStream;
import java.net.URL;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import de.m19r.cim.ctrl.TextCommand;

public class DrawHelper {

	private Paint paint;

	public DrawHelper() {
		// set paint values for text...
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTextSize(15f);
		paint.setTypeface(Typeface.MONOSPACE);
	}

	public boolean setImage(Drawable drw, Canvas c) {
				
		if(drw == null) {
			return false;
		}
		c.drawColor(Color.BLACK);
		// draw on canvas
		drw.draw(c);

		return true;

	}

	public boolean setText(TextCommand tc, Canvas c) {
		if (!tc.isValid()) {
			return false;
		}
		c.drawText(tc.getValue(), tc.getX(), tc.getY(), paint);
		return true;

	}
		
	
	 protected Drawable LoadImageFromWeb(String url)
	   {
	  try
	  {
	   InputStream is = (InputStream) new URL(url).getContent();
	   Drawable d = Drawable.createFromStream(is, "src name");
	   return d;
	  }catch (Exception e) {
	   System.out.println("Exc="+e);
	   return null;
	  }
	 }

}
