package de.m19r.cim.ctrl.impl;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import de.m19r.cim.ctrl.CommandType;
import de.m19r.cim.ctrl.ICimController;
import de.m19r.cim.ctrl.ICommand;
import de.m19r.cim.ctrl.ImageCommand;
import de.m19r.cim.ctrl.TextCommand;

public class CimController implements ICimController {

	private List<ICommand> commands = new ArrayList<ICommand>();

	private DrawHelper delegate = new DrawHelper();

	public CimController() {

	}

	public boolean pushCommand(ICommand c, Canvas cv) {

		if (c == null || cv == null) {
			return false;
		}

		boolean status = false;

		if (c.getType().equals(CommandType.TEXT)) {
			status = doText((TextCommand) c, cv);

		} else {
			ImageCommand ic = (ImageCommand) c;
			status = doImage(ic,cv);
		}

		if (status = true) {
			commands.add(c);
		}
		return status;
	}

	/**
	 * clear existing commands and load the specified image
	 * 
	 * @param ic
	 * @return true, if command could be executed
	 */
	protected boolean doImage(ImageCommand ic, Canvas cv) {
		if (delegate.setImage(ic, cv)) {
			commands.clear();
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

}
