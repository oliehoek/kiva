/**
 * 
 */
package alphabetsoup.base;

import java.util.*;

import alphabetsoup.framework.*;

/**WordStationBase implements the basic functionality of a passive Word station,
 * such that it can take letters from buckets when requested, clears words when complete,
 * and can be assigned new words when space permits.
 * @author Chris Hazard
 */
public class WordStationBase extends Circle implements WordStation, Updateable {
	private int capacity;
	private float bucketToLetterTime;
	private float wordCompletionTime;
	public List<Word> assignedWords;
	
	protected double blockedUntilTime;
	
	protected int numLettersRequested;	//number of individual letters requested to be taken
	protected double idleTime;
	
	public void resetStatistics() {
		numLettersRequested = 0;
		idleTime = 0.0;
	}
	
	protected static class LetterRequested {
		public LetterRequested(Bucketbot bb, Letter l, Word w) {
			bucketbot = bb;	letter = l; word = w;	
		}
		public Bucketbot bucketbot;
		public Letter letter;
		public Word word;
	}
	List<LetterRequested> letterTakesRequested = new ArrayList<LetterRequested>();

	public WordStationBase(float station_radius, float bucket_to_letter_time, float word_completion_time, int station_capacity) {
		super(station_radius);
		resetStatistics();
		bucketToLetterTime = bucket_to_letter_time;
		wordCompletionTime = word_completion_time;
		assignedWords = new ArrayList<Word>();
		capacity = station_capacity;

		blockedUntilTime = -1.0;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#getCapacity()
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#assignWord(alphabetsoup.framework.Word)
	 */
	public void assignWord(Word w) {
		if(assignedWords.size() < capacity)
			assignedWords.add(w);
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
		
		if(removeAnyCompletedWord(cur_time) != null)
			return;
		
		if(takeLetterFromBucket(cur_time) != null)
			return;
	}
	
	/**Checks to see if any letter can be taken from a bucket.  If so, it moves the letter from the bucket
	 * to the word station and returns the pertanent information (requested).  In this case, a caller should not 
	 * perform any other function until the word station is done blocking.  If no letter is
	 * ready to be taken (usually the case), the function returns null.
	 * @param cur_time
	 * @return
	 */
	protected LetterRequested takeLetterFromBucket(double cur_time) {
		//keep going through queue until have something to take or done with queue
		while(letterTakesRequested.size() > 0) {
			LetterRequested request = letterTakesRequested.remove(0);

			Bucketbot bb = request.bucketbot; 
			Bucket b = bb.getBucket();
			Letter l = request.letter;
			Word target_word = request.word;
			
			if(b.containsLetter(l)
					&& getDistance((Circle)b) < getRadius() ) {
				
				//if w is null, then just choose the first one that fits 
				if(target_word == null) {
					for(Word w : assignedWords)
						if(w.addLetter(l)) {
							b.removeLetter(l);
							blockedUntilTime = cur_time + bucketToLetterTime;
							bb.waitUntil(blockedUntilTime);
							request.word = w;
							return request;
						}
				}
				else { //word is specified
					//if it's at this station and the letter can be added, then add it
					if(assignedWords.contains(target_word) && target_word.addLetter(l)) {
						b.removeLetter(l);
						blockedUntilTime = cur_time + bucketToLetterTime;
						bb.waitUntil(blockedUntilTime);
						return request;
					}
				}
			}
		}
		
		return null;
	}
	
	/**Checks to see if any word is completed.  If so, it moves the word to the completed word list
	 * and frees the word location, and returns the word.  In this case, a caller should not 
	 * perform any other function until the word station is done blocking.  If no word is
	 * ready to be completed (usually the case), the function returns null.
	 * @param cur_time
	 * @return
	 */
	protected Word removeAnyCompletedWord(double cur_time) {
		//remove any words that are finished, going backwards to remove from the list
		for(int w = assignedWords.size()-1; w >= 0; w--)
			if(assignedWords.get(w).isCompleted()) {
				Word completed = assignedWords.get(w);
				SimulationWorld.getSimulationWorld().getWordList().completedWord(completed);
				assignedWords.remove(w);

				//exit early and block action
				blockedUntilTime = cur_time + wordCompletionTime;
				return completed;
			}
		return null;
	}
	
	/**Returns the word that contains the specified instance of the letter in its originalLetters field.
	 * Returns null if not found.
	 * @param l
	 * @return
	 */
	public Word getWordContainingOriginalLetterInstance(Letter l) {
		for(Word w : assignedWords) {
			for(Letter o : w.getOriginalLetters()) {
				if(l == o)
					return w;
			}
		}
		return null;
	}
	
	/**Returns a list of strings containing detailed information about the current state of the word station,
	 * null if no additional info.
	 * Override to use.
	 * @return
	 */
	public List<String> getAdditionalInfo() {
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#getAssignedWords()
	 */
	public List<Word> getAssignedWords() {
		return assignedWords;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#requestLetterTake(alphabetsoup.framework.Bucketbot, alphabetsoup.framework.Letter, alphabetsoup.framework.Word)
	 */
	public void requestLetterTake(Bucketbot bb, Letter l, Word w) {
		numLettersRequested++;

		if(bb.getBucket() != null)
			letterTakesRequested.add(new LetterRequested(bb, l, w));
	}

	/**
	 * @return Returns the numLettersRequested.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#getNumLettersRequested()
	 */
	public int getNumLettersRequested() {
		return numLettersRequested;
	}

	/**
	 * @return Returns the idleTime.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#getIdleTime()
	 */
	public double getIdleTime() {
		return idleTime;
	}

	/**
	 * @return Returns the bucketToLetterTime.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#getBucketToLetterTime()
	 */
	public float getBucketToLetterTime() {
		return bucketToLetterTime;
	}

	/**
	 * @return Returns the wordCompletionTime.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordStation#getWordCompletionTime()
	 */
	public float getWordCompletionTime() {
		return wordCompletionTime;
	}
}
