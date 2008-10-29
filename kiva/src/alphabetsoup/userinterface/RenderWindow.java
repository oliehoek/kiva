/**Singleton class that is the user interface.
 * 
 */
package alphabetsoup.userinterface;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.glu.GLU;

import alphabetsoup.base.SummaryReport;
import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Bucketbot;
import alphabetsoup.framework.Circle;
import alphabetsoup.framework.Letter;
import alphabetsoup.framework.LetterColor;
import alphabetsoup.framework.SimulationWorld;
import alphabetsoup.framework.Word;

/**RenderWindow contains all of the code to initialize and draw the window and accept input
 * from the user.   It also contains the main loop for user interaction and rendering.
 * This class is not necessary if an AlphabetSoup simulation is being run without graphics. 
 * @author Chris Hazard
 */
public class RenderWindow {

	static private SimulationWorld simulationWorld = null;
	
	static private int windowWidth, windowHeight;
	static private float renderWindowWidth, renderWindowHeight;
	static private float renderWindowXOffset, renderWindowYOffset;
	static private float updateRate = 4.0f;
	static private float framesPerSecond = 0.0f;
	
	static private int windowLeftPanelWidth = 200;
	static private int windowRightPanelWidth = 200;
	
	//things that the mouse is currently over
	static private List<Renderable> mouseOver = new ArrayList<Renderable>();
	static private boolean keepMouseOver = false;	//if set to true, doesn't reset mouseOver every render
	
	static private boolean paused = false;
	static private boolean renderReport = false;
	
	static private List<Renderable> solidRenders = new ArrayList<Renderable>();
	static private List<Renderable> lineRenders = new ArrayList<Renderable>();
	/**Adds a Renderable that will always be rendered in map space 
	 * @param r Renderable to always render
	 */
	static public void addSolidRender(Renderable r) {
		solidRenders.add(r);
	}
	/**Adds a Renderable that will always be rendered in map space 
	 * @param r Renderable to always render
	 */
	static public void addLineRender(Renderable r) {
		lineRenders.add(r);
	}
	
	static private List<Renderable> additionalDetailRenders = new ArrayList<Renderable>();
	/**Adds a Renderable that will always be rendered in detail (window) space 
	 * @param r Renderable to always render in detail (window) space
	 */
	static public void addAdditionalDetailRender(Renderable r) {
		additionalDetailRenders.add(r);
	}
	
	static class ZoomWindow {
		public ZoomWindow(float x1, float y1, float x2, float y2) {
			this.x1 = x1;	this.y1 = y1;	this.x2 = x2;	this.y2 = y2;
		}
		float x1, y1, x2, y2;
	}
	//zooms is a LIFO queue of zooms, to allow the user to zoom in and zoom out
	// by pushing things on the stack.  index 0 is current zoom.
	static private List<ZoomWindow> zooms = new ArrayList<ZoomWindow>();

	//make it a singleton class
	private RenderWindow() { }
	public Object clone() throws CloneNotSupportedException { throw new CloneNotSupportedException(); }
	
	//set to true if exiting the program
	static public boolean exitRequested = false;
	
	/**Sets up user interface, including display and fonts
	 * @param width window width
	 * @param height window height
	 * @param sw reference to SimulationWorld
	 */
	static public void initializeUserInterface(int width, int height, SimulationWorld sw)
	{
		try {
			Display.setDisplayMode(new DisplayMode(windowWidth = width, windowHeight = height));
			Display.setTitle("Alphabet Soup");
			Display.create();
			//Display.setIcon();
			//Display.setFullscreen(true);

	        //initialize keyboard
			Keyboard.create();
			Mouse.create();
		} catch (LWJGLException le) {
			le.printStackTrace();
			System.out.println("Failed to initialize LWJGL.");
			return;
		}
		
		simulationWorld = sw;

		//make background white
		GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		//setup rendering style
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		//sync frame (only works on windows)
		Display.setVSyncEnabled(true);

		buildFont("Courier New", 48, 11, 1024);
	}
	
