package me.stijn.checkers;

public enum Direction {
	LEFTUP(-1,-1), LEFTDOWN(-1,1), RIGHTUP(1,-1), RIGHTDOWN(1,1);
	
	int x,y;
	
	Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
