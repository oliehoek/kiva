/**
 * 
 */
package alphabetsoup.simulators.greedytaskallocation;

import alphabetsoup.base.BucketbotTask;
import alphabetsoup.framework.*;
import alphabetsoup.simulators.greedytaskallocation.BucketbotGlobalResources.LetterStationPickupRequest;
import alphabetsoup.simulators.greedytaskallocation.BucketbotGlobalResources.WordStationDeliveryRequest;
import alphabetsoup.waypointgraph.*;

/**
 * @author Chris Hazard
 */
public class BucketbotAgent implements BucketbotManager, Updateable {
	
	BucketbotDriver bucketbot = null;
	BucketbotGlobalResources manager = null;
	Map map = null;
	WaypointGraph waypointGraph = null;
	
	Bucket reservedBucket = null;
	LetterStation targetLetterStation = null;
	WordStation targetWordStation = null;
	Word targetWord;
	Letter targetLetter = null;	//letter want to fill
	Letter letterToDropOff = null;	//letter want to drop off
	Waypoint reservedStorage = null;
	
	public BucketbotAgent(BucketbotDriver bucketbot) 
	{
		this.bucketbot = bucketbot;
		bucketbot.manager = (BucketbotManager)this;
	}
	
	/**Optimistically estimates the time it will take this.bucketbot to reach Waypoint w
	 * @param w
	 * @return estimated travel time
	 */
	public float estimateTravelTime(Waypoint w) {
		float travel_time = bucketbot.getDistance(w) / bucketbot.getMaxVelocity();
		return travel_time;
	}
	
	/**Optimistically estimates the time it will take a bucketbot to go from Waypoints start to end
	 * @param start
	 * @param end
	 * @return estimated travel time
	 */
	public float estimateTravelTime(Waypoint start, Waypoint end) {
		float travel_time = start.getDistance(end) / bucketbot.getMaxVelocity();
		return travel_time;
	}
	
	/**Estimates the amount of time it will take before a new letter can be delivered to the word station 
	 * @param w
	 * @return estimated time
	 */
	public float estimateWordStationWaitTime(Waypoint w) {
		WordStation ws = w.getWordStation();
		//assume the wait time is 2 * length of the bucketbot there plus the time for all letters 
		float wait_time = ((ws.getBucketToLetterTime() + 5 * 2* bucketbot.getRadius()) / bucketbot.getMaxVelocity())
								* w.getBucketbots().size() * w.getBucketbots().size();
		return wait_time;
	}
	
	/**Estimates the amount of time it will take before a new letter can be picked up from the letter station
	 * @param w
	 * @return estimated time
	 */
	public float estimateLetterStationWaitTime(Waypoint w) {
		LetterStation ls = w.getLetterStation();
		//assume the wait time is 2 * length of the bucketbot there plus the time for all letters 
		float wait_time = ((ls.getLetterToBucketTime() + 5 * 2* bucketbot.getRadius()) / bucketbot.getMaxVelocity())
								* w.getBucketbots().size() * w.getBucketbots().size();
		return wait_time;
	}
	
	/**Tells bucketbot to store its current bucket at the specified waypoint
	 * @param w
	 */
	public void storeBucketAtStorageLocation(Waypoint w) {
		//if have a storage location, but it's not the closest, then free it
		if(reservedStorage != null && reservedStorage != w) {
			manager.unusedBucketStorageLocations.add(manager.usedBucketStorageLocations.get(reservedStorage) );
			manager.usedBucketStorageLocations.remove(reservedStorage);
		}
		
		//get storage location
		if(reservedStorage == null) {
			reservedStorage = w;
			manager.unusedBucketStorageLocations.remove(w);
			manager.usedBucketStorageLocations.put(reservedBucket, reservedStorage);
		}
		
		bucketbot.<BucketbotTask>assignTask(BucketbotTask.createTaskSTORE_BUCKET(bucketbot.getBucket(), reservedStorage));
	}
	
	/**Tells bucketbot to store its current bucket at the closest waypoint
	 */
	public void storeBucketAtClosestStorageLocation() {
		//find closest free storage location
		Waypoint closest = null;
		double closest_distance = Double.POSITIVE_INFINITY;
		for(Waypoint w : manager.unusedBucketStorageLocations) {
			double distance = bucketbot.getDistance(w);
			//if it's closer than the previous closest, then use this new one instead
			if(distance < closest_distance) {
				closest_distance = distance;
				closest = w;
			}
		}
		
		storeBucketAtStorageLocation(closest);
	}
	
