/**
 * 
 */
package alphabetsoup.framework;

import java.util.*;

import alphabetsoup.base.BucketbotBase;

/**Map manages the spatial location of all of the buckets, bucketbots, and stations.
 * It moves bucketbots and buckets upon request, as long as they will not collide with 
 * another bucketbot or bucket.  It also provides queries relating to if objects will collide.
 * @author Chris Hazard
 */
public class Map implements Updateable {
	
	private float width, height;
	private float tolerance;		//distance between a bucket and a station, etc. where it will still be considered "close enough"
	private float maxAcceleration;	//maximum acceleration of anything on the map
	private float maxVelocity;		//maximum velocity of anything on the map
	
	List<Bucketbot> bucketbots;
	List<Bucket> buckets;
	List<LetterStation> letterStations;
	List<WordStation> wordStations;
	
	Quadtree bucketbotQuadtree;
	Quadtree bucketQuadtree;

	/**Constructs a map with given dimensions and tolerance
	 * @param map_width width of the map
	 * @param map_height height of the map
	 * @param map_tolerance tolerance for Buckets to be picked up by Bucketbots
	 * @param max_acceleration maximum acceleration of anything on the map
	 * @param max_velocity maximum velocity of anything on the map
	 */
	public Map(float map_width, float map_height, float map_tolerance, float max_acceleration, float max_velocity) 
	{
		width = map_width;	height = map_height;	tolerance = map_tolerance;
		maxAcceleration = max_acceleration;
		maxVelocity = max_velocity;
		bucketbots = new ArrayList<Bucketbot>();
		buckets = new ArrayList<Bucket>();
		letterStations = new ArrayList<LetterStation>();
		wordStations = new ArrayList<WordStation>();
		
		bucketbotQuadtree = new Quadtree(map_width, map_height);
		bucketQuadtree = new Quadtree(map_width, map_height);
	}

	/**Adds a Bucketbot to the map and gives the Bucketbot a reference to the Map.
	 * @param r		Bucketbot object to add
	 * @return		Returns true if the Bucketbot can be and has been placed, false if it cannot be placed
	 */
	public boolean addRobot(Bucketbot r) 
	{	
		if(!isBucketbotMoveValid(r, r.getX(), r.getY())) return false;
		bucketbots.add(r);
		bucketbotQuadtree.addCircleObject((Circle)r);
		return true;
	}
	
	/**Adds a Bucket to the map, and gives the Bucket a reference to the Map.
	 * @param b		Bucket object to add
	 * @return		Returns true if the Bucket can be and has been placed, false if it cannot be placed
	 */
	public boolean addBucket(Bucket b) 
	{
		if(!isValidBucketStorageLocation(b, b.getX(), b.getY())) return false;
		buckets.add(b);
		bucketQuadtree.addCircleObject((Circle)b);
		return true;
	}
	
	/**Adds a LetterStation to the map.
	 * @param s		LetterStation object to add
	 */
	public void addLetterStation(LetterStation s) {
		letterStations.add(s);
	}
	
	/**Adds a WordStation to the map.
	 * @param s		WordStation object to add
	 */
	public void addWordStation(WordStation s) {
		wordStations.add(s);
	}
	
	/**Returns a Collection of the bucketbots within the specified distance from the point
	 * @param x x coordinate of center to find bucketbots
	 * @param y y coordinate of center to find bucketbots
	 * @param distance from (x,y)
	 * @return Collection<Circle> of bucketbots within the specified distance from (x,y)
	 */
	public Collection<Circle> getBucketbotsWithinDistance(float x, float y, float distance) {
		Circle c = new Circle(distance, x, y);
		return bucketbotQuadtree.getObjectsWithinCircle(c);
	}
	
	/**Returns a Collection of the buckets within the specified distance from the point
	 * @param x x coordinate of center to find buckets
	 * @param y y coordinate of center to find buckets
	 * @param distance from (x,y)
	 * @return Collection<Circle> of buckets within the specified distance from (x,y)
	 */
	public Collection<Circle> getBucketsWithinDistance(float x, float y, float distance) {
		Circle c = new Circle(distance, x, y);
		return bucketQuadtree.getObjectsWithinCircle(c);
	}
	
	/**Returns true if moving the bucketbot to the new location is valid.
	 * It checks map boundaries, whether there are any robots in the new location,
	 * or, if the bucketbot is carrying a bucket, it will make sure there is no bucket in that location.
	 * @param r bucketbot to move
	 * @param x_new new x position
	 * @param y_new new y position
	 * @return true if the move is valid
	 */
	public boolean isBucketbotMoveValid(Bucketbot r, float x_new, float y_new) {
		if(x_new - r.getRadius() < 0 || x_new + r.getRadius() > getWidth()
				|| y_new - r.getRadius() < 0 || y_new + r.getRadius() > getHeight() )
			return false;
		if(bucketbotQuadtree.isValidMove((Circle)r, x_new, y_new)) {
			if(r.getBucket() == null)
				return true;

			if(bucketQuadtree.isValidMove((Circle)r.getBucket(), x_new, y_new))
				return true;
		}
		return false;
	}

