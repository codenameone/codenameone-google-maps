package com.codename1.googlemaps;


/**
 *  This is an internal implementation class
 * 
 *  @author Shai Almog
 *  @deprecated used internally please use MapContainer
 */
public interface InternalNativeMaps {

	public void setMapType(int type);

	public int getMapType();

	public int getMaxZoom();

	public int getMinZoom();

	public void setMarkerSize(int width, int height);

	public long addMarker(byte[] icon, double lat, double lon, String text, String longText, boolean callback, float anchorU, float anchorV);

	public long beginPath();

	public void addToPath(long pathId, double lat, double lon);

	public long finishPath(long pathId);

	public void removeMapElement(long id);

	public void removeAllMarkers();

	public PeerComponent createNativeMap(int mapId);

	public double getLatitude();

	public double getLongitude();

	public void setPosition(double lat, double lon);

	public void setZoom(double lat, double lon, float zoom);

	public float getZoom();

	public void deinitialize();

	public void initialize();

	public void calcScreenPosition(double lat, double lon);

	public int getScreenX();

	public int getScreenY();

	public void calcLatLongPosition(int x, int y);

	public double getScreenLat();

	public double getScreenLon();

	public void setShowMyLocation(boolean show);

	public void setRotateGestureEnabled(boolean e);

	public void setPathStrokeColor(int color);

	public int getPathStrokeColor();

	public void setPathStrokeWidth(int width);

	public int getPathStrokeWidth();
}
