package com.codename1.googlemaps;


/**
 *  An abstract Map API that encapsulates the device native map and seamlessly replaces
 *  it with MapComponent when unsupported by the platform.
 * 
 *  @author Shai Almog
 */
public class MapContainer extends Container {

	/**
	 *  Map type for native maps
	 */
	public static final int MAP_TYPE_TERRAIN = 1;

	/**
	 *  Map type for native maps
	 */
	public static final int MAP_TYPE_HYBRID = 2;

	/**
	 *  Map type for native maps
	 */
	public static final int MAP_TYPE_NONE = 3;

	/**
	 *  Default constructor creates an instance with the standard OpenStreetMap version if necessary
	 */
	public MapContainer() {
	}

	/**
	 *  Uses the given provider in case of a fallback
	 *  
	 *  @param provider the map provider
	 */
	public MapContainer(MapProvider provider) {
	}

	/**
	 *  Sets the color used to stroke paths on the map.
	 *  @param color The color
	 */
	public void setPathStrokeColor(int color) {
	}

	/**
	 *  Gets the color used to stroke paths on the map.
	 *  @return The color
	 */
	public int getPathStrokeColor() {
	}

	/**
	 *  Sets the pixel width used to stroke paths on the map.
	 *  @param width The pixel width.
	 */
	public void setPathStrokeWidth(int width) {
	}

	/**
	 *  Gets the pixel width used to stroke paths on the map.
	 *  @return The pixel width.
	 */
	public int getPathStrokeWidth() {
	}

	/**
	 *  @inheritDoc
	 */
	@java.lang.Override
	protected void initComponent() {
	}

	/**
	 *  @inheritDoc
	 */
	@java.lang.Override
	protected void deinitialize() {
	}

	/**
	 *  Returns true if native maps are used
	 *  @return false if the lightweight maps are used
	 */
	public boolean isNativeMaps() {
	}

	/**
	 *  Adds a component as a marker on the map.
	 *  @param marker The component to be placed on the map.
	 *  @param location The location of marker.
	 *  @param anchorU The horizontal alignment of the marker. 0.0 = align top edge with coord. 0.5 = align center.  1.0 = align bottom edge with coord.
	 *  @param anchorV The vertical alignment of the marker.  0.0 = align top edge with coord. 0.5 = align center.  1.0 = align bottom edge with coord.
	 */
	public void addMarker(Component marker, Coord location, float anchorU, float anchorV) {
	}

	/**
	 *  Adds a component as a marker on the map with default horizontal/vertical alignment (0.5, 1.0)
	 *  @param marker The component to add as a marker.
	 *  @param location The location of the marker.
	 *  @return A MapObject that can be used for later removing the marker.
	 */
	public MapContainer.MapObject addMarker(Component marker, Coord location) {
	}

	/**
	 *  Removes a component marker that was previously added using {@link #addMarker(com.codename1.ui.Component, com.codename1.maps.Coord) }
	 *  @param marker 
	 */
	public void removeMarker(Component marker) {
	}

	/**
	 *  Adds a marker to the map with the given attributes
	 *  @param icon the icon, if the native maps are used this value can be null to use the default marker
	 *  @param location the coordinate for the marker
	 *  @param text the string associated with the location
	 *  @param longText longer description associated with the location
	 *  @param onClick will be invoked when the user clicks the marker. Important: events are only sent when the native map is in initialized state
	 *  @return marker reference object that should be used when removing the marker 
	 */
	public MapContainer.MapObject addMarker(EncodedImage icon, Coord location, String text, String longText, ActionListener onClick) {
	}

	/**
	 *  Adds a marker to the map with the given attributes
	 *  @param opts The marker options.
	 *  @return marker reference object that should be used when removing the marker
	 */
	public MapContainer.MapObject addMarker(MapContainer.MarkerOptions opts) {
	}

	/**
	 *  Draws a path on the map
	 *  @param path the path to draw on the map
	 *  @return a map object instance that allows us to remove the drawn path
	 */
	public MapContainer.MapObject addPath(Coord[] path) {
	}

	/**
	 *  Returns the max zoom level of the map
	 * 
	 *  @return max zoom level
	 */
	public int getMaxZoom() {
	}

