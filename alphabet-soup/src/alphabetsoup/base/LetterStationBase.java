/**
 * 
 */
package alphabetsoup.base;

import java.util.*;

import alphabetsoup.framework.*;

/**LetterStationBase implements the basic functionality of a passive Letter station,
 * such that it can receive letters when given them by the system, and will place letters in buckets
 * when requested to do so.
 * @author Chris Hazard
 */
public class LetterStationBase extends Circle implements LetterStation, Updateable {
	private int capacity;
	private int bundleSize;
	private float letterToBucketTime;
	public HashSet<Letter> assignedLetters = new HashSet<Letter>();
	
	protected double blockedUntilTime;
	
	//statistics
	protected int numLettersRequested;	//number of individual letters requested
	protected double idleTime;
	
	public void resetStatistics() 
	{
		idleTime = 0.0;
		numLettersRequested = 0;
	}
	
	protected static class LetterRequested 
	{
		public LetterRequested(Bucketbot bb, Letter l) {
			bucketbot = bb;	letter = l;
		}
		public Bucketbot bucketbot;
		public Letter letter;		
	}
	List<LetterRequested> lettersRequested = new ArrayList<LetterRequested>();
	
	public LetterStationBase(float station_radius, float letter_to_bucket_time, int bundle_size, int station_capacity) 
	{
		super(station_radius);
		resetStatistics();
		letterToBucketTime = letter_to_bucket_time;
		bundleSize = bundle_size;
		capacity = station_capacity;

		blockedUntilTime = -1.0;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#getCapacity()
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#addBundle(alphabetsoup.framework.Letter)
	 */
	public void addBundle(Letter l) {
		if(assignedLetters.size() < capacity)
			assignedLetters.add(l);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) {
		if(cur_time >= blockedUntilTime)
			return Double.POSITIVE_INFINITY;
		else
			return blockedUntilTime;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) {
		if(cur_time < blockedUntilTime)
			return;
		idleTime += cur_time - last_time;
		giveLetterBundleToBucket(cur_time);
	}
	
	protected LetterRequested giveLetterBundleToBucket(double cur_time) {
		while(lettersRequested.size() > 0) {
			LetterRequested request = lettersRequested.remove(0);
			Bucketbot bb = request.bucketbot; 
			Bucket b = bb.getBucket();
			Letter l = request.letter;

			//can't service the request if the bucket is full or not close enough
			if(b.getLetters().size() + bundleSize > b.getCapacity()
					|| getDistance((Circle)b) > getRadius() )
				continue;
				
			//find matching letter to transfer
			if(assignedLetters.contains(l)) {
				//transfer letter, wait til transfer is complete, remove request
				b.addLetter(l);
				for(int i = 1; i < bundleSize; i++)
					b.addLetter(l.clone());
				assignedLetters.remove(l);
				blockedUntilTime = cur_time + letterToBucketTime;
				bb.waitUntil(blockedUntilTime);
				return request;
			}

			lettersRequested.remove(0);
		}
		return null;
	}
	
	/**Returns a list of strings containing detailed information about the current state of the letter station.
	 * Override to use.
	 * @return
	 */
	public List<String> getAdditionalInfo() {
		return new ArrayList<String>();
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#getAssignedLetters()
	 */
	public Collection<Letter> getAssignedLetters() {
		return assignedLetters;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#requestLetter(alphabetsoup.framework.Bucketbot, alphabetsoup.framework.Letter)
	 */
	public void requestLetter(Bucketbot bb, Letter l) {
		numLettersRequested++;

		if(bb.getBucket() != null)
			lettersRequested.add(new LetterRequested(bb, l));
	}

	/**
	 * @return Returns the bundleSize.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#getBundleSize()
	 */
	public int getBundleSize() {
		return bundleSize;
	}

	/**
	 * @return Returns the numLettersRequested.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#getNumLettersRequested()
	 */
	public int getNumLettersRequested() {
		return numLettersRequested;
	}

	/**
	 * @return Returns the idleTime.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.LetterStation#getIdleTime()
	 */
	public double getIdleTime() {
		return idleTime;
	}

	/**
	 * @return Returns the letterToBucketTime.
	 */
	public float getLetterToBucketTime() {
		return letterToBucketTime;
	}

}
