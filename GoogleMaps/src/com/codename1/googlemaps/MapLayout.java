/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.googlemaps;

import com.codename1.googlemaps.MapContainer.MapObject;
import com.codename1.googlemaps.MapContainer.MarkerOptions;
import com.codename1.maps.Coord;
import com.codename1.maps.MapListener;
import com.codename1.ui.CN;
import static com.codename1.ui.CN.callSerially;
import com.codename1.ui.Component;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.Point;
import com.codename1.ui.layouts.Layout;
import com.codename1.ui.util.UITimer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class MapLayout extends Layout implements MapListener {
        private static final String COORD_KEY = "$coord";
        private static final String POINT_KEY = "$point";
        private static final String MARKER_KEY = "$marker";
        private static final String HORIZONTAL_ALIGNMENT = "$align";
        private static final String VERTICAL_ALIGNMENT = "$valign";
        private final MapContainer map;
        private final Container actual;
        private boolean inUpdate;
        private Runnable nextUpdate;
        private int updateCounter;

        private Form installedForm;
        private List<MapObject> markers = new ArrayList<MapObject>();
        boolean pressed;
        
        
        
        private ActionListener pointerPressed = e -> {
            pressed = true;
            $(()->{
                Form f = getActual().getComponentForm();
                Component cmp = f != null ? f.getComponentAt(e.getX(), e.getY()) : null;
                if (cmp != null && (cmp == getMap() || getMap().contains(cmp))) {
                    if (getActual() == cmp || getActual().contains(cmp)) {
                        return;
                    }
                //if (getMap().contains(e.getX(), e.getY())){
                    
                    installMarkers();
                    $(()->{
                        getActual().revalidate();
                    });
                    
                }
            });
            
        };
        
        private MapContainer getMap() {
            return map;
        }
        
        private ActionListener pointerReleased = e->{
            if (pressed) {
                pressed = false;
                $(()->{
                    
                    uninstallMarkers();
                    $(()->{
                        getActual().getParent().repaint();
                    });
                    
                });
            }
            
            
        };
        
        private Container getActual() {
            return actual;
        }

        

        private int convertX(int x, int width, float anchorU) {
            return (int)(x - width * anchorU);
        }
        
        private int convertY(int y, int height, float anchorV) {
            return (int)(y - height * anchorV);
        }
        
        public MapLayout(MapContainer map, Container actual) {

            this.map = map;
            this.actual = actual;
            map.addMapListener(this);
            
        }
        
        void onInit() {
            installPointerListeners();
        }
        
        void onDeinit() {
            uninstallPointerListeners();
        }

        @Override
        public void addLayoutComponent(Object value, Component comp, Container c) {
            //super.addLayoutComponent(value, comp, c);
            comp.putClientProperty(COORD_KEY, (Coord) value);
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            uninstallMarker(comp);
            super.removeLayoutComponent(comp); //To change body of generated methods, choose Tools | Templates.
        }

        private Image generateMarkerImage(Component c) {
            return c.toImage();
        }
        
        private void installMarkerFor(Component c) {
            MapObject marker = (MapObject)c.getClientProperty(MARKER_KEY);
            if (marker != null) {
                map.removeMapObject(marker);
                markers.remove(marker);
                c.putClientProperty(MARKER_KEY, null);
                
            }
            Image im = generateMarkerImage(c);
            if (im == null) {
                return;
            }
            EncodedImage img = EncodedImage.createFromImage(generateMarkerImage(c), false);
            Float h = (Float)c.getClientProperty(HORIZONTAL_ALIGNMENT);
            if(h == null) {
                h = 0f;
            }
            Float v = (Float)c.getClientProperty(VERTICAL_ALIGNMENT);
            if(v == null) {
                v = 0f;
            }
            MarkerOptions markerOpts = new MarkerOptions((Coord)c.getClientProperty(COORD_KEY), img)
                    .anchorU(h)
                    .anchorV(v);
            
            marker = map.addMarker(markerOpts);
            c.putClientProperty(MARKER_KEY, marker);
            markers.add(marker);
            
        }
        
        private void installMarkers() {
            $(">", actual).each(child->{
                $(()->installMarkerFor(child));
            });
            
            $(()->{
                
                actual.setVisible(false);
                actual.setShouldCalcPreferredSize(true);
                actual.revalidate();
                


            });
            
        }
        
        private void uninstallMarkers() {
            actual.setVisible(true);
            actual.setShouldCalcPreferredSize(true);
            actual.revalidate();
            List<MapObject> toRemove = new ArrayList<MapObject>(markers);
            List<Component> children = new ArrayList<Component>();
            for (Component child : actual) {
                children.add(child);
            }
            for (MapObject marker : toRemove) {
                $(()->map.removeMapObject(marker));
            }
            for (Component child : children) {
                child.putClientProperty(MARKER_KEY, null);
            }
            
            markers.clear();
        }
        
        private void uninstallMarker(Component cmp) {
            MapObject marker = (MapObject)cmp.getClientProperty(MARKER_KEY);
            if (marker != null) {
                map.removeMapObject(marker);
                markers.remove(marker);
                cmp.putClientProperty(MARKER_KEY, null);
            }
        }

        @Override
        public boolean isConstraintTracking() {
            return true;
        }

        @Override
        public Object getComponentConstraint(Component comp) {
            return comp.getClientProperty(COORD_KEY);
        }

        @Override
        public boolean isOverlapSupported() {
            return true;
        }

        public static void setHorizontalAlignment(Component cmp, Float a) {
            cmp.putClientProperty(HORIZONTAL_ALIGNMENT, a);
        }

        public static void setVerticalAlignment(Component cmp, Float a) {
            cmp.putClientProperty(VERTICAL_ALIGNMENT, a);
        }

        
        private void installPointerListeners() {
            Form actualForm = actual.getComponentForm();
            if (actualForm != installedForm) {
                if (installedForm != null) {
                    installedForm.removePointerPressedListener(pointerPressed);
                    installedForm.removePointerReleasedListener(pointerReleased);
                }
                installedForm = actualForm;
                if (installedForm != null) {
                    installedForm.addPointerPressedListener(pointerPressed);
                    installedForm.addPointerReleasedListener(pointerReleased);
                }
            }
        }
        
        private void uninstallPointerListeners() {
            if (installedForm != null) {
                installedForm.removePointerPressedListener(pointerPressed);
                installedForm.removePointerReleasedListener(pointerReleased);
                installedForm = null;
            }
        }
        
        @Override
        public void layoutContainer(Container parent) {
            //if (true) {
            //    return;
           // }
            int parentX = 0;
            int parentY = 0;
            for (Component current : parent) {
                Coord crd = (Coord) current.getClientProperty(COORD_KEY);
                Point p = (Point) current.getClientProperty(POINT_KEY);
                if (p == null) {
                    p = map.getScreenCoordinate(crd);
                    current.putClientProperty(POINT_KEY, p);
                }
                Float h = (Float)current.getClientProperty(HORIZONTAL_ALIGNMENT);
                if(h == null) {
                    h = 0f;
                }
                Float v = (Float)current.getClientProperty(VERTICAL_ALIGNMENT);
                if(v == null) {
                    v = 0f;
                }
                current.setSize(current.getPreferredSize());
                current.setX(convertX(p.getX() - parentX, current.getWidth(), h));
                current.setY(convertY(p.getY() - parentY, current.getHeight(), v));
            }
        }

        @Override
        public Dimension getPreferredSize(Container parent) {
            return new Dimension(100, 100);
        }

        @Override
        public void mapPositionUpdated(Component source, int zoom, Coord center) {
            //if (true) return;
            Runnable r = new Runnable() {
                public void run() {
                    inUpdate = true;

                    try {
                        List<Coord> coords = new ArrayList<>();
                        List<Component> cmps = new ArrayList<>();
                        int len = actual.getComponentCount();
                        for (Component current : actual) {
                            Coord crd = (Coord) current.getClientProperty(COORD_KEY);
                            coords.add(crd);
                            cmps.add(current);
                        }
                        int startingUpdateCounter = ++updateCounter;
                        List<Point> points = map.getScreenCoordinates(coords);
                        if (startingUpdateCounter != updateCounter || len != points.size()) {
                            // Another update must have run while we were waiting for the bounding box.
                            // in which case, that update would be more recent than this one.
                            return;
                        }
                        for (int i=0; i<len; i++) {
                            Component current = cmps.get(i);
                            Point p = points.get(i);
                            current.putClientProperty(POINT_KEY, p);
                        }
                        if (!pressed) {
                            // If the pointer is pressed
                            // we're showing the bitmap versions of the markers anyways
                            // so we don't need to revalidate the overlay aggressively.
                            actual.setShouldCalcPreferredSize(true);
                            actual.revalidate();
                        }
                        if (nextUpdate != null) {
                            Runnable nex = nextUpdate;
                            nextUpdate = null;
                            callSerially(nex);
                        }
                    } finally {
                        inUpdate = false;
                    }

                }


            };
            if (inUpdate) {
                nextUpdate = r;
            } else {
                nextUpdate = null;
                callSerially(r);
            }
        }
}