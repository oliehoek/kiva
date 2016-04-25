/**
 * 
 */
package alphabetsoup.framework;

/**The class Letter makes up wordList for completion
 * It is dispensed by LetterStation, carried in Bucket, and consumed by WordStation
 * @author Chris Hazard
 */
public class Letter {
	private char letter;
	private int colorID;
	
	/**Creates a Letter tile based on the given letter and color
	 * @param l letter to make tile out of
	 * @param color color of Letter tile
	 */
	public Letter(char l, int color) {
		letter = l;		colorID = color;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Letter clone() {
		return new Letter(letter, colorID);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return letter + "(" + colorID + ")";
	}
	
	/**Returns a LetterType version of itself (abstracting away the instance).
	 * @return
	 */
	public LetterType getType() {
		return new LetterType(this);
	}
	
	/**Returns true if the letter and color match the Letter passed in
	 * @param l Letter to compare to
	 * @return true if the letter and color match
	 */
	public boolean doesMatch(Letter l) {
		return letter == l.getLetter() && colorID == l.getColorID();
	}

	/**
	 * @return Returns the color.
	 */
	public int getColorID() {
		return colorID;
	}

	/**
	 * @return Returns the letter.
	 */
	public char getLetter() {
		return letter;
	}	
}
