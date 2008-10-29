/**
 * 
 */
package alphabetsoup.framework;

import java.util.*;

/**LetterStation defines the interface that letter stations must provide to operate
 * in AlphabetSoup.
 * @author Chris Hazard
 */
public interface LetterStation {
	
	/**Returns the maximum capacity of the LetterStation in terms of number of Letter bundles
	 * @return maximum number of Letter bundles LetterStation can store
	 */
	public int getCapacity();
	
	/**Returns the number of identical Letters that are delivered to the LetterStation (bundle) at the same time 
	 * @return number of letters per bundle
	 */
	public int getBundleSize();
	
	/**Adds a bundle of the given Letter to the LetterStation 
	 * @param l Letter tile type which makes up bundle
	 */
	public void addBundle(Letter l);
	
	/**Returns a Collection of all the letter bundles currently being held at the LetterStation
	 * @return Collection of assigned letter bundles
	 */
	Collection<Letter> getAssignedLetters();
	
	/**Requests that the LetterStation transfer the Letters to the Bucket
	 * @param bb Bucketbot that is carrying the bucket where the letters should be placed (if the within range)
	 * @param l References to the letter that should be placed in the Bucket
	 */
	public void requestLetter(Bucketbot bb, Letter l);

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

	/**
	 * @return Returns the total number of Letters requested
	 */
	public int getNumLettersRequested();
	
	/**
	 * @return Returns the idleTime.
	 */
	public double getIdleTime();
	
	/**
	 * @return Returns the letterToBucketTime.
	 */
	public float getLetterToBucketTime();
	
	/**Clears the statistics kept.
	 */
	public void resetStatistics();
}