	/**
	 *  Returns the min zoom level of the map
	 * 
	 *  @return min zoom level
	 */
	public int getMinZoom() {
	}

	/**
	 *  Removes the map object from the map
	 *  @param obj the map object to remove
	 */
	public void removeMapObject(MapContainer.MapObject obj) {
	}

	/**
	 *  Removes all the layers from the map
	 */
	public void clearMapLayers() {
	}

	/**
	 *  Zoom to the given coordinate on the map
	 *  @param crd the coordinate
	 *  @param zoom the zoom level
	 */
	public void zoom(Coord crd, int zoom) {
	}

	/**
	 *  Pans and zooms to fit the given bounding box.
	 *  @param bounds The bounding box to display.
	 */
	public void fitBounds(BoundingBox bounds) {
	}

	/**
	 *  Returns the current zoom level
	 *  @return the current zoom level between min/max zoom
	 */
	public float getZoom() {
	}

	public BoundingBox getBoundingBox() {
	}

	/**
	 *  Sets the native map type to one of the MAP_TYPE constants
	 *  @param type one of the MAP_TYPE constants
	 */
	public void setMapType(int type) {
	}

	/**
	 *  Returns the native map type
	 *  @return one of the MAP_TYPE constants
	 */
	public int getMapType() {
	}

	/**
	 *  Position the map camera
	 *  @param crd the coordinate
	 */
	public void setCameraPosition(Coord crd) {
	}

	/**
	 *  Returns the position in the center of the camera
	 *  @return the position
	 */
	public Coord getCameraPosition() {
	}

	/**
	 *  Returns the lat/lon coordinate at the given x/y position
	 *  @param x the x position in component relative coordinate system
	 *  @param y the y position in component relative coordinate system
	 *  @return a lat/lon coordinate
	 */
	public Coord getCoordAtPosition(int x, int y) {
	}

	/**
	 *  Returns the screen position for the coordinate in component relative position
	 *  @param lat the latitude
	 *  @param lon the longitude
	 *  @return the x/y position in component relative position
	 */
	public Point getScreenCoordinate(double lat, double lon) {
	}

	/**
	 *  Returns the location on the screen for the given coordinate
	 *  @param c the coordinate
	 *  @return the x/y position in component relative position
	 */
	public Point getScreenCoordinate(Coord c) {
	}

	/**
	 *  Returns the screen points for a list of coordinates.  This is likely more efficient
	 *  than calling {@link #getScreenCoordinate(com.codename1.maps.Coord) } for each coordinate
	 *  in the list because this only involves a single call to the native layer.
	 *  @param coords The coordinates to convert to points.
	 *  @return A list of points relative to (0,0) of the map container.
	 */
	public java.util.List getScreenCoordinates(java.util.List coords) {
	}

	/**
	 *  @deprecated For internal use only.   This is only public to allow access from the internal UWP implementation because IKVM doesn't seem to allow access to package-private methods.
	 *  @param mapId
	 *  @param zoom
	 *  @param lat
	 *  @param lon 
	 */
	public static void fireMapChangeEvent(int mapId, int zoom, double lat, double lon) {
	}

	/**
	 *  Adds a listener to user tapping on a map location, this shouldn't fire for 
	 *  dragging.  Note that the (x, y) coordinate of tap events are relative to the 
	 *  MapComponent origin, and not the screen origin.
	 *  
	 *  @param e the tap listener
	 */
	public void addTapListener(ActionListener e) {
	}

	/**
	 *  Removes the listener to user tapping on a map location, this shouldn't fire for 
	 *  dragging.
	 *  
	 *  @param e the tap listener
	 */
	public void removeTapListener(ActionListener e) {
	}

	/**
	 *  Adds a listener to user long pressing on a map location, this shouldn't fire for 
	 *  dragging. Note the (x, y) coordinates of long press events are relative to the 
	 *  origin of the MapContainer and not the screen origin.
	 *  
	 *  @param e the tap listener
	 */
	public void addLongPressListener(ActionListener e) {
	}

	/**
	 *  Removes the long press listener to user tapping on a map location, this shouldn't fire for 
	 *  dragging.
	 *  
	 *  @param e the tap listener
	 */
	public void removeLongPressListener(ActionListener e) {
	}

