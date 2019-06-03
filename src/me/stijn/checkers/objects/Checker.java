package me.stijn.checkers.objects;

public class Checker {
	
	private CheckerType type;
	private Checker toStrike;
	
	public Checker(CheckerType type) {
		this.type = type;
	}
	
	public void setType(CheckerType type) {
		this.type = type;
	}
	
	public CheckerType getType() {
		return type;
	}

	
	
	
	
	
	
	public enum CheckerType {
		BLACK,WHITE,BLACKKING,WHITEKING;
	}
}


