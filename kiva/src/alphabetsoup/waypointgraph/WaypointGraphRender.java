/**
 * 
 */
package alphabetsoup.waypointgraph;

import alphabetsoup.framework.*;
import alphabetsoup.userinterface.Renderable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.Disk;

/**
 * WaypointGraphRender draws the Waypoint graph.
 * @author Chris Hazard
 */
public class WaypointGraphRender implements Renderable {

	WaypointGraph waypointGraph = null;
	
	public WaypointGraphRender(WaypointGraph waypointGraph) 
	{
		this.waypointGraph = waypointGraph;
	}
	
	private Disk disk = new Disk();
	
	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() 
	{
		GL11.glLineWidth(2.0f);
		for(Waypoint w : waypointGraph.getWaypoints()) 
		{
			//draw the center
			GL11.glPushMatrix();
			GL11.glTranslatef(w.getX(), w.getY(), 0.0f);
			disk.draw(0.0f, SimulationWorld.getSimulationWorld().map.getTolerance()/2, 10, 1);
			GL11.glPopMatrix();
			
			//draw the paths
			GL11.glBegin(GL11.GL_LINES);
			for(Waypoint path : w.getPaths()) 
			{
				GL11.glColor4ub((byte)0xC0, (byte)0x0, (byte)0x0, (byte)0x80);
				GL11.glVertex2f(w.getX(), w.getY());
				GL11.glColor4ub((byte)0x0, (byte)0xC0, (byte)0x0, (byte)0x80);
				GL11.glVertex2f(path.getX(), path.getY());
			}			
			GL11.glEnd();
		}
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderOverlayDetails()
	 */
	public void renderOverlayDetails() {
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() {
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#isMouseOver(float, float)
	 */
	public boolean isMouseOver(float mouse_x, float mouse_y) {
		return false;
	}

}
