import java.io.Serializable;

/**
 * File: Point.java
 * 
 * Maintains Points of a 2D coordinate space
 * @author Richa Singh
 *
 */
public class Point implements Serializable{

	private double x;
	private double y;

	public Point( double x, double y){
		this.x = x;
		this.y = y;

	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

}
