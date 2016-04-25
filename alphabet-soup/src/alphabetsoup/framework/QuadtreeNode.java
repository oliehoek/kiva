
package alphabetsoup.framework;

import java.util.*;

import org.lwjgl.opengl.GL11;

/**QuadtreeNode is the class that makes up the nodes of the Quadtree and performs recursive functions.
 * @author Chris Hazard
 */
public class QuadtreeNode {

	//using the shape below, with the 4 values representing the order of child nodes
	// y1 \ x1...x2
	// ...  0  1
	// y2   2  3
	private float x1, y1, x2, y2;
	QuadtreeNode children[];
	HashSet<Circle> objects;
	private Circle largestCircle = null;	//largest circle within the node
	
	//used by getShortestDistanceWithoutCollision() to iterate over the the contents of the HashSet objects 
	static Circle staticDistanceArray[] = new Circle[Quadtree.divisionThreshold+1];
	
	/**Constructs a QuadtreeNode
	 * @param parent_node Parent QuadtreeNode, null if head node.
	 * @param x1_pos x coordinate of the top left corner of the bounding area.
	 * @param y1_pos y coordinate of the top left corner of the bounding area..
	 * @param x2_pos x coordinate of the bottom right corner of the bounding area.
	 * @param y2_pos y coordinate of the bottom right corner of the bounding area.
	 */
	public QuadtreeNode(QuadtreeNode parent_node, float x1_pos, float y1_pos, float x2_pos, float y2_pos) {
		x1 = x1_pos;	y1 = y1_pos;	x2 = x2_pos;	y2 = y2_pos;
		children = new QuadtreeNode[4];
		//adjust these initial capacity values if plan on having a larger flux of objects 
		objects = new HashSet<Circle>(Quadtree.divisionThreshold + 4,
				(float)(Quadtree.divisionThreshold+3) / (Quadtree.divisionThreshold+4));
	}
	
	
	/**Returns true if Circle c moving to location x_new, y_new will not collide with another Circle,
	 * false if it will collide. 
	 * @param c Circle to check against for collisions.
	 * @param x_new new x position.
	 * @param y_new new y position.
	 * @return true if the move is valid
	 */
	public boolean isValidMove(Circle c, float x_new, float y_new) {
		//if leaf node, check all the objects contained within
		if(children[0] == null) {
			for(Circle o : objects) {
				//don't detect collision with self
				if(c == o)
					continue;
				//if it's a collision, then return false
				if(o.IsCollision(x_new, y_new, c.getRadius()))
					return false;
			}
			return true;
		}
		
		//use 2*diameter to account for another circle beyond the QuadtreeNode
		float diameter = 4*c.getRadius();
		
		//check to see if it's in any of the four quadrants; be leniant by a diameter in case it's at the edge
		//see if in left half
		if(c.getX() - diameter <= x1 + (x2 - x1)/2) {
			//see if in top half
			if(c.getY() - diameter <= y1 + (y2 - y1)/2)
				if(!children[0].isValidMove(c, x_new, y_new))
					return false;

			//see if in bottom half
			if(c.getY() + diameter >= y1 + (y2 - y1)/2)
				if(!children[2].isValidMove(c, x_new, y_new))
					return false;
		}
		
		//see if in right half
		if(c.getX() + diameter >= x1 + (x2 - x1)/2) {
			//see if in top half
			if(c.getY() - diameter <= y1 + (y2 - y1)/2)
				if(!children[1].isValidMove(c, x_new, y_new))
					return false;

			//see if in bottom half
			if(c.getY() + diameter >= y1 + (y2 - y1)/2)
				if(!children[3].isValidMove(c, x_new, y_new))
					return false;
		}
		return true;	//survived all the collision tests
	}
	
