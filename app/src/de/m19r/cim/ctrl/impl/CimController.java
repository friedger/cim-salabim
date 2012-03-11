package de.m19r.cim.ctrl.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import de.m19r.cim.ctrl.CommandType;
import de.m19r.cim.ctrl.ICimController;
import de.m19r.cim.ctrl.ICommand;
import de.m19r.cim.ctrl.ImageCommand;
import de.m19r.cim.ctrl.TextCommand;

public class CimController implements ICimController {

	private List<ICommand> commands = new ArrayList<ICommand>();

	private DrawHelper delegate = new DrawHelper();
	
	private Drawable image = null;
	

	public CimController() {

	}

	public boolean pushCommand(ICommand c) {

		if (c == null) {
			return false;
		}

		if (!c.isValid()) {
			return false;
		}

		boolean status = false;

		if (c.getType().equals(CommandType.TEXT)) {
			commands.add(c);
			status = true;

		} else {
			ImageCommand ic = (ImageCommand)c;
			image = this.LoadImageFromWeb(ic.getImgUrl());
			commands.clear();
			commands.add(ic);
			status = true;
		}

		return status;
	}

	public boolean popCommand() {

		int last = commands.size() - 1;

		if (last >= 0) {
			commands.remove(last);
			if(last == 0) { // the first command in the list is the image...
				image = null;
			}
		}

		return true;
	}

	/**
	 * load the specified image
	 * 
	 * @param ic
	 * @return true, if command could be executed
	 */
	protected boolean doImage(Drawable image, Canvas cv) {
		if (delegate.setImage(image, cv)) {
			return true;
		}
		return false;
	}

	/**
	 * execute the specified text command
	 * 
	 * @param tc
	 * @return true, if command could be executed
	 */
	protected boolean doText(TextCommand tc, Canvas cv) {
		return delegate.setText(tc, cv);
	}

	@Override
	public boolean replay(Canvas canvas) {

		boolean status = true;
		for (ICommand c : commands) {
			if (c.getType().equals(CommandType.TEXT)) {
				status = status & doText((TextCommand) c, canvas);
			} else {
				doImage(image,canvas);
			}
		}

		return status;
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
