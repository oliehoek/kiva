/**
 * 
 */
package alphabetsoup.framework;

import java.io.FileInputStream;
import java.lang.reflect.*;
import java.util.*;

/**SimulationWorld is the base class of the AlphabetSoup simulation itself.
 * This class should be extended to load and initialize all of the entities within
 * AlphabetSoup.  SimulationWorld contains these entities and updates them all accordingly
 * to advance the simulation forward in time.
 * @author Chris Hazard
 */
public class SimulationWorld 
{
	public Bucketbot bucketbots[] = null;
	public LetterStation letterStations[] = null;
	public WordStation wordStations[] = null;
	public Bucket buckets[] = null;
	public WordList wordList = null;
	protected List<LetterColor> letterColors;	/* letterColors is the list of probabilities of each color. the colorID is the color's index into this list */
	protected List<Updateable> updateables;
	public Map map = null;
	
	private double currentTime = 0.0;
	private double statisticsTime = 0.0;
	
	public static MersenneTwisterFast rand = new MersenneTwisterFast();
	protected boolean usingGUI;
	protected Properties params;	//parameters of the simulation
	protected static SimulationWorld simulationWorld;
	
	public static SimulationWorld getSimulationWorld() 
	{
		return simulationWorld;
	}
	
	public void resetStatistics() 
	{
		// SET IDLE TIME TO 0.0
		statisticsTime = currentTime;
		for(Bucketbot b : bucketbots) b.resetStatistics();
		for(LetterStation ls : letterStations) ls.resetStatistics();
		for(WordStation ws : wordStations) ws.resetStatistics();
		for(Bucket b : buckets) b.resetStatistics();
		wordList.resetStatistics();
	}
	
	/**Builds the base for the simulation.  Loads in parameters from the specified configuration file.
	 * Instantiates some of the basic simulation structure.
	 * @param configuration_file_name path of the configuration file to load
	 */
	public SimulationWorld(String configuration_file_name) 
	{
		simulationWorld = this;
		
		//load in parameters from configuration file
		params = new Properties();
		try 
		{
			FileInputStream fis = new FileInputStream(configuration_file_name);
			params.load(fis);
		} catch(Throwable e) { System.out.println("could not open alphabetsoup.config"); System.exit(1); }
		
		float map_width = Float.parseFloat(params.getProperty("map_width"));
		float map_length = Float.parseFloat(params.getProperty("map_length"));
		float tolerance = Float.parseFloat(params.getProperty("tolerance"));
		float max_acceleration = Float.parseFloat( params.getProperty("max_acceleration"));
		float max_velocity = Float.parseFloat( params.getProperty("max_velocity"));
		map = new Map(map_width, map_length, tolerance, max_acceleration, max_velocity);
		
		long random_seed = Integer.parseInt(params.getProperty("random_seed"));
		if(random_seed != 0) rand.setSeed(random_seed);
		
		wordList = (WordList)createClass(params.getProperty("word_list_class"));
		
		int num_bucketbots = Integer.parseInt(params.getProperty("num_bucketbots"));
		bucketbots = new Bucketbot[num_bucketbots];
		
		int num_buckets = Integer.parseInt(params.getProperty("num_buckets"));
		buckets = new Bucket[num_buckets];
		
		int num_word_stations = Integer.parseInt(params.getProperty("num_word_stations"));
		wordStations = new WordStation[num_word_stations];
		
		int num_letter_stations = Integer.parseInt(params.getProperty("num_letter_stations"));
		letterStations = new LetterStation[num_letter_stations];
		
		//get letter colors
		letterColors = new ArrayList<LetterColor>();
		for(int color_id = 0; true; color_id++) 
		{
			String s = params.getProperty("letter_color"+color_id);
			//if no more colors, we're done
			if(s == null)
				break;
			//break into values
			String values[] = s.split(":|,");
			float probability = Float.parseFloat(values[0]);
			int r = Integer.parseInt(values[1]),
				g = Integer.parseInt(values[2]),
				b = Integer.parseInt(values[3]);
			letterColors.add(color_id, new LetterColor(color_id, probability, r, g, b));
		}		
	}

	/**Load a class with a constructor with any signature, as specified by parameters
	 * java's reflection api SHOULD have this function, but it doesn't...
	 * @param class_name name of the class to load
	 * @param params parameter list as used by class's desired constructor
	 * @return new object as specified
	 */
	public static Object createClass(String class_name, Object ... params) 
	{
		try
		{
			Class classes[] = new Class[params.length];
			for(int i = 0; i < params.length; i++) 
			{
				classes[i] = params[i].getClass();
				//if it's a built-in type, we actually want the TYPE field, rather than the class itself
				// e.g. we would like "float" instead of "java.lang.Float"
				for(Field f : classes[i].getFields())
					if(f.getName().equals("TYPE")) 
					{
						try {
							classes[i] = (Class)f.get(null);
						} catch (Throwable e) { System.out.println("could not load class " + class_name + ". " + e); System.exit(1); }
						break;
					}
			}
			Constructor constr_def = Class.forName(class_name).getConstructor(classes);
			return constr_def .newInstance(params);
		} catch(Throwable e) { System.out.println("could not load class " + class_name + ". " + e + ": " + e.getCause()); System.exit(1); }
		return null;
	}

