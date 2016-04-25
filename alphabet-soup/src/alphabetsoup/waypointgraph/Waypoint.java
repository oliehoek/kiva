/**
 * 
 */
package alphabetsoup.waypointgraph;

import java.text.DecimalFormat;
import java.util.*;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Bucketbot;
import alphabetsoup.framework.Circle;
import alphabetsoup.framework.LetterStation;
import alphabetsoup.framework.WordStation;

/**Waypoint represents a location and any objects that are relevant to that location,
 * such as Buckets, Bucket storage locations, WordStations, and LetterStations.
 * Waypoints connect to each other to make up a graph of paths for Bucketbots to traverse. 
 * @author Chris Hazard
 */
public class Waypoint extends Circle {
	
	private HashMap<Waypoint, Float> paths = new HashMap<Waypoint, Float>();
	private WordStation wordStation = null;
	private LetterStation letterStation = null;
	private Bucket bucket = null;
	private boolean bucketStorageLocation = false;
	private HashSet<Bucketbot> bucketbots = new HashSet<Bucketbot>();
	
	/**Constructs a destinationWaypoint based on a LetterStation
	 * @param ls
	 */
	public Waypoint(LetterStation ls) 
	{
		super(0.0f, ls.getX(), ls.getY());
		letterStation = ls;
	}
	
	/**Constructs a destinationWaypoint based on a WordStation
	 * @param ws
	 */
	public Waypoint(WordStation ws) 
	{
		super(0.0f, ws.getX(), ws.getY());
		wordStation = ws;
	}
	
	/**Constructs a destinationWaypoint based on a Bucket, and sets the Waypoint
	 *  to be a Bucket storage location
	 * @param b
	 */
	public Waypoint(Bucket b) 
	{
		super(0.0f, b.getX(), b.getY());
		bucketStorageLocation = true;
		bucket = b;
	}

	/**Constructs a Waypoint at a given location
	 * @param x
	 * @param y
	 * @param bucket_storage_location if true, then it is a valid Bucket storage location
	 */
	public Waypoint(float x, float y, boolean bucket_storage_location) 
	{
		super(0.0f, x, y);
		bucketStorageLocation = bucket_storage_location;
	}
	
	/**Gets the list of paths to other Waypoints from this Waypoint
	 * @return Set of Waypoints
	 */
	public Set<Waypoint> getPaths() 
	{
		return paths.keySet();
	}

	/**Adds a path to the possible paths
	 * @param w Waypoint to add path
	 * @param weight weight of the path
	 */
	public void addPath(Waypoint w, float weight) 
	{
		paths.put(w, weight);
	}
	
	/**Adds a path to the possible paths, using distance as the weight
	 * @param w Waypoint to add path
	 */
	public void addPath(Waypoint w) 
	{
		addPath(w, getDistance(w));
	}

	/**Removes a path from the possible paths
	 * @param w Waypoint to remove path
	 */
	public void removePath(Waypoint w) 
	{
		paths.remove(w);
	}
	
	/**Adds a path to the possible paths in both directions
	 * @param w Waypoint to add path
	 * @param weight weight of the path
	 */
	public void addBidirectionalPath(Waypoint w, float weight) 
	{
		paths.put(w, weight);
		w.addPath(this, weight);
	}
	
	/**
	 * Adds a path to the possible paths in both directions, using distance as the weight
	 * @param w Waypoint to add path
	 */
	public void addBidirectionalPath(Waypoint w) 
	{
		float dist = getDistance(w);
		addPath(w, dist);
		w.addPath(this, dist);
	}

	/**
	 * Removes a path from the possible paths
	 * @param w Waypoint to remove path
	 */
	public void removeBidirectionalPath(Waypoint w)
	{
		paths.remove(w);
		w.removePath(this);
	}
	
	/**Returns the distance for a given path
	 * @param w Waypoint to get path distance for 
	 * @return returns the path distance for the path to the given Waypoint,
	 * but returns Float.POSITIVE_INFINITY if no path exists
	 */
	public float getPathDistance(Waypoint w) 
	{
		if(paths.containsKey(w)) return getDistance(w);
		else return Float.POSITIVE_INFINITY;
	}
	
	/**Returns the weight for a given path
	 * @param w Waypoint to get path weight for 
	 * @return returns the path weight for the path to the given Waypoint
	 */
	public float getPathWeight(Waypoint w) {
		return paths.get(w);
	}

	/**
	 * @return Returns the bucket.
	 */
	public Bucket getBucket() {
		return bucket;
	}

	/**
	 * @param bucket The bucket to set.
	 */
	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	/**
	 * @return Returns the bucketbots.
	 */
	public Set<Bucketbot> getBucketbots() {
		return bucketbots;
	}
	
	/**Adds a Bucketbot going to this location 
	 * @param b
	 */
	public void addBucketbot(Bucketbot b) {
		bucketbots.add(b);
	}
	
	/**Removes a Bucketbot that is no longer going to this location
	 * @param b
	 */
	public void removeBucketbot(Bucketbot b) {
		bucketbots.remove(b);
	}

	/**
	 * @return Returns the bucketStorageLocation.
	 */
	public boolean isBucketStorageLocation() {
		return bucketStorageLocation;
	}
	
	/**Marks the Waypoint as a bucket storage location
	 */
	public void setBucketStorageLocation() {
		bucketStorageLocation = true;
	}

	/**
	 * @return Returns the letterStation.
	 */
	public LetterStation getLetterStation() 
	{
		return letterStation;
	}

	/**
	 * @return Returns the wordStation.
	 */
	public WordStation getWordStation() 
	{
		return wordStation;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() 
	{
		DecimalFormat three_digits = new DecimalFormat("0.00");
		return "Waypoint(" + three_digits.format(getX()) + "," + three_digits.format(getY()) + ")";
	}
}