	/**Tells bucketbot to store its current bucket at the closest location (if it has one),
	 * and then keep moving to stay out of the way of other bucketbots.
	 */
	void getOutOfTheWay() 
	{
		cancelCurrentLetterDelivery();
		cancelCurrentLetterPickup();
		
		if(bucketbot.getBucket() != null) {
			storeBucketAtClosestStorageLocation();
			return;
		}
		
		//pick a random offset to move
		float x = bucketbot.getX() + bucketbot.getRadius() * 2 * (SimulationWorld.rand.nextFloat() - .5f);
		float y = bucketbot.getY() + bucketbot.getRadius() * 2 * (SimulationWorld.rand.nextFloat() - .5f);
		
		//don't get too close to the edge
		int radii_from_edge = 8;
		x = Math.max(x, radii_from_edge*bucketbot.getRadius());
		x = Math.min(x, BucketbotDriver.map.getWidth() - radii_from_edge*bucketbot.getRadius());
		y = Math.max(y, radii_from_edge*bucketbot.getRadius());
		y = Math.min(y, BucketbotDriver.map.getHeight() - radii_from_edge*bucketbot.getRadius());
		bucketbot.<BucketbotTask>assignTask(BucketbotTask.createTaskMOVE(x, y));
	}
	
	/**Based on the current situation, bucketbot needs to get letter, but doesn't have it.
	 * This function reserves the letter the bucket should pick up.
	 * Returns the bucket that should be used, null if it's carying a bucket
	 * and the current bucket won't work (too full).
	 */
	public Bucket reserveBestLetterToPickUp() {
		int bundle_size = map.getLetterStations().get(0).getBundleSize();
			
		//if have a current bucket
		if(reservedBucket != null || bucketbot.getBucket() != null) {
			Bucket b = bucketbot.getBucket();
			if(b == null)
				b = reservedBucket;
			
			//if no room, get rid of the current bucket
			if(b.getLetters().size() + bundle_size > b.getCapacity())
				return null;
			
			//current bucket works, so find closest letter station and best task
			float closest_letter_station_time = Float.POSITIVE_INFINITY;
			LetterStationPickupRequest best_pickup = null;

			//check all tasks
			for(LetterStationPickupRequest lspr : manager.availableLetters) {
				//see how long it would take to get to this letter station
				// chose the worst of delivering or waiting
				Waypoint sw = waypointGraph.getLetterStationWaypoint(lspr.station);
				float time = Math.max( estimateTravelTime(bucketbot.getCurrentWaypoint(), sw), estimateLetterStationWaitTime(sw) );
				
				//if it's the best, then use it
				if(time < closest_letter_station_time) {
					best_pickup = lspr;
					closest_letter_station_time = time;
				}
			}
			
			if(best_pickup == null)
				return null;

			//allocate task!
			targetLetter = best_pickup.letter;
			targetLetterStation = best_pickup.station;
			manager.availableLetters.remove(best_pickup); 
			return b;
		}
		
		//don't have a bucket

		float best_pickup_time = Float.POSITIVE_INFINITY;
		LetterStationPickupRequest best_pickup = null;
		Bucket best_bucket = null;
		
		for(Bucket b : manager.unusedBuckets) {
			//find time to pick up the letter
			float pickup_time = estimateTravelTime(waypointGraph.getBucketWaypoint(b));

			//check all tasks
			for(LetterStationPickupRequest lspr : manager.availableLetters) {
				//if it has room
				if(b.getLetters().size() + lspr.station.getBundleSize() <= b.getCapacity()) {
					//see how long it would take to get to this letter station
					// chose the worst of delivering or waiting
					Waypoint sw = waypointGraph.getLetterStationWaypoint(lspr.station);
					float deliver_time = Math.max( estimateTravelTime(waypointGraph.getBucketWaypoint(b), sw), estimateLetterStationWaitTime(sw) );
					
					//if it's the best, then use it
					if(pickup_time + deliver_time < best_pickup_time) {
						best_pickup = lspr;
						best_pickup_time = pickup_time + deliver_time;
						best_bucket = b;
					}
				}
			}
		}
		
		if(best_pickup == null)
			return null;
		
		targetLetter = best_pickup.letter;
		targetLetterStation = best_pickup.station;
		manager.availableLetters.remove(best_pickup);
		
		return best_bucket;
	}
	

