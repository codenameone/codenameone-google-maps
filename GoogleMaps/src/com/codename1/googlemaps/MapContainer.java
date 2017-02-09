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

import com.codename1.io.Log;
import com.codename1.javascript.JSFunction;
import com.codename1.javascript.JSObject;
import com.codename1.javascript.JavascriptContext;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
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
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.PeerComponent;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Point;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.util.EventDispatcher;
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
    private MapComponent internalLightweightCmp;
    private BrowserComponent internalBrowser;
    private  JavascriptContext browserContext;
    private ArrayList<MapListener> listeners;
    private PointsLayer points;
    
    private ArrayList<MapObject> markers = new ArrayList<MapObject>();
    private static HashMap<Integer, MapContainer> instances = new HashMap<Integer, MapContainer>();
    private static int currentMapId;
    private int mapId;
    private boolean showMyLocation;
    private boolean rotateGestureEnabled;
    
    private EventDispatcher tapListener;
    
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
        this(provider, null);
    }
    
    /**
     * Uses HTML JavaScript google maps on fallback platforms instead of the tiled map
     * @param javaScriptMapsAPIKey the API key for HTML maps
     */
    public MapContainer(String javaScriptMapsAPIKey) {
        this(null, javaScriptMapsAPIKey);
    }
    
    /**
     * Uses the given provider in case of a fallback
     * 
     * @param provider the map provider
     */
    private MapContainer(MapProvider provider, String htmlApiKey) {
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
        if(provider != null) {
            internalLightweightCmp = new MapComponent(provider) {
                private boolean drg = false;

                @Override
                public void pointerDragged(int x, int y) {
                    super.pointerDragged(x, y); 
                    drg = true;
                }

                @Override
                public void pointerDragged(int[] x, int[] y) {
                    super.pointerDragged(x, y); 
                    drg = true;
                }

                @Override
                public void pointerReleased(int x, int y) {
                    super.pointerReleased(x, y); 
                    if(!drg) {
                        fireTapEvent(x, y);
                    }
                    drg = false;
                }

            };
            addComponent(BorderLayout.CENTER, internalLightweightCmp);
        } else {
            internalBrowser = new BrowserComponent();
            internalBrowser.putClientProperty("BrowserComponent.firebug", 
                    Display.getInstance().getProperty("MapContainer.firebug", "").toLowerCase().equals("true")
            );
            Location loc = LocationManager.getLocationManager().getLastKnownLocation();
            internalBrowser.setPage(
                        "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "  <head>\n" +
                        "    <title>Simple Map</title>\n" +
                        "    <meta name=\"viewport\" content=\"initial-scale=1.0\">\n" +
                        "    <meta charset=\"utf-8\">\n" +
                        "    <style>\n" +
                        "      /* Always set the map height explicitly to define the size of the div\n" +
                        "       * element that contains the map. */\n" +
                        "      #map {\n" +
                        "        height: 100%;\n" +
                        "      }\n" +
                        "      /* Optional: Makes the sample page fill the window. */\n" +
                        "      html, body {\n" +
                        "        height: 100%;\n" +
                        "        margin: 0;\n" +
                        "        padding: 0;\n" +
                        "      }\n" +
                        "    </style>\n" +
                        "  </head>\n" +
                        "  <body>\n" +
                        "    <div id=\"map\"></div>\n" +
                        "    <script>\n" +                               
                        "      var map;\n" +
                        "      function initMap() {\n" +
                        "        var origin = {lat: "+ loc.getLatitude() + ", lng: "  + loc.getLongitude() + "};\n" +
                        "        map = new google.maps.Map(document.getElementById('map'), {\n" +
                        "          center: origin,\n" +
                        "          zoom: 8\n" +
                        "        });\n" +
                        "        var clickHandler = new ClickEventHandler(map, origin);\n" +
                        "      }\n" +
                        "      var ClickEventHandler = function(map, origin) {\n" +
                        "        var self = this;\n" +
                        "        this.origin = origin;\n" +
                        "        this.map = map;\n" +
                        "        //this.directionsService = new google.maps.DirectionsService;\n" +
                        "        //this.directionsDisplay = new google.maps.DirectionsRenderer;\n" +
                        "        //this.directionsDisplay.setMap(map);\n" +
                        "        //this.placesService = new google.maps.places.PlacesService(map);\n" +
                        "        //this.infowindow = new google.maps.InfoWindow;\n" +
                        "        //this.infowindowContent = document.getElementById('infowindow-content');\n" +
                        "        //this.infowindow.setContent(this.infowindowContent);\n" +
                        "\n" +
//                        "        google.maps.event.addListener(this.map, 'click', function(evt) {\n" +
//                        "           self.handleClick(evt);\n" +
//                        "        });" +
                                "this.map.addListener('click', this.handleClick.bind(this));\n" +
                        "      };\n" +
                        "      ClickEventHandler.prototype.handleClick = function(event) {\n" + 
                        "           //document.getElementById('map').innerHTML = 'foobar';\n" +
                        "           cn1OnClickCallback(event);" +
                        "      };\n" +
                        "    </script>\n" +
                        "    <script src=\"https://maps.googleapis.com/maps/api/js?key=" + 
                        htmlApiKey +
                        "&callback=initMap\"\n" +
                        "    async defer></script>\n" +
                        "  </body>\n" +
                        "</html>", "/");
            browserContext = new JavascriptContext(internalBrowser);
            internalBrowser.addWebEventListener("onLoad", new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JSObject window = (JSObject)browserContext.get("window");
                    window.set("cn1OnClickCallback", new JSFunction() {
                        public void apply(JSObject self, Object[] args) {
                            Log.p("Click");
                        }
                    });
                }
            });
            addComponent(BorderLayout.CENTER, internalBrowser);
        }
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
            if(internalLightweightCmp != null) {
                PointLayer pl = new PointLayer(location, text, icon);
                if(points == null) {
                    points = new PointsLayer();
                    internalLightweightCmp.addLayer(points);
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
                    points.addPoint(pl);
                    MapObject o = new MapObject();
                    o.point = pl;
                    o.callback = onClick;
                    markers.add(o);
                    return o;
                } 
            } else {
                // TODO: Browser component
            }
        }
        return null;
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
            if(internalLightweightCmp != null) {
                LinesLayer ll = new LinesLayer();
                ll.addLineSegment(path);

                internalLightweightCmp.addLayer(ll);
                MapObject o = new MapObject();
                o.lines = ll;
                markers.add(o);
                return o;
            } else {
                // TODO: Browser component                
                return null;
            }
        }
    }
    
    /**
     * Returns the max zoom level of the map
     *
     * @return max zoom level
     */
    public int getMaxZoom() {
        if(internalNative == null) {
            if(internalLightweightCmp != null) {
                return internalLightweightCmp.getMaxZoomLevel();
            } else {
                // TODO: Browser component
                return 20;
            }
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
            if(internalLightweightCmp != null) {
                return internalLightweightCmp.getMinZoomLevel();
            } else {
                // TODO: Browser component
                return 1;
            }
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
                if(internalLightweightCmp != null) {
                    internalLightweightCmp.removeLayer(obj.lines);
                } else {
                    // TODO: Browser component
                }
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
            if(internalLightweightCmp != null) {
                internalLightweightCmp.removeAllLayers();
                points = null;
            } else {
                // TODO: Browser component                
            }
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
            if(internalLightweightCmp != null) {
                internalLightweightCmp.zoomTo(crd, zoom);
            } else {
                // TODO: Browser component                
            }
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
            if(internalLightweightCmp != null) {
                return internalLightweightCmp.getZoomLevel();
            }
            // TODO: Browser component
            return 7;
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
            if(internalLightweightCmp != null) {
                internalLightweightCmp.zoomTo(crd, internalLightweightCmp.getZoomLevel());
            } else {
                // TODO: Browser component                
            }
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
            if(internalLightweightCmp != null) {
                return internalLightweightCmp.getCenter();
            } 
            // TODO: Browser component
            return new Coord(0, 0);
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
            if(internalLightweightCmp != null) {
                return internalLightweightCmp.getCoordFromPosition(x, y);
            }
            // TODO: Browser component
            return new Coord(0, 0);
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
            if(internalLightweightCmp != null) {
                return internalLightweightCmp.getPointFromCoord(new Coord(lat, lon));
            }
            // TODO: Browser component
            return new Point(0, 0);
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
    
    /**
     * Adds a listener to user tapping on a map location, this shouldn't fire for 
     * dragging.
     * 
     * @param e the tap listener
     */
    public void addTapListener(ActionListener e) {
        if(tapListener == null) {
            tapListener = new EventDispatcher();
        }
        tapListener.addListener(e);
    }
    
    /**
     * Removes the listener to user tapping on a map location, this shouldn't fire for 
     * dragging.
     * 
     * @param e the tap listener
     */
    public void removeTapListener(ActionListener e) {
        if(tapListener == null) {
            return;
        }
        tapListener.removeListener(e);
        if(!tapListener.hasListeners()) {
            tapListener = null;
        }
    }
    
    static void fireTapEventStatic(int mapId, int x, int y) {
        final MapContainer mc = instances.get(mapId);
        if(mc != null) {
            mc.fireTapEvent(x, y);
        }
    }
    
    private void fireTapEvent(int x, int y) { 
        final MapContainer mc = instances.get(mapId);
        if(mc != null) {
            if(tapListener != null) {
                tapListener.fireActionEvent(new ActionEvent(this, x, y));
            }
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
            if(internalLightweightCmp != null) {
                internalLightweightCmp.addMapListener(listener);
            } else {
                // TODO: Browser component                
            }
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
            if(internalLightweightCmp != null) {
                internalLightweightCmp.removeMapListener(listener);
            } else {
                // TODO: Browser component
            }
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
