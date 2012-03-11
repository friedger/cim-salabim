package de.m19r.cim.ctrl;

public interface ICommand {

	/**
	 * IMAGE or TEXT
	 * @return
	 */
	public CommandType getType();
	
	/**
	 * test for safety
	 * 
	 * @return true, if valid
	 */
	public boolean isValid();
}
