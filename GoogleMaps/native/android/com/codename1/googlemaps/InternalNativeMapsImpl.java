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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.codename1.impl.android.AndroidImplementation;
import static com.codename1.impl.android.AndroidImplementation.getActivity;
import com.codename1.impl.android.AndroidNativeUtil;
import com.codename1.impl.android.LifecycleListener;
import com.codename1.ui.Display;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.HashMap;

public class InternalNativeMapsImpl implements LifecycleListener {
    private static String TAG = "InternalNativeMapsImpl";
    private static int mapId;
    private static MapView view;
    private static GoogleMap mapInstance;
    private static int uniqueIdCounter = 0;
    private HashMap<Long, Marker> markerLookup = new HashMap<Long, Marker>();
    private static HashMap<Marker, Long> listeners = new HashMap<Marker, Long>();
    private static boolean supported = true;
    private HashMap<Long, Polyline> paths = new HashMap<Long, Polyline>();
    private PolylineOptions currentPath;
    private LatLng lastPosition;
    private Point lastPoint;
    private static boolean showMyLocation;
    private static boolean rotateGestureEnabled;
    private static boolean initialized = false;
    private  final static boolean[] completed = new boolean[1];

    static {
        
        if(AndroidNativeUtil.getActivity() != null) {
            android.util.Log.i("CN1 Maps", "Initializing maps");
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                android.util.Log.i("CN1 Mapss", "static()->getMainLooper:ok");
                initMaps();
            } else {
                AndroidNativeUtil.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        android.util.Log.d("CN1 Mapss", "static()->runOnUiThread:ok");
                        initMaps();
                    }
                });
            }
        } else {
            android.util.Log.i("CN1 Mapss", "static()->Did not initialize maps because activity was null");
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
                                    } catch(InterruptedException er) {
                                        android.util.Log.d("CN1 Mapss", "invokeAndBlock()->ex:"+er.toString());
                                    }
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
                android.util.Log.i("CN1 Mapss", "initMaps()->initialized:"+initialized);
            } catch (Exception e) {
                supported = false;
                System.out.println("Failed to initialize, google play services not installed: " + e);
                e.printStackTrace();
            }
        } else
            android.util.Log.i("CN1 Mapss", "initMaps()->initialized:"+initialized);
        
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
                
                if (text!=null)
                    m.showInfoWindow();
                
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
                Log.i(TAG, "installListeners()->loaded:ok");
            }
        });

    }
    //--------------------------------------------------------------------------
    public android.view.View createNativeMap(int mapId) {
        this.mapId = mapId;
        runOnUiThreadAndBlock();
//        runOnUiThreadAndBlock(new Runnable() {
//            public void run() {
//                try {
//                    
//                    view = new MapView(AndroidNativeUtil.getActivity());
//                    Log.i(TAG, "createNativeMap()->view1:"+(view!=null?"ok":"null"));
//                    view.onCreate(AndroidNativeUtil.getActivationBundle());
//                    Log.i(TAG, "createNativeMap()->view2:"+(view!=null?"ok":"null"));
//                    mapInstance = view.getMap();
//                    Log.i(TAG, "createNativeMap()->mapInstance1:"+(mapInstance!=null?"ok":"null"));
//                    installListeners();
//
//                } catch (Exception e) {
//                    System.out.println("Failed to initialize, google play services not installed: " + e);
//                    e.printStackTrace();
//                    view = null;
//                    return;
//                }
//            }
//        });
        Log.i(TAG, "createNativeMap()->mapInstance2:"+(mapInstance!=null?"ok":"null"));
        Log.i(TAG, "createNativeMap()->view3:"+(view!=null?"ok":"null"));
        return view;
    }
    //--------------------------------------------------------------------------
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
        android.util.Log.d("CN1 Mapss", "onCreate()");
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
        android.util.Log.i("CN1 Mapss", "onResume()");
        try {
            if(view != null) {
                
                view.onResume();
                mapInstance = view.getMap();
                installListeners();
                android.util.Log.i("CN1 Mapss", "onResume()->ok");
            }else
                android.util.Log.i("CN1 Mapss", "onResume()->view:null");

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("CN1 Mapss", "onResume()->err:"+e.toString());
        }
    }

    public void onPause() {
        android.util.Log.i("CN1 Mapss", "onPause()");
        try {
            if(view != null) {
                view.onPause();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            android.util.Log.e("CN1 Mapss", "onPause()->err:"+ex.toString());
        }
    }

    public void onDestroy() {
        android.util.Log.i("CN1 Mapss", "onDestroy()");
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
            android.util.Log.e("CN1 Mapss", "onPause()->err:"+e.toString());
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
        Log.i(TAG, "onLowMemory()");
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
                    Log.e(TAG, "initialize()->err1:"+e.toString());
                }
            }
        });
    }
    //--------------------------------------------------------------------------
    public static void runOnUiThreadAndBlock() {
        
        if (getActivity() == null) {
            throw new RuntimeException("Cannot run on UI thread because getActivity() is null.  This generally means we are running inside a service in the background so UI access is disabled.");
        }

        //final boolean[] completed = new boolean[1];
        completed[0] = false;
        Log.i(TAG, "runOnUiThreadAndBlock()->completed:" + completed[0]);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //r.run();

                    try {

                        view = new MapView(AndroidNativeUtil.getActivity());
                        Log.i(TAG, "createNativeMap()->view1:" + (view != null ? "ok" : "null"));
                        view.onCreate(AndroidNativeUtil.getActivationBundle());
                        Log.i(TAG, "createNativeMap()->view2:" + (view != null ? "ok" : "null"));
                        mapInstance = view.getMap();
                        Log.i(TAG, "createNativeMap()->mapInstance1:" + (mapInstance != null ? "ok" : "null"));
                        view.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                mapInstance = googleMap;
                                mapInstance.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    public boolean onMarkerClick(Marker marker) {
                                        Long val = listeners.get(marker);
                                        if (val != null) {
                                            MapContainer.fireMarkerEvent(InternalNativeMapsImpl.mapId, val.longValue());
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                mapInstance.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                    public void onCameraChange(CameraPosition position) {
                                        MapContainer.fireMapChangeEvent(InternalNativeMapsImpl.mapId, (int) position.zoom, position.target.latitude, position.target.longitude);
                                    }
                                });
                                mapInstance.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                    public void onMapClick(LatLng point) {
                                        Point p = mapInstance.getProjection().toScreenLocation(point);
                                        MapContainer.fireTapEventStatic(InternalNativeMapsImpl.mapId, p.x, p.y);
                                    }
                                });
                                mapInstance.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                                    public void onMapLongClick(LatLng point) {
                                        Point p = mapInstance.getProjection().toScreenLocation(point);
                                        MapContainer.fireLongPressEventStatic(InternalNativeMapsImpl.mapId, p.x, p.y);
                                    }
                                });
                                mapInstance.setMyLocationEnabled(showMyLocation);
                                mapInstance.getUiSettings().setRotateGesturesEnabled(rotateGestureEnabled);
                                Log.i(TAG, "onMapReady()->loaded:ok");
                                Log.i(TAG, "onMapReady()->completed:" + completed[0]);
                                synchronized (completed) {
                                    completed[0] = true;
                                    completed.notify();
                                    Log.i(TAG, "onMapReady()->notify ->completed:" + completed[0]);
                                }


                            }
                        });
                        


                    } catch (Exception e) {
                        System.out.println("Failed to initialize, google play services not installed: " + e);
                        e.printStackTrace();
                        view = null;
                        return;
                    }

                } catch (Exception t) {
                    Log.e(TAG, "runOnUiThread()->err1:" + t.toString());
                }

            }
        });
        Display.getInstance().invokeAndBlock(new Runnable() {
            @Override
            public void run() {
                
                Log.i(TAG, "invokeAndBlock()->completed:" + completed[0]);
                synchronized (completed) {
                    while (!completed[0]) {
                        try {
                            Log.i(TAG, "invokeAndBlock()->completed.waiting...");
                            completed.wait();
                            //Log.i(TAG, "invokeAndBlock()->completed->continue");
                        } catch (InterruptedException err) {
                            Log.e(TAG, "invokeAndBlock()->err2:" + err.toString());
                        }
                    }
                    //completed[0] = false;
                    Log.i(TAG, "invokeAndBlock()->completed.continue EDT...");
                }
            }
        });

    }
    //--------------------------------------------------------------------------
