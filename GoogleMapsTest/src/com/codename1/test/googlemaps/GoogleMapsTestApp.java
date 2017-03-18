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

import com.codename1.components.InteractionDialog;
import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.googlemaps.MapContainer.MapObject;
import com.codename1.maps.Coord;
import com.codename1.maps.MapListener;
import com.codename1.maps.providers.OpenStreetMapProvider;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
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
    MapObject me; 
    MapObject markerAdded;
    boolean tapDisabled = false;
    //-----------------------------------------------------------------------------------------
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
    //-----------------------------------------------------------------------------------------
    public void start() {
        
        if (current != null) {
            current.show();
            return;
        }
        
        Form hi = new Form("Native Maps Test");
        hi.setLayout(new BorderLayout());
        final MapContainer cnt = new MapContainer(new OpenStreetMapProvider());
        //final MapContainer cnt = new MapContainer(HTML_API_KEY);
        
        Style s = new Style();
        s.setFgColor(0xff0000);
        s.setBgTransparency(0);
        FontImage markerImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, s, 3);
        
        
        Button btnMoveCamera = new Button("Move Camera");
        Button btnAddMarker = new Button("Add Marker");
        Button btnAddPath = new Button("Add Path");
        Button btnClearAll = new Button("Clear All");
        btnMoveCamera.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //cnt.setCameraPosition(new Coord(-33.867, 151.206));
            }
        });
        btnAddMarker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //cnt.setCameraPosition(new Coord(41.889, -87.622));
               markerAdded = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), cnt.getCameraPosition(), "Hi marker", "Optional long description", new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        ToastBar.showMessage("You clicked the marker", FontImage.MATERIAL_PLACE);
                    }
                });
            }
        });
        btnAddPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cnt.addPath(
                        cnt.getCameraPosition(),
                        new Coord(-33.866, 151.195), // Sydney
                        new Coord(-18.142, 178.431),  // Fiji
                        new Coord(21.291, -157.821),  // Hawaii
                        new Coord(37.423, -122.091)  // Mountain View
                );
            }
        });
        btnClearAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cnt.clearMapLayers();
            }
        });
        
        //cnt.setCameraPosition(new Coord(18.4823039439783, -69.91718675560759));
        cnt.zoom(new Coord(18.4823039439783, -69.91718675560759), 15);
//        me = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), new Coord(-33.866, 151.195), "test", "test", new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Marker clicked");
//                cnt.removeMapObject(me);
//            }
//        });
//        
//        System.out.println("me:"+me);
//
//        agent = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), new Coord(-18.142, 178.431), "test", "test", new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Marker clicked");
//            }
//        });
//
//        System.out.println("agent:"+agent);
        
        
        cnt.addTapListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                if (tapDisabled) {
//                    return;
//                }
//                tapDisabled = true;
                
                
                TextField enterName = new TextField();
                Button  add = new Button("add");
                
                Container wrapper = BoxLayout.encloseY(new Label("Name:"), enterName, add);
                InteractionDialog dlg = new InteractionDialog("Add Marker");
                dlg.getContentPane().add(wrapper);
                
//                enterName.setDoneListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e2) {
//                        String txt = enterName.getText();
//                       me = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), cnt.getCoordAtPosition(e.getX(), e.getY()), enterName.getText(), "", new ActionListener() {
//                            @Override
//                            public void actionPerformed(ActionEvent e3) {
//                                ToastBar.showMessage("You clicked "+txt, FontImage.MATERIAL_PLACE);
//                            }
//                        });
//                        dlg.dispose();
//                        tapDisabled = false;
//                    }
//                });
                add.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {

                        String txt = enterName.getText();
                        //if (me!=null)
                            //cnt.removeMapObject(me);
                        me = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), cnt.getCoordAtPosition(e.getX(), e.getY()), enterName.getText(), "", new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e3) {
                                ToastBar.showMessage("You clicked " + txt, FontImage.MATERIAL_PLACE);
                            }
                        });
                        dlg.dispose();
                    }
                });
                
                dlg.showPopupDialog(new Rectangle(e.getX(), e.getY(), 10, 10));
                enterName.startEditingAsync();
            }
        });
        
        
        cnt.addMapListener(new MapListener() {
            @Override
            public void mapPositionUpdated(Component source, int zoom, Coord center) {
                
                
                double latitude = center.getLatitude();
                double longitude = center.getLongitude();

                System.out.println("latitude :"+latitude);
                System.out.println("longitude:"+longitude);
                System.out.println("zoom     :"+zoom);
                
//                if (me!=null){
//                    cnt.removeMapObject(me);
//                    System.out.println("removing markert->"+latitude+","+longitude);
//                }
                me = cnt.addMarker(EncodedImage.createFromImage(markerImg, false), new Coord(latitude, longitude), "Me", "", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e3) {
                        ToastBar.showMessage("You clicked ", FontImage.MATERIAL_PLACE);
                    }
                });
                
                System.out.println(" markert->me:"+me);
                
                //cnt.revalidate();

            }
        });
        
        Container root = LayeredLayout.encloseIn(BorderLayout.center(cnt),BorderLayout.south(FlowLayout.encloseBottom(btnAddMarker, btnAddPath, btnClearAll)));
        
        hi.add(BorderLayout.CENTER, root);
        hi.show();
        
    }
    //-----------------------------------------------------------------------------------------
    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    //-----------------------------------------------------------------------------------------
    public void destroy() {
    }
    //-----------------------------------------------------------------------------------------
    

}
