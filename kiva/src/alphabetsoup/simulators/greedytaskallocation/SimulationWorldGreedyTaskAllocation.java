/**
 * 
 */
package alphabetsoup.simulators.greedytaskallocation;

import java.util.*;
import alphabetsoup.base.*;
import alphabetsoup.framework.*;
import alphabetsoup.userinterface.*;
import alphabetsoup.waypointgraph.*;

/**Example AlphabetSoup simulation file, which puts buckets in a grid, lays out bots randomly,
 * parameratizes everything based on "alphabetsoup.config", and starts everything running.
 * @author Chris Hazard
 */
public class SimulationWorldGreedyTaskAllocation extends SimulationWorld 
{
	private double simulationDuration = 0.0;
	private double simulationWarmupTime = 0.0;
	
	public LetterManager letterManager = null;
	public Updateable wordManager = null;						// WHERE IS IT IMPLEMENTED?
	public BucketbotGlobalResources bucketbotManager = null; 	// THIS IS NOT THE INSTANCE OF CLASS BucketbotManager
	public WaypointGraph waypointGraph = null;
	public BucketbotAgent bucketbotagents[] = null;	// ONE BUCKET BOT HAS A BUCKET BOT AGEENT

	private static SimulationWorldGreedyTaskAllocation simulationWorldGraphExample;
	public static SimulationWorldGreedyTaskAllocation getSimulationWorld() 
	{
		return simulationWorldGraphExample;
	}
	
