package de.m19r.cim.ctrl;

public class TextCommand implements ICommand {

	String value;

	int x;

	int y;

	public TextCommand(String value, int x, int y) {
		super();
		this.value = value;
		this.x = x;
		this.y = y;
	}

	public String getValue() {
		return value;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public CommandType getType() {
		return CommandType.TEXT;
	}

	@Override
	public boolean isValid() {

		if (this.value == null | this.x < 0 | this.y < 0) {
			return false;
		}

		return true;
	}

}
