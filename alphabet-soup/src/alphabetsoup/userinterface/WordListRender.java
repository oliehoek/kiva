/**
 * 
 */
package alphabetsoup.userinterface;

import org.lwjgl.opengl.GL11;

import alphabetsoup.base.WordListBase;
import alphabetsoup.framework.Word;

/**Draws the WordList
 * @author Chris Hazard
 */
public class WordListRender implements Renderable {
	
	WordListBase wordList = null;
	public WordListRender(WordListBase wordList) {
		this.wordList = wordList;
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() {

	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() {
		float x_pos = RenderWindow.getWindowWidth() - RenderWindow.getWindowRightPanelWidth(), y_pos = 20.0f;
		y_pos = RenderWindow.getWindowHeight() / 3;
		
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x_pos - RenderWindow.getFontTileRenderWidth(), y_pos + RenderWindow.getFontRenderHeight());
		GL11.glVertex2f(RenderWindow.getWindowWidth(), y_pos + RenderWindow.getFontRenderHeight());
		GL11.glEnd();

		for(int i = wordList.completedWords.size()-1; i >= 0; i--) {
			RenderWindow.renderTiledWord(x_pos, y_pos, wordList.completedWords.get(i));
			y_pos -= RenderWindow.getFontRenderHeight();
			//don't bother drawing past the end of the window
			if(y_pos < RenderWindow.getFontRenderHeight())
				break;
		}
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		RenderWindow.renderString(x_pos, y_pos, "Completed Words");

		y_pos = (2*RenderWindow.getWindowHeight()) / 3;
		
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x_pos - RenderWindow.getFontTileRenderWidth(), y_pos - RenderWindow.getFontRenderHeight()/2);
		GL11.glVertex2f(RenderWindow.getWindowWidth(), y_pos - RenderWindow.getFontRenderHeight()/2);
		GL11.glEnd();
		
		RenderWindow.renderString(x_pos, y_pos, "Pending Words");
		y_pos += RenderWindow.getFontRenderHeight();

		for(Word w : wordList.availableWords) {
			RenderWindow.renderTiledWord(x_pos, y_pos, w);
			y_pos += RenderWindow.getFontRenderHeight();
			//don't bother drawing past the end of the window
			if(y_pos > RenderWindow.getWindowHeight())
				break;
		}
		
		
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderOverlayDetails(float, float)
	 */
	public void renderOverlayDetails() {
		
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#isMouseOver(float, float)
	 */
	public boolean isMouseOver(float mouse_x, float mouse_y) {
		return false;
	}
}
