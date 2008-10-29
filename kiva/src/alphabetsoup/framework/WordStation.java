/**
 * 
 */
package alphabetsoup.framework;

import java.util.List;


/**WordStation defines the interface that word stations must provide to operate
 * in AlphabetSoup.
 * @author Chris Hazard
 */
public interface WordStation {
	
	/**Returns the maximum capacity of the WordStation in terms of number of Words
	 * it can have simultaneously
	 * @return maximum number of Words WordStation can store
	 */
	public int getCapacity();
	
	/**Assigns a new word to the WordStation, as long as it has the free capacity to work
	 * on another word.  
	 * @param w Word to assign
	 */
	public void assignWord(Word w);
	
	/**Returns a List of all the incomplete Words currently being held at the WordStation
	 * @return List of assigned Words
	 */
	public List<Word> getAssignedWords();

	/**Requests that the WordStation transfer the Letters from the Bucket to the WordStation
	 * and placed into Words.
	 * @param bb Bucketbot carying the bucket from which the letters should be taken from (if within range)
	 * @param l Reference to the Letter that should be taken from the Bucket
	 * @param w Word that the Letter should be placed into.  If null, then it will chose the first Word that requires the letter. 
	 */
	public void requestLetterTake(Bucketbot bb, Letter l, Word w);

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
	 * @return Returns the bucketToLetterTime.
	 */
	public float getBucketToLetterTime();
	
	/**
	 * @return Returns the wordCompletionTime.
	 */
	public float getWordCompletionTime();
	
	/**Clears the statistics kept.
	 */
	public void resetStatistics();
}
