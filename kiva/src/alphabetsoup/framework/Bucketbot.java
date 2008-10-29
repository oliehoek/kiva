package alphabetsoup.framework;

import java.util.HashMap;


/**Bucketbot defines the interface that robots (bucketbots) must provide to operate
 * in AlphabetSoup.
 * @author Chris Hazard
 */
public interface Bucketbot {

	/**If Bucketbot is within range of picking up the Bucket, it will do so, return true,
	 * and incur any corresponding timeout.  If the Bucket is out of range, it will return false.  
	 * @param b Bucket to attempt to pick up
	 * @return true if Bucket was successfully picked up
	 */
	public boolean pickupBucket(Bucket b);
	
	/**Sets down Bucket if Bucketbot is currently carying one, and incurs any corresponding timeouts.
	 * @return true if Bucket was successfully put down
	 */
	public boolean setdownBucket();
	
	/**Makes bucketbot block and wait until at least the time specified
	 * (used making bucketbots wait for stations to transfer letters) 
	 */
	public void waitUntil(double time);
	
	/**Notifies Bucketbot that it has been assigned a new task. 
	 * @param tt new task information -implementations may use any object types as a task
	 */
	public <T> void assignTask(T tt);

	/**
	 * @return Returns the x.
	 */
	public float getX();

	/**
	 * @return Returns the y.
	 */
	public float getY();

	/**
	 * @return Returns the radius.
	 */
	public float getRadius();
	
	/**
	 * @return Returns the direction in radians.
	 */
	public float getDirection();
	
	/**
	 * @return Returns the maximum velocity.
	 */
	public float getMaxVelocity();
	
	/**
	 * @return Returns the maximum acceleration.
	 */
	public float getMaxAcceleration();

	/**
	 * @param direction The direction to set in radians.
	 */
	public void setDirection(float direction);
	
	/**
	 * @return Returns the Bucket currently picked up by the Bucketbot, null if none picked up. 
	 */
	public Bucket getBucket();
	
	/**
	 * @return Returns the number of times the Bucketbot has picked up a Bucket.
	 */
	public int getNumPickups();
	
	/**
	 * @return Returns the number of times the Bucketbot has set down a Bucket.
	 */
	public int getNumSetdowns();
	
	/**
	 * @return Returns the distance the Bucketbot has distanceTraveled.
	 */
	public double getDistanceTraveled();
	
	/**
	 * @return Returns the number of collisions the Bucketbot has encountered.
	 */
	public int getNumCollisions();
	
	/**
	 * @return Returns a HashMap with keys being the names of each state or task the Bucketbot has been in,
	 * and the values being the percentage total of time spent in each state or task.  Note that the Bucketbot
	 * can be in two states at the same time (such as a low-level state and high-level task), the numbers
	 * may sum to > 1.0.
	 */
	public HashMap<String, Double> getTotalTimes();
	
	/**
	 * @return Returns the bucketPickupSetdownTime.
	 */
	public float getBucketPickupSetdownTime();
	
	/**Clears the statistics kept.
	 */
	public void resetStatistics();
}
