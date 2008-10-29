/**
 * 
 */
package alphabetsoup.framework;

/**LetterType extends Letter such that a Letter can be referred to as its type
 * e.g. a HashSet of LetterType can be used to find out stats of each type of letter
 * @author chrishazard
 *
 */
public class LetterType extends Letter {
	
	/**Builds a LetterType from the respective letter
	 * @param l
	 */
	public LetterType(Letter l) {
		super(l.getLetter(), l.getColorID());
	}
	
	/**Creates a Letter tile based on the given letter and color
	 * @param l letter to make tile out of
	 * @param color color of Letter tile
	 */
	public LetterType(char l, int color) {
		super(l, color);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if(!(o instanceof LetterType))
			return false;
		Letter l = (Letter)o;
		return doesMatch(l);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getColorID() * 1013 + (int)getLetter();
	}

}
