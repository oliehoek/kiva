/**
 * 
 */
package alphabetsoup.framework;

import java.util.*;

/**The class Word contains Letter objects that make up a word that must be completed.
 * Word also keeps track of the Letter objects given to it for the purpose of completing the word. 
 * @author Chris Hazard
 */
public class Word {
	
	protected Letter originalLetters[] = null;
	protected boolean completedLetters[] = null;
	
	/**Builds a word colored according to the specified distribution
	 * @param s String of word to build
	 * @param colors Map with keys being Letter.Color, and values being a float representing
	 * the corresponding probability that the Color will be selected.
	 * The probabilities specified should add up to 1.0.
	 */
	public Word(String s, List<LetterColor> colors) {
		originalLetters = new Letter[s.length()];
		completedLetters = new boolean[s.length()];
		
		MersenneTwisterFast rand = SimulationWorld.rand;
		//add each letter to originalLetters
		for(int i = 0; i < s.length(); i++) {
			//get color based on distribution
			float r = rand.nextFloat();
			//chose a default one just incase
			int chosen_color = 0;
			
			//go through and check the range of each color,
			// pulling random number down to the current range
			for(LetterColor c : colors) {
				if(c.getProbability() > r) {
					chosen_color = c.getID();
					break;
				}
				r -= c.getProbability();
			}
			originalLetters[i] = new Letter(s.charAt(i), chosen_color);
			completedLetters[i] = false;
		}
	}
	
	/**Builds a word colored according to the arrays of letters
	 * @param letters array of letters to build the new word from
	 */
	public Word(Letter letters[]) {
		originalLetters = new Letter[letters.length];
		completedLetters = new boolean[letters.length];
		
		//add each letter to originalLetters
		for(int i = 0; i < letters.length; i++) {
			originalLetters[i] = letters[i];
			completedLetters[i] = false;
		}		
	}
	
	
	/**Fulfills a letter of the Word 
	 * @param l Letter to add into the Word
	 * @return returns true if the Word needed the letter, false if it did not
	 */
	public boolean addLetter(Letter l) {
		for(int i = 0; i < originalLetters.length; i++)
			if(!completedLetters[i] && originalLetters[i].doesMatch(l)) {
				completedLetters[i] = true;
				return true;
			}
		return false;
	}

	/**Returns true if all the letters Word contains all of the letters it requires
	 * @return true if the word is complete, false otherwise
	 */
	public boolean isCompleted() {
		for(boolean b : completedLetters)
			if(!b) return false;
		return true;
	}
	
	/**Returns a HashMap of the needed letters to fulfill the word 
	 * @return HashMap<Letter, Integer, where each key is the Letter required,
	 * and each value is the number of those letters required
	 */
	public HashMap<Letter, Integer> getNeededLettersHashed() {
		HashMap<Letter, Integer> needed = new HashMap<Letter, Integer>();
		for(int i = 0; i < originalLetters.length; i++) {
			if(completedLetters[i])
				continue;

			if(needed.get(originalLetters[i]) != null)
				needed.put(originalLetters[i], needed.get(originalLetters[i])+1);
			else
				needed.put(originalLetters[i], 1);
		}
		return needed;
	}
	
	/**Returns a List of the needed letters to fulfill the word
	 * @return
	 */
	public List<Letter> getNeededLetters() {
		List<Letter> needed = new ArrayList<Letter>();
		for(int i = 0; i < originalLetters.length; i++)
			if(!completedLetters[i])
				needed.add(originalLetters[i]);
		return needed;
	}
	
	/**Returns a String object containing the word, with colors removed
	 * @return String of Word
	 */
	public String toString() {
		String s = new String();
		for(Letter l : originalLetters)
			s += l.getLetter();
		return s;
	}

	/**
	 * @return Returns boolean array of the completedLetters.
	 */
	public boolean[] getCompletedLetters() {
		return completedLetters;
	}

	/**
	 * @return Returns the originalLetters.
	 */
	public Letter[] getOriginalLetters() {
		return originalLetters;
	}

}