	/**Moves the bucketbot to a new location if it is a valid move.  It is just like
	 * isBucketbotMoveValid, but it actually moves the bucketbot (and bucket if applicable).
	 * @see alphabetsoup.framework.Map#isBucketbotMoveValid(Bucketbot, float, float)
	 * @param r bucketbot to move
	 * @param x_new new x position
	 * @param y_new new y position
	 * @return true if the move is valid and was made
	 */
	public boolean moveBucketbot(Bucketbot r, float x_new, float y_new) {
		if(x_new - r.getRadius() < 0 || x_new + r.getRadius() > getWidth()
				|| y_new - r.getRadius() < 0 || y_new + r.getRadius() > getHeight() )
			return false;
		if(bucketbotQuadtree.isValidMove((Circle)r, x_new, y_new)) {
			if(r.getBucket() == null) {
				bucketbotQuadtree.moveTo((Circle)r, x_new, y_new);			
				return true;
			}
			
			if(bucketQuadtree.isValidMove((Circle)r.getBucket(), x_new, y_new)) {
				bucketQuadtree.moveTo((Circle)r.getBucket(), x_new, y_new);
				bucketbotQuadtree.moveTo((Circle)r, x_new, y_new);			
				return true;
			}
		}
		return false;
	}
	
	/**Checks to see if a location is valid for storing a bucket.  A location is invalid
	 * if storing the bucket will overlap a word or letter station, or if it will collide with another bucket.
	 * @param b bucket to check
	 * @param x_new new x position
	 * @param y_new new y position
	 * @return true if the location is valid
	 */
	public boolean isValidBucketStorageLocation(Bucket b, float x_new, float y_new) {
		if(x_new - b.getRadius() < 0 || x_new + b.getRadius() > getWidth()
				|| y_new - b.getRadius() < 0 || y_new + b.getRadius() > getHeight() )
			return false;
		if(!bucketQuadtree.isValidMove((Circle)b, x_new, y_new))
			return false;
		
		//make sure not overlapping with any station
		for(WordStation s : wordStations)
			if( ((Circle)s).IsCollision(x_new, y_new, b.getRadius()))
				return false;
		for(LetterStation s : letterStations)
			if( ((Circle)s).IsCollision(x_new, y_new, b.getRadius()))
				return false;
		
		return true;		
	}
	
	/**Returns the shortest amount of time that bucketbots will operate until the next possible collision.
	 * @return shortest possible time until next collision 
	 */
	public float getShortestTimeWithoutCollision() {
		//TODO revisit this minimal timing code
		//float min_dist = bucketbotQuadtree.getShortestDistanceWithoutCollision();
		//min_dist = Math.min(min_dist, bucketQuadtree.getShortestDistanceWithoutCollision());
		
		float max_vel = 0.001f;	//start non-zero
		for(Bucketbot b : bucketbots)
			max_vel = Math.max(max_vel, b.getMaxVelocity());
		//return min_dist / max_vel;
		
		//temporary method - simply find the min time a bucketbot can cover its diameter
		//under the diameters, such that bucketbots don't "tunnel" through each other
		float diam = 1.8f * ((BucketbotBase)bucketbots.get(0)).getRadius();
		return diam / max_vel;
	}

	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) {
		return Double.POSITIVE_INFINITY;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) {
		bucketQuadtree.updateTree();
		bucketbotQuadtree.updateTree();
	}

	/**
	 * @return Returns the map height.
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @return Returns the map width.
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @return Returns the tolerance of letter and word stations.
	 */
	public float getTolerance() {
		return tolerance;
	}

	/**
	 * @return the bucketbots
	 */
	public List<Bucketbot> getBucketbots() {
		return bucketbots;
	}

	/**
	 * @return the buckets
	 */
	public List<Bucket> getBuckets() {
		return buckets;
	}

	/**
	 * @return the letterStations
	 */
	public List<LetterStation> getLetterStations() {
		return letterStations;
	}

	/**
	 * @return the wordStations
	 */
	public List<WordStation> getWordStations() {
		return wordStations;
	}

	/**
	 * @return the maxAcceleration
	 */
	public float getMaxAcceleration() {
		return maxAcceleration;
	}

	/**
	 * @return the maxVelocity
	 */
	public float getMaxVelocity() {
		return maxVelocity;
	}
}
