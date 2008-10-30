/**
 * 
 */
package alphabetsoup.userinterface;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;

import alphabetsoup.base.LetterStationBase;
import alphabetsoup.framework.Letter;

/**Draws the LetterStation.
 * @author Chris Hazard
 */
public class LetterStationRender implements Renderable {

	LetterStationBase letterStation = null;
	public LetterStationRender(LetterStationBase letterStation) 
	{
		this.letterStation = letterStation;
	}
	
	static private Disk disk = new Disk();
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() 
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(letterStation.getX(), letterStation.getY(), 0.0f);
		
		GL11.glColor4ub((byte)0x0, (byte)0xCC, (byte)0xFF, (byte)0xC0);
		disk.draw(0.0f, letterStation.getRadius(), 14, 1);

		//draw a point in the center
		GL11.glColor4ub((byte)0x0, (byte)0xCC, (byte)0xFF, (byte)0xFF);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2f(0, 0);
		GL11.glEnd();
		
		GL11.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() 
	{
		float x = 10.0f, y = 100.0f;
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		RenderWindow.renderString(x, y, "Letter Station");
		y += RenderWindow.getFontRenderHeight();
		RenderWindow.renderString(x, y, "Bundle size: " + letterStation.getBundleSize());
		y += RenderWindow.getFontRenderHeight();
		for(Letter l : letterStation.assignedLetters) 
		{
			RenderWindow.renderTiledLetter(x, y, l, true);
			x += RenderWindow.getFontTileRenderWidth();
		}
		x = 10.0f;
		y += RenderWindow.getFontRenderHeight();
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		
		//render additional info
		for(String s : letterStation.getAdditionalInfo()) 
		{
			RenderWindow.renderString(x, y, s);
			y += RenderWindow.getFontRenderHeight();
		}
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderOverlayDetails(float, float)
	 */
	public void renderOverlayDetails() {
		GL11.glPushMatrix();
		GL11.glTranslatef(letterStation.getX(), letterStation.getY(), 0.0f);
		
		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xA0);
		disk.draw(0.0f, letterStation.getRadius(), 14, 1);

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
		return letterStation.IsCollision(mouse_x, mouse_y, 0);
	}

}
