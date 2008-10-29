/**
 * 
 */
package alphabetsoup.framework;

/**Class Circle implements a circle in 2D space and functions to determine its
 * relation to other geometric shapes (such as collisions with rectangles and circles) 
 * @author Chris Hazard
 */
public class Circle {
	
	private float x = Float.NaN, y = Float.NaN;	//start out at a non-existant location
	float radius;
	boolean isMoving;	//set to true if the circle is currently moving (to check for collisions) 
	
	/**Constructs a circle with the given radius at location 0,0.
	 * @param circle_radius Radius of the circle
	 */
	public Circle(float circle_radius) 
	{
		radius = circle_radius;
		isMoving = false;
	}
	
	/**Constructs a circle with the given radius and position
	 * @param circle_radius Radius of the circle
	 * @param x_pos x position
	 * @param y_pos y position
	 */
	public Circle(float circle_radius, float x_pos, float y_pos) 
	{
		radius = circle_radius; x = x_pos; y = y_pos;
		isMoving = false;
	}
	
	/**Sets the initial position of the Circle.  Once the initial position has been set,
	 * it may not be set again with this function.  
	 * @param initial_x x position
	 * @param initial_y y position
	 */
	public void setInitialPosition(float initial_x, float initial_y) {
		if(Float.isNaN(x)) x = initial_x;
		if(Float.isNaN(y)) y = initial_y;
	}

	/**Returns true if the circle overlaps with the circle provided in the parameter, false otherwise.
	 * Returns false if a circle is passed itself (since it can't collide with itself).
	 * @param c Circle to test overlap
	 * @return Returns the radius.
	 */
	public boolean IsCollision(Circle c) 
	{
		return this != c && IsCollision(c.x, c.y, c.radius);
	};
	
	/**Returns true if the circle overlaps with the circle provided in the parameter.
	 * @param other_x x position of other circle
	 * @param other_y y position of other circle
	 * @param other_radius radius of other circle
	 * @return Returns the radius.
	 */
	public boolean IsCollision(float other_x, float other_y, float other_radius) 
	{
		return (x - other_x) * (x - other_x) + (y - other_y) * (y - other_y) <= (radius + other_radius) * (radius + other_radius);
	}
	
	/**Returns the distance from the circle to the position specified
	 * @param other_x x position
	 * @param other_y y position
	 * @return Returns the distance.
	 */
	public float getDistance(float other_x, float other_y) {
		return (float)Math.sqrt( (x - other_x) * (x - other_x)
		+ (y - other_y) * (y - other_y) );
	}
	
	/**Returns the distance from the circle to the other specified Circle
	 * @param c Circle to compute distance to
	 * @return Returns the distance.
	 */
	public float getDistance(Circle c) {
		return getDistance(c.getX(), c.getY());
	}
	
	/**Returns true if the circle overlaps with the rectangle specified by (x1,y1) to (x2,y2). 
	 * @param x1 x position of the top left corner of the rectangle
	 * @param y1 y position of the top left corner of the rectangle
	 * @param x2 x position of the bottom right corner of the rectangle
	 * @param y2 y position of the bottom right corner of the rectangle
	 * @return true if circle overlaps the rectangle
	 */
	public boolean IsCollision(float x1, float y1, float x2, float y2) {
		//translate such that the circle is at (0,0)
		x1 -= x;	
		x2 -= x;	
		y1 -= y;	
		y2 -= y;
		
		if(x2 < 0.0f) { //rectangle to the left of center of circle
			if(y2 < 0.0)
				return (x2*x2 + y2*y2) < radius*radius;
			else if(y1 > 0.0)
				return (x2*x2 + y1*y1) < radius*radius;
			else
				return Math.abs(x2) < radius;
		}
		if(x1 > 0.0f) { //rectangle to the right of center of circle
			if(y2 < 0.0)
				return (x1*x1 + y2*y2) < radius*radius;
			else if(y1 > 0.0)
				return (x1*x1 + y1*y1) < radius*radius;
			else
				return Math.abs(x1) < radius;
		}
		else { //rectangle covers circle center on x axis
			if(y2 < 0.0)
				return Math.abs(y2) < radius;
			else if(y1 > 0.0)
				return Math.abs(y1) < radius;
			else
				return true;
		}
	}

	/**
	 * @return Returns the radius.
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * @param radius The radius to set.
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * @return Returns the x.
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param x The x to set.
	 */
	void setX(float x) {
		this.x = x;
	}

	/**
	 * @return Returns the y.
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y The y to set.
	 */
	void setY(float y) {
		this.y = y;
	}

	/**
	 * @return Returns the isMoving.
	 */
	public boolean isMoving() {
		return isMoving;
	}

	/**
	 * @param isMoving The isMoving to set.
	 */
	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

}
