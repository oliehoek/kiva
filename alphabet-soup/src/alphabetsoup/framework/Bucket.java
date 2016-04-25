/**
 * 
 */
package alphabetsoup.framework;

import java.util.*;

/**Bucket defines the interface that buckets must implement in order to properly interact
 * within AlphabetSoup.
 * @author Chris Hazard
 */
public interface Bucket {
	/**Returns the maximum capacity of the Bucket in terms of number of Letter tiles
	 * @return maximum number of Letter tiles bucket can store
	 */
	public int getCapacity();
	
	/**Returns a Collection of the Letters currently in the Bucket
	 * @return Collection of Letters in the bucket
	 */
	public Collection<Letter> getLetters();
	
	/**Adds a letter to the contents of the bucket.  If the bucket is already at capacity,
	 * it should not be added.
	 * @param l reference of Letter to add to bucket
	 */
	public void addLetter(Letter l);
	
	/**Removes a Letter from the contents of the bucket.  The specified letter should be a reference
	 * to a letter currently contained in the Bucket.
	 * @param l Reference to a Letter currently in the Bucket to be removed 
	 */
	public void removeLetter(Letter l);
	
	/**Returns true if the Bucket contains the specific reference of the Letter specified 
	 * @param l reference of a Letter to see if it is in the bucket
	 * @return true if Letter is contained in the Bucket
	 */
	public boolean containsLetter(Letter l);
	
	/**Returns a matching letter within the Bucket if the Bucket contains a letter that
	 * matches (color, letter) the Letter specified 
	 * @param l reference of a Letter to see a match is in the bucket
	 * @return a letter reference to a match contained in the Bucket, null if no matches are found
	 */
	public Letter containsMatchingLetter(Letter l);

	/**
	 * @return Returns the x.
	 */
	public float getX();

	/**
	 * @return Returns the y.
	 */
	public float getY();
	
	/**
	 * @return Returns the radius.
	 */
	public float getRadius();
	
	/**Clears the statistics kept.
	 */
	public void resetStatistics();
}