	WordStationDeliveryRequest bestDeliveryRequest = null;
	float bestTimeForDeliveryRequest = 0.0f;
	Letter bestLetterForDeliveryRequest = null;
	/**Finds the best delivery task for the specified bucekt.
	 * Sets bestDeliveryRequest to null if none found, otherwise bestDeliveryRequest and bestTimeForDeliveryRequest are initialized.
	 * @param b Bucket to take
	 * @param w Current location of the Bucket b
	 */
	void getBestTaskForBucket(Bucket b, Waypoint w) 
	{
		bestDeliveryRequest = null;
		if(b == null || w == null) return;
		
		bestTimeForDeliveryRequest = Float.POSITIVE_INFINITY;

		//check all tasks
		for(WordStationDeliveryRequest wsdr : manager.openLetterRequests) 
		{
			//if it has the letter
			Letter l = b.containsMatchingLetter(wsdr.letter);
			if(l != null) 
			{
				//see how long it would take to get to the word station
				// chose the worst of delivering or waiting
				Waypoint sw = waypointGraph.getWordStationWaypoint(wsdr.station);
				float time = Math.max( estimateTravelTime(w, sw), estimateWordStationWaitTime(sw) );
				
				//if it's the best, then use it
				if(time < bestTimeForDeliveryRequest) 
				{
					bestDeliveryRequest = wsdr;
					bestTimeForDeliveryRequest = time;
					bestLetterForDeliveryRequest = l;
				}
			}
		}
	}
	
	Waypoint bestDeliveryTaskStorageLocation;
	/**Determines the best delivery task for the given bucketbot, and allocates the required resources
	 * @return Bucket to be used to complete the task
	 */
	public Bucket getBestDeliveryTask() 
	{	
		//find a letter to retrieve
		WordStationDeliveryRequest best_task = null;
		Bucket best_bucket = null;
		Letter best_letter = null;
		float best_task_time = Float.POSITIVE_INFINITY;
		Waypoint best_storage_location = null;	//set to non-null if best task requires a different bucket
		
		//try with the current bucket
		getBestTaskForBucket(bucketbot.getBucket(), bucketbot.getCurrentWaypoint());
		best_task = bestDeliveryRequest;
		if(best_task != null) 
		{
			best_task_time = bestTimeForDeliveryRequest;
			best_bucket = bucketbot.getBucket();
			best_letter = bestLetterForDeliveryRequest;
		}

		//time to pick up the bucket
		float base_time = bucketbot.getBucketPickupSetdownTime();
		//time to set down the current bucket if it has one
		if(bucketbot.getBucket() != null) base_time += bucketbot.getBucketPickupSetdownTime();
		
		for(Bucket b : manager.unusedBuckets) 
		{	
			float t = base_time;
			Waypoint best_storage_location_for_this_bucket = null;
			
			//if need to set down current bucket
			if(bucketbot.getBucket() != null) 
			{
				
				//grab this once to pull it out of the loop
				Waypoint target_bucket_waypoint = waypointGraph.getBucketWaypoint(b);	
				float min_storage_time = Float.POSITIVE_INFINITY;
				for(Waypoint w : manager.unusedBucketStorageLocations) {
					//need time to set down current bucket and also pick up new bucket
					float store_time = 2 * bucketbot.getBucketPickupSetdownTime();
					
					//find time to get to storage location
					store_time += estimateTravelTime(w);
					//find time to get from storage location to new bucket
					store_time += estimateTravelTime(w, target_bucket_waypoint);
					
					if(store_time < min_storage_time) {
						min_storage_time = store_time;
						best_storage_location_for_this_bucket = w;
					}				
				}
				
				//use the best waypoint
				t += min_storage_time;
			}
			else { //no bucket, just go pick it up
				t += estimateTravelTime(waypointGraph.getBucketWaypoint(b));				
			}
			
			//get place to take the bucket
			getBestTaskForBucket(b, waypointGraph.getBucketWaypoint(b));
			if(bestDeliveryRequest != null) {
				//if the time is still better, then count it
				if(t + bestTimeForDeliveryRequest < best_task_time) {
					best_task = bestDeliveryRequest;
					best_task_time = t + bestTimeForDeliveryRequest;
					best_bucket = b;
					best_letter = bestLetterForDeliveryRequest;
					best_storage_location = best_storage_location_for_this_bucket; 
				}
			}
		}

		//if found no task, take the oldest
		if(best_task == null)
			best_task = manager.openLetterRequests.get(0);
		
		targetLetter = best_task.letter;
		targetWord = best_task.word;
		targetWordStation = best_task.station;
		manager.openLetterRequests.remove(best_task);
		letterToDropOff = best_letter;
		bestDeliveryTaskStorageLocation = best_storage_location;
		return best_bucket;
	}
	
