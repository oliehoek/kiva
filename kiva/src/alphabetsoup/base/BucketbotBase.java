/**
 * 
 */
package alphabetsoup.base;

import java.util.*;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Bucketbot;
import alphabetsoup.framework.Circle;
import alphabetsoup.framework.Map;
import alphabetsoup.framework.SimulationWorld;
import alphabetsoup.framework.Updateable;

/**BucketbotBase provides the basic functionality for a passive bucketbot.  It contains all of the
 * mechanisms to interact with other entities, as well as the basic physics,
 * but does not have logic to control them.  BucketbotBase should be extended to include logic.
 * @author Chris Hazard
 */
public class BucketbotBase extends Circle implements Bucketbot, Updateable {
	
	private float bucketPickupSetdownTime;
	private float maxAcceleration;
	private float maxVelocity;
	private float collisionPenaltyTime;

	private Bucket bucket = null;
	
	private float direction;		//current direction in radians
	
	private float xVelocity;
	private float yVelocity;
	
	private float targetXVelocity;
	private float targetYVelocity;
	
	//timing information
	private double blockedUntil;	//time when bucketbot will be done performing a blocking action
									// (such as picking up a bucket)
	private double accelerateUntil;	//time when bucketbot will be done [ac|de]celerating to [maxVelocity|0]
	public double cruiseUntil;	//freely setable by anything -how long the bucketbot plans to cruise for
	private double minUntil;		//minimum of *Until times -set in getNextEventTime

	public double curTime;		//current time of last action
	
	private BucketbotTask currentTask;
	
	public boolean drawBolded = false;
	
	public float frustration = 0.0f;	//0.0->1.0 for maximal frustration
	
	//statistics data
	private int numPickups;
	private int numSetdowns;
	private double distanceTraveled;
	private int numCollisions;
	
	private HashMap<String, Double> totalTimes;	//amount of time in each state/task
	private double taskStartTime = 0.0;
	
	public void resetStatistics() {
		numPickups = 0;
		numSetdowns = 0;
		distanceTraveled = 0.0;
		numCollisions = 0;
		totalTimes = new HashMap<String, Double>();
		taskStartTime = curTime;
	}

	/**BucketbotState is interface which should be inherited to provied states for the
	 * Bucketbot to be in.  BucketbotBase keeps track of the fraction of time
	 * in each state, and also maintains the stateQueue.
	 * @author Chris Hazard
	 */
	public interface BucketbotState {
		/**Returns the state name as will be kept track in the global statistics
		 * @return the state name
		 */
		public String getStateName();
		/**act will be called whenever the Bucketbot is not blocked or waiting for an
		 * event to finish.
		 * @param self a reference to the Bucketbot object
		 */
		public void act(BucketbotBase self);
	};
	public List<BucketbotState> stateQueue = new ArrayList<BucketbotState>(); 
	
	public BucketbotBase(float bucketbot_radius, float bucket_pickup_setdown_time,
			float bucketbot_max_acceleration, float bucketbot_max_velocity, float collision_penalty_time) {
		super(bucketbot_radius);
		
		resetStatistics();
		
		bucketPickupSetdownTime = bucket_pickup_setdown_time;
		maxAcceleration = bucketbot_max_acceleration;
		maxVelocity = bucketbot_max_velocity;
		collisionPenaltyTime = collision_penalty_time;
		
		direction = 0.0f;
		targetXVelocity = 0.0f;
		targetYVelocity = 0.0f;

		xVelocity = 0.0f;
		yVelocity = 0.0f;
		
		blockedUntil = -1.0;
		accelerateUntil = -1.0;
		cruiseUntil = -1.0;
	}
	
