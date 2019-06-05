package me.stijn.checkers.objects;

import java.io.Serializable;

public class Checker implements Serializable {
	
	private CheckerType type;
	
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