	/**Frees the resources allocated to the bucketbot for its current letter delivery task
	 */
	public void cancelCurrentLetterDelivery() {
		if(targetLetter != null && targetWord != null && targetWordStation != null)
			manager.openLetterRequests.add(new BucketbotGlobalResources.WordStationDeliveryRequest(targetLetter, targetWord, targetWordStation));
		targetLetter = null;
		targetWord = null;
		targetWordStation = null;
		letterToDropOff = null;
	}
	
	/**Frees the resources allocated to the bucketbot for its current letter pickup task
	 */
	public void cancelCurrentLetterPickup() {
		if(targetLetter != null && targetLetterStation != null)
			manager.availableLetters.add(new BucketbotGlobalResources.LetterStationPickupRequest(targetLetter, targetLetterStation));
		targetLetter = null;
		targetLetterStation = null;		
	}
	
	/**Finds the best letter delivery task and then executes it
	 * @return false if don't have available task
	 */
	public boolean doDeliverLetterTask() {
		if(manager.openLetterRequests.size() == 0)
			return false;
			
		Bucket best_bucket = getBestDeliveryTask();
		
		//if no bucket, then get one
		if(reservedBucket == null) {
			reservedBucket = best_bucket;
			
			//no good buckets!
			if(reservedBucket == null) {
				cancelCurrentLetterDelivery();
				return false;
			}
			
			//reserve the bucket
			manager.unusedBuckets.remove(reservedBucket);
			manager.usedBuckets.add(reservedBucket);
		}
		else { //already have a bucket... see if it's the best one
			//if don't have the best bucket for the job, then store the old one
			if(reservedBucket != best_bucket) {
				manager.openLetterRequests.add(new BucketbotGlobalResources.WordStationDeliveryRequest(targetLetter, targetWord, targetWordStation));
				targetLetter = null;
				targetWord = null;
				targetWordStation = null;
				letterToDropOff = null;
				if(bestDeliveryTaskStorageLocation != null)
					storeBucketAtStorageLocation(bestDeliveryTaskStorageLocation);
				else
					storeBucketAtClosestStorageLocation();
				return true;
			}
		}

		bucketbot.<BucketbotTask>assignTask(BucketbotTask.createTaskTAKE_BUCKET_TO_WORD_STATION(
				reservedBucket, letterToDropOff, targetWordStation, targetWord));
		return true;
	}
	
	/**Finds the best letter pickup task and then executes it
	 * @return false if don't have a task
	 */
	public boolean doPickupLetterTask() {
		//make sure there's a letter available to get
		if(manager.availableLetters.size() == 0)
			return false;

		Bucket best_bucket = reserveBestLetterToPickUp();
		
		if(reservedBucket == null) {
			reservedBucket = best_bucket;
			
			//no free buckets!
			if(reservedBucket == null) {
				//cancel task reservation and store the bucket
				cancelCurrentLetterPickup();
				return false;
			}
			
			//reserve the bucket
			manager.unusedBuckets.remove(reservedBucket);
			manager.usedBuckets.add(reservedBucket);
		}
		else { //already have a bucket... see if it's the best one
			//if don't have the best bucket for the job, then store the old one
			if(reservedBucket != best_bucket) {
				//cancel task reservation and store the bucket
				cancelCurrentLetterPickup();
				storeBucketAtClosestStorageLocation();
				return true;
			}
		}

		bucketbot.<BucketbotTask>assignTask(BucketbotTask.createTaskTAKE_BUCKET_TO_LETTER_STATION(reservedBucket, targetLetter, targetLetterStation));
		return true;
	}
	
