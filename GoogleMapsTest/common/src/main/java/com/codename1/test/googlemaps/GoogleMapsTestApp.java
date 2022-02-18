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
package com.codename1.test.googlemaps;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.InteractionDialog;
import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.googlemaps.MapContainer.MapObject;
import com.codename1.io.Util;
import com.codename1.maps.BoundingBox;
import com.codename1.maps.Coord;
import com.codename1.maps.MapListener;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.SideMenuBar;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class GoogleMapsTestApp {

    private static final String HTML_API_KEY = "AIzaSyBWeRU02YUYPdwRuMFyTKIXUbHjq6e35Gw";
    private Form current;

    public void init(Object context) {
        try {
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
            Display.getInstance().setCommandBehavior(Display.COMMAND_BEHAVIOR_SIDE_NAVIGATION);
            UIManager.getInstance().getLookAndFeel().setMenuBarClass(SideMenuBar.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    MapObject sydney;
    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        Form hi = new Form("Native Maps Test");
        hi.setLayout(new BorderLayout());
        final MapContainer cnt = new MapContainer(HTML_API_KEY);
        //final MapContainer cnt = new MapContainer();
        cnt.setCameraPosition(new Coord(-26.1486233, 28.67401229999996));//this breaks the code //because the Google map is not loaded yet
        cnt.addMapListener(new MapListener() {

            @Override
            public void mapPositionUpdated(Component source, int zoom, Coord center) {
                System.out.println("Map position updated: zoom="+zoom+", Center="+center);
            }
            
        });
        
        cnt.addLongPressListener(e->{
            System.out.println("Long press");
            ToastBar.showMessage("Received longPress at "+e.getX()+", "+e.getY(), FontImage.MATERIAL_3D_ROTATION);
        });
        cnt.addTapListener(e->{
            ToastBar.showMessage("Received tap at "+e.getX()+", "+e.getY(), FontImage.MATERIAL_3D_ROTATION);
        });
        
        int maxZoom = cnt.getMaxZoom();
        System.out.println("Max zoom is "+maxZoom);
        Button btnMoveCamera = new Button("Move Camera");
        btnMoveCamera.addActionListener(e->{
            cnt.setCameraPosition(new Coord(-33.867, 151.206));
        });
        Style s = new Style();
        s.setFgColor(0xff0000);
        s.setBgTransparency(0);
        FontImage markerImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, s, 3);
        
        Button btnAddMarker = new Button("Add Marker");
        btnAddMarker.addActionListener(e->{
           
            cnt.setCameraPosition(new Coord(41.889, -87.622));
            cnt.addMarker(EncodedImage.createFromImage(markerImg, false), cnt.getCameraPosition(), "Hi marker", "Optional long description", new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    System.out.println("Bounding box is "+cnt.getBoundingBox());
                    ToastBar.showMessage("You clicked the marker", FontImage.MATERIAL_PLACE);
                }
            });
            
        });
        
        Button btnAddPath = new Button("Add Path");
        btnAddPath.addActionListener(e->{
            
            cnt.addPath(
                    cnt.getCameraPosition(),
                    new Coord(-33.866, 151.195), // Sydney
                    new Coord(-18.142, 178.431),  // Fiji
                    new Coord(21.291, -157.821),  // Hawaii
                    new Coord(37.423, -122.091)  // Mountain View
            );
        });
        
        Button panTo = new Button("Pan To");
        panTo.addActionListener(e->{
            //bounds.extend(new google.maps.LatLng('66.057878', '-22.579047')); // Iceland
            //bounds.extend(new google.maps.LatLng('37.961952', '43.878878')); // Turkey
            Coord c1 = new Coord(49.0986192, -122.6764454);
            Coord c2 = new Coord(49.2577142, -123.1941149);
            //Coord center = new Coord(c1.getLatitude()/2 +  c2.getLatitude() / 2, c1.getLongitude()/2 + c2.getLongitude()/2 );
            Coord center = new Coord(49.1110928, -122.9414646);
            
            float zoom = cnt.getZoom();
            
            boolean[] finished = new boolean[1];
            cnt.addMapListener(new MapListener() {

                @Override
                public void mapPositionUpdated(Component source, int zoom, Coord c) {
                    
                    if (Math.abs(c.getLatitude() - center.getLatitude()) > .001 || Math.abs(c.getLongitude() - center.getLongitude()) > .001) {
                        return;
                    }
                    finished[0] = true;
                    synchronized(finished) {
                        final MapListener fthis = this;
                        Display.getInstance().callSerially(()->{
                            cnt.removeMapListener(fthis);
                        });
                        finished.notify();
                    }
                    
                }
                
            });
            cnt.zoom(center, (int)zoom);
            while (!finished[0]) {
                Display.getInstance().invokeAndBlock(()->{
                    while (!finished[0]) {
                        Util.wait(finished, 100);
                    }
                });
            }
            BoundingBox box = cnt.getBoundingBox();
            if (!box.contains(c1) || !box.contains(c2)) {
                while (!box.contains(c1) || !box.contains(c2)) {
                    if (!box.contains(c1)) {
                        System.out.println("Box "+box+" doesn't contain "+c1);
                    }
                    if (!box.contains(c1)) {
                        System.out.println("Box "+box+" doesn't contain "+c2);
                    }
                    zoom -= 1;
                    final boolean[] done = new boolean[1];
                    
                    final int fzoom = (int)zoom;
                    cnt.addMapListener(new MapListener() {

                        @Override
                        public void mapPositionUpdated(Component source, int zm, Coord center) {
                            
                            if (zm == fzoom) {
                                final MapListener fthis = this;
                                Display.getInstance().callSerially(()->{
                                    cnt.removeMapListener(fthis);
                                });
                                
                                done[0] = true;
                                synchronized(done) {
                                    done.notify();
                                }
                            }
                        }
                        
                    });
                    cnt.zoom(center, (int)zoom);
                    while (!done[0]) {
                        Display.getInstance().invokeAndBlock(()->{
                            while (!done[0]) {
                                Util.wait(done, 100);
                            }
                        });
                    }
                    box = cnt.getBoundingBox();
                    System.out.println("Zoom now "+zoom);
                    
                }
            } else if (box.contains(c1) && box.contains(c2)) {
                while (box.contains(c1) && box.contains(c2)) {
                    zoom += 1;
                    final boolean[] done = new boolean[1];
                    
                    final int fzoom = (int)zoom;
                    cnt.addMapListener(new MapListener() {
                        public void mapPositionUpdated(Component source, int zm, Coord center)  {
                            if (zm == fzoom) {
                                final MapListener fthis = this;
                                Display.getInstance().callSerially(()->{
                                    cnt.removeMapListener(fthis);
                                });
                                done[0] = true;
                                synchronized(done) {
                                    done.notify();
                                }
                            }
                        }
                    });
                    cnt.zoom(center, (int)zoom);
                    while (!done[0]) {
                        Display.getInstance().invokeAndBlock(()->{
                            while (!done[0]) {
                                Util.wait(done, 100);
                            }
                        });
                    }
                    box = cnt.getBoundingBox();
                    
                }
                zoom -= 1;
                cnt.zoom(center, (int)zoom);
                cnt.addTapListener(null);
            }
            
        });
        
        Button testCoordPositions = $(new Button("Test Coords"))
                .addActionListener(e->{
                    Coord topLeft = cnt.getCoordAtPosition(0, 0);
                    System.out.println("Top Left is "+topLeft+" -> "+cnt.getScreenCoordinate(topLeft) +" Should be (0,0)");
                    Coord bottomRight = cnt.getCoordAtPosition(cnt.getWidth(), cnt.getHeight());
                    System.out.println("Bottom right is "+bottomRight+" -> "+cnt.getScreenCoordinate(bottomRight) + " Should be "+cnt.getWidth()+", "+cnt.getHeight());
                    Coord bottomLeft = cnt.getCoordAtPosition(0, cnt.getHeight());
                    System.out.println("Bottom Left is "+bottomLeft+" -> "+cnt.getScreenCoordinate(bottomLeft) + " Should be 0, "+cnt.getHeight());
                    Coord topRight = cnt.getCoordAtPosition(cnt.getWidth(), 0);
                    System.out.println("Top right is "+topRight + " -> "+cnt.getScreenCoordinate(topRight)+ " Should be "+cnt.getWidth()+", 0");
                    Coord center = cnt.getCoordAtPosition(cnt.getWidth()/2, cnt.getHeight()/2);
                    System.out.println("Center is "+center+" -> "+cnt.getScreenCoordinate(center)+", should be "+(cnt.getWidth()/2)+", "+(cnt.getHeight()/2));
                    EncodedImage encImg = EncodedImage.createFromImage(markerImg, false);
                    cnt.addMarker(encImg, topLeft,"Top Left", "Top Left", null);
                    cnt.addMarker(encImg, topRight, "Top Right", "Top Right", null);
                    cnt.addMarker(encImg, bottomRight, "Bottom Right", "Bottom Right", null);
                    cnt.addMarker(encImg, bottomLeft, "Bottom Left", "Bottom Left", null);
                    cnt.addMarker(encImg, center, "Center", "Center", null);
                    
                    
                })
                .asComponent(Button.class);
        
        Button toggleTopMargin = $(new Button("Toggle Margin"))
                .addActionListener(e->{
                    int marginTop = $(cnt).getStyle().getMarginTop();
                    if (marginTop < Display.getInstance().getDisplayHeight() / 3) {
                        $(cnt).selectAllStyles().setMargin(Display.getInstance().getDisplayHeight() / 3, 0, 0, 0);
                    } else {
                        $(cnt).selectAllStyles().setMargin(0,0,0,0);
                    }
                    $(cnt).getComponentForm().revalidate();
                })
                .asComponent(Button.class);
        
        
        Button btnClearAll = new Button("Clear All");
        btnClearAll.addActionListener(e->{
            cnt.clearMapLayers();
        });
        
        MapObject mo = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), new Coord(-33.866, 151.195), "test", "test",e->{
            System.out.println("Marker clicked");
            cnt.removeMapObject(sydney);
        });
        sydney = mo;
        System.out.println("MO is "+mo);
        mo = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), new Coord(-18.142, 178.431), "test", "test",e->{
            System.out.println("Marker clicked");
        });
        System.out.println("MO is "+mo);
        cnt.addTapListener(e->{
            if (tapDisabled) {
                return;
            }
            tapDisabled = true;
            TextField enterName = new TextField();
            Container wrapper = BoxLayout.encloseY(new Label("Name:"), enterName);
            InteractionDialog dlg = new InteractionDialog("Add Marker");
            dlg.getContentPane().add(wrapper);
            enterName.setDoneListener(e2->{
                String txt = enterName.getText();
                cnt.addMarker(EncodedImage.createFromImage(markerImg, false), cnt.getCoordAtPosition(e.getX(), e.getY()), enterName.getText(), "", e3->{
                    ToastBar.showMessage("You clicked "+txt, FontImage.MATERIAL_PLACE);
                });
                dlg.dispose();
                tapDisabled = false;
            });
            dlg.showPopupDialog(new Rectangle(e.getX(), e.getY(), 10, 10));
            enterName.startEditingAsync();
        });
        
        Button showNextForm = $(new Button("Next Form"))
                .addActionListener(e->{
                    Form form = new Form("Hello World");
                    Button b1 = $(new Button("B1"))
                            .addActionListener(e2->{
                                ToastBar.showMessage("B1 was pressed", FontImage.MATERIAL_3D_ROTATION);
                            })
                            .asComponent(Button.class);
                    
                    Button back = $(new Button("Back"))
                            .addActionListener(e2->{
                                hi.showBack();
                            })
                            .asComponent(Button.class);
                    form.add(b1);
                })
                .asComponent(Button.class);
        
        FloatingActionButton nextForm = FloatingActionButton.createFAB(FontImage.MATERIAL_ACCESS_ALARM);
        nextForm.addActionListener(e->{
            Form form = new Form("Hello World");
            Button b1 = $(new Button("B1"))
                    .addActionListener(e2->{
                        ToastBar.showMessage("B1 was pressed", FontImage.MATERIAL_3D_ROTATION);
                    })
                    .asComponent(Button.class);

            Button back = $(new Button("Back"))
                    .addActionListener(e2->{
                        hi.showBack();
                    })
                    .asComponent(Button.class);
            form.add(b1).add(back);
            form.show();
        });
        
        
        
        Container root = LayeredLayout.encloseIn(
                BorderLayout.center(nextForm.bindFabToContainer(cnt)),
                BorderLayout.south(
                        FlowLayout.encloseBottom(panTo, testCoordPositions, toggleTopMargin, btnMoveCamera, btnAddMarker, btnAddPath, btnClearAll )
                )
        );
        
        hi.add(BorderLayout.CENTER, root);
        hi.show();
        
    }
    boolean tapDisabled = false;

    public void stop() {
        current = Display.getInstance().getCurrent();
    }

    public void destroy() {
    }

    

}
