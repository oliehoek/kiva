/**
 * 
 */
package alphabetsoup.userinterface;

import java.text.DecimalFormat;
import java.util.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;

import alphabetsoup.base.BucketbotBase;
import alphabetsoup.framework.*;

/**Draws the bucketbot.
 * @author Chris Hazard
 */
public class BucketbotRender implements Renderable {

	BucketbotBase bucketbot = null;
	
	public BucketbotRender(BucketbotBase bucketbot) {
		this.bucketbot = bucketbot;
	}

	static private Disk disk = new Disk();
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() {
		GL11.glPushMatrix();
		GL11.glTranslatef(bucketbot.getX(), bucketbot.getY(), 0.0f);

		//draw line to destination
		if(bucketbot.getCurrentTask() != null) {
			
			float dest_x = 0.0f, dest_y = 0.0f;
			float dest_x2 = 0.0f, dest_y2 = 0.0f;
			boolean draw_line = false;
			
			switch(bucketbot.getCurrentTask().getTaskType()) {
			case NONE:
				break;
			case CANCEL:
				break;
			case MOVE:
				draw_line = true;
				dest_x = bucketbot.getCurrentTask().getDestinationX();	dest_y = bucketbot.getCurrentTask().getDestinationY();
				dest_x2 = dest_x;	dest_y2 = dest_y;
				break;
			case STORE_BUCKET:
				if(bucketbot.getCurrentTask().getBucket() != null) {
					draw_line = true;
					dest_x = bucketbot.getCurrentTask().getBucket().getX();			dest_y = bucketbot.getCurrentTask().getBucket().getY();
					dest_x2 = bucketbot.getCurrentTask().getDestinationX();			dest_y2 = bucketbot.getCurrentTask().getDestinationY();
				}
				break;
			case TAKE_BUCKET_TO_LETTER_STATION:
				if(bucketbot.getCurrentTask().getBucket() != null) {
					draw_line = true;
					dest_x = bucketbot.getCurrentTask().getBucket().getX();			dest_y = bucketbot.getCurrentTask().getBucket().getY();
					dest_x2 = bucketbot.getCurrentTask().getLetterStation().getX();	dest_y2 = bucketbot.getCurrentTask().getLetterStation().getY();
				}
				break;
			case TAKE_BUCKET_TO_WORD_STATION:
				if(bucketbot.getCurrentTask().getBucket() != null) {
					draw_line = true;
					dest_x = bucketbot.getCurrentTask().getBucket().getX();			dest_y = bucketbot.getCurrentTask().getBucket().getY();
					dest_x2 = bucketbot.getCurrentTask().getWordStation().getX();		dest_y2 = bucketbot.getCurrentTask().getWordStation().getY();
				}
				break;
			}
draw_line = false;
			if(draw_line) {
				GL11.glColor4ub((byte)0x90, (byte)0x90, (byte)0x90, (byte)0x70);
				GL11.glBegin(GL11.GL_LINE_STRIP);
				GL11.glVertex2f(0, 0);
				GL11.glVertex2f(dest_x - bucketbot.getX(), dest_y - bucketbot.getY());
				GL11.glVertex2f(dest_x2 - bucketbot.getX(), dest_y2 - bucketbot.getY());
				GL11.glEnd();
			}
		}
		
		GL11.glColor4ub((byte)0xFF, (byte)0x33, (byte)0x33, (byte)0xC0);
		disk.draw(bucketbot.getRadius(), bucketbot.getRadius(), 10, 1);

		//draw a line in the center pointing the direction it's heading
		GL11.glRotatef((float)(bucketbot.getDirection() * 360 / (2 * Math.PI)), 0.0f, 0.0f, 1.0f);
		GL11.glColor4ub((byte)0xFF, (byte)0x33, (byte)0x33, (byte)0xFF);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(bucketbot.getRadius(), 0.0f);
		GL11.glEnd();
		
		//if evading, draw bold circle around bot
		if(bucketbot.isDrawBolded())
			disk.draw(1.2f*bucketbot.getRadius(), 1.2f*bucketbot.getRadius(), 10, 1);
		
		GL11.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() {
		float x = 10.0f, y = 400.0f;
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		DecimalFormat df = new DecimalFormat("0.00");
		RenderWindow.renderString(x, y, "Bucketbot speed: " + df.format(bucketbot.getSpeed()) );
		y += RenderWindow.getFontRenderHeight();
		RenderWindow.renderString(x, y, "Bucketbot target speed: " + df.format(bucketbot.getTargetSpeed()));
		y += RenderWindow.getFontRenderHeight();

String state_name = "idle";
if(bucketbot.stateQueue.size() > 0)
	state_name = bucketbot.stateQueue.get(0).getStateName();
if(bucketbot.getCurrentTask() != null) {
	String task_string = "task: " + bucketbot.getCurrentTask().getTaskType();
	
	if(bucketbot.getCurrentTask().getLetter() != null)
		task_string += "  letter: " + bucketbot.getCurrentTask().getLetter().getLetter();
	
	if(bucketbot.getCurrentTask().getTargetWord() != null)
		task_string += "  word:" + bucketbot.getCurrentTask().getTargetWord();

	Bucket buckets[] = SimulationWorld.getSimulationWorld().getBuckets();
	Bucketbot bucketbots[] = SimulationWorld.getSimulationWorld().bucketbots;
	
for(int i = 0; i < bucketbots.length; i++)
	if(bucketbot == bucketbots[i]) {
		task_string += "  bb: " + i;
		break;
	}

RenderWindow.renderString(x, y, task_string);
y += RenderWindow.getFontRenderHeight();
task_string = "";

for(int i = 0; i < buckets.length; i++)
	if(bucketbot.getCurrentTask().getBucket() == buckets[i]) {
		task_string += "bucket: " + i;
		break;
	}

if(bucketbot.getCurrentTask().getWordStation() != null) {
	task_string += "  word station:" + bucketbot.getCurrentTask().getWordStation().getY();
	RenderWindow.renderString(x, y, task_string);
	y += RenderWindow.getFontRenderHeight();
	task_string = "";
}
			
			RenderWindow.renderString(x, y, task_string);
			y += RenderWindow.getFontRenderHeight();
		}
		RenderWindow.renderString(x, y, "state: " + state_name);
		y += RenderWindow.getFontRenderHeight();
		RenderWindow.renderString(x, y, "Cruise Time: " + df.format(bucketbot.cruiseUntil - bucketbot.curTime));
		y += RenderWindow.getFontRenderHeight();
		RenderWindow.renderString(x, y, "Distance Traveled: " + df.format(bucketbot.getDistanceTraveled()) );
		y += RenderWindow.getFontRenderHeight();
		
		HashMap<String, Double> times = bucketbot.getTotalTimes();
		for(String s : times.keySet()) {
			RenderWindow.renderString(x, y, " % " + s + " Time: " + df.format(100.0 * times.get(s) / bucketbot.curTime));
			y += RenderWindow.getFontRenderHeight();			
		}
		
		RenderWindow.renderString(x, y, "Object: " + bucketbot);
		y += RenderWindow.getFontRenderHeight();
	}
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderOverlayDetails(float, float)
	 */
	public void renderOverlayDetails() {
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
		GL11.glPushMatrix();
		GL11.glTranslatef(bucketbot.getX(), bucketbot.getY(), 0.0f);

		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xC0);
		disk.draw(bucketbot.getRadius(), bucketbot.getRadius(), 10, 1);

		//draw a line in the center pointing the direction it's heading
		GL11.glRotatef((float)(bucketbot.getDirection() * 360 / (2 * Math.PI)), 0.0f, 0.0f, 1.0f);
		GL11.glColor4ub((byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(bucketbot.getRadius(), 0.0f);
		GL11.glEnd();
		
		GL11.glPopMatrix();
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#isMouseOver(float, float)
	 */
	public boolean isMouseOver(float mouse_x, float mouse_y) {
		return bucketbot.IsCollision(mouse_x, mouse_y, 0);
	}

}