	boolean deliver_mode = true;
	
	/**Bucketbots should call requestNewTask of their corresponding BucketbotAgent when they are idle and have no tasks
	 */
	public void requestNewTask(Bucketbot r) {
		manager = SimulationWorldGreedyTaskAllocation.getSimulationWorld().bucketbotManager;
		map = SimulationWorldGreedyTaskAllocation.getSimulationWorld().map;
		waypointGraph = SimulationWorldGreedyTaskAllocation.getSimulationWorld().waypointGraph;

		if(deliver_mode) {
			if(doDeliverLetterTask())
				return;
			
			if(doPickupLetterTask()) {
				deliver_mode = false;
				return;
			}
		}
		else {
			if(doPickupLetterTask())
				return;
			
			if(doDeliverLetterTask()) {
				deliver_mode = true;
				return;
			}
		}

		getOutOfTheWay();		
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.waypointgraph.BucketbotManager#bucketPickedUp(alphabetsoup.framework.Bucketbot, alphabetsoup.framework.Bucket)
	 */
	public void bucketPickedUp(Bucketbot r, Bucket b) {
		//free storage location
		manager.unusedBucketStorageLocations.add(manager.usedBucketStorageLocations.get(b) );
		manager.usedBucketStorageLocations.remove(b);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.waypointgraph.BucketbotManager#bucketSetDown(alphabetsoup.framework.Bucketbot, alphabetsoup.framework.Bucket, alphabetsoup.waypointgraph.Waypoint)
	 */
	public void bucketSetDown(Bucketbot r, Bucket b, Waypoint w) {
		//free the bucket
		if(b == reservedBucket) {
			manager.unusedBuckets.add(reservedBucket);
			manager.usedBuckets.remove(reservedBucket);
			reservedBucket = null;
		}
		
		if(w == reservedStorage)
			reservedStorage = null;
	}
	
	/**Bucketbots should call their corresponding BucketbotAgent's taskComplete when an assigned task has been completed
	 * @param r bucketbot calling the function 
	 * @param t task which was completed -implementations may use any object types as a task
	 */
	public void taskComplete(Bucketbot r, BucketbotTask t) {
		if(t == null)
			return;
		bucketbot.assignTask(null);
		
		switch(t.getTaskType()) {
		case TAKE_BUCKET_TO_WORD_STATION:
			targetLetter = null;
			targetWord = null;
			targetWordStation = null;
			letterToDropOff = null;
			break;
		case TAKE_BUCKET_TO_LETTER_STATION:
			targetLetter = null;
			targetLetterStation = null;
			break;
		}
	}
	
	/**Bucketbots should call their corresponding BucketbotAgent's taskAborted when an assigned task has been aborted
	 * @param r bucketbot calling the function 
	 * @param t task which was aborted -implementations may use any object types as a task
	 */
	public void taskAborted(Bucketbot r, BucketbotTask t) {
		if(t == null)
			return;

		if(reservedBucket != null && bucketbot.getBucket() == null) {
			manager.unusedBuckets.add(reservedBucket);
			manager.usedBuckets.remove(reservedBucket);
		}
		reservedBucket = bucketbot.getBucket();
		
		//if still have reserved storage, release the reserved storage
		if(reservedStorage != null) {
			manager.unusedBucketStorageLocations.add(reservedStorage);
			manager.usedBucketStorageLocations.remove(reservedStorage);
			reservedStorage = null;
		}
			
		switch(t.getTaskType()) {
		case TAKE_BUCKET_TO_LETTER_STATION:
			//if didn't take letter, then put letter back in available list
			if(reservedBucket != null && !reservedBucket.containsLetter(targetLetter))
				manager.availableLetters.add(new BucketbotGlobalResources.LetterStationPickupRequest(targetLetter, targetLetterStation));
			targetLetter = null;
			targetLetterStation = null;
			letterToDropOff = null;
			break;
		case TAKE_BUCKET_TO_WORD_STATION:
			//if didn't give away letter, then put letter back in task list
			if(reservedBucket != null && reservedBucket.containsLetter(targetLetter))
				manager.openLetterRequests.add(new BucketbotGlobalResources.WordStationDeliveryRequest(targetLetter, targetWord, targetWordStation));
			targetLetter = null;
			targetWord = null;
			targetWordStation = null;
			break;
		}
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

	}
}
