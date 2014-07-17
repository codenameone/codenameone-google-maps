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

import android.app.Activity;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.codename1.impl.android.AndroidNativeUtil;
import java.util.HashMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import java.util.Map;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import com.google.android.gms.maps.model.LatLngBounds;
import android.graphics.Point;
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
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                initMaps();
            } else {
                AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        initMaps();
                    }
                });
            }
        }
        AndroidNativeUtil.registerViewRenderer(MapView.class, new AndroidNativeUtil.BitmapViewRenderer() {
            public Bitmap renderViewOnBitmap(View v, int w, int h) {
                // prevent potential exception during transitions
                if(w < 10 || h < 10) {
                    return null;
                }
                final MapView mv = (MapView)v;
                if(mv.getParent() == null || mv.getHeight() < 10 || mv.getWidth() < 10) {
                    return null;
                }
                final Bitmap[] finished = new Bitmap[1];
                mv.getMap().snapshot(new GoogleMap.SnapshotReadyCallback() {
                    public void onSnapshotReady(Bitmap snapshot) {
                        /*mv.setDrawingCacheEnabled(true);
                        Bitmap backBitmap = mv.getDrawingCache();
                        Bitmap bmOverlay = Bitmap.createBitmap(backBitmap.getWidth(), backBitmap.getHeight(),backBitmap.getConfig());
                        Canvas canvas = new Canvas(bmOverlay);
                        canvas.drawBitmap(snapshot, new Matrix(), null);
                        canvas.drawBitmap(backBitmap, 0, 0, null);*/
                        //mv.setVisibility(vis);
                        synchronized(finished) {
                            finished[0] = snapshot;//bmOverlay;
                            finished.notify();
                        }
                    }
                });
                /*AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int vis = mv.getVisibility();
                        mv.setVisibility(View.VISIBLE);
                    }
                });*/
                System.out.println("Blocking to render view on bitmap");
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
                System.out.println("Finished blocking to render view on bitmap");
                return finished[0];
            }
        });
    }

    private static void initMaps() {
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
        System.out.println("Blocking for zoom");
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = mapInstance.getCameraPosition().zoom;
            }
        });
        System.out.println("Finished blocking for zoom");
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
        System.out.println("Blocking for minZoom");
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = (int)mapInstance.getMinZoomLevel();
            }
        });
        System.out.println("Finished blocking for minZoom");
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
        System.out.println("Blocking for latitude");
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = mapInstance.getCameraPosition().target.latitude;
            }
        });
        System.out.println("Finished blocking for latitude");
        return result[0];
    }

    public double getLongitude() {
        final double[] result = new double[1];
        System.out.println("Blocking for longitude");
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = mapInstance.getCameraPosition().target.longitude;
            }
        });
        System.out.println("Finished blocking for longitude");
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
    
    public android.view.View createNativeMap(int mapId) {
        this.mapId = mapId;
        System.out.println("Blocking creation");
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                try {
                    view = new MapView(AndroidNativeUtil.getActivity());
                    view.onCreate(AndroidNativeUtil.getActivationBundle());
                    mapInstance = view.getMap();
                    if(mapInstance == null) {
                        view = null;
                        System.out.println("Failed to get map instance, it seems google play services are not installed");
                        return;
                    }
                    mapInstance.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        public boolean onMarkerClick (Marker marker) {
                            Long val = listeners.get(marker);
                            if(val != null) {
                                MapContainer.fireMarkerEvent(InternalNativeMapsImpl.this.mapId, val.longValue());
                                return true;
                            }
                            return false;
                        }
                    });
                    mapInstance.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                       public void onCameraChange(CameraPosition position) {
                           MapContainer.fireMapChangeEvent(InternalNativeMapsImpl.this.mapId, (int)position.zoom, position.target.latitude, position.target.longitude);
                       }
                    });
                    mapInstance.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        public void onMapClick(LatLng point) {
                            Point p = mapInstance.getProjection().toScreenLocation(point);
                            MapContainer.fireTapEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
                        }
                    });
                    mapInstance.setMyLocationEnabled(showMyLocation);
                    mapInstance.getUiSettings().setRotateGesturesEnabled(rotateGestureEnabled);
                } catch (Exception e) {
                    System.out.println("Failed to initialize, google play services not installed: " + e);
                    e.printStackTrace();
                    view = null;
                    return;
                }
            }
        });
        System.out.println("Finished blocking creation");
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
        System.out.println("Blocking max zoom");
        AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                result[0] = (int)mapInstance.getMaxZoomLevel();
            }
        });
        System.out.println("Finished blocking max zoom");
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
        System.out.println("On create..");
        if(view != null) {
            view.onCreate(savedInstanceState);
        }
    }

    public void onResume() {
        System.out.println("On resume..");
        if(view != null) {
            view.onResume();
        }
    }

    public void onPause() {
        System.out.println("On pause..");
        if(view != null) {
            view.onPause();
        }
    }

    public void onDestroy() {
        System.out.println("On destroy..");
        if(view != null) {
            view.onDestroy();
        }
    }

    public void onSaveInstanceState(Bundle b) {
        System.out.println("On save instance state..");
        if(view != null) {
            view.onSaveInstanceState(b);
        }
    }

    public void onLowMemory() {
        System.out.println("On low memory..");
        if(view != null) {
            view.onLowMemory();
        }
    }

    public void deinitialize() {
        AndroidNativeUtil.removeLifecycleListener(this);
    }

    public void initialize() {
        AndroidNativeUtil.addLifecycleListener(this);        
        AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                view.invalidate();
                view.onPause();
                view.onResume();
            }
        });
    }
}
