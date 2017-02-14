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

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.codename1.impl.android.AndroidNativeUtil;
import java.util.HashMap;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.ViewGroup;
import android.os.Bundle;
import com.codename1.impl.android.AndroidImplementation;
import com.codename1.impl.android.LifecycleListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import android.os.Looper;
import android.view.View;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;
import android.graphics.Point;
//import com.codename1.impl.android.AndroidImplementation.PeerDraw;
import com.codename1.io.Log;
import com.codename1.ui.Display;
import com.google.android.gms.maps.model.CameraPosition;

public class InternalNativeMapsImpl implements LifecycleListener {
    private int mapId;
    private MapView view;
    private GoogleMap mapInstance;
    private static int uniqueIdCounter = 0;
    private HashMap<Long, Marker> markerLookup = new HashMap<Long, Marker>();
    private HashMap<Marker, Long> listeners = new HashMap<Marker, Long>();
    private static boolean supported = true;
    private HashMap<Long, Polyline> paths = new HashMap<Long, Polyline>();
    private PolylineOptions currentPath;
    private LatLng lastPosition;
    private Point lastPoint;
    private boolean showMyLocation;
    private boolean rotateGestureEnabled;

    static {
        if(AndroidNativeUtil.getActivity() != null) {
            android.util.Log.d("CN1 Maps", "Initializing maps");
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                initMaps();
            } else {
                AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        initMaps();
                    }
                });
            }
        } else {
            android.util.Log.d("CN1 Mapss", "Did not initialize maps because activity was null");
        }
        AndroidNativeUtil.registerViewRenderer(MapView.class, new AndroidNativeUtil.BitmapViewRenderer() {
            private boolean rendering;
            public Bitmap renderViewOnBitmap(View v, int w, int h) {
                if (rendering) {
                    return null;
                }
                rendering = true;
                try {
                    // prevent potential exception during transitions
                    if(w < 10 || h < 10) {
                        rendering = false;
                        return null;
                    }
                    final MapView mv = (MapView)v;
                    if(mv.getParent() == null || mv.getHeight() < 10 || mv.getWidth() < 10) {
                        return null;
                    }
                    final Bitmap[] finished = new Bitmap[1];
                    AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {

                        public void run() {
                            mv.getMap().snapshot(new GoogleMap.SnapshotReadyCallback() {
                                public void onSnapshotReady(Bitmap snapshot) {

                                    synchronized(finished) {
                                        finished[0] = snapshot;//bmOverlay;
                                        finished.notify();
                                    }
                                }
                            });
                        }

                    });

                    com.codename1.ui.Display.getInstance().invokeAndBlock(new Runnable() {
                        @Override
                        public void run() {
                            synchronized(finished) {
                                while(finished[0] == null) {
                                    try {
                                        finished.wait(100);
                                    } catch(InterruptedException er) {}
                                }
                            }
                        }
                    });
                    return finished[0];
                } finally {
                    rendering = false;
                }
            }
        });
    }
    private static boolean initialized = false;
    private static void initMaps() {
        if (!initialized) {
            initialized = true;
            try {
                // this triggers the creation of the maps so they are ready when the peer component is invoked
                MapsInitializer.initialize(AndroidNativeUtil.getActivity());
                MapView v = new MapView(AndroidNativeUtil.getActivity());

                v.onCreate(AndroidNativeUtil.getActivationBundle());
                v.onResume();
                v.getMap();
            } catch (Exception e) {
                supported = false;
                System.out.println("Failed to initialize, google play services not installed: " + e);
                e.printStackTrace();
            }
        } 
    }
    
    public long addMarker(final byte[] icon, final double lat, final double lon, final String text, final String snippet, final boolean callback) {
        uniqueIdCounter++;
        final long key = uniqueIdCounter;
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                MarkerOptions mo = new MarkerOptions();
                if(text != null) {
                    mo.title(text);
                }
                if(icon != null) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(icon, 0, icon.length);
                    mo.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                }
                if(snippet != null) {
                    mo.snippet(snippet);
                }
                mo.position(new LatLng(lat, lon));

                Marker m = mapInstance.addMarker(mo);
                if(callback) {
                    listeners.put(m, key);
                }
                markerLookup.put(key, m);
            }
        });
        return key;
    }

    public long beginPath() {
        currentPath = new PolylineOptions();
        return 1;
    }

    public void setPosition(final double lat, final double lon) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mapInstance.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
            }
        });
    }

    public float getZoom() {
        final float[] result = new float[1];
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = mapInstance.getCameraPosition().zoom;
            }
        });
        return result[0];
    }

    public void setZoom(final double lat, final double lon, final float zoom) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mapInstance.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), zoom));
            }
        });
    }

    public void addToPath(long param, double param1, double param2) {
        currentPath.add(new LatLng(param1, param2));
    }

    public void removeAllMarkers() {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mapInstance.clear();
                markerLookup.clear();
                listeners.clear();
            }
        });
    }

    public int getMinZoom() {
        final int[] result = new int[1];
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = (int)mapInstance.getMinZoomLevel();
            }
        });
        return result[0];
    }

    public void removeMapElement(final long param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Marker m = markerLookup.get(param);
                if(m != null) {
                    m.remove();
                    markerLookup.remove(param);
                    listeners.remove(m);
                    return;
                }
                
                Polyline p = paths.get(param);
                if(p != null) {
                    p.remove();
                    paths.remove(param);
                }
            }
        });
    }

    public double getLatitude() {
        final double[] result = new double[1];
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = mapInstance.getCameraPosition().target.latitude;
            }
        });
        return result[0];
    }

    public double getLongitude() {
        final double[] result = new double[1];
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = mapInstance.getCameraPosition().target.longitude;
            }
        });
        return result[0];
    }

    public void setMapType(final int param) {
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                switch(param) {
                    case MapContainer.MAP_TYPE_HYBRID:
                        mapInstance.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        return;
                    case MapContainer.MAP_TYPE_TERRAIN:
                        mapInstance.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        return;
                }
                mapInstance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });
    }

    public void setShowMyLocation(boolean show) {
        showMyLocation = show;
        if(mapInstance != null) {
            AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapInstance.setMyLocationEnabled(showMyLocation);
                }
            });            
        }
    }

    private void setupMap() {

    }

    private void installListeners() {
        /*
        if (mapInstance == null) {
            view = null;
            System.out.println("Failed to get map instance, it seems google play services are not installed");
            return;
        }*/
        view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapInstance = googleMap;
                mapInstance.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker marker) {
                        Long val = listeners.get(marker);
                        if (val != null) {
                            MapContainer.fireMarkerEvent(InternalNativeMapsImpl.this.mapId, val.longValue());
                            return true;
                        }
                        return false;
                    }
                });
                mapInstance.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    public void onCameraChange(CameraPosition position) {
                        MapContainer.fireMapChangeEvent(InternalNativeMapsImpl.this.mapId, (int) position.zoom, position.target.latitude, position.target.longitude);
                    }
                });
                mapInstance.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    public void onMapClick(LatLng point) {
                        Point p = mapInstance.getProjection().toScreenLocation(point);
                        MapContainer.fireTapEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
                    }
                });
                mapInstance.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                    public void onMapLongClick(LatLng point) {
                        Point p = mapInstance.getProjection().toScreenLocation(point);
                        MapContainer.fireLongPressEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
                    }
                });
                mapInstance.setMyLocationEnabled(showMyLocation);
                mapInstance.getUiSettings().setRotateGesturesEnabled(rotateGestureEnabled);
            }
        });

    }

    public android.view.View createNativeMap(int mapId) {
        this.mapId = mapId;
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                try {
                    view = new MapView(AndroidNativeUtil.getActivity());
                    view.onCreate(AndroidNativeUtil.getActivationBundle());
                    mapInstance = view.getMap();
                    installListeners();

                } catch (Exception e) {
                    System.out.println("Failed to initialize, google play services not installed: " + e);
                    e.printStackTrace();
                    view = null;
                    return;
                }
            }
        });
        return view;
    }

    public int getMapType() {
        switch(mapInstance.getMapType()) {
            case GoogleMap.MAP_TYPE_HYBRID:
                return MapContainer.MAP_TYPE_HYBRID;
            case GoogleMap.MAP_TYPE_TERRAIN:
            case GoogleMap.MAP_TYPE_SATELLITE:
                return MapContainer.MAP_TYPE_TERRAIN;
        }
        return MapContainer.MAP_TYPE_NONE;
    }

    public int getMaxZoom() {
        final int[] result = new int[1];
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = (int)mapInstance.getMaxZoomLevel();
            }
        });
        return result[0];
    }
    
    public void setRotateGestureEnabled(boolean e) {
        rotateGestureEnabled = e;
        if(mapInstance != null) { 
            AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapInstance.getUiSettings().setRotateGesturesEnabled(rotateGestureEnabled);
                }
            });
        }
    }

    public long finishPath(long param) {
        uniqueIdCounter++;
        final long key = uniqueIdCounter;
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                paths.put(key, mapInstance.addPolyline(currentPath));
            }
        });
        return key;
    }

    public void calcScreenPosition(final double lat, final double lon) {
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                lastPoint = mapInstance.getProjection().toScreenLocation(new LatLng(lat, lon));
            }
        });
    }
    
    public int getScreenX() {
        return lastPoint.x;
    }
    
    public int getScreenY() {
        return lastPoint.y;
    }

    public void calcLatLongPosition(final int x, final int y) {
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                lastPosition = mapInstance.getProjection().fromScreenLocation(new Point(x, y));
            }
        });
    }
    
    public double getScreenLat() {
        return lastPosition.latitude;
    }
    
    public double getScreenLon() {
        return lastPosition.longitude;
    }
        
    public boolean isSupported() {
        return supported;
    }

    public void onCreate(Bundle savedInstanceState) {
        try {
            if (view != null) {
                view.onCreate(savedInstanceState);
                initMaps();
                mapInstance = view.getMap();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        try {
            if(view != null) {
                mapInstance = view.getMap();
                view.onResume();
                installListeners();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        try {
            if(view != null) {
                view.onPause();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onDestroy() {
        try {
            if(view != null) {
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                view.onDestroy();
                initialized = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSaveInstanceState(Bundle b) {
        try {
            if(view != null) {
                view.onSaveInstanceState(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLowMemory() {
        try {
            if(view != null) {
                view.onLowMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deinitialize() {
        AndroidNativeUtil.removeLifecycleListener(this);
    }

    public void initialize() {
        AndroidNativeUtil.addLifecycleListener(this);        
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    view.invalidate();
                    view.onPause();
                    view.onResume();
                } catch (Exception e) {
                    Log.e(e);
                }
            }
        });
    }
}
