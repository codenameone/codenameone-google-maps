/*
 * Copyright (c) 2014, Codename One LTD. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codename1.googlemaps;

import com.codename1.maps.Coord;
import com.codename1.maps.MapComponent;
import com.codename1.maps.MapListener;
import com.codename1.maps.layers.LinesLayer;
import com.codename1.maps.layers.PointLayer;
import com.codename1.maps.layers.PointsLayer;
import com.codename1.ui.Container;
import com.codename1.maps.providers.MapProvider;
import com.codename1.maps.providers.OpenStreetMapProvider;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.PeerComponent;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Point;
import com.codename1.ui.layouts.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An abstract Map API that encapsulates the device native map and seamlessly replaces
 * it with MapComponent when unsupported by the platform.
 *
 * @author Shai Almog
 */
public class MapContainer extends Container {
    /**
     * Map type for native maps
     */
    public static final int MAP_TYPE_TERRAIN = 1;

    /**
     * Map type for native maps
     */
    public static final int MAP_TYPE_HYBRID = 2;

    /**
     * Map type for native maps
     */
    public static final int MAP_TYPE_NONE = 3;
    
    private InternalNativeMaps internalNative;
    private MapComponent internalLightweight;
    private ArrayList<MapListener> listeners;
    private PointsLayer points;
    
    private ArrayList<MapObject> markers = new ArrayList<MapObject>();
    private static HashMap<Integer, MapContainer> instances = new HashMap<Integer, MapContainer>();
    private static int currentMapId;
    private int mapId;
    private boolean showMyLocation;
    private boolean rotateGestureEnabled;
    
    /**
     * Default constructor creates an instance with the standard OpenStreetMap version if necessary
     */
    public MapContainer() {
        this(new OpenStreetMapProvider());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    protected void initComponent() {
        instances.put(mapId, this);
        super.initComponent();
        if(isNativeMaps()) {
            internalNative.initialize();
            getComponentAt(0).setVisible(true);
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    protected void deinitialize() {
        super.deinitialize();
        instances.remove(mapId);
        if(internalNative != null) {
            internalNative.deinitialize();
        }
    }
    
    /**
     * Uses the given provider in case of a fallback
     * 
     * @param provider the map provider
     */
    public MapContainer(MapProvider provider) {
        super(new BorderLayout());
        internalNative = (InternalNativeMaps)NativeLookup.create(InternalNativeMaps.class);
        if(internalNative != null) {
            if(internalNative.isSupported()) {
                currentMapId++;
                mapId = currentMapId;
                PeerComponent p = internalNative.createNativeMap(mapId);
                
                // can happen if Google play services failed or aren't installed on an Android device
                if(p != null) {
                    addComponent(BorderLayout.CENTER, p);
                    return;
                }
            } 
            internalNative = null;
        }
        internalLightweight = new MapComponent(provider);
        addComponent(BorderLayout.CENTER, internalLightweight);
        setRotateGestureEnabled(true);
    }
    
    static void mapUpdated(int mapId) {
        final MapContainer mc = instances.get(mapId);
        if(mc != null) {
            Display.getInstance().callSerially(new Runnable() {
                public void run() {
                    mc.repaint();
                }
            });
        }
    }
    
    /**
     * Returns true if native maps are used
     * @return false if the lightweight maps are used
     */
    public boolean isNativeMaps() {
        return internalNative != null;
    }
    
    static void fireMarkerEvent(int mapId, final long markerId) {
        final MapContainer mc = instances.get(mapId);
        if(mc != null) {
            if(!Display.getInstance().isEdt()) {
                Display.getInstance().callSerially(new Runnable() {
                    public void run() {
                        mc.fireMarkerEvent(markerId);
                    }
                });
                return;
            }
            mc.fireMarkerEvent(markerId);
        }
    }
    
    void fireMarkerEvent(long markerId) {
        for(MapObject m : markers) {
            if(m.mapKey == markerId) {
                if(m.callback != null) {
                    m.callback.actionPerformed(new ActionEvent(m));
                }
                return;
            }
        }
    }
    
    /**
     * Adds a marker to the map with the given attributes
     * @param icon the icon, if the native maps are used this value can be null to use the default marker
     * @param location the coordinate for the marker
     * @param text the string associated with the location
     * @param longText longer description associated with the location
     * @param onClick will be invoked when the user clicks the marker. Important: events are only sent when the native map is in initialized state
     * @return marker reference object that should be used when removing the marker
     */
    public MapObject addMarker(EncodedImage icon, Coord location, String text, String longText, ActionListener onClick) {
        if(internalNative != null) {
            byte[] iconData = null;
            if(icon != null) {
                iconData = icon.getImageData();
            }
            long key = internalNative.addMarker(iconData, location.getLatitude(), location.getLongitude(), text, longText, onClick != null);
            MapObject o = new MapObject();
            o.mapKey = key;
            o.callback = onClick;
            markers.add(o);
            return o;
        } else {
            PointLayer pl = new PointLayer(location, text, icon);
            if(points == null) {
                points = new PointsLayer();
                internalLightweight.addLayer(points);
                points.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        PointLayer point = (PointLayer)evt.getSource();
                        for(MapObject o : markers) {
                            if(o.point == point) {
                                if(o.callback != null) {
                                    o.callback.actionPerformed(new ActionEvent(o));
                                }
                                return;
                            }
                        }
                    }
                });
            }
            points.addPoint(pl);
            MapObject o = new MapObject();
            o.point = pl;
            o.callback = onClick;
            markers.add(o);
            return o;
        }
    }
    