//    public static void runOnUiThreadAndBlock(final Runnable r) {
//        
//        if (getActivity() == null) {
//            throw new RuntimeException("Cannot run on UI thread because getActivity() is null.  This generally means we are running inside a service in the background so UI access is disabled.");
//        }
//
//        final boolean[] completed = new boolean[1];
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //r.run();
//                    
//                    try {
//
//                        view = new MapView(AndroidNativeUtil.getActivity());
//                        Log.i(TAG, "createNativeMap()->view1:" + (view != null ? "ok" : "null"));
//                        view.onCreate(AndroidNativeUtil.getActivationBundle());
//                        Log.i(TAG, "createNativeMap()->view2:" + (view != null ? "ok" : "null"));
//                        mapInstance = view.getMap();
//                        Log.i(TAG, "createNativeMap()->mapInstance1:" + (mapInstance != null ? "ok" : "null"));
//                        view.getMapAsync(new OnMapReadyCallback() {
//                            @Override
//                            public void onMapReady(GoogleMap googleMap) {
//                                mapInstance = googleMap;
//                                mapInstance.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                                    public boolean onMarkerClick(Marker marker) {
//                                        Long val = listeners.get(marker);
//                                        if (val != null) {
//                                            MapContainer.fireMarkerEvent(InternalNativeMapsImpl.this.mapId, val.longValue());
//                                            return true;
//                                        }
//                                        return false;
//                                    }
//                                });
//                                mapInstance.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//                                    public void onCameraChange(CameraPosition position) {
//                                        MapContainer.fireMapChangeEvent(InternalNativeMapsImpl.this.mapId, (int) position.zoom, position.target.latitude, position.target.longitude);
//                                    }
//                                });
//                                mapInstance.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                                    public void onMapClick(LatLng point) {
//                                        Point p = mapInstance.getProjection().toScreenLocation(point);
//                                        MapContainer.fireTapEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
//                                    }
//                                });
//                                mapInstance.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//
//                                    public void onMapLongClick(LatLng point) {
//                                        Point p = mapInstance.getProjection().toScreenLocation(point);
//                                        MapContainer.fireLongPressEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
//                                    }
//                                });
//                                mapInstance.setMyLocationEnabled(showMyLocation);
//                                mapInstance.getUiSettings().setRotateGesturesEnabled(rotateGestureEnabled);
//                                Log.i(TAG, "onMapReady()->loaded:ok");
//                                
//                                Log.i(TAG, "onMapReady()->notify ->completed:" + completed[0]);
//                                synchronized (completed) {
//                                    completed[0] = true;
//                                    completed.notify();
//                                    Log.i(TAG, "onMapReady()->notify ->completed:" + completed[0]);
//                                }
//                            }
//                        });
//
//
//                    } catch (Exception e) {
//                        System.out.println("Failed to initialize, google play services not installed: " + e);
//                        e.printStackTrace();
//                        view = null;
//                        return;
//                    }
//                    
//                } catch (Exception  t) {
//                    Log.e(TAG, "runOnUiThread()->err1:"+t.toString());  
//                }
//                
//                Log.i(TAG, "runOnUiThread()->completed:"+completed[0]);
////                synchronized (completed) {
////                    completed[0] = true;
////                    completed.notify();
////                    Log.i(TAG, "runOnUiThread()->notify ->completed:"+completed[0]);
////                }
//                
//                
//            }
//        });
//        Display.getInstance().invokeAndBlock(new Runnable() {
//            @Override
//            public void run() {
//
//                synchronized (completed) {
//                    Log.i(TAG, "invokeAndBlock()->completed:"+completed[0]);
//                    while (!completed[0]) {
//                        try {
//                            completed.wait();
//                            Log.i(TAG, "invokeAndBlock()->completed.wait...");
//                        } catch (InterruptedException err) {
//                            Log.e(TAG, "invokeAndBlock()->err2:"+err.toString());
//                        }
//                    }
//                    Log.i(TAG, "invokeAndBlock()->completed.continue EDT...");
//                }
//            }
//        });
//    }


}//EndClass
