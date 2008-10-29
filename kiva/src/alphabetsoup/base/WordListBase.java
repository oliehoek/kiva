/**
 * 
 */
package alphabetsoup.base;

import java.io.FileInputStream;
import java.util.*;

import alphabetsoup.framework.Letter;
import alphabetsoup.framework.LetterColor;
import alphabetsoup.framework.MersenneTwisterFast;
import alphabetsoup.framework.SimulationWorld;
import alphabetsoup.framework.Word;
import alphabetsoup.framework.WordList;

/**WordListBase implements basic functionality of the WordList, such as loading a word list from a file,
 * and generating letters and words. 
 * @author Chris Hazard
 */
public class WordListBase implements WordList {

	public List<Word> words = new ArrayList<Word>();
	public List<Word> availableWords = new ArrayList<Word>();
	public List<Word> completedWords = new ArrayList<Word>();
	
	protected String baseWords[];
	protected List<LetterColor> baseColors;
	protected HashMap<Character, Float> letterProbabilities;
	
	public void resetStatistics() {
		completedWords = new ArrayList<Word>();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#generateWordsFromFile(java.lang.String, java.util.List, int)
	 */
	public void generateWordsFromFile(String filename, List<LetterColor> colors, int num_words) {
		//open file to get words
		String content;
		try {
			FileInputStream fis = new FileInputStream(filename);
			int x= fis.available();
			byte b[]= new byte[x];
			fis.read(b);
			content = new String(b);
		}
		catch (Throwable e) {
			System.out.println("Could not open file " + filename);
			return;
		}

		//store info to build more new words
		baseWords = content.split("(\r\n)|\n|\r");	//split on any newline combo
		baseColors = colors;
		
		//compute letter probabilities based on word set
		letterProbabilities = new HashMap<Character, Float>();
		int number_of_letters = 0;
		for(String s : baseWords) {
			for(char c : s.toCharArray()) {
				number_of_letters++;
				if(letterProbabilities.containsKey(c))
					letterProbabilities.put(c, letterProbabilities.get(c) + 1.0f);
				else
					letterProbabilities.put(c, 1.0f);
			}
		}
		//normalize probabilities
		for(char c :letterProbabilities.keySet())
			letterProbabilities.put(c, letterProbabilities.get(c) / number_of_letters);
		
		//build initial list
		MersenneTwisterFast rand = SimulationWorld.rand;
		for(int i = 0; i < num_words; i++) {
			Word w = new Word(baseWords[rand.nextInt(baseWords.length)], baseColors);
			words.add(w);
			availableWords.add(w);
		}
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#takeAvailableWord(int)
	 */
	public Word takeAvailableWord(int index) {
		//add a new word on to the end
		MersenneTwisterFast rand = SimulationWorld.rand;
		Word w = new Word(baseWords[rand.nextInt(baseWords.length)], baseColors);
		words.add(w);
		availableWords.add(w);

		//take selected word out of the list
		w = availableWords.remove(index);
		return w;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#generateRandomLetter()
	 */
	public Letter generateRandomLetter() 
	{
		MersenneTwisterFast rand = SimulationWorld.rand;
		
		//get color based on distribution
		float r = rand.nextFloat();
		//chose a default one just incase
		int chosen_color = 0;
		
		//go through and check the range of each color,
		// pulling random number down to the current range
		for(LetterColor c : baseColors) {
			if(c.getProbability() > r) {
				chosen_color = c.getID();
				break;
			}
			r -= c.getProbability();
		}
		
		//now get letter based on distribution
		r = rand.nextFloat();
		//chose a default one just incase
		char chosen_letter = 'a';
		
		//go through and check the range of each letter,
		// pulling random number down to the current range
		for(char c : letterProbabilities.keySet()) {
			float probability = letterProbabilities.get(c);
			if(probability > r) {
				chosen_letter = c;
				break;
			}
			r -= probability;
		}
			
		return new Letter(chosen_letter, chosen_color);
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#getLetterProbability(alphabetsoup.framework.Letter)
	 */
	public float getLetterProbability(Letter l) {
		return letterProbabilities.get(l.getLetter()) * baseColors.get(l.getColorID()).getProbability();
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#completedWord(alphabetsoup.framework.Word)
	 */
	public void completedWord(Word w) {
		completedWords.add(w);
	}

	/**
	 * @return Returns the availableWords.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#getAvailableWords()
	 */
	public List<Word> getAvailableWords() {
		return availableWords;
	}
	
	/**
	 * @return Returns the completedWords.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#getCompletedWords()
	 */
	public List<Word> getCompletedWords() {
		return completedWords;
	}

	/**
	 * @return Returns the letterProbabilities.
	 */
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.WordList#getLetterProbabilities()
	 */
	public HashMap<Character, Float> getLetterProbabilities() {
		return letterProbabilities;
	}
}
