/**
 * 
 */
package alphabetsoup.waypointgraph;

import java.util.*;

import alphabetsoup.framework.*;

/**
 * @author Chris Hazard
 *
 */
public class GenerateWaypointGraph {
	
	/**Moves the LetterStations evenly across the left side, WordStations evenly across the right side,
	 * and randomly distributes buckets and bucketbots. 
	 */
	static public HashMap<Waypoint, Bucket> initializeSparseRandomLayout(SimulationWorld sw, WaypointGraph waypointGraph) {
		
		HashMap<Waypoint, Bucket> bucket_storage_locations = new HashMap<Waypoint, Bucket>(); 
		
		//create a list to place all circles in to test later on (to eliminate any overlap)
		List<Circle> circles = new ArrayList<Circle>();
		
		float bucketbot_radius = sw.bucketbots[0].getRadius();
		
		//spread letter stations evenly across on the left side
		for(int i = 0; i < sw.letterStations.length; i++ ) {
			Circle c = (Circle) sw.letterStations[i];
			c.setInitialPosition(Math.max(c.getRadius(), bucketbot_radius), (i + 1) * sw.map.getHeight() / (1 + sw.letterStations.length) );
			circles.add(c);
			sw.map.addLetterStation(sw.letterStations[i]);
		}
		
		//spread word stations evenly across on the right side
		for(int i = 0; i < sw.wordStations.length; i++ ) {
			Circle c = (Circle) sw.wordStations[i];
			c.setInitialPosition(sw.map.getWidth() - Math.max(c.getRadius(), bucketbot_radius), (i + 1) * sw.map.getHeight() / (1 + sw.wordStations.length) );
			circles.add(c);
			sw.map.addWordStation(sw.wordStations[i]);
		}
		
		//find area to put buckets within
		float placeable_width = sw.map.getWidth() - sw.wordStations[0].getRadius() - sw.letterStations[0].getRadius() - 10 * sw.bucketbots[0].getRadius();
		float placeable_height = sw.map.getHeight() - 2 * sw.bucketbots[0].getRadius();

		//find area to store bucket that will allow all buckets to be placed
		// want to have one more location on each side so that the grid can go around it
		int width_count = (int)(placeable_width / Math.sqrt(placeable_width * placeable_height / (sw.buckets.length)));
		int height_count = (int)Math.ceil((float)(sw.buckets.length)/width_count);
		width_count += 3;
		height_count += 3;
		//find size of each side of grid block
		float grid_link_width = placeable_width / width_count;
		float grid_link_height = placeable_height / height_count;

		//get offset of grid with respect to the map
		float x_start = (sw.map.getWidth() - placeable_width + grid_link_width) / 2;
		float y_start = (sw.map.getHeight() - placeable_height + grid_link_height) / 2;
		
		//create all the waypoint nodes for the grid
		Waypoint[][] grid = new Waypoint[height_count][width_count];
		for(int i = 0; i < height_count; i++) {
			for(int j = 0; j < width_count; j++) {
				grid[i][j] = new Waypoint(x_start + j*grid_link_width, y_start + i*grid_link_height, false);
				waypointGraph.addWaypoint(grid[i][j]);
				
				//as long as not a first node, connect back to previous spots
				if(j > 0)
					grid[i][j].addBidirectionalPath(grid[i][j-1]);
				if(i > 0)
					grid[i][j].addBidirectionalPath(grid[i-1][j]);
			}
		}
		
		double max_connection_angle = Math.PI/3;
		//connect edges of grid to stations
		for(LetterStation ls : sw.letterStations) {
			//create a, b, c points, where a is above the station, b is at, c is below,
			// so each station has its own little queue
			Waypoint wa = new Waypoint(ls.getX(), ls.getY() + 2.5f*ls.getRadius(), false);
			Waypoint wb = new Waypoint(ls);
			Waypoint wc = new Waypoint(ls.getX(), ls.getY() - 2.5f*ls.getRadius(), false);
			waypointGraph.addWaypoint(wa);
			waypointGraph.addWaypoint(wb);
			waypointGraph.addWaypoint(wc);
			for(int i = 0; i < height_count; i++) {
				double angle = Math.atan2(grid[i][0].getY() - wa.getY(), grid[i][0].getX() - wa.getX()); 
				if(angle > -max_connection_angle && angle < max_connection_angle)
					grid[i][0].addPath(wa);
			}
			for(int i = 0; i < height_count; i++) {
				double angle = Math.atan2(grid[i][0].getY() - wc.getY(), grid[i][0].getX() - wc.getX()); 
				if(angle > -max_connection_angle && angle < max_connection_angle)
					wc.addPath(grid[i][0]);
			}
			wa.addPath(wb);
			wb.addPath(wc);
		}
		for(WordStation ws : sw.wordStations) {
			Waypoint wa = new Waypoint(ws.getX(), ws.getY() + 2.5f*ws.getRadius(), false);
			Waypoint wb = new Waypoint(ws);
			Waypoint wc = new Waypoint(ws.getX(), ws.getY() - 2.5f*ws.getRadius(), false);
			waypointGraph.addWaypoint(wa);
			waypointGraph.addWaypoint(wb);
			waypointGraph.addWaypoint(wc);
			for(int i = 0; i < height_count; i++) {
				double angle = Math.atan2(wa.getY() - grid[i][width_count-1].getY(), wa.getX() - grid[i][width_count-1].getX()); 
				if(angle > -max_connection_angle && angle < max_connection_angle)
					grid[i][width_count-1].addPath(wa);
			}
			for(int i = 0; i < height_count; i++) {
				double angle = Math.atan2(wc.getY() - grid[i][width_count-1].getY(), wc.getX() - grid[i][width_count-1].getX()); 
				if(angle > -max_connection_angle && angle < max_connection_angle)
					wc.addPath(grid[i][width_count-1]);
			}
			wa.addPath(wb);
			wb.addPath(wc);
		}

		
		//put buckets in the spots between the grids, and add storage locations
		int x_index = 1;
		int y_index = 1;
		for(Bucket b : sw.buckets) {
			//place bucket
			((Circle)b).setInitialPosition(x_start + (x_index + 0.5f) * grid_link_width,
					y_start + (y_index + 0.5f) * grid_link_height);
			circles.add((Circle)b);
			
			//add destinationWaypoint, and connect up to grid
			Waypoint w = new Waypoint(b);
			waypointGraph.addWaypoint(w);
			w.addBidirectionalPath(grid[y_index][x_index]);
			w.addBidirectionalPath(grid[y_index][x_index+1]);
			w.addBidirectionalPath(grid[y_index+1][x_index]);
			w.addBidirectionalPath(grid[y_index+1][x_index+1]);

			bucket_storage_locations.put(w, b);

			x_index++;
			//wrap around the end, when run out of width room
			if(x_index >= width_count-2) {
				x_index = 1;
				y_index++;
			}
		}

		//add the remaining empty storage locations to the graph and BucketStorageAgent
		while(y_index < height_count-2) {
			
			//add destinationWaypoint, and connect up to grid
			Waypoint w = new Waypoint(x_start + (x_index + 0.5f) * grid_link_width,
					y_start + (y_index + 0.5f) * grid_link_height, true);
			waypointGraph.addWaypoint(w);
			w.addBidirectionalPath(grid[y_index][x_index]);
			w.addBidirectionalPath(grid[y_index][x_index+1]);
			w.addBidirectionalPath(grid[y_index+1][x_index]);
			w.addBidirectionalPath(grid[y_index+1][x_index+1]);
			
			bucket_storage_locations.put(w, null);
			
			x_index++;
			//wrap around the end, when run out of width room
			if(x_index >= width_count-2) {
				x_index = 1;
				y_index++;
			}
		}
	
		//keep track of bucket bots to add
		List<Circle> bucketbots_to_add = new ArrayList<Circle>();
		for(Bucketbot r: sw.bucketbots)		bucketbots_to_add.add((Circle)r);
		
		//set up random locations for buckets and bucketbots, making sure they don't collide
		MersenneTwisterFast rand = SimulationWorld.rand;
		for(Circle c : bucketbots_to_add)
		{
			boolean collision;
			float new_x, new_y;
			do {
				new_x = rand.nextFloat() * (sw.map.getWidth() - 2*c.getRadius()) + c.getRadius();
				new_y = rand.nextFloat() * (sw.map.getHeight() - 2*c.getRadius()) + c.getRadius();

				collision = false;
				for(Circle d : circles)
					if(collision = d.IsCollision(new_x, new_y, c.getRadius()))
						break;
			} while(collision);
			c.setInitialPosition(new_x, new_y);
			circles.add(c);
		}
		
		//initialize bucketbots and buckets
		//(once this is done, their positions may no longer be directly written to)
		for(Bucket b : sw.buckets)			sw.map.addBucket(b);
		for(Bucketbot r: sw.bucketbots)	sw.map.addRobot(r);
		
		return bucket_storage_locations;
	}
	
	
	/**Moves the LetterStations evenly across the left side, WordStations evenly across the right side,
	 * and randomly distributes buckets and bucketbots. 
	 */
	static public HashMap<Waypoint, Bucket> initializeCompactRandomLayout(SimulationWorld sw, WaypointGraph waypointGraph) {
		
		HashMap<Waypoint, Bucket> bucket_storage_locations = new HashMap<Waypoint, Bucket>(); 
		
		//create a list to place all circles in to test later on (to eliminate any overlap)
		List<Circle> circles = new ArrayList<Circle>();
		
		float bucketbot_radius = sw.bucketbots[0].getRadius();
		
		//spread letter stations evenly across on the left side
		for(int i = 0; i < sw.letterStations.length; i++ ) {
			Circle c = (Circle) sw.letterStations[i];
			c.setInitialPosition(Math.max(c.getRadius(), bucketbot_radius), (i + 1) * sw.map.getHeight() / (1 + sw.letterStations.length) );
			circles.add(c);
			sw.map.addLetterStation(sw.letterStations[i]);
		}
		
		//spread word stations evenly across on the right side
		for(int i = 0; i < sw.wordStations.length; i++ ) {
			Circle c = (Circle) sw.wordStations[i];
			c.setInitialPosition(sw.map.getWidth() - Math.max(c.getRadius(), bucketbot_radius), (i + 1) * sw.map.getHeight() / (1 + sw.wordStations.length) );
			circles.add(c);
			sw.map.addWordStation(sw.wordStations[i]);
		}
		
		//find area to put buckets within
		float placeable_width = sw.map.getWidth() - sw.wordStations[0].getRadius() - sw.letterStations[0].getRadius() - 8 * sw.bucketbots[0].getRadius();
		float placeable_height = sw.map.getHeight() - 2 * sw.bucketbots[0].getRadius();
		
		float block_size = 2.0f * sw.buckets[0].getRadius() + 2 * sw.map.getTolerance();
		
		int width_count = (int)(placeable_width / block_size);
		int height_count = (int)(placeable_height / block_size);

		//get offset of grid with respect to the map
		float x_start = (sw.map.getWidth() - placeable_width + block_size) / 2;
		float y_start = (sw.map.getHeight() - placeable_height + block_size) / 2;
		
		int bucket_block_length = 5;
		
		//create all the waypoint nodes for the grid
		Waypoint[][] grid = new Waypoint[height_count][width_count];
		for(int i = 0; i < height_count; i++) {
			for(int j = 0; j < width_count; j++) {
				grid[i][j] = new Waypoint(x_start + j*block_size, y_start + i*block_size, false);
				waypointGraph.addWaypoint(grid[i][j]);
				
				//connect horizontally
				if(j > 0) { //don't connect if first node
					//connect based on the row
					switch(i % 10) {
					case 0:	case 4:
						grid[i][j].addPath(grid[i][j-1]);	break;
					case 1:	case 7:
						grid[i][j-1].addPath(grid[i][j]);	break;
					default:
						grid[i][j].addBidirectionalPath(grid[i][j-1]);
						//make sure it's not on the edge, and leave gaps
						if(j > 1 && j < width_count - 2
								&& j % bucket_block_length != 0) {
							grid[i][j].setBucketStorageLocation();
							bucket_storage_locations.put(grid[i][j], null);
						}
						break;
					}
				}
				
				//connect vertically
				if(i > 0) { //don't connect if first node
					//grid[i][j].addBidirectionalPath(grid[i-1][j]);
					
					if(j == 0)
						grid[i-1][j].addPath(grid[i][j]);
					else if(j == 1)
						grid[i][j].addPath(grid[i-1][j]);
					else if(j == width_count - 2)
						grid[i-1][j].addPath(grid[i][j]);
					else if(j == width_count - 1)
						grid[i][j].addPath(grid[i-1][j]);
					else { //need to check if open isle
						int column_remainder = j % (2 * bucket_block_length);
						if(column_remainder == 0)
							grid[i][j].addPath(grid[i-1][j]);
						else if(column_remainder == bucket_block_length)
							grid[i-1][j].addPath(grid[i][j]);
						else
							grid[i][j].addBidirectionalPath(grid[i-1][j]);
					}
						
				}
			}
		}
		
		//put buckets on storage locations
		ArrayList<Waypoint> storage_locations = new ArrayList<Waypoint>();
		storage_locations.addAll(bucket_storage_locations.keySet());
		for(int i = 0; i < sw.buckets.length; i++) {
			Bucket b = sw.buckets[i];
			Waypoint w = storage_locations.get(i);
			
			((Circle)b).setInitialPosition(w.getX(), w.getY());
			circles.add((Circle)b);

			waypointGraph.bucketSetdown(b, w);
			bucket_storage_locations.put(w, b);
		}
		
		//connect edges of grid to stations
		for(LetterStation ls : sw.letterStations) {
			//create a, b, c points, where a is above the station, b is at, c is below,
			// so each station has its own little queue
			Waypoint wa = new Waypoint(ls.getX(), ls.getY() - 2.5f*ls.getRadius(), false);
			Waypoint wb = new Waypoint(ls);
			Waypoint wc = new Waypoint(ls.getX(), ls.getY() + 2.5f*ls.getRadius(), false);
			waypointGraph.addWaypoint(wa);
			waypointGraph.addWaypoint(wb);
			waypointGraph.addWaypoint(wc);
			
			Waypoint closest = null;
			float closest_distance = Float.POSITIVE_INFINITY; 
			for(int i = 0; i < height_count; i++) {
				float dist = grid[i][0].getDistance(wa);
				if(dist < closest_distance) {
					closest_distance = dist;
					closest = grid[i][0];
				}
			}
			closest.addPath(wa, closest_distance);
			
			closest = null;
			closest_distance = Float.POSITIVE_INFINITY;
			for(int i = 0; i < height_count; i++) {
				float dist = grid[i][0].getDistance(wc);
				if(dist < closest_distance) {
					closest_distance = dist;
					closest = grid[i][0];
				}
			}
			wc.addPath(closest, closest_distance);

			wa.addPath(wb, wa.getDistance(wb));
			wb.addPath(wc, wb.getDistance(wc));
		}
		
		for(WordStation ws : sw.wordStations) {
			Waypoint wa = new Waypoint(ws.getX(), ws.getY() + 2.5f*ws.getRadius(), false);
			Waypoint wb = new Waypoint(ws);
			Waypoint wc = new Waypoint(ws.getX(), ws.getY() - 2.5f*ws.getRadius(), false);
			waypointGraph.addWaypoint(wa);
			waypointGraph.addWaypoint(wb);
			waypointGraph.addWaypoint(wc);
			
			Waypoint closest = null;
			float closest_distance = Float.POSITIVE_INFINITY; 
			for(int i = 0; i < height_count; i++) {
				float dist = grid[i][width_count-1].getDistance(wa);
				if(dist < closest_distance) {
					closest_distance = dist;
					closest = grid[i][width_count-1];
				}
			}
			closest.addPath(wa, closest_distance);
			
			closest = null;
			closest_distance = Float.POSITIVE_INFINITY;
			for(int i = 0; i < height_count; i++) {
				float dist = grid[i][width_count-1].getDistance(wc);
				if(dist < closest_distance) {
					closest_distance = dist;
					closest = grid[i][width_count-1];
				}
			}
			wc.addPath(closest, closest_distance);

			wa.addPath(wb, wa.getDistance(wb));
			wb.addPath(wc, wb.getDistance(wc));
		}
	
		//keep track of bucketbots to add
		List<Circle> bucketbots_to_add = new ArrayList<Circle>();
		for(Bucketbot r: sw.bucketbots)		bucketbots_to_add.add((Circle)r);
		
		//set up random locations for buckets and bucketbots, making sure they don't collide
		MersenneTwisterFast rand = SimulationWorld.rand;
		for(Circle c : bucketbots_to_add)
		{
			boolean collision;
			float new_x, new_y;
			do {
				new_x = rand.nextFloat() * (sw.map.getWidth() - 2*c.getRadius()) + c.getRadius();
				new_y = rand.nextFloat() * (sw.map.getHeight() - 2*c.getRadius()) + c.getRadius();

				collision = false;
				for(Circle d : circles)
					if(collision = d.IsCollision(new_x, new_y, c.getRadius()))
						break;
			} while(collision);
			c.setInitialPosition(new_x, new_y);
			circles.add(c);
		}
		
		//initialize bucketbots and buckets
		//(once this is done, their positions may no longer be directly written to)
		for(Bucket b : sw.buckets)			sw.map.addBucket(b);
		for(Bucketbot r: sw.bucketbots)	sw.map.addRobot(r);
		
		return bucket_storage_locations;
	}

}
