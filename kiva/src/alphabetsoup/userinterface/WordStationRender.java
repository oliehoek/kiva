/**
 * 
 */
package alphabetsoup.userinterface;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;

import alphabetsoup.base.WordStationBase;
import alphabetsoup.framework.Word;

/**Draws the WordStation
 * @author Chris Hazard
 */
public class WordStationRender implements Renderable {
	
	WordStationBase wordStation = null;
	
	public WordStationRender(WordStationBase wordStation) {
		this.wordStation = wordStation;
	}
	
	static private Disk disk = new Disk();

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() {
		GL11.glPushMatrix();
		GL11.glTranslatef(wordStation.getX(), wordStation.getY(), 0.0f);

		GL11.glColor4ub((byte)0x66, (byte)0xFF, (byte)0x33, (byte)0xC0);
		disk.draw(0.0f, wordStation.getRadius(), 14, 1);

		//draw a point in the center
		GL11.glColor4ub((byte)0x66, (byte)0xFF, (byte)0x33, (byte)0xFF);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2f(0, 0);
		GL11.glEnd();

		GL11.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() {
		float x = RenderWindow.getWindowWidth() - RenderWindow.getWindowRightPanelWidth(),
			y = RenderWindow.getWindowHeight() / 3 + RenderWindow.getFontRenderHeight() + RenderWindow.getFontRenderHeight();

		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		RenderWindow.renderString(x, y, "Word Station");
		y += RenderWindow.getFontRenderHeight();
		for(Word w : wordStation.assignedWords) {
			RenderWindow.renderTiledWord(x, y, w);
			y += RenderWindow.getFontRenderHeight();
		}
		
		//render additional info
		x = 10.0f;
		y = 100.0f;
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		for(String s : wordStation.getAdditionalInfo()) {
			RenderWindow.renderString(x, y, s);
			y += RenderWindow.getFontRenderHeight();
		}
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderOverlayDetails(float, float)
	 */
	public void renderOverlayDetails() {
		GL11.glPushMatrix();
		GL11.glTranslatef(wordStation.getX(), wordStation.getY(), 0.0f);

		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xA0);
		disk.draw(0.0f, wordStation.getRadius(), 14, 1);

		//draw a point in the center
		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2f(0, 0);
		GL11.glEnd();
		
		GL11.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#isMouseOver(float, float)
	 */
	public boolean isMouseOver(float mouse_x, float mouse_y) {
		return wordStation.IsCollision(mouse_x, mouse_y, 0);
	}

}
