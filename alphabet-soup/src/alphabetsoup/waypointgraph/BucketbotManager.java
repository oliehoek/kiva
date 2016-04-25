/**
 * 
 */
package alphabetsoup.waypointgraph;

import alphabetsoup.base.BucketbotTask;
import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Bucketbot;

/**
 * @author Chris Hazard
 *
 */
public interface BucketbotManager {
	
	/**Called when a Bucketbot successfully picks a Bucket up 
	 * @param r Bucketbot that picked up the Bucket
	 * @param b Bucket that was picked up
	 */
	public void bucketPickedUp(Bucketbot r, Bucket b);
	
	/**Called when a Bucketbot successfully sets down a Bucket 
	 * @param r Bucketbot that picked up the Bucket
	 * @param b Bucket that was picked up
	 * @param w Waypoint where the Bucket was set down 
	 */
	public void bucketSetDown(Bucketbot r, Bucket b, Waypoint w);
	
	/**Bucketbot requests a new task (currently idle)
	 * @param r
	 */
	public void requestNewTask(Bucketbot r);
	
	/**Called when a Bucketbot has successfully completed the task 
	 * @param r
	 * @param t
	 */
	public void taskComplete(Bucketbot r, BucketbotTask t);
	
	/**Called when a Bucketbot has failed the task
	 * @param r
	 * @param t
	 */
	public void taskAborted(Bucketbot r, BucketbotTask t);

}
