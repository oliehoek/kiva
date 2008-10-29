/**An object should implement this interface if it is renderable.
 * Any interactable GUI's of a renderable object should be created via the static
 * alphabetsoup.framework.userinterface.RenderWindow.getGui().
 */
package alphabetsoup.userinterface;

/**Interface which must be extended for an entity to be rendered to the screen.
 * @author Chris Hazard
 */
public interface Renderable {
	/**Renders the object to the screen.
	 */
	void render();

	/**Renders details of the object next to or over the object 
	 */
	void renderOverlayDetails();
	
	/**Renders details of the object to a pre-translated location
	 */
	void renderDetails();
	
	/**isMouseOver is used to see whether the mouse cursor is over an object for selection and displaying details
	 * @param mouse_x		X position of mouse cursor in object space
	 * @param mouse_y		X position of mouse cursor in object space
	 * @return				Return true if the mouse is over the object, false otherwise
	 */
	boolean isMouseOver(float mouse_x, float mouse_y);
}