	/**
	 *  Adds a listener to map panning/zooming Important: events are only sent when the native map is in initialized state
	 *  @param listener the listener callback
	 */
	public void addMapListener(MapListener listener) {
	}

	/**
	 *  Removes the map listener callback
	 *  @param listener the listener
	 */
	public void removeMapListener(MapListener listener) {
	}

	/**
	 *  Show my location is a feature of the native maps only that allows marking
	 *  a users location on the map with a circle
	 *  @return the showMyLocation
	 */
	public boolean isShowMyLocation() {
	}

	/**
	 *  Show my location is a feature of the native maps only that allows marking
	 *  a users location on the map with a circle
	 *  @param showMyLocation the showMyLocation to set
	 */
	public void setShowMyLocation(boolean showMyLocation) {
	}

	/**
	 *  @return the rotateGestureEnabled
	 */
	public boolean isRotateGestureEnabled() {
	}

	/**
	 *  @param rotateGestureEnabled the rotateGestureEnabled to set
	 */
	public final void setRotateGestureEnabled(boolean rotateGestureEnabled) {
	}

	/**
	 *  A class to encapsulate parameters for adding markers to map.
	 */
	public static class MarkerOptions {


		public MarkerOptions(Coord coord, EncodedImage icon) {
		}

		/**
		 *  Gets the icon image for this marker.
		 *  @return the icon
		 */
		public EncodedImage getIcon() {
		}

		/**
		 *  Gets the location of this marker
		 *  @return the location
		 */
		public Coord getLocation() {
		}

		/**
		 *  Returns the text for this marker.
		 *  @return the text
		 */
		public String getText() {
		}

		/**
		 *  Sets the text for this marker
		 *  @param text the text to set
		 *  @return Self for chaining
		 */
		public MapContainer.MarkerOptions text(String text) {
		}

		/**
		 *  Gets the long text for this marker.
		 *  @return the longText
		 */
		public String getLongText() {
		}

		/**
		 *  Sets the long text for this marker.
		 *  @param longText the longText to set
		 *  @return Self for chaining.
		 */
		public MapContainer.MarkerOptions longText(String longText) {
		}

		/**
		 *  Gets the onclick handler for this marker.
		 *  @return the onClick
		 */
		public ActionListener getOnClick() {
		}

		/**
		 *  Sets the onclick handler for this marker.
		 *  @param onClick the onClick to set
		 *  @return Self for chaining.
		 */
		public MapContainer.MarkerOptions onClick(ActionListener onClick) {
		}

		/**
		 *  Gets the horizontal alignment of this marker in (u,v) coordinates.  
		 *  0.0 = align left edge with coord. 0.5 = align center.  1.0 = align right edge with coord.
		 *  @return the anchorU
		 */
		public float getAnchorU() {
		}

		/**
		 *  Sets the horizontal alignment of this marker in (u,v) coordinates.
		 *  0.0 = align left edge with coord. 0.5 = align center.  1.0 = align right edge with coord.
		 *  @param anchorU the anchorU to set
		 */
		public MapContainer.MarkerOptions anchorU(float anchorU) {
		}

		/**
		 *  Gets the vertical alignment of this marker in (u,v) coordinates.
		 *  0.0 = align top edge with coord. 0.5 = align center.  1.0 = align bottom edge with coord.
		 *  @return the anchorV
		 */
		public float getAnchorV() {
		}

		/**
		 *  Sets the vertical alignment of this marker in (u,v) coordinates.
		 *  0.0 = align top edge with coord. 0.5 = align center.  1.0 = align bottom edge with coord.
		 *  @param anchorV the anchorV to set
		 */
		public MapContainer.MarkerOptions anchorV(float anchorV) {
		}

		/**
		 *  Sets the horizontal and vertical alignments of this marker in (u,v) coordinates.
		 *  @param anchorU 0.0 = align top edge with coord. 0.5 = align center.  1.0 = align bottom edge with coord.
		 *  @param anchorV 0.0 = align top edge with coord. 0.5 = align center.  1.0 = align bottom edge with coord.
		 *  @return Self for chaining.
		 */
		public MapContainer.MarkerOptions anchor(float anchorU, float anchorV) {
		}
	}

	/**
	 *  Object on the map
	 */
	public static class MapObject {


		public MapObject() {
		}
	}
}