    /**
     * Draws a path on the map
     * @param path the path to draw on the map
     * @return a map object instance that allows us to remove the drawn path
     */
    public MapObject addPath(Coord... path) {
        if(internalNative != null) {
            long key = internalNative.beginPath();
            for(Coord c : path) {
                internalNative.addToPath(key, c.getLatitude(), c.getLongitude());
            }
            key = internalNative.finishPath(key);
            MapObject o = new MapObject();
            o.mapKey = key;
            markers.add(o);
            return o;
        } else {
            LinesLayer ll = new LinesLayer();
            ll.addLineSegment(path);
            
            internalLightweight.addLayer(ll);
            MapObject o = new MapObject();
            o.lines = ll;
            markers.add(o);
            return o;
        }
    }
    
    /**
     * Returns the max zoom level of the map
     *
     * @return max zoom level
     */
    public int getMaxZoom() {
        if(internalNative == null) {
            return internalLightweight.getMaxZoomLevel();
        }
        return internalNative.getMaxZoom();
    }
    
    /**
     * Returns the min zoom level of the map
     *
     * @return min zoom level
     */
    public int getMinZoom() {
        if(internalNative == null) {
            return internalLightweight.getMinZoomLevel();
        }
        return internalNative.getMinZoom();
    }
    
    /**
     * Removes the map object from the map
     * @param obj the map object to remove
     */
    public void removeMapObject(MapObject obj) {
        markers.remove(obj);
        if(internalNative != null) {
            internalNative.removeMapElement(obj.mapKey);
        } else {
            if(obj.lines != null) {
                internalLightweight.removeLayer(obj.lines);
            } else {
                points.removePoint(obj.point);
            }
        }
    }
    
    /**
     * Removes all the layers from the map
     */
    public void clearMapLayers() {
        if(internalNative != null) {
            internalNative.removeAllMarkers();
            markers.clear();
        } else {
            internalLightweight.removeAllLayers();
            points = null;
        }
    }
    
    /**
     * Zoom to the given coordinate on the map
     * @param crd the coordinate
     * @param zoom the zoom level
     */
    public void zoom(Coord crd, int zoom) {
        if(internalNative != null) {
            internalNative.setZoom(crd.getLatitude(), crd.getLongitude(), zoom);
        } else {
            internalLightweight.zoomTo(crd, zoom);
        }
    }
    
    /**
     * Returns the current zoom level
     * @return the current zoom level between min/max zoom
     */
    public float getZoom() {
        if(internalNative != null) {
            return internalNative.getZoom();
        } else {
            return internalLightweight.getZoomLevel();
        }        
    }

    /**
     * Sets the native map type to one of the MAP_TYPE constants
     * @param type one of the MAP_TYPE constants
     */
    public void setMapType(int type) {
        if(internalNative != null) {
            internalNative.setMapType(type);
        }
    }
    
    /**
     * Returns the native map type
     * @return one of the MAP_TYPE constants
     */
    public int getMapType() {
        if(internalNative != null) {
            return internalNative.getMapType();
        }        
        return MAP_TYPE_NONE;
    }
    
    /**
     * Position the map camera
     * @param crd the coordinate
     */
    public void setCameraPosition(Coord crd) {
        if(internalNative == null) {
            internalLightweight.zoomTo(crd, internalLightweight.getZoomLevel());
            return;
        }
        internalNative.setPosition(crd.getLatitude(), crd.getLongitude());
    }
    
