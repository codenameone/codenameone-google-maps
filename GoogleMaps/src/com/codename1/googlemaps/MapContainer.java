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

import com.codename1.components.WebBrowser;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.javascript.JSFunction;
import com.codename1.javascript.JSObject;
import com.codename1.javascript.JavascriptContext;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.maps.BoundingBox;
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
import com.codename1.util.StringUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private EventDispatcher longPressListener;
    
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
        instances.remove(mapId);
        if(internalNative != null) {
            internalNative.deinitialize();
        }
        super.deinitialize();
        
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
    private MapContainer(MapProvider provider, final String htmlApiKey) {
        super(new BorderLayout());
        if (provider == null && "win".equals(Display.getInstance().getPlatformName())) {
            
            // Right now UWP gives an NPE when we use the internal browser
            // so disabling it for now.
            provider = new OpenStreetMapProvider();
        }
        internalNative = (InternalNativeMaps)NativeLookup.create(InternalNativeMaps.class);
        if(internalNative != null) {
            if(internalNative.isSupported()) {
                currentMapId++;
                mapId = currentMapId;
                PeerComponent p = internalNative.createNativeMap(mapId);
                
                // can happen if Google play services failed or aren't installed on an Android device
                if(p != null) {
                    //System.out.println("Adding native map "+p);
                    
                    addComponent(BorderLayout.CENTER, p);
                    return;
                } else {
                    //System.out.println("Failed to add native map");
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

                @Override
                public void longPointerPress(int x, int y) {
                    super.longPointerPress(x, y); 
                    fireLongPressEvent(x, y);
                }
            };
            addComponent(BorderLayout.CENTER, internalLightweightCmp);
        } else {
            internalBrowser = new BrowserComponent();
            internalBrowser.getAllStyles().setPadding(0,0,0,0);
            internalBrowser.getAllStyles().setMargin(0,0,0,0);

            initBrowserComponent(htmlApiKey);
            
            addComponent(BorderLayout.CENTER, internalBrowser);
        }
        setRotateGestureEnabled(true);
    }

    private BrowserBridge browserBridge = new BrowserBridge();
    
    private class BrowserBridge {
        List<Runnable> onReady = new ArrayList<Runnable>();
        private JSObject bridge;
        
        BrowserBridge() {
            
        }
        
        private void ready(Runnable r) {
            if (bridge != null) {
                if (!onReady.isEmpty()) {
                    List<Runnable> tmp = new ArrayList<Runnable>();
                    synchronized(onReady) {
                        tmp.addAll(onReady);
                        onReady.clear();
                    }
                    for (Runnable tr : tmp) {
                        tr.run();
                    }
                }
                if (r != null) {
                    r.run();
                }
            } else {
                if (r == null) {
                    return;
                }
                synchronized(onReady) {
                    onReady.add(r);
                }
            }
        }
        
        private void waitForReady() {
            int ctr = 0;
            while (bridge == null) {
                if (ctr++ > 500) {
                    throw new RuntimeException("Waited too long for browser bridge");
                }
                Display.getInstance().invokeAndBlock(new Runnable() {

                    public void run() {
                        try {
                            Thread.sleep(20);
                        } catch (Exception ex){}
                    }
                    
                });
            }
            
        }
    }
    
    private void initBrowserComponent(String htmlApiKey) {
        
        //System.out.println("About to check location");
        Location loc = LocationManager.getLocationManager().getLastKnownLocation();
        try {
            //if (true)return;
            //System.out.println("About to load map text");
            String str = Util.readToString(Display.getInstance().getResourceAsStream(null, "/com_codename1_googlemaps_MapContainer.html"));
            //System.out.println("Map text: "+str);
            str = StringUtil.replaceAll(str, "YOUR_API_KEY", htmlApiKey);
            //System.out.println("Finished setting API key");
            str = StringUtil.replaceAll(str, "//origin = MAPCONTAINER_ORIGIN", "origin = {lat: "+ loc.getLatitude() + ", lng: "  + loc.getLongitude() + "};");
            //System.out.println("Finished setting origin");
            internalBrowser.setPage(str, "/");
            internalBrowser.addWebEventListener("onLoad", new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    JavascriptContext ctx = new JavascriptContext(internalBrowser);
                    JSObject jsProxy = (JSObject)ctx.get("{}");
                    jsProxy.set("fireTapEvent", new JSFunction() {

                        public void apply(JSObject self, Object[] args) {
                            fireTapEvent(((Double)args[0]).intValue() + internalBrowser.getAbsoluteX(), ((Double)args[1]).intValue() + internalBrowser.getAbsoluteY());
                        }
                    });
                    
                    jsProxy.set("fireMapChangeEvent", new JSFunction() {

                        public void apply(JSObject self, Object[] args) {
                            int zoom = ((Double)args[0]).intValue();
                            double lat = (Double)args[1];
                            double lon = (Double)args[2];
                            fireMapListenerEvent(zoom, lat, lon);
                        }
                        
                    });
                    
                    jsProxy.set("fireMarkerEvent", new JSFunction() {
                        public void apply(JSObject self, Object[] args) {
                            int key = ((Double)args[0]).intValue();
                            fireMarkerEvent(key);
                        }
                    });
                    
                    JSObject window = (JSObject)ctx.get("window");
                    window.set("com_codename1_googlemaps_MapContainer", jsProxy);
                    //System.out.println("About to load bridge");
                    browserBridge.bridge = (JSObject)window.get("com_codename1_googlemaps_MapContainer_bridge");
                    //if (browserBridge.bridge != null) {
                    //    System.out.println("BrowserBridge pointer at 307 is "+browserBridge.bridge.toJSPointer());
                    //}
                    //System.out.println("Bridge is "+browserBridge.bridge);
                    if (browserBridge.bridge == null) {
                        window.set("com_codename1_googlemaps_MapContainer_onReady", new JSFunction() {

                            public void apply(JSObject self, Object[] args) {
                                //System.out.println("Browser bridge in JS onReady callback");
                                browserBridge.bridge = (JSObject)args[0];
                                //System.out.println("Browser bridge pointer at 316 is "+browserBridge.bridge.toJSPointer());
                            }

                        });
                    }
                
                    ///System.out.println("Bridge is ready");
                    browserBridge.ready(null);
                }
            });
            
            
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
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
    public MapObject addMarker(EncodedImage icon, Coord location, String text, String longText, final ActionListener onClick) {
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
                
                String uri = null;
                if(icon != null) {
                    uri = WebBrowser.createDataURI(icon.getImageData(), "image/png");
                } 
                browserBridge.waitForReady();
                long key = ((Double)browserBridge.bridge.call("addMarker", new Object[]{
                    uri,
                    location.getLatitude(),
                    location.getLongitude(),
                    text,
                    longText
                })).intValue();
                MapObject o = new MapObject();
                
                o.mapKey = key;
                o.callback = onClick;
                markers.add(o);
                //System.out.println("MapKey added "+o.mapKey);
                return o;
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
                browserBridge.waitForReady();
                StringBuilder json = new StringBuilder();
                json.append("[");
                boolean first = true;
                for(Coord c : path) {
                    if (first) {
                        first = false;
                    } else {
                        json.append(", ");
                    }
                    json.append("{\"lat\":").append(c.getLatitude()).append(", \"lon\": ").append(c.getLongitude()).append("}");
                }
                json.append("]");
                long key = ((Double)browserBridge.bridge.call("addPathAsJSON", new Object[]{json.toString()})).intValue();
                
                
                MapObject o = new MapObject();
                o.mapKey = key;
                markers.add(o);
                return o;
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
                browserBridge.waitForReady();
                return browserBridge.bridge.callInt("getMaxZoom");
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
                browserBridge.waitForReady();
                return browserBridge.bridge.callInt("getMinZoom");
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
                    browserBridge.waitForReady();
                    browserBridge.bridge.call("removeMapElement", new Object[]{obj.mapKey});
                    
                }
            } else {
                if(internalLightweightCmp != null) {
                    if (points != null) {
                        points.removePoint(obj.point);
                    }
                } else {
                    browserBridge.waitForReady();
                    browserBridge.bridge.call("removeMapElement", new Object[]{obj.mapKey});
                    
                }
                
                
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
                browserBridge.waitForReady();
                browserBridge.bridge.call("removeAllMarkers");
                markers.clear();
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
                browserBridge.waitForReady();
                browserBridge.bridge.call("zoom", new Object[]{ crd.getLatitude(), crd.getLongitude(), zoom});
                
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
            browserBridge.waitForReady();
            return browserBridge.bridge.callInt("getZoom");
            
        }        
    }

    public BoundingBox getBoundingBox() {
        Coord sw = this.getCoordAtPosition(getAbsoluteX(), getAbsoluteY() + getHeight());
        Coord ne = this.getCoordAtPosition(getAbsoluteX() + getWidth(), getAbsoluteY());
        return new BoundingBox(sw, ne);
        
    }
    
    /**
     * Sets the native map type to one of the MAP_TYPE constants
     * @param type one of the MAP_TYPE constants
     */
    public void setMapType(int type) {
        if(internalNative != null) {
            internalNative.setMapType(type);
        } else {
            if (internalLightweightCmp != null) {
                
            } else {
                // browser component
                browserBridge.waitForReady();
                browserBridge.bridge.call("setMapType", new Object[]{type});
            }
        }
    }
    
    /**
     * Returns the native map type
     * @return one of the MAP_TYPE constants
     */
    public int getMapType() {
        if(internalNative != null) {
            return internalNative.getMapType();
        } else if (browserBridge != null) {
            browserBridge.waitForReady();
            return browserBridge.bridge.callInt("getMapType");
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
                browserBridge.waitForReady();
                browserBridge.bridge.call(
                        "setCameraPosition", 
                        new Object[]{crd.getLatitude(), crd.getLongitude()}
                );
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
            } else {
                browserBridge.waitForReady();
                String pos = browserBridge.bridge.callString("getCameraPosition");
                try {
                    String latStr = pos.substring(0, pos.indexOf(" "));
                    String lnStr = pos.substring(pos.indexOf(" ")+1);
                    return new Coord(Double.parseDouble(latStr), Double.parseDouble(lnStr));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return new Coord(0, 0);
                }
                
            }

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
            browserBridge.waitForReady();
            x -= internalBrowser.getAbsoluteX();
            y -= internalBrowser.getAbsoluteY();
            //System.out.println("Browser bridge pointer here is "+browserBridge.bridge.toJSPointer());
            //Object res = browserBridge.bridge.call("getCoordAtPosition", new Object[]{x, y});
            //if (res instanceof Double) {
            //    int i = 0;
            //}
            String coord = (String)browserBridge.bridge.call("getCoordAtPosition", new Object[]{x, y});
            try {
                String xStr = coord.substring(0, coord.indexOf(" "));
                String yStr = coord.substring(coord.indexOf(" ")+1);
                return new Coord(Double.parseDouble(xStr), Double.parseDouble(yStr));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
            browserBridge.waitForReady();
            String coord = (String)browserBridge.bridge.call("getScreenCoord", new Object[]{lat, lon});
            try {
                String xStr = coord.substring(0, coord.indexOf(" "));
                String yStr = coord.substring(coord.indexOf(" ")+1);
                return new Point(
                        (int)Double.parseDouble(xStr) + internalBrowser.getAbsoluteX(), 
                        (int)Double.parseDouble(yStr) + internalBrowser.getAbsoluteY()
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
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
        if(tapListener != null) {
            tapListener.fireActionEvent(new ActionEvent(this, x, y));
        }
    }
    
    /**
     * Adds a listener to user long pressing on a map location, this shouldn't fire for 
     * dragging.
     * 
     * @param e the tap listener
     */
    public void addLongPressListener(ActionListener e) {
        if (longPressListener == null) {
            longPressListener = new EventDispatcher();
        }
        longPressListener.addListener(e);
    }

    /**
     * Removes the long press listener to user tapping on a map location, this shouldn't fire for 
     * dragging.
     * 
     * @param e the tap listener
     */
    public void removeLongPressListener(ActionListener e) {
        if (longPressListener != null) {
            longPressListener.removeListener(e);
        }
    }

    static void fireLongPressEventStatic(int mapId, int x, int y) {
        final MapContainer mc = instances.get(mapId);
        if (mc != null) {
            mc.fireLongPressEvent(x, y);
        }
    }

    private void fireLongPressEvent(int x, int y) {
        if (longPressListener != null) {
            longPressListener.fireActionEvent(new ActionEvent(this, x, y));
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
        if(internalNative == null && internalLightweightCmp != null) {
            internalLightweightCmp.addMapListener(listener);
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
        if(internalNative == null && internalLightweightCmp != null) {
            internalLightweightCmp.removeMapListener(listener);
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