	/**Move the simulation forward by the specified amount of time
	 * @param elapsed_time relative time to move the system forward
	 */
	public void update(double elapsed_time) {
		
		//don't want to update less than the time required for something to move past 
		// 1/3 of the tolerance in a given time interval
		float minimumUpdateTime = map.getTolerance()/3 / map.getMaxVelocity();
		
		double update_finish_time = currentTime + elapsed_time;
		while(currentTime < update_finish_time) {
			
			//get the next event time
			double next_time = update_finish_time;

			//find the time of the earliest next event
			for(Updateable u : updateables)
				next_time = Math.min(u.getNextEventTime(currentTime), next_time);
			
			//see if a potential collision will happen before the next event
			double min_time_delta = Math.min( map.getShortestTimeWithoutCollision(), next_time - currentTime);
			min_time_delta = Math.max(min_time_delta, minimumUpdateTime);	//make sure update rate never gets too slow

			//update by at least a the minimum, but don't go past the next time
			next_time = Math.min(update_finish_time, currentTime + min_time_delta);

			//run up til the next event
			for(Updateable u : updateables)
				u.update(currentTime, next_time);
			
			currentTime = next_time;
		}
	}
	
	/**populates buckets with completely random inventory
	 * @param initial_inventory fraction of total capacity that should be full in the range of [0,1]
	 * @param bundle_size size of a bundle of letters (all inventory will be grouped by bundle size)
	 */
	public void initializeBucketContentsRandom(float initial_inventory, int bundle_size) 
	{
		int initial_num_letter_bundles = (int)(initial_inventory * buckets.length * buckets[0].getCapacity() / bundle_size + 0.5f);
		for(int i = 0; i < initial_num_letter_bundles; i++) 
		{
			while(true) 
			{
				//pick random bucket
				Bucket b = buckets[rand.nextInt(buckets.length)];
				if(b.getLetters().size() + bundle_size > b.getCapacity()) continue;
				
				//give it a new letter
				Letter l = wordList.generateRandomLetter();
				for(int j = 0; j < bundle_size; j++) b.addLetter(l.clone());
				break;
			}
		}
	}

	/**
	 * @return Returns the letterStations.
	 */
	public LetterStation[] getLetterStations() 
	{
		return letterStations;
	}

	/**
	 * @param letterStations The letterStations to set.
	 */
	public void setLetterStations(LetterStation[] letterStations) 
	{
		this.letterStations = letterStations;
	}

	/**
	 * @return Returns the wordStations.
	 */
	public WordStation[] getWordStations() 
	{
		return wordStations;
	}

	/**
	 * @param wordStations The wordStations to set.
	 */
	public void setWordStations(WordStation[] wordStations) 
	{
		this.wordStations = wordStations;
	}

	/**
	 * @return Returns the buckets.
	 */
	public Bucket[] getBuckets() 
	{
		return buckets;
	}

	/**
	 * @param buckets The buckets to set.
	 */
	public void setBuckets(Bucket[] buckets) 
	{
		this.buckets = buckets;
	}
	
	/**
	 * @return Returns the map.
	 */
	public Map getMap() 
	{
		return map;
	}

	/**
	 * @param map The map to set.
	 */
	public void setMap(Map map) 
	{
		this.map = map;
	}

	/**
	 * @return Returns the bucketbots.
	 */
	public Bucketbot[] getRobots() 
	{
		return bucketbots;
	}

	/**
	 * @param bucketbots The bucketbots to set.
	 */
	public void setRobots(Bucketbot[] bucketbots) 
	{
		this.bucketbots = bucketbots;
	}

	/**
	 * @return Returns the wordList.
	 */
	public WordList getWordList() 
	{
		return wordList;
	}

	/**
	 * @param words The WordList to use.
	 */
	public void setWordList(WordList words) 
	{
		this.wordList = words;
	}
	
	/**Gets the letter color specified by the index
	 * @param index
	 */
	public LetterColor getLetterColor(int index) 
	{
		return letterColors.get(index);
	}
	
	/**
	 * @return Returns the letter colors
	 */
	public List<LetterColor> getLetterColors() 
	{
		return letterColors;
	}

	/**
	 * @return Returns the currentTime.
	 */
	public double getCurrentTime() 
	{
		return currentTime;
	}

	/**
	 * @return Returns the usingGUI.
	 */
	public boolean isUsingGUI() 
	{
		return usingGUI;
	}

	/**
	 * @return the statisticsTime
	 */
	public double getStatisticsTime() 
	{
		return statisticsTime;
	}
}