    /**
     * Returns the position in the center of the camera
     * @return the position
     */
    public Coord getCameraPosition() {
        if(internalNative == null) {
            return internalLightweight.getCenter();
        }
        return new Coord(internalNative.getLatitude(), internalNative.getLongitude());
    }
    
    /**
     * Returns the lat/lon coordinate at the given x/y position
     * @param x the x position in component relative coordinate system
     * @param y the y position in component relative coordinate system
     * @return a lat/lon coordinate
     */
    public Coord getCoordAtPosition(int x, int y) {
        if(internalNative == null) {
            return internalLightweight.getCoordFromPosition(x, y);
        }
        internalNative.calcLatLongPosition(x, y);
        return new Coord(internalNative.getScreenLat(), internalNative.getScreenLon());
    }
    
    /**
     * Returns the screen position for the coordinate in component relative position
     * @param lat the latitude
     * @param lon the longitude
     * @return the x/y position in component relative position
     */
    public Point getScreenCoordinate(double lat, double lon) {
        if(internalNative == null) {
            return internalLightweight.getPointFromCoord(new Coord(lat, lon));
        }
        internalNative.calcScreenPosition(lat, lon);
        return new Point(internalNative.getScreenX(), internalNative.getScreenY());
    }
    
    /**
     * Returns the location on the screen for the given coordinate
     * @param c the coordinate
     * @return the x/y position in component relative position
     */
    public Point getScreenCoordinate(Coord c) {
        return getScreenCoordinate(c.getLatitude(), c.getLongitude());
    }

    static void fireMapChangeEvent(int mapId, final int zoom, final double lat, final double lon) {
        final MapContainer mc = instances.get(mapId);
        if(mc != null) {
            if(!Display.getInstance().isEdt()) {
                Display.getInstance().callSerially(new Runnable() {
                    public void run() {
                        mc.fireMapListenerEvent(zoom, lat, lon);
                    }
                });
                return;
            }
            mc.fireMapListenerEvent(zoom, lat, lon);
        }
    }
    
    void fireMapListenerEvent(int zoom, double lat, double lon) {
        // assuming always EDT
        if(listeners != null) {
            Coord c = new Coord(lat, lon);
            for(MapListener l : listeners) {
                l.mapPositionUpdated(this, zoom, c);
            }
        }
    }
    
    /**
     * Adds a listener to map panning/zooming Important: events are only sent when the native map is in initialized state
     * @param listener the listener callback
     */
    public void addMapListener(MapListener listener) {
        if(internalNative == null) {
            internalLightweight.addMapListener(listener);
            return;
        }
        if(listeners == null) {
            listeners = new ArrayList<MapListener>();
        }
        listeners.add(listener);
    }

    /**
     * Removes the map listener callback
     * @param listener the listener
     */
    public void removeMapListener(MapListener listener) {
        if(internalNative == null) {
            internalLightweight.removeMapListener(listener);
            return;
        }
        if(listeners == null) {
            return;
        }
        listeners.remove(listener);
    }

    /**
     * Show my location is a feature of the native maps only that allows marking
     * a users location on the map with a circle
     * @return the showMyLocation
     */
    public boolean isShowMyLocation() {
        return showMyLocation;
    }

    /**
     * Show my location is a feature of the native maps only that allows marking
     * a users location on the map with a circle
     * @param showMyLocation the showMyLocation to set
     */
    public void setShowMyLocation(boolean showMyLocation) {
        this.showMyLocation = showMyLocation;
        if(isNativeMaps()) {
            internalNative.setShowMyLocation(showMyLocation);
        }
    }

    /**
     * @return the rotateGestureEnabled
     */
    public boolean isRotateGestureEnabled() {
        return rotateGestureEnabled;
    }

    /**
     * @param rotateGestureEnabled the rotateGestureEnabled to set
     */
    public final void setRotateGestureEnabled(boolean rotateGestureEnabled) {
        this.rotateGestureEnabled = rotateGestureEnabled;
        if(isNativeMaps()) {
            internalNative.setRotateGestureEnabled(rotateGestureEnabled);
        }
    }
    
    /**
     * Object on the map
     */
    public static class MapObject {
        long mapKey;
        ActionListener callback;
        PointLayer point;
        LinesLayer lines;
    }
}
