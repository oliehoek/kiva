/**
 * 
 */
package alphabetsoup.base;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Circle;
import alphabetsoup.framework.Letter;
import alphabetsoup.framework.LetterStation;
import alphabetsoup.framework.Word;
import alphabetsoup.framework.WordStation;

/**BucketbotTask is a class which describes the major tasks which can be "physically" completed
 * (physically meaning via bucketbots, buckets, and stations).
 * @author Chris Hazard
 */
public class BucketbotTask {
	/**TaskType is the type of task, and indicates the meaning of each of the fields of this class.
	 * See the corresponding createTaskXXX method in this class for each TaskType for details.
	 */
	public enum TaskType {	NONE, CANCEL, MOVE, STORE_BUCKET,
		TAKE_BUCKET_TO_LETTER_STATION, TAKE_BUCKET_TO_WORD_STATION		}
	
	private TaskType taskType = TaskType.NONE;
	private Bucket bucket = null;
	private Circle destination = null;
	private Letter letter = null;				//letter to deliver or pick up
	private LetterStation letterStation = null;	//letter station to go to
	private WordStation wordStation = null;		//word station to go to
	private Word targetWord = null;				//word to place a letter into
	
	/**Creates a task to tell the bucketbot to cancel the last task
	 * @return new BucketbotTask object containing task details
	 */
	public static BucketbotTask createTaskCANCEL() {
		BucketbotTask t = new BucketbotTask();
		t.taskType = TaskType.CANCEL;
		return t;
	}

	/**Creates a task to tell the bucketbot to move to a specified location
	 * @param destination_x x position bucketbot should move to
	 * @param destination_y y position bucketbot should move to
	 * @return new BucketbotTask object containing task details
	 */
	public static BucketbotTask createTaskMOVE(float destination_x, float destination_y) {
		BucketbotTask t = new BucketbotTask();
		t.taskType = TaskType.MOVE;
		t.destination = new Circle(0.0f, destination_x, destination_y);
		return t;
	}
	
	/**Creates a task to tell the bucketbot to move to a specified location
	 * @param c circle containing the position to move to
	 * @return new BucketbotTask object containing task details
	 */
	public static BucketbotTask createTaskMOVE(Circle c) {
		BucketbotTask t = new BucketbotTask();
		t.taskType = TaskType.MOVE;
		t.destination = c;
		return t;
	}
	
	/**Creates a task to tell the bucketbot to take the specified bucket
	 * @param b Bucket to go pick up and take to the destination location
	 * @param bucket_storage_location Circle object containing where bucket should be stored
	 * @return new BucketbotTask object containing task details
	 */
	public static BucketbotTask createTaskSTORE_BUCKET(Bucket b, Circle bucket_storage_location) {
		BucketbotTask t = new BucketbotTask();
		t.taskType = TaskType.STORE_BUCKET;
		t.destination = bucket_storage_location;
		t.bucket = b;
		return t;
	}
	
	/**Creates a task to tell the bucketbot to take a specified Bucket to a LetterStation, and get a certain Letter
	 * @param b Bucket to go pick up and take to the destination location
	 * @param l List of Letter specifying which letters should be taken from the LetterStation 
	 * @param ls LetterStation to take the letters from
	 * @return new BucketbotTask object containing task details
	 */
	public static BucketbotTask createTaskTAKE_BUCKET_TO_LETTER_STATION(Bucket b, Letter l, LetterStation ls) {
		BucketbotTask t = new BucketbotTask();
		t.taskType = TaskType.TAKE_BUCKET_TO_LETTER_STATION;
		t.bucket = b;		
		t.letter = l;		
		t.letterStation = ls;
		return t;
	}
	
	/**Creates a task to tell the bucketbot to take a specified Bucket to a WordStation, and drop off specified Letters
	 * @param b Bucket to go pick up and take to the destination location
	 * @param l List of Letter specifying which letters should be given to the WordStation from the Bucket
	 * @param ws WordStation to give the letters to
	 * @param w Word to place Letter l into (the Word must be at WordStation ws).  If null, then the first word that needs the letter will receive it.
	 * @return new BucketbotTask object containing task details
	 */
	public static BucketbotTask createTaskTAKE_BUCKET_TO_WORD_STATION(Bucket b, Letter l, WordStation ws, Word w) {
		BucketbotTask t = new BucketbotTask();
		t.taskType = TaskType.TAKE_BUCKET_TO_WORD_STATION;
		t.bucket = b;		
		t.letter = l;		
		t.wordStation = ws;		
		t.targetWord = w;
		return t;
	}
	
	/**
	 * @return Returns the bucket.
	 */
	public Bucket getBucket() {
		return bucket;
	}
	
	/**
	 * @return Returns the destinationX.
	 */
	public float getDestinationX() {
		return destination.getX();
	}
	/**
	 * @return Returns the destinationY.
	 */
	public float getDestinationY() {
		return destination.getY();
	}
	/**
	 * @return Returns the letter.
	 */
	public Letter getLetter() {
		return letter;
	}
	/**
	 * @return Returns the letterStation.
	 */
	public LetterStation getLetterStation() {
		return letterStation;
	}
	/**
	 * @return Returns the wordStation.
	 */
	public WordStation getWordStation() {
		return wordStation;
	}

	/**
	 * @return Returns the taskType.
	 */
	public TaskType getTaskType() {
		return taskType;
	}

	/**
	 * @return Returns the bucketStorageLocation.
	 */
	public Circle getBucketStorageLocation() {
		return destination;
	}

	/**
	 * @return the targetWord
	 */
	public Word getTargetWord() {
		return targetWord;
	}

	public void setLetter(Letter letter) {
		this.letter = letter;
	}

	/**
	 * @return the destination
	 */
	public Circle getDestination() {
		return destination;
	}
}
