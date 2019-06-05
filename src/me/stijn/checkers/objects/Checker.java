package me.stijn.checkers.objects;

import java.io.Serializable;

import me.stijn.checkers.objects.Checker.CheckerType;

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
	
	public boolean isKing() {
		return (type == CheckerType.BLACKKING ||type == CheckerType.WHITEKING);
	}
	
	
	
	
	public enum CheckerType {
		BLACK,WHITE,BLACKKING,WHITEKING;
	}
}