	/**Recursively adds specified Circle object from tree based on position. 
	 * @param c Circle object to remove.
	 */
	public void addCircleObject(Circle c) {
		//if it's bigger, then it's the largest circle
		if(largestCircle == null || c.getRadius() > largestCircle.getRadius())
			largestCircle = c;

		if(children[0] == null) {
			objects.add(c);
		}
		else {
			//see if in left half
			if(c.getX() < x1 + (x2 - x1)/2) {
				//see if in top half
				if(c.getY() < y1 + (y2 - y1)/2)
					children[0].addCircleObject(c);
				else //in bottom half
					children[2].addCircleObject(c);					
			}
			else { //in right half
				//see if in top half
				if(c.getY() < y1 + (y2 - y1)/2)
					children[1].addCircleObject(c);
				else //in bottom half
					children[3].addCircleObject(c);				
			}
		}
	}
	
	/**Recursively removes specified Circle object from tree based on position. 
	 * @param c Circle object to remove.
	 */
	public void removeCircleObject(Circle c) 
	{
		//if leaf node, remove circle, otherwise traverse subnodes
		if(children[0] == null) 
		{
			objects.remove(c);
			
			//get new largest circle
			float largest_size = 0.0f;
			largestCircle = null;
			for(Circle i : objects)
			{
				if(i.getRadius() > largest_size) {
					largestCircle = i;
					largest_size = largestCircle.getRadius();
				}
			}
		}
		else 
		{
			//see if in left half
			if(c.getX() < x1 + (x2 - x1)/2) {
				//see if in top half
				if(c.getY() < y1 + (y2 - y1)/2)
					children[0].removeCircleObject(c);
				else //in bottom half
					children[2].removeCircleObject(c);					
			}
			else { //in right half
				//see if in top half
				if(c.getY() < y1 + (y2 - y1)/2)
					children[1].removeCircleObject(c);
				else //in bottom half
					children[3].removeCircleObject(c);				
			}
			
			//get new largest circle
			float largest_size = 0.0f;
			largestCircle = null;
			for(int i = 0; i < 4; i++)
			{
				if(children[i].largestCircle != null && children[i].largestCircle.getRadius() > largest_size) {
					largestCircle = children[i].largestCircle;
					largest_size = largestCircle.getRadius();
				}
			}
		}
	}
	
	/**Populates in_view with the list of objects within the specified circle.
	 * @param in_view HashSet<Circle> containing objects within distance.
	 * @param c Circle to find objects within
	 */
	public void getObjectsWithinDistance(List<Circle> in_view, Circle c) {
		if(children[0] == null) {
			for(Circle o : objects)
				if(o.IsCollision(c))
					in_view.add(o);
			return;
		}
		
		//find distance to search for another object (but make sure to include
		// a padding to check for the nodes where an object may be overlapping
		// two nodes (but don't pass this distance on to the collision detection itself)
		float dist = c.getRadius();
		if(largestCircle != null)
			dist += largestCircle.getRadius();

		//check to see if it's in any of the four quadrants
		//see if in left half
		if(c.getX() - dist <= x1 + (x2 - x1)/2) {
			//see if in top half
			if(c.getY() - dist <= y1 + (y2 - y1)/2)
				children[0].getObjectsWithinDistance(in_view, c);

			//see if in bottom half
			if(c.getY() + dist >= y1 + (y2 - y1)/2)
				children[2].getObjectsWithinDistance(in_view, c);
		}
		
		//see if in right half
		if(c.getX() + dist >= x1 + (x2 - x1)/2) {
			//see if in top half
			if(c.getY() - dist <= y1 + (y2 - y1)/2)
				children[1].getObjectsWithinDistance(in_view, c);

			//see if in bottom half
			if(c.getY() + dist >= y1 + (y2 - y1)/2)
				children[3].getObjectsWithinDistance(in_view, c);
		}
	}
	