	static boolean inZoomMode = false;
	/**Processes keyboard input
	 */
	static private void readKeyboard() {
		Keyboard.poll();
		while(Keyboard.next()) {
			
			//if key up message
			if(!Keyboard.getEventKeyState()) {
				if(Keyboard.getEventKey() == Keyboard.KEY_LCONTROL
						|| Keyboard.getEventKey() == Keyboard.KEY_RCONTROL) isCtrlDown = false;
				continue;
			}
			
			//key down message
			if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) exitRequested = true;
			if(Keyboard.getEventKey() == Keyboard.KEY_SPACE) paused = !paused;
			if(Keyboard.getEventKey() == Keyboard.KEY_ADD) updateRate *= 2;
			if(Keyboard.getEventKey() == Keyboard.KEY_SUBTRACT) {
				updateRate /= 2;
				if(updateRate > 0.5 && updateRate < 1.0)
					updateRate = 1.0f;
			}
			if(Keyboard.getEventKey() == Keyboard.KEY_R) SummaryReport.generateReport(simulationWorld);
			if(Keyboard.getEventKey() == Keyboard.KEY_C) simulationWorld.resetStatistics();
			if(Keyboard.getEventKey() == Keyboard.KEY_TAB) renderReport = !renderReport;
			if(Keyboard.getEventKey() == Keyboard.KEY_LSHIFT
					|| Keyboard.getEventKey() == Keyboard.KEY_RSHIFT) inZoomMode = !inZoomMode;
			if(Keyboard.getEventKey() == Keyboard.KEY_LCONTROL
					|| Keyboard.getEventKey() == Keyboard.KEY_RCONTROL) isCtrlDown = true;
		}
	}
	
	static private void reselectRendersUnderMouse(float rx, float ry) {
		//find everything underneath the mouse
		mouseOver.clear();
		
		for(Renderable r : solidRenders) {
			if(r.isMouseOver(rx, ry))
				mouseOver.add(r);
		}
		for(Renderable r : lineRenders) {
			if(r.isMouseOver(rx, ry))
				mouseOver.add(r);
		}
	}

	static boolean isButton0Down = false;
	static boolean isButton1Down = false; //ctrl-button 1 works too
	static boolean isCtrlDown = false;
	static ZoomWindow curZoom = null;
	/**Process mouse input
	 */
	static private void readMouse() {
		int mouse_x = Mouse.getX();
		int mouse_y = Mouse.getY();

		//renderable mouse coordinates
		if(zooms.size() == 0)
			return;
		ZoomWindow z = zooms.get(0);
		float x_offset = Math.min(z.x1, z.x2);
		float y_offset = Math.min(z.y1, z.y2);
		float width = Math.abs(z.x1 - z.x2);
		float height = Math.abs(z.y1 - z.y2);
		float rx = (mouse_x * width)/windowWidth + x_offset;
		float ry = (mouse_y * height)/windowHeight + y_offset;

		if(!keepMouseOver)
			reselectRendersUnderMouse(rx, ry);

		//check for mouse buttons down.  for mac compatability,
		// treat button 0 down with ctrl as button 1
		if(Mouse.isButtonDown(0) && !isCtrlDown) {
			
			if(!inZoomMode) {
				//move around thing under mouse
				for(Circle c : simulationWorld.getMap().getBucketbotsWithinDistance(rx, ry, simulationWorld.getMap().getTolerance())) {
					simulationWorld.getMap().moveBucketbot((Bucketbot)c, rx, ry);
					break;
				}
				for(Circle c : simulationWorld.getMap().getBucketsWithinDistance(rx, ry, simulationWorld.getMap().getTolerance())) {
					if(simulationWorld.getMap().isValidBucketStorageLocation((Bucket)c, rx, ry)) {
						//c.setX(rx);
						//c.setY(ry);
						break;
					}
				}
			}
			else { //in zoom mode
				if(!isButton0Down && curZoom == null) {
					curZoom = new ZoomWindow(rx, ry, rx, ry);
				}
				
				//set end location of zoom box
				curZoom.x2 = rx;
				curZoom.y2 = ry;
				isButton0Down = true;
			}
		}
		else if(isButton0Down){ //button 0 not down anymore
			
			//if have a zoom and isn't too small, add it
			if(curZoom != null && Math.abs(curZoom.x1 - curZoom.x2) > .001 && Math.abs(curZoom.y1 - curZoom.y2) > .001 )
				zooms.add(0, curZoom);
			curZoom = null;
			isButton0Down = false;
		}

		//secondary mouse button zooms out 
		if(Mouse.isButtonDown(1) || (Mouse.isButtonDown(0) && isCtrlDown) ) {
			if(inZoomMode && !isButton1Down)
				zooms.remove(0);
			if(!inZoomMode && !isButton1Down) {
				reselectRendersUnderMouse(rx, ry);
				//if nothing selected, then allow hover-select
				if(mouseOver.size() == 0)
					keepMouseOver = false;
				else
					keepMouseOver = true;
			}
			isButton1Down = true;
		}
		else { //button 1 not down, but was before
			isButton1Down = false;
		}
	}
	
	/**Draw all objects to the window
	 */
	static private void render()
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		if(zooms.size() == 0) {
			float visible_window_width = (float)windowWidth - windowLeftPanelWidth - windowRightPanelWidth;
			float visible_window_height = (float)windowHeight - 2*fontRenderHeight;
			//have the view width be twice that of the map
			renderWindowWidth = simulationWorld.getMap().getWidth();
			renderWindowHeight = simulationWorld.getMap().getHeight();
	
			if( ((float)visible_window_width)/visible_window_height > renderWindowWidth / renderWindowHeight)
				renderWindowWidth *=   (((float)visible_window_width)/visible_window_height) /  (renderWindowWidth / renderWindowHeight);
			else
				renderWindowHeight *=   (renderWindowWidth / renderWindowHeight) / (((float)visible_window_width)/visible_window_height);
			renderWindowWidth *= (windowWidth/visible_window_width);
			renderWindowHeight *= (windowHeight/visible_window_height);
	
			renderWindowXOffset = renderWindowWidth/2 - simulationWorld.getMap().getWidth()/2; // + (renderWindowWidth*windowLeftPanelWidth)/windowWidth;
			renderWindowYOffset = renderWindowHeight/2 - simulationWorld.getMap().getHeight()/2;
			
			//ZoomWindow z = new ZoomWindow(renderWindowXOffset, renderWindowYOffset, renderWindowWidth, renderWindowHeight);
			ZoomWindow z = new ZoomWindow(-renderWindowXOffset, -renderWindowYOffset,
					renderWindowWidth-renderWindowXOffset, renderWindowHeight-renderWindowYOffset);
			zooms.add(z);
		}

		//setup matrices
		ZoomWindow z = zooms.get(0);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(Math.min(z.x1, z.x2), Math.max(z.x1, z.x2), Math.min(z.y1, z.y2), Math.max(z.y1, z.y2));
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		//center the map
		
		//GL11.glTranslatef(z.x1, z.y1, 0.0f); 
		
		for(Renderable r : solidRenders)
			r.render();
		
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
		for(Renderable r : lineRenders)
			r.render();
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
		
		//render highlights for anything the mouse is over
		for(Renderable r : mouseOver)
			r.renderOverlayDetails();
		
		if(inZoomMode && curZoom != null) {
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
			GL11.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
			GL11.glBegin(GL11.GL_QUADS);
			float x1 = Math.min(curZoom.x1, curZoom.x2);
			float x2 = Math.max(curZoom.x1, curZoom.x2);
			float y1 = Math.min(curZoom.y1, curZoom.y2);
			float y2 = Math.max(curZoom.y1, curZoom.y2);
			GL11.glVertex2f(x1, y1);
			GL11.glVertex2f(x2, y1);
			GL11.glVertex2f(x2, y2);
			GL11.glVertex2f(x1, y2);
			GL11.glEnd();
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
			GL11.glFlush();
			GL11.glFinish();
		}

		//draw gui
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(0, windowWidth, windowHeight, 0);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		for(Renderable r : additionalDetailRenders)
			r.renderDetails();

		//render details for anything the mouse is over
		for(Renderable r : mouseOver)
			r.renderDetails();
		
		DecimalFormat double_format = new DecimalFormat("0.00");
		
		GL11.glColor4f(0, 0, 0, 1);
		float x = 10.0f, y = 10.0f;
		renderString(x, y, "update speed: " + double_format.format(updateRate) );
		y += RenderWindow.getFontRenderHeight();
		renderString(x, y, "fps: " + double_format.format(framesPerSecond) );
		y += RenderWindow.getFontRenderHeight();
		if(inZoomMode) {
			renderString(x, y, "zoom mode");
			y += RenderWindow.getFontRenderHeight();
		}
		
		if(renderReport) {
			List<String> report = SummaryReport.generateReportText(simulationWorld);
			
			//draw background "window"
			int longest_line_length = 0;
			for(String s : report) longest_line_length = Math.max(longest_line_length, s.length());
			GL11.glColor4f(0.8f, 0.8f, 0.8f, 0.9f);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(x, y - fontRenderHeight/2);
			GL11.glVertex2f(x, y + fontRenderHeight * report.size());
			GL11.glVertex2f(x + fontRenderWidth * longest_line_length, y + fontRenderHeight * report.size());
			GL11.glVertex2f(x + fontRenderWidth * longest_line_length, y - fontRenderHeight/2);
			GL11.glEnd();

			//render report text
			GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			for(String s : report) {
				RenderWindow.renderString(x, y, s);
				y += RenderWindow.getFontRenderHeight();
			}
		}
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glFlush();
		GL11.glFinish();
	}
	
	/**Continually loops, processing user input and rendering the state of the simulation until
	 * finished
	 * @param elapsed_time time to run the simulation.  if 0.0, then continually run
	 */
	static public void mainLoop(SimulationWorld sw, double elapsed_time) {
		simulationWorld = sw;
		
		double simulation_end_time = simulationWorld.getCurrentTime() + elapsed_time;

		//set up frames per second measuring
		final int num_frame_times = 6;
		double[] frame_times = new double[num_frame_times];
		for(int i = 0; i < num_frame_times; i++)
			frame_times[i] = 0;
		int cur_frame_time_index = 0;
		
		long last_time = System.nanoTime();
		//loop until exit, display closed, or simulation time is done running (if applicable)
		while(!exitRequested && !Display.isCloseRequested()
				&& (elapsed_time == 0.0 || simulation_end_time > simulationWorld.getCurrentTime()) ) {
			readKeyboard();
			readMouse();
			
			long new_time = System.nanoTime();
			double update_amount_in_seconds = (new_time - last_time) / 1000000000.0;
			last_time = new_time;
			
			//store time it took to render last frame
			frame_times[cur_frame_time_index++] = update_amount_in_seconds;
			cur_frame_time_index %= num_frame_times;
			
			//compute frames per second as the average of the last num_frame_times
			double ave_frame_time = 0.0f;
			for(int i = 0; i < num_frame_times; i++)
				ave_frame_time += frame_times[i];
			ave_frame_time /= num_frame_times;
			framesPerSecond = (float)(1/ave_frame_time);

			//if last frame was less than a quarter second, reduce update rate to keep things under control
			if(update_amount_in_seconds > 0.25)
				updateRate *= 0.25 / update_amount_in_seconds;

			if(!paused) {
				double time_delta = updateRate * update_amount_in_seconds;
				//if simulation_duration is specified, don't let it go past that time 
				if(elapsed_time > 0.0)
					time_delta = Math.min(simulation_end_time - simulationWorld.getCurrentTime(), time_delta);

				simulationWorld.update(time_delta);
			}

			render();
			Display.update();
		}
	}

	/**Frees the display resources
	 * 
	 */
	static public void destroyUserInterface()
	{		Display.destroy();		}

	/**Renders the specified string to the specified position
	 * @param x x coordinate to begin rendering (left justified)
	 * @param y y coordinate to begin rendering.  text will be centered vertically on this y position
	 * @param msg string to render
	 */
	public static void renderString(float x, float y, String msg) {
		if(msg == null)
			return;
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexture);
		for(int i = 0; i < msg.length(); i++) {
			GL11.glCallList(fontBase + msg.charAt(i));
			GL11.glTranslatef(fontRenderWidth, 0.0f, 0.0f);
		}
		GL11.glPopMatrix();
	}
	
	/**Sets the rendering color based on a 32-bit integer (RGBA) 
	 * @param c 32-bit integer representing the color
	 */
	public static void setColor(int c) {
		LetterColor lc = simulationWorld.getLetterColor(c);
		GL11.glColor4f(lc.getRValue(), lc.getGValue(), lc.getBValue(), 1.0f);
	}
	
	/**Renders a specified letter to the specified location
	 * @param x x coordinate of the tile (left justified)
	 * @param y y coordinate of the tile.  text will be centered vertically on this y position
	 * @param l Letter to render
	 * @param filled if true, the tile will be rendered as filled.  if false, it will be renedred
	 * as an empty tile slot
	 */
	public static void renderTiledLetter(float x, float y, Letter l, boolean filled) {
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glLineWidth(2.0f);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		//draw background color of tile
		setColor(l.getColorID());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(0+1, fontRenderHeight/2-1);
		GL11.glVertex2f(fontTileRenderWidth-1, fontRenderHeight/2-1);
		GL11.glVertex2f(fontTileRenderWidth-1, -fontRenderHeight/2+1);
		GL11.glVertex2f(0+1, -fontRenderHeight/2+1);
		GL11.glEnd();

		//draw white + gray border
		//draw gray shadow
		GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex2f(0+1, fontRenderHeight/2-1);
		GL11.glVertex2f(fontTileRenderWidth-1, fontRenderHeight/2-1);	
		GL11.glVertex2f(fontTileRenderWidth-1, -fontRenderHeight/2+1);
		GL11.glEnd();
		//draw white highlight
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glColor4f(0.9f, 0.9f, 0.9f, 1.0f);
		GL11.glVertex2f(fontTileRenderWidth-1, -fontRenderHeight/2+1);
		GL11.glVertex2f(0+1, -fontRenderHeight/2+1);
		GL11.glVertex2f(0+1, fontRenderHeight/2-1);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glCallList(fontBase + l.getLetter());
		
		if(filled) {
			GL11.glTranslatef(1.0f, -1.0f, 0.0f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glCallList(fontBase + l.getLetter());
		}
		
		GL11.glPopMatrix();
	}

	/**Renders an entire tiled Word at the specified location
	 * @param x x coordinate of the tile (left justified)
	 * @param y y coordinate of the tile.  text will be centered vertically on this y position
	 * @param w reference to the word to render
	 */
	public static void renderTiledWord(float x, float y, Word w) {		
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0.0f);
		GL11.glLineWidth(2.0f);

		Letter l[] = w.getOriginalLetters();
		boolean completed[] = w.getCompletedLetters();
		for(int i = 0; i < l.length; i++) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			//draw background color of tile
			setColor(l[i].getColorID());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(0+1, fontRenderHeight/2-1);
			GL11.glVertex2f(fontTileRenderWidth-1, fontRenderHeight/2-1);
			GL11.glVertex2f(fontTileRenderWidth-1, -fontRenderHeight/2+1);
			GL11.glVertex2f(0+1, -fontRenderHeight/2+1);
			GL11.glEnd();

			//draw white + gray border
			//draw gray shadow
			if(completed[i])
				GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
			else
				GL11.glColor4f(0.9f, 0.9f, 0.9f, 1.0f);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex2f(0+1, fontRenderHeight/2-1);
			GL11.glVertex2f(fontTileRenderWidth-1, fontRenderHeight/2-1);	
			GL11.glVertex2f(fontTileRenderWidth-1, -fontRenderHeight/2+1);
			GL11.glEnd();
			//draw white highlight
			if(completed[i])
				GL11.glColor4f(0.9f, 0.9f, 0.9f, 1.0f);
			else
				GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex2f(fontTileRenderWidth-1, -fontRenderHeight/2+1);
			GL11.glVertex2f(0+1, -fontRenderHeight/2+1);
			GL11.glVertex2f(0+1, fontRenderHeight/2-1);
			GL11.glEnd();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
	
			//draw black letter
			GL11.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			GL11.glCallList(fontBase + l[i].getLetter());
			
			//if letter is completed, draw white
			if(completed[i]) {
				GL11.glTranslatef(1.0f, -1.0f, 0.0f);
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				GL11.glCallList(fontBase + l[i].getLetter());
				GL11.glTranslatef(-1.0f, 1.0f, 0.0f);
			}
			
			GL11.glTranslatef(fontTileRenderWidth, 0.0f, 0.0f);
		}
		GL11.glPopMatrix();
	}
	

	static private float fontRenderHeight;
	static private float fontTileRenderWidth;
	static private float fontRenderWidth;
	static private int fontTexture;
	static private int fontBase;
	
	/**Builds a set of texturemaps based on the font name and size requested
	 * @param font_name System name of the font
	 * @param font_size Size in points
	 * @param font_render_size Specifies how big the letters should be rendered for other string 
	 * and letter rendering functions
	 * @param bitmap_size resolution of each letter's texturemap
	 */
	static public void buildFont(String font_name, int font_size, float font_render_size, int bitmap_size) {
		fontRenderHeight = 2*font_render_size;
		fontTileRenderWidth = fontRenderHeight/1.61803399f + 2;
		fontRenderWidth = font_render_size;
		Font font;
		BufferedImage font_image;
		int width_per_letter = bitmap_size / 16;
		boolean size_found = false;
		boolean direction_set = false;
		int delta = 0;
		
		//find the font size that best fits with the bitmap size
		while(!size_found) {
			font = new Font(font_name, Font.PLAIN, font_size);
			font_image = new BufferedImage(bitmap_size, bitmap_size, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = (Graphics2D)font_image.getGraphics();
			g.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			int width = fm.stringWidth("W");	//get width of widest letter
			int height = fm.getHeight();
			int line_width = (width > height) ? width * 16 : height * 16;
			if(!direction_set) {
				if(line_width > bitmap_size)
					delta = -2;
				else
					delta = 2;
				direction_set = true;
			}
			if(delta > 0) {
				if(line_width < bitmap_size)
					font_size += delta;
				else {
					size_found = true;
					font_size -= delta;
				}
			}
			else if(delta < 0) {
				if(line_width > bitmap_size)
					font_size += delta;
				else {
					size_found = true;
					font_size -= delta;
				}
			}
		}
		
		//draw ascii character set for the font
		font = new Font(font_name, Font.BOLD, font_size);
		font_image = new BufferedImage(bitmap_size, bitmap_size, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D)font_image.getGraphics();
		g.setFont(font);
		g.setColor(new Color(0xFFFFFFFF, true));
		g.setBackground(new Color(0x00000000, true));
		FontMetrics fm = g.getFontMetrics();
		for(int i=0;i<256;i++) {
			int x = i % 16;
			int y = i / 16;
			char ch[] = {(char)i};
			String temp = new String(ch);
			g.drawString(temp, (x * width_per_letter) + 1, (y * width_per_letter) + fm.getAscent());
		}

		//take java image and convert it to texturemap
		//flip image
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -font_image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
		font_image = op.filter(font_image, null);
		
		//put image in memory
        ByteBuffer scratch = ByteBuffer.allocateDirect(4 * font_image.getWidth() * font_image.getHeight());

        byte data[] = (byte[])font_image.getRaster().getDataElements(0, 0,
        												font_image.getWidth(), font_image.getHeight(), null);
		scratch.clear();
		scratch.put(data);
		scratch.rewind();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//create IntBuffer for image address in memory
		IntBuffer buf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		GL11.glGenTextures(buf);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, buf.get(0));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, font_image.getWidth(), font_image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, scratch);
		
		fontTexture = buf.get(0);
		fontBase = GL11.glGenLists(256);

		//generate display lists
		float textureDelta = 1.0f / 16.0f;
		for(int i=0;i<256;i++) {
			float u = ((float)(i % 16)) / 16.0f;
			float v = 1.f - (((float)(i / 16)) / 16.0f);
			GL11.glNewList(fontBase + i, GL11.GL_COMPILE);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexture);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(u, v);
			GL11.glVertex2f(0+2, -fontRenderHeight/2+1);
			GL11.glTexCoord2f((u + textureDelta), v);
			GL11.glVertex2f(fontRenderHeight+2, -fontRenderHeight/2+1);
			GL11.glTexCoord2f((u + textureDelta), v - textureDelta);
			GL11.glVertex2f(fontRenderHeight+2, fontRenderHeight/2+1);
			GL11.glTexCoord2f(u, v - textureDelta);
			GL11.glVertex2f(0+2, fontRenderHeight/2+1);
			GL11.glEnd();
		    GL11.glEndList();
        }
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		fontRenderHeight += 2;
		fontRenderWidth += 2;
		fontTileRenderWidth += 2;
	}
	/**
	 * @return Returns the fontRenderHeight.
	 */
	public static float getFontRenderHeight() {
		return fontRenderHeight;
	}
	/**
	 * @return Returns the fontTileRenderWidth.
	 */
	public static float getFontTileRenderWidth() {
		return fontTileRenderWidth;
	}
	/**
	 * @return Returns the windowHeight.
	 */
	public static int getWindowHeight() {
		return windowHeight;
	}
	/**
	 * @return Returns the windowWidth.
	 */
	public static int getWindowWidth() {
		return windowWidth;
	}
	/**
	 * @return Returns the windowLeftPanelWidth.
	 */
	public static int getWindowLeftPanelWidth() {
		return windowLeftPanelWidth;
	}
	/**
	 * @return Returns the windowRightPanelWidth.
	 */
	public static int getWindowRightPanelWidth() {
		return windowRightPanelWidth;
	}
}
