/**
 * 
 */
package alphabetsoup.base;

import java.util.*;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Circle;
import alphabetsoup.framework.Letter;

/**BucketBase is the basic and fully capable implementation of a passive Bucket.
 * BucketBase manages its contents, but performs no actions itself. 
 * @author Chris Hazard
 */
public class BucketBase extends Circle implements Bucket {
	private int capacity;
	protected HashSet<Letter> letters;
	
	public BucketBase(float bucket_radius, int bucket_capacity) {
		super(bucket_radius);
		capacity = bucket_capacity;
		letters = new HashSet<Letter>();
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucket#getCapacity()
	 */
	public int getCapacity() {
		return capacity;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucket#getLetters()
	 */
	public Collection<Letter> getLetters() {
		return letters;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucket#addLetter(alphabetsoup.framework.Letter)
	 */
	public void addLetter(Letter l) {
		if(letters.size() < capacity)
			letters.add(l);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucket#removeLetter(alphabetsoup.framework.Letter)
	 */
	public void removeLetter(Letter l) {
		letters.remove(l);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucket#containsLetter(alphabetsoup.framework.Letter)
	 */
	public boolean containsLetter(Letter l) {
		return letters.contains(l);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Bucket#containsMatchingLetter(alphabetsoup.framework.Letter)
	 */
	public Letter containsMatchingLetter(Letter l) {
		for(Letter m : letters)
			if(l.doesMatch(m))
				return m;
		return null;
	}
	
	/**Returns a list of strings containing detailed information about the current state of the letter station.
	 * Override to use.
	 * @return
	 */
	public List<String> getAdditionalInfo() {
		return new ArrayList<String>();
	}
	
	public void resetStatistics() {
		
	}
}