	/**reoptimizeNode recursively optimizes each node, adjusting the number of children and rearanging
	 * the objects in each node.
	 */
	public void reoptimizeNode() {
		//growing the tree
		if(objects.size() >= Quadtree.divisionThreshold) {

			//create child trees
			children[0] = new QuadtreeNode(this, x1, y1, (x1+x2)/2, (y2+y1)/2);
			children[1] = new QuadtreeNode(this, (x1+x2)/2, y1, x2, (y2+y1)/2);
			children[2] = new QuadtreeNode(this, x1, (y2+y1)/2, (x1+x2)/2, y2);
			children[3] = new QuadtreeNode(this, (x1+x2)/2, (y2+y1)/2, x2, y2);

			//move all objects into the corresponding one
			for(Circle c : objects)
				addCircleObject(c);

			objects.clear();
		}
		
		//update all children
		if(children[0] != null)
			for(QuadtreeNode q : children)
				q.reoptimizeNode();

		//if has children, and each child is a leaf node, check to see if the sum of all of the objects
		// within those 4 children is less than the Quadtree.combineThreshold.  if so, take all of the children's
		// objects into this node and detach (thus dealocate) the children. 
		if(children[0] != null
					&& children[0].children[0] == null		&& children[1].children[0] == null
					&& children[2].children[0] == null		&& children[3].children[0] == null
					&& (children[0].objects.size() + children[1].objects.size()
						+ children[2].objects.size() + children[3].objects.size()) < Quadtree.combineThreshold) {
				for(int i = 0; i < 4; i++) {
					//need to clear children[0] first so that addCircleObject
					// will know it has no children.  but, need to grab the objects first
					// before setting children[i] to null.
					HashSet<Circle> circles = children[i].objects; 
					children[i] = null;
					//add all objects from child nodes
					for(Circle c : circles)
						addCircleObject(c);
				}
		}
	}
	
	/**Finds the shortest distance any circle can move before a collision could happen.
	 * @return shortest distance to a collision
	 */
	//TODO make this find the shortest time based on bucketbot velocity
	public float getShortestDistanceWithoutCollision() {
		//if not a leaf node, then get values from child nodes, and find the minimum
		if(children[0] != null) {
			return Math.min(
					Math.min(children[0].getShortestDistanceWithoutCollision(),
							children[1].getShortestDistanceWithoutCollision()),
					Math.min(children[2].getShortestDistanceWithoutCollision(),
							children[3].getShortestDistanceWithoutCollision()) );
		}

		//leaf node, so find shortest distances for each robot
		staticDistanceArray = objects.toArray(staticDistanceArray);
		
		float min_distance = Float.POSITIVE_INFINITY;
		for(int i = 0; i < objects.size() - 1; i++) {
			
			Circle c1 = staticDistanceArray[i];
			//only check moving objects
			if(!c1.isMoving())
				continue;
			
			float min_distance_squared = Float.POSITIVE_INFINITY;

			//find distance to closest other circle object
			for(int j = i+1; j < objects.size(); j++) {
				Circle c2 = staticDistanceArray[j];
				//only check moving objects
				if(!c2.isMoving())
					continue;

				float dist = (c1.getX() - c2.getX()) * (c1.getX() - c2.getX())
										+ (c1.getY() - c2.getY()) * (c1.getY() - c2.getY());
				if(dist < min_distance_squared)
					min_distance_squared = dist;
			}
			min_distance = Math.min(min_distance, (float)Math.sqrt(min_distance_squared) );

			//check distance to sides of collision bucket
			//use 2*diameter to account for another circle beyond the QuadtreeNode
			float diameter = 2*c1.getRadius();
			
			//left side
			min_distance = Math.min(min_distance, c1.getX() - (x1 - diameter) );
			//right side
			min_distance = Math.min(min_distance, (x2 + diameter) - c1.getX());
			//top side
			min_distance = Math.min(min_distance, (y1 + diameter) - c1.getY() );
			//bottom side
			min_distance = Math.min(min_distance, c1.getY() - (y2 - diameter));
			
		}
		
		return min_distance;
	}
	
	/**Renders a rectangle over the are covered by the QuadtreeNode.
	 * Note that glBegin and glEnd must be called before and after this function, as it only
	 * generates vertecies.
	 */
	public void render() {
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glVertex2f(x1, y2);
		
		for(QuadtreeNode q : children)
			if(q != null)
				q.render();
	}

}
