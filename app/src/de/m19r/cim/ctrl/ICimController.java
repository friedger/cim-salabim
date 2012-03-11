package de.m19r.cim.ctrl;

import android.graphics.Canvas;

public interface ICimController {
	
	/**
	 * push a new command. If the specified
	 * command is an image command, then command stack is cleared
	 * 
	 * @param command to be executed
	 * @return true, on success
	 */
	public boolean  pushCommand(ICommand command);
	
	/**
	 * pop the last command from the list of commands
	 * 
	 * @param command
	 * @param c
	 * @return
	 */
	public boolean popCommand();
	
	/**
	 * replay all stored commands on the specified canvas
	 * 
	 * @param canvas to be used
	 * @return
	 */
	public boolean replay(Canvas canvas);
	
	
}
