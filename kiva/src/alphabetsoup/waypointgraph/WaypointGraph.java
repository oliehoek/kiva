/**
 * 
 */
package alphabetsoup.waypointgraph;

import java.util.*;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Circle;
import alphabetsoup.framework.LetterStation;
import alphabetsoup.framework.Quadtree;
import alphabetsoup.framework.WordStation;

/**WaypointGraph maintains a collection of connected Waypoints, and the associated
 * ways to reference and manage them.   
 * @author Chris Hazard
 */
public class WaypointGraph {

	private HashSet<Waypoint> waypoints = new HashSet<Waypoint>();
	private HashMap<LetterStation, Waypoint> letterStations = new HashMap<LetterStation, Waypoint>();
	private HashMap<WordStation, Waypoint> wordStations = new HashMap<WordStation, Waypoint>();
	private HashMap<Bucket, Waypoint> buckets = new HashMap<Bucket, Waypoint>();
	private Quadtree waypointQuadtree;
	
	public WaypointGraph(float map_width, float map_height) 
	{
		 waypointQuadtree = new Quadtree(map_width, map_height);
	}
	
	/**Adds a Waypoint to the Waypoint graph, and couples it with any corresponding
	 * entities set in the Waypoint, such as Buckets, stations, etc.
	 * @param w Waypoint to add
	 */
	public void addWaypoint(Waypoint w) {
		waypoints.add(w);
		waypointQuadtree.addCircleObject(w);

		if(w.getLetterStation() != null) letterStations.put(w.getLetterStation(), w);
		if(w.getWordStation() != null) wordStations.put(w.getWordStation(), w);
		if(w.getBucket() != null) buckets.put(w.getBucket(), w);
	}
	
	/**Removes a Waypoint from the WaypointGraph, and decouples it from any associations,
	 * such as Buckets, Stations, etc. 
	 * @param w Waypoint to remove
	 */
	public void removeWaypoint(Waypoint w) {
		waypoints.remove(w);
		waypointQuadtree.removeCircleObject(w);
		
		//remove all links to given Waypoint
		for(Waypoint wp : waypoints)
			if(wp.getPaths().contains(w))
				wp.removePath(w);
		
		//remove any stations that point to it
		for(LetterStation ls : letterStations.keySet())
			if(letterStations.get(ls) == w)
				letterStations.remove(ls);
		for(WordStation ws : wordStations.keySet())
			if(wordStations.get(ws) == w)
				wordStations.remove(ws);
		for(Bucket b : buckets.keySet())
			if(buckets.get(b) == w)
				buckets.remove(b);
	}
	
	/**Returns all Waypoints within a given distance of a point
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param distance maximum distance from (x,y) to find Waypoints
	 * @return a List of Waypoints in the specified region
	 */
	public List<Waypoint> getWaypointsWithinDistance(float x, float y, float distance) {
		List<Circle> cl = waypointQuadtree.getObjectsWithinCircle(new Circle(distance, x, y));

		//need to typecast everything to Waypoints
		List<Waypoint> wl = new ArrayList<Waypoint>(cl.size());
		for(Circle c : cl) wl.add((Waypoint)c);
		return wl;
	}
	
	/**Returns the closest Waypoint to the specified Circle 
	 * @param c
	 * @return
	 */
	public Waypoint getClosestWaypoint(Circle c) {
		float min_dist = Float.POSITIVE_INFINITY;
		Waypoint closest = null;
		for(Waypoint w : getWaypoints()) {
			if(c.getDistance(w.getX(), w.getY()) < min_dist) {
				closest = w; 
				min_dist = c.getDistance(w.getX(), w.getY());
			}
		}
		return closest;
	}

	/**Returns the Waypoint corresponding to the given LetterStation
	 * @param ls LetterStation
	 * @return corresponding Waypoint
	 */
	public Waypoint getLetterStationWaypoint(LetterStation ls) {
		return letterStations.get(ls);
	}
	
	/**Returns the Waypoint corresponding to the given WordStation
	 * @param ws WordStation
	 * @return corresponding Waypoint
	 */
	public Waypoint getWordStationWaypoint(WordStation ws) {
		return wordStations.get(ws);
	}
	
	/**Returns the Waypoint corresponding to the given Bucket
	 * @param b Bucket currently set down on a Waypoint
	 * @return corresponding Waypoint
	 */
	public Waypoint getBucketWaypoint(Bucket b) {
		return buckets.get(b);
	}
	
	/**Should be called when a Bucket is picked up, so that it can be
	 * decoupled from the waypoint 
	 * @param b Bucket picked up
	 */
	public void bucketPickup(Bucket b) {
		buckets.get(b).setBucket(null);
		buckets.remove(b);
	}
	
	/**Should be called when a bucket is set down, so that the Bucket
	 * can be coupled to the corresponding Waypoint
	 * @param b Bucket set down
	 * @param w Waypoint the bucket was set down on
	 */
	public void bucketSetdown(Bucket b, Waypoint w) {
		w.setBucket(b);
		buckets.put(b, w);
	}

	/**
	 * @return Returns the waypoints.
	 */
	public Set<Waypoint> getWaypoints() {
		return waypoints;
	}
}