	/**returns the difference of two angles, positive if angle2 is to the left of angle1,
	 * negative if angle2 is to the right of angle1
	 * @param angle1
	 * @param angle2
	 * @return difference of angle2-angle1, normalized to [0,pi]
	 */
	public float angleDifference(float angle1, float angle2) {
		double relative_direction = angle2 - angle1;
		if(relative_direction >= Math.PI)
			relative_direction -= 2*Math.PI;
		else if(relative_direction <= -Math.PI)
			relative_direction += 2*Math.PI;
		return (float)relative_direction;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#assignTask(alphabetsoup.base.BucketbotTask)
	 */
	public <T> void assignTask(T tt) {		
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#pickupBucket(alphabetsoup.framework.Bucket)
	 */
	final public boolean pickupBucket(Bucket b) {
		//can't pick up a bucket if already carying one
		if(bucket != null)
			return false;
		
		//can't set down while moving
		if(getSpeed() > 0.0f)
			return false;
		
		Map map = SimulationWorld.getSimulationWorld().getMap();
		
		//if outside of tolerance range, can't pick it up
		if( (b.getX()-getX())*(b.getX()-getX()) + (b.getY()-getY())*(b.getY()-getY())
				> map.getTolerance()*map.getTolerance())
			return false;
		
		//see if bucketbot can adjust position to pick it up
		if(!map.moveBucketbot(this, b.getX(), b.getY()))
			return false;		
	
		//pick it up!
		bucket = b;
		blockedUntil = curTime + bucketPickupSetdownTime;
		numPickups++;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#setdownBucket()
	 */
	final public boolean setdownBucket() {
		if(bucket == null)
			return false;
		//can't set down while moving
		if(getSpeed() > 0.0f)
			return false;
		if(bucket != null) {
			blockedUntil = curTime + bucketPickupSetdownTime;
			numSetdowns++;
		}
		bucket = null;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#waitUntil(double time)
	 */
	final public void waitUntil(double time) {
		blockedUntil = Math.max(time, blockedUntil);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) {
		//if not waiting on anything, then not planning on moving in the near future
		// -only if bucketbot has nothing to do
		if(cur_time >= blockedUntil && cur_time >= accelerateUntil
				&& cur_time >= cruiseUntil) {
			return Double.POSITIVE_INFINITY;
		}
		else { //return soonest event that has not happened yet
			minUntil = Double.POSITIVE_INFINITY;
			if(blockedUntil > cur_time)		minUntil = Math.min(blockedUntil,		minUntil);
			if(accelerateUntil > cur_time)	minUntil = Math.min(accelerateUntil,	minUntil);
			if(cruiseUntil > cur_time)		minUntil = Math.min(cruiseUntil, 		minUntil);
			return minUntil;
		}
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) {
		if(cur_time < blockedUntil)
			return;
		curTime = cur_time;
		double time_delta = (cur_time - last_time);

		//use old velocities to clamp acceleration
		float old_x_velocity = xVelocity;
		float old_y_velocity = yVelocity;

		//set new velocity
		xVelocity = targetXVelocity;
		yVelocity = targetYVelocity;

		//clamp velocity
		if(xVelocity*xVelocity + yVelocity*yVelocity > maxVelocity*maxVelocity) {
			float velocity_magnitude = (float)Math.sqrt(xVelocity*xVelocity + yVelocity*yVelocity);
			xVelocity = maxVelocity * (xVelocity/velocity_magnitude);
			yVelocity = maxVelocity * (yVelocity/velocity_magnitude);
		}

		//clamp acceleration
		double x_accel = (float)( (xVelocity - old_x_velocity) / time_delta );
		double y_accel = (float)( (yVelocity - old_y_velocity) / time_delta );
		if( x_accel*x_accel + y_accel*y_accel > maxAcceleration*maxAcceleration) {
			double accel_magnitude = Math.sqrt(x_accel*x_accel + y_accel*y_accel);
			xVelocity = (float)(old_x_velocity + maxAcceleration*time_delta * (x_accel/accel_magnitude));
			yVelocity = (float)(old_y_velocity + maxAcceleration*time_delta * (y_accel/accel_magnitude));
		}
		
		//calculate movement
		float x_old = getX();
		float y_old = getY();
		float x_new = (float)(xVelocity * time_delta + getX());
		float y_new = (float)(yVelocity * time_delta + getY());

		//try to make move.  if can't move due to a collision, then stop
		if(!SimulationWorld.getSimulationWorld().getMap().moveBucketbot(this, x_new, y_new)) {
			xVelocity = 0.0f;
			yVelocity = 0.0f;
			targetXVelocity = 0.0f;
			targetYVelocity= 0.0f;
			blockedUntil = curTime + collisionPenaltyTime;
			numCollisions++;
			return;
		}
		
		//set moving flags
		if(xVelocity == 0.0f && yVelocity == 0.0f)
			setMoving(false);
		else
			setMoving(true);
		if(getBucket() != null)
			((Circle)getBucket()).setMoving(isMoving());
		
		//count distanceTraveled
		distanceTraveled += Math.sqrt( (x_new-x_old)*(x_new-x_old) + (y_new-y_old)*(y_new-y_old));

		//compute time in previous task and state
		if(currentTask != null) {
			String s = currentTask.getTaskType().toString();
			if(totalTimes.containsKey(s))
				totalTimes.put(s, totalTimes.get(s) + curTime - taskStartTime);
			else
				totalTimes.put(s, curTime - taskStartTime);
		}
		
		if(stateQueue.size() > 0) {
			String s = stateQueue.get(0).getStateName();
			if(totalTimes.containsKey(s))
				totalTimes.put(s, totalTimes.get(s) + curTime - taskStartTime);
			else
				totalTimes.put(s, curTime - taskStartTime);
		}
		
		taskStartTime = curTime;

		if(stateQueue.size() > 0)
			stateQueue.get(0).act(this);
		else
			idle();
	}
	
	
	/**idle is called whenever the stateQueue is empty,
	 * and should attempt to allocate tasks for the bucketbot
	 */
	public void idle() {

	}

	/**
	 * @return Returns the direction in radians.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getDirection()
	 */
	public float getDirection() {
		return direction;
	}
	
	/**
	 * @param dir The direction to set in radians.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#setDirection(float)
	 */
	public void setDirection(float dir) {		
		//clamp to [0, 2*pi)
		if(dir >= 2 * Math.PI)
			dir -= (2*Math.PI * Math.floor(dir / (2 * Math.PI) )); 
		else if(dir < 0)
			dir += (2*Math.PI * Math.ceil(-dir / (2 * Math.PI) ));
		direction = dir;
		
		setTargetSpeed(getTargetSpeed());
	}
	
	/**
	 * @return Returns the bucket.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getBucket()
	 */
	public Bucket getBucket() {
		return bucket;
	}

	/**
	 * @return Returns the speed.
	 */
	public float getSpeed() {
		return (float)Math.sqrt(xVelocity*xVelocity + yVelocity*yVelocity);
	}
	
	/**
	 * @return Returns the targetSpeed.
	 */
	public float getTargetSpeed() {
		return (float)Math.sqrt(targetXVelocity*targetXVelocity + targetYVelocity*targetYVelocity);
	}
	
	/**Returns the difference in velocity between the current velocity and target velocity. 
	 * @return Returns the difference in velocity.
	 */
	public float getTargetSpeedDifference() {
		return (float)Math.sqrt( (targetXVelocity-xVelocity)*(targetXVelocity-xVelocity)
				+ (targetYVelocity-yVelocity)*(targetYVelocity-yVelocity));
	}
	
	/**Sets the target speed.  Also sets accelerateUntil to be the time
	 * when the Bucketbot will finish accelerating and will reach new_speed. 
	 * @param new_speed target speed value in [0,maxVelocity]
	 */
	public void setTargetSpeed(float new_speed) {
		//clamp to allowed values
		if(new_speed > maxVelocity)
			new_speed = maxVelocity;
		else if(new_speed < 0)
			new_speed = 0;

		targetXVelocity = (float)(new_speed * Math.cos(getDirection()));
		targetYVelocity = (float)(new_speed * Math.sin(getDirection()));

		//find out how long it will take until the desired speed is reached
		accelerateUntil = curTime + getTargetSpeedDifference() / maxAcceleration;
	}
	
	/**Sets the target velocity (and updates the target direction).  Also sets
	 * accelerateUntil to be the time when the Bucketbot will finish accelerating
	 * and will reach the specified velocity.
	 * @param x_velocity desired x component of the velocity
	 * @param y_velocity desired y component of the velocity
	 */
	public void setTargetVelocity(float x_velocity, float y_velocity) {
		targetXVelocity = x_velocity;
		targetYVelocity = y_velocity;
		
		//update direction unless stopping
		if(x_velocity != 0.0f || y_velocity != 0.0f)
			direction = (float)Math.atan2(targetYVelocity, targetXVelocity);
		
		//find out how long it will take until the desired speed is reached
		accelerateUntil = curTime + getTargetSpeedDifference() / maxAcceleration;
	}

	/**
	 * @return Returns the maxVelocity.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getMaxVelocity()
	 */
	public float getMaxVelocity() {
		return maxVelocity;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getNumPickups()
	 */
	public int getNumPickups() {
		return numPickups;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getNumSetdowns()
	 */
	public int getNumSetdowns() {
		return numSetdowns;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getDistanceTraveled()
	 */
	public double getDistanceTraveled() {
		return distanceTraveled;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getNumCollisions()
	 */
	public int getNumCollisions() {
		return numCollisions;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getTotalTimes()
	 */
	public HashMap<String, Double> getTotalTimes() {
		return totalTimes;
	}

	/**
	 * @return Returns the currentTask.
	 */
	public BucketbotTask getCurrentTask() {
		return currentTask;
	}

	/**
	 * @param currentTask The currentTask to set.
	 */
	public void setCurrentTask(BucketbotTask currentTask) {
		this.currentTask = currentTask;
	}

	/**
	 * @return Returns the maxAcceleration.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucketbot#getMaxAcceleration()
	 */
	public float getMaxAcceleration() {
		return maxAcceleration;
	}

	/**
	 * @return Returns the drawBolded.
	 */
	public boolean isDrawBolded() {
		return drawBolded;
	}

	/**
	 * @param drawBolded The drawBolded to set.
	 */
	public void setDrawBolded(boolean drawBolded) {
		this.drawBolded = drawBolded;
	}

	/**
	 * @return Returns the minUntil.
	 */
	public double getMinUntil() {
		return minUntil;
	}

	/**
	 * @return Returns the accelerateUntil.
	 */
	public double getAccelerateUntil() {
		return accelerateUntil;
	}

	/**
	 * @return Returns the targetXVelocity.
	 */
	public float getTargetXVelocity() {
		return targetXVelocity;
	}

	/**
	 * @return Returns the targetYVelocity.
	 */
	public float getTargetYVelocity() {
		return targetYVelocity;
	}

	/**
	 * @return Returns the xVelocity.
	 */
	public float getXVelocity() {
		return xVelocity;
	}

	/**
	 * @return Returns the yVelocity.
	 */
	public float getYVelocity() {
		return yVelocity;
	}

	/**
	 * @return Returns the bucketPickupSetdownTime.
	 */
	public float getBucketPickupSetdownTime() {
		return bucketPickupSetdownTime;
	}

	/**
	 * @return the collisionPenaltyTime
	 */
	public float getCollisionPenaltyTime() {
		return collisionPenaltyTime;
	}
}