	public SimulationWorldGreedyTaskAllocation() 
	{
		// SUPER CLASS DOES THE FOLLOWING:
		// 1. INITIALIZE BUCKET, BUCKETBOT WORDSTATION, LETTERSTATION
		// 2. INITIALIZE BUCKET CONTENT
		super("kiva.config");
		
		simulationWorldGraphExample = this;
		
		float bucketbot_size = Float.parseFloat(params.getProperty("bucketbot_size"));
		float bucket_size = Float.parseFloat(params.getProperty("bucket_size"));
		float station_size = Float.parseFloat(params.getProperty("station_size"));
		
		int bucket_capacity = Integer.parseInt(params.getProperty("bucket_capacity"));
		int bundle_size = Integer.parseInt(params.getProperty("bundle_size"));
		int letter_station_capacity = Integer.parseInt(params.getProperty("letter_station_capacity"));
		int word_station_capacity = Integer.parseInt(params.getProperty("word_station_capacity"));
		
		float bucket_pickup_setdown_time = Float.parseFloat( params.getProperty("bucket_pickup_setdown_time"));
		float letter_to_bucket_time = Float.parseFloat( params.getProperty("letter_to_bucket_time"));
		float bucket_to_letter_time = Float.parseFloat( params.getProperty("bucket_to_letter_time"));
		float word_completion_time = Float.parseFloat( params.getProperty("word_completion_time"));
		float collision_penalty_time = Float.parseFloat( params.getProperty("collision_penalty_time"));
		
		usingGUI = (Integer.parseInt(params.getProperty("useGUI")) == 1);
		String window_size[] = params.getProperty("window_size").split("x");
		simulationDuration = Double.parseDouble(params.getProperty("simulation_duration"));
		simulationWarmupTime = Double.parseDouble(params.getProperty("simulation_warmup_time"));
		
		// WAYPOINTGRAPH DOES THE FOLLOWING:
		// 1. CREATE ROOT NODE USING WIDTH AND HEIGHT
		waypointGraph = new WaypointGraph(map.getWidth(), map.getHeight());
		
		//Set up base map to add things to
		// CREATE A FRAME OF WIDTH AND HEIGHT AND PASS THIS OBJECT TO UI
		if(usingGUI) RenderWindow.initializeUserInterface(Integer.parseInt(window_size[0]), Integer.parseInt(window_size[1]), this);
		
		//SET GLOBAL waypointGraph WHERE BucketbotDriver HAS TO TRAVEL ON. 
		// THERE ARE MANY BUCKET BUCKET BOT DRIVER, BUT IT TRAVEL ON THE SAME ***WAYPOINTGRAPH***
		BucketbotDriver.waypointGraph = waypointGraph;
		BucketbotDriver.map = map;

		//Create classes and agents, and add them to the map accordingly
        //////////////////////////////////////////////////////////////////////////////////////
		// THIS IS WHERE DRIVER AGENT, DRIVER BOT MANAGER, BUCKET BOT DRIVER ASSIGNED
		// BUCKETBOT AGENT CONTAINS BUCKET BOT DRIVER
		//////////////////////////////////////////////////////////////////////////////////////
		bucketbotagents = new BucketbotAgent[bucketbots.length];
		for(int i = 0; i < bucketbots.length; i++) 
		{
			bucketbots[i] = (Bucketbot) new BucketbotDriver( bucketbot_size, bucket_pickup_setdown_time, map.getMaxAcceleration(), map.getMaxVelocity(), collision_penalty_time);
			bucketbotagents[i] = new BucketbotAgent((BucketbotDriver)bucketbots[i]);
		}
		
		for(int i = 0; i < letterStations.length; i++)
		{
			letterStations[i] = (LetterStation) new LetterStationBase(station_size, letter_to_bucket_time, bundle_size, letter_station_capacity);
		}
		
		for(int i = 0; i < wordStations.length; i++)
		{
			wordStations[i] = (WordStation) new WordStationBase(station_size, bucket_to_letter_time, word_completion_time, word_station_capacity);
		}
		
		for(int i = 0; i < buckets.length; i++)
		{
			buckets[i] = (Bucket) new BucketBase(bucket_size, bucket_capacity);
		}
		
		bucketbotManager	= new BucketbotGlobalResources(buckets);
		letterManager	= new LetterManager();
		wordManager		= (Updateable)new WordOrderManager();

		//generate waypoint graph
		HashMap<Waypoint, Bucket> storage = GenerateWaypointGraph.initializeCompactRandomLayout(this, waypointGraph);
		for(Waypoint w : storage.keySet())
		{
			if(storage.get(w) == null) bucketbotManager.addNewValidBucketStorageLocation(w);
			else bucketbotManager.addNewUsedBucketStorageLocation(storage.get(w), w);
		}
		
		//generate words
		wordList.generateWordsFromFile(params.getProperty("dictionary"), letterColors, Integer.parseInt(params.getProperty("number_of_words")) );
		
		//populate buckets
		initializeBucketContentsRandom(Float.parseFloat(params.getProperty("initial_inventory")), bundle_size);
		
		//populate update list
		updateables = new ArrayList<Updateable>();
		for(Bucketbot r : bucketbots) updateables.add((Updateable)r);
		for(BucketbotAgent a : bucketbotagents) updateables.add((Updateable)a);
		updateables.add((Updateable)map);
		updateables.add((Updateable)bucketbotManager);		// ADD BucketbotGlobalResources
		updateables.add((Updateable)wordManager);
		updateables.add((Updateable)letterManager);
		for(WordStation s : wordStations) updateables.add((Updateable)s);
		for(LetterStation s : letterStations) updateables.add((Updateable)s);
		//System.exit(1);
		
		//finish adding things to be rendered
		if(usingGUI)
		{
			RenderWindow.addAdditionalDetailRender(new WordListRender((WordListBase)wordList));
			RenderWindow.addLineRender(new MapRender(map));
			for(LetterStation s : letterStations) RenderWindow.addSolidRender(new LetterStationRender((LetterStationBase)s));
			for(WordStation s : wordStations) RenderWindow.addSolidRender(new WordStationRender((WordStationBase)s));
			for(Bucket b : buckets) RenderWindow.addLineRender(new BucketRender((BucketBase)b));
			for(Bucketbot r : bucketbots) RenderWindow.addLineRender(new BucketbotRender((BucketbotBase)r));
			//RenderWindow.addSolidRender(bucketbotManager);
			//RenderWindow.addSolidRender(new WaypointGraphRender(waypointGraph));
		}
	}
	
	/**Launches the Alphabet Soup simulation without user interface.
	 * @param args
	 */
	public static void main(String[] args) 
	{
		simulationWorld = new SimulationWorldGreedyTaskAllocation();
		double warmup_time = ((SimulationWorldGreedyTaskAllocation)simulationWorld).simulationWarmupTime;
		double simulation_time = ((SimulationWorldGreedyTaskAllocation)simulationWorld).simulationDuration; 
		if(simulationWorld.isUsingGUI()) 
		{
			RenderWindow.mainLoop(simulationWorld, warmup_time);
			simulationWorld.resetStatistics();
			RenderWindow.mainLoop(simulationWorld, simulation_time);
			RenderWindow.destroyUserInterface();
		}
		else 
		{
			simulationWorld.update(warmup_time);
			simulationWorld.resetStatistics();
			simulationWorld.update(simulation_time);
		}
		SummaryReport.generateReport(simulationWorld);
	}
}
