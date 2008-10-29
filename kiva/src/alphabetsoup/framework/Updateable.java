/**
 * 
 */
package alphabetsoup.framework;

/**Updateable is an interface which should be implemented by any agent in the system.
 * Here, we define agent to be any entity that can act or perform any actions on its own.
 * @author Chris Hazard
 */
public interface Updateable {

	/**Gets the time of the earliest scheduled or possible event
	 * @param cur_time the current system time
	 * @return the absolute time of the soonest possible event.  Double.POSITIVE_INFINITY is valid 
	 * return value if no events are scheduled, and no known collisions or pending events are known
	 * to happen.
	 */
	public double getNextEventTime(double cur_time);
	
	/**Updates the object up to the current time, performing any actions
	 * @param last_time absolute time all objects were last updated (used as a point of reference)
	 * @param cur_time current system time to update to
	 */
	public void update(double last_time, double cur_time);
}
