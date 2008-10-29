/**
 * 
 */
package alphabetsoup.userinterface;

import org.lwjgl.opengl.GL11;

import alphabetsoup.framework.Map;

/**Renders the map and every object contained within it.
 * @author Chris Hazard
 */
public class MapRender implements Renderable {

	Map map;
	
	public MapRender(Map map) {
		this.map = map;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#render()
	 */
	public void render() {
		GL11.glPointSize(2.0f);
		GL11.glLineWidth(3.0f);
		
		//draw bounding box
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(map.getWidth(), 0);
		GL11.glVertex2f(map.getWidth(), map.getHeight());
		GL11.glVertex2f(0, map.getHeight());
		GL11.glEnd();

		//bucketbotQuadtree.render();
		//bucketQuadtree.render();
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Renderable#renderDetails()
	 */
	public void renderDetails() {
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
