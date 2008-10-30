/**
 * 
 */
package alphabetsoup.simulators.greedytaskallocation;

import java.util.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;
import alphabetsoup.framework.*;
//import alphabetsoup.simulators.graphexample.SimulationWorldGraphExample;
import alphabetsoup.userinterface.Renderable;
import alphabetsoup.waypointgraph.Waypoint;

/**
 * BucketbotGlobalResources implements a basic Bucketbot manager that uses basic queues of tasks
 * to dispense jobs to bucketbots.
 * @author Chris Hazard
 */
public class BucketbotGlobalResources implements Updateable, Renderable {
	
	protected LinkedHashSet<Bucket> usedBuckets = new LinkedHashSet<Bucket>();
	protected LinkedHashSet<Bucket> unusedBuckets = new LinkedHashSet<Bucket>();
	
	public BucketbotGlobalResources(Bucket [] buckets) 
	{
		for(Bucket b : buckets) unusedBuckets.add(b);
	}
	
	/**
	 * THIS CLASS BINDS REQUESTED WORD WITHIN A WORD BY A WORK STATION  
	 * @author Chris Hazard
	 *
	 */
	public static class WordStationDeliveryRequest 
	{
		public WordStationDeliveryRequest(Letter l, Word w, WordStation s) 
		{
			letter = l;	
			word = w; 
			station = s;
		}
		public Letter letter;
		public Word word;
		public WordStation station;
	}
	
	/**
	 * THIS CLASS BINDS REQUESTED LETTER BY LETTER STATION
	 * @author Chris Hazard
	 *
	 */
	public static class LetterStationPickupRequest 
	{
		public LetterStationPickupRequest(Letter l, LetterStation s) 
		{	
			letter = l;	
			station = s;
		}
		public Letter letter;
		public LetterStation station;
	}

	List<WordStationDeliveryRequest> openLetterRequests = new ArrayList<WordStationDeliveryRequest>();
	List<LetterStationPickupRequest> availableLetters = new ArrayList<LetterStationPickupRequest>();
	
	HashSet<Waypoint> unusedBucketStorageLocations = new HashSet<Waypoint>();
	HashMap<Bucket,Waypoint> usedBucketStorageLocations = new HashMap<Bucket,Waypoint>();
	
	/**
	 * Adds a new valid currently used location to store buckets on the map
	 */
	public void addNewUsedBucketStorageLocation(Bucket b, Waypoint w) 
	{
		usedBucketStorageLocations.put(b, w);
	}
	
	/**Adds a new valid unused location to store buckets on the map
	 */
	public void addNewValidBucketStorageLocation(Waypoint w) 
	{
		unusedBucketStorageLocations.add(w);
	}

	/**Called whenever a new Word has been assigned to a WordStation
	 * @param w Word assigned
	 * @param s WordStation the word was assigned to
	 */
	public void newWordAssignedToStation(Word w, WordStation s) 
	{
		// THIS IS WHERE LETTERS ARE REQUESTED
		MersenneTwisterFast rand = SimulationWorldGreedyTaskAllocation.rand;
		
		for(Letter l : w.getOriginalLetters()) 
		{
			if(openLetterRequests.size() > 0)
			{
				// FOR EVERY LETTER, INSERT IN A RANDOM LOCATION
				openLetterRequests.add(rand.nextInt(openLetterRequests.size()), new WordStationDeliveryRequest(l, w, s));
			}
			else
			{
				openLetterRequests.add(new WordStationDeliveryRequest(l, w, s));
			}
		}
	}
	
	/**
	 * Called whenever a new Letter has been assigned to a LetterStation
	 * @param l Letter assigned
	 * @param s LetterStation the Letter was assigned to
	 */
	public void newLetterBundleAssignedToStation(Letter l, LetterStation s) 
	{
		//MersenneTwisterFast rand = SimulationWorldGraphExample.rand;
		MersenneTwisterFast rand = SimulationWorldGreedyTaskAllocation.rand;
		if(availableLetters.size() > 0)
		{
			availableLetters.add(rand.nextInt(availableLetters.size()), new LetterStationPickupRequest(l, s));
		}
		else
		{
			availableLetters.add(new LetterStationPickupRequest(l, s));
		}
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) 
	{
		return Double.POSITIVE_INFINITY;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) {

	}	
	
	//routines which may be used to render the current used storage locations
	private Disk disk = new Disk();
	
	public void render() 
	{
		GL11.glLineWidth(1.0f);
		GL11.glColor4ub((byte)0xFF, (byte)0x0, (byte)0x0, (byte)0xFF);
		for(Bucket b : usedBucketStorageLocations.keySet()) 
		{
			Waypoint w = usedBucketStorageLocations.get(b);
			
			//draw the center
			GL11.glPushMatrix();
			GL11.glTranslatef(w.getX(), w.getY(), 0.0f);
			disk.draw(0.0f, SimulationWorldGreedyTaskAllocation.getSimulationWorld().map.getTolerance(), 10, 1);
			GL11.glPopMatrix();
			
			//draw the paths
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2f(w.getX(), w.getY());
			GL11.glVertex2f(b.getX(), b.getY());			
			GL11.glEnd();
		}
		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0x0, (byte)0xFF);
		for(Waypoint w : unusedBucketStorageLocations) 
		{	
			//draw the center
			GL11.glPushMatrix();
			GL11.glTranslatef(w.getX(), w.getY(), 0.0f);
			disk.draw(0.0f, SimulationWorldGreedyTaskAllocation.getSimulationWorld().map.getTolerance(), 10, 1);
			GL11.glPopMatrix();
		}
	}

	public void renderOverlayDetails() {
		
	}
	
	public void renderDetails() {
		
	}

	public boolean isMouseOver(float mouse_x, float mouse_y) {
		return false;
	}
}
