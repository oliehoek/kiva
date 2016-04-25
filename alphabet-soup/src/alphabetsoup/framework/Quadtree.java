/**
 * 
 */
package alphabetsoup.framework;

import java.util.*;

import org.lwjgl.opengl.GL11;

/**Quadtree mantains a tree where each node quadrasects the area of its parent,
 * used for fast lookup of local interactions between objects.
 * @author Chris Hazard
 */
public class Quadtree {

	/**Head node of the Quadtree
	 */
	private QuadtreeNode topnode;
	
	/**When at least divisionThreshold objects are in the same QuadtreeNode,
	 *  the QuadtreeNode is split into 4.
	 */
	public final static int divisionThreshold = 12;
	/**When fewer than combineThreshold objects are in the four child nodes of a QuadtreeNode,
	 *  the 4 QuadtreeNodes are recombined.
	 */
	public final static int combineThreshold = 8;
	
	
	/**Constructs a Quadtree with the dimensions specified.
	 * @param width Width of the Quadtree area
	 * @param height Height of the Quadtree area
	 */
	public Quadtree(float width, float height) {
		topnode = new QuadtreeNode(null, 0, 0, width, height);
	}
	
	/**Adds a circle object to the robotQuadtree
	 * @param c circle object to add
	 */
	public void addCircleObject(Circle c) {
		topnode.addCircleObject(c);
		topnode.reoptimizeNode();
	}
	
	/**Removes a circle object from the robotQuadtree
	 * @param c circle object to remove
	 */
	public void removeCircleObject(Circle c) {
		topnode.removeCircleObject(c);
		topnode.reoptimizeNode();
	}
	
	/**Returns the shortest distance until bucketbots could potentially collide
	 * @return shortest distance before potential collision
	 */
	public float getShortestDistanceWithoutCollision() {
		return topnode.getShortestDistanceWithoutCollision();
	}
	
	/**Returns true if Circle c moving to location x_new, y_new will not collide with another Circle,
	 * false if it will collide.
	 * @param c Circle to check against for collisions.
	 * @param x_new new x position.
	 * @param y_new new y position.
	 * @return true if the move is valid
	 */
	public boolean isValidMove(Circle c, float x_new, float y_new) {
		return topnode.isValidMove(c, x_new, y_new);
	}
	
	/**Moves Circle c from its current location to the position specified by x_new, y_new 
	 * @param c Circle to move
	 * @param x_new new x position 
	 * @param y_new new y position
	 */
	public void moveTo(Circle c, float x_new, float y_new) {
		topnode.removeCircleObject(c);
		c.setX(x_new);	c.setY(y_new);
		topnode.addCircleObject(c);
	}
	
	/**Updates the Quadtree such that it stays the optimal size
	 */
	public void updateTree() {
		topnode.reoptimizeNode();
	}
	
	/**Returns a list of objects within the specified Circle.
	 * @param c Circle to find objects within
	 * @return HashSet<Circle> containing all objects within the specified bounds
	 */
	public List<Circle> getObjectsWithinCircle(Circle c) {
		List<Circle> in_view = new ArrayList<Circle>();
		topnode.getObjectsWithinDistance(in_view, c);
		return in_view;
	}

	/**Recursively renders the quadtree.
	 * Note that glBegin and glEnd must be called before and after this function, as it only
	 * generates vertecies.
	 */
	public void render() {
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
		GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
		GL11.glBegin(GL11.GL_QUADS);
		topnode.render();
		GL11.glEnd();
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
	}
	
}
