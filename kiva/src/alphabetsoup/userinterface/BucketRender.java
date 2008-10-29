/**
 * 
 */
package alphabetsoup.userinterface;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;

import alphabetsoup.base.BucketBase;
import alphabetsoup.framework.*;

/**Draws the Bucket.
 * @author Chris Hazard
 */
public class BucketRender implements Renderable {
	
	BucketBase bucket = null;
	
	public BucketRender(BucketBase bucket) {
		this.bucket = bucket;
	}

	static private Disk disk = new Disk();

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() {
		GL11.glPushMatrix();
		GL11.glTranslatef(bucket.getX(), bucket.getY(), 0.0f);

		GL11.glColor4ub((byte)0x90, (byte)0x00, (byte)0xCC, (byte)0xFF);
		if(bucket.getLetters().size() > 0)
			disk.draw(2*bucket.getRadius() / 4, bucket.getRadius(), 10, 1);
		else
			disk.draw(3*bucket.getRadius() / 4, bucket.getRadius(), 10, 1);

		GL11.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() {
		float x = 10.0f, y = 260.0f;
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

Bucket buckets[] = SimulationWorld.getSimulationWorld().getBuckets();
String task_string = "Bucket "; 
for(int i = 0; i < buckets.length; i++)
	if(bucket == buckets[i]) {
		task_string += i;
		break;
	}
RenderWindow.renderString(x, y, task_string);
		//RenderWindow.renderString(x, y, "Bucket");
		y += RenderWindow.getFontRenderHeight();
		for(Letter l : bucket.getLetters()) {
			RenderWindow.renderTiledLetter(x, y, l, true);
			x += RenderWindow.getFontTileRenderWidth();
		}
		
		x = 10.0f;
		y += RenderWindow.getFontRenderHeight();
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		
		//render additional info
		for(String s : bucket.getAdditionalInfo()) {
			RenderWindow.renderString(x, y, s);
			y += RenderWindow.getFontRenderHeight();
		}
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderOverlayDetails(float, float)
	 */
	public void renderOverlayDetails() {
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
		GL11.glPushMatrix();
		GL11.glTranslatef(bucket.getX(), bucket.getY(), 0.0f);

		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xC0);
		disk.draw(3*bucket.getRadius() / 4, bucket.getRadius(), 10, 1);

		GL11.glPopMatrix();
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#isMouseOver(float, float)
	 */
	public boolean isMouseOver(float mouse_x, float mouse_y) {
		return bucket.IsCollision(mouse_x, mouse_y, 0);
	}

}
