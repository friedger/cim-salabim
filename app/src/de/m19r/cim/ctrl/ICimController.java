package de.m19r.cim.ctrl;

import android.graphics.Canvas;

public interface ICimController {
	
	/**
	 * push a new command to be executed immediatly. If the specified
	 * command is an image command, then command stack is cleared
	 * and the canvas is initialized with the specified image
	 * 
	 * @param command to be executed
	 * @return true, on success
	 */
	public boolean  pushCommand(ICommand command,Canvas c);
	
	
}
