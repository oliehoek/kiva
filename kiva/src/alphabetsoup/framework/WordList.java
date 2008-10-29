/**
 * 
 */
package alphabetsoup.framework;

import java.util.HashMap;
import java.util.List;

/**WordList manages the incoming and outgoing Words of AlphabetSoup.
 * Implementations should keep some form of a buffer of incoming Words,
 * allow Words to be taken from the buffer and given to WordStations,
 * generate new incoming Words, and keep track of the Words completed.  
 * @author Chris Hazard
 */
public interface WordList {

	/**Loads a text file with a list of words, stores the color distribution,
	 * and generates the initial buffer of available words. 
	 * @param filename dictionary file to load
	 * @param colors a list of all possible colors (LetterColor contains the probability)
	 * @param num_words number of words in the available word buffer
	 */
	public void generateWordsFromFile(String filename, List<LetterColor> colors, int num_words);
	
	/**Returns and removes the specified Word from the available words list
	 * This function will also add a new word to the available word list (returned via getAvailableWords)
	 * which will be added to the end of the list.
	 * @param index index of the Word to remove
	 * @return reference to the Word
	 */
	public Word takeAvailableWord(int index);
	
	/**Generates a random letter based on the color distribution
	 * and distribution of letters in the WordList
	 * @return a random Letter tile
	 */
	public Letter generateRandomLetter();
	
	/**Returns the probability of the specified letter tile
	 * @param l
	 * @return probability of the specified letter in [0.0,1.0]
	 */
	public float getLetterProbability(Letter l);
	
	/**Should be called whenever a WordStation completes a Word, so that the Word
	 * will move to the list of completed Words.
	 * @param w Word completed
	 */
	public void completedWord(Word w);
	
	/**Returns the current list of available words in the buffer 
	 * @return List of available Words
	 */
	public List<Word> getAvailableWords();
	
	/**Returns a list of Words that have been completed
	 * @return List of completed Words
	 */
	public List<Word> getCompletedWords();
	
	/**Returns the distribution of letters in the WordList.
	 * @return the HashMap returned has each character appearing in the WordList
	 * as its keys, and the corresponding probability of that letter as the value
	 * in the range (0,1]
	 */
	public HashMap<Character, Float> getLetterProbabilities();
	
	/**Clears the statistics kept.
	 */
	public void resetStatistics();
}
