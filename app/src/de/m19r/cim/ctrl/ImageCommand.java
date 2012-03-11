package de.m19r.cim.ctrl;

public class ImageCommand implements ICommand {

	String imgUrl;

	
	
	public ImageCommand(String imgUrl) {
		super();
		this.imgUrl = imgUrl;
	}



	@Override
	public CommandType getType() {
		return CommandType.IMAGE;
	}



	public String getImgUrl() {
		return imgUrl;
	}



	@Override
	public boolean isValid() {
		return (imgUrl != null);
	}
	
	
	
	
}
