/**
 * 
 */
package alphabetsoup.framework;

/**LetterColor represents the color of a Letter tile.  It maintains the color,
 * probability of any tile having the color, and each color's unique ID.
 * @author Chris Hazard
 */
public class LetterColor {
	
	private float rValue, gValue, bValue;
	private float probability;
	private int colorID;
	
	/**Constructs a color based on floating point values.
	 * @param id ID # of the color
	 * @param prob probability that this color will be chosen for a letter in range [0.0,1.0]
	 * @param r red value in range [0.0,1.0]
	 * @param g green value in range [0.0,1.0]
	 * @param b blue value in range [0.0,1.0]
	 */
	public LetterColor(int id, float prob, float r, float g, float b) {
		colorID = id;	probability = prob;
		rValue = r;	gValue = g;	bValue = b;
	}
	
	/**Constructs a color based on integer values.
	 * @param id ID number of the color
	 * @param prob probability that this color will be chosen for a letter in range [0.0,1.0]
	 * @param r red value in range [0,255]
	 * @param g green value in range [0,255]
	 * @param b blue value in range [0,255]
	 */
	public LetterColor(int id, float prob, int r, int g, int b) {
		colorID = id;	probability = prob;
		rValue = r/255.0f;	gValue = g/255.0f;	bValue = b/255.0f;
	}
	
	/**Returns the ID of the color
	 * @return ID number
	 */
	public int getID() {
		return colorID;
	}
	
	/**Returns the probability that this color will be chosen for a letter
	 * @return probability in the range [0.0,1.0]
	 */
	public float getProbability() {
		return probability;
	}

	/**Gets the blue value of the color
	 * @return Returns the bValue.
	 */
	public float getBValue() {
		return bValue;
	}

	/**Gets the green value of the color
	 * @return Returns the gValue.
	 */
	public float getGValue() {
		return gValue;
	}

	/**Gets the red value of the color
	 * @return Returns the rValue.
	 */
	public float getRValue() {
		return rValue;
	}
	
}
