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
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Label;
import com.codename1.ui.SideMenuBar;
import com.codename1.ui.Slider;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.Animation;
import com.codename1.ui.animations.Motion;
import com.codename1.ui.animations.Transition;
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

    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        showNativeMapsTestForm();
        if (true) {
            return;
        }
        zlayerDemo();
        if (true) {
            return;
        }
        Form hi = new Form("Native Maps Test");
        hi.setLayout(new BorderLayout());
        final MapContainer cnt = new MapContainer(HTML_API_KEY);

        final Label lbl = new Label("Location: ...");
        cnt.addMapListener(new MapListener() {
            public void mapPositionUpdated(Component source, int zoom, Coord center) {
                //lbl.setText("Location: " + center.getLatitude() + ", " + center.getLongitude());
                lbl.setText("0 lon: " + cnt.getCoordAtPosition(0, 0).getLongitude() + " w lon " + cnt.getCoordAtPosition(Display.getInstance().getDisplayWidth(), 0).getLongitude());
            }
        });

        cnt.addTapListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                MapContainer currentMap = cnt;
                Coord NE = currentMap.getCoordAtPosition(currentMap.getAbsoluteX() + currentMap.getWidth(), currentMap.getAbsoluteY());
                Coord SW = currentMap.getCoordAtPosition(currentMap.getAbsoluteX(), currentMap.getAbsoluteY() + currentMap.getHeight());
                System.out.println("NE=" + NE + ", SW=" + SW);
                Dialog.show("Tap", "Tap detected", "OK", null);
            }
        });

        hi.addComponent(BorderLayout.SOUTH, lbl);
        hi.addComponent(BorderLayout.CENTER, cnt);
        hi.addCommand(new Command("Move Camera") {
            public void actionPerformed(ActionEvent ev) {
                cnt.setCameraPosition(new Coord(-33.867, 151.206));
            }
        });
        hi.addCommand(new Command("Add Marker") {
            public void actionPerformed(ActionEvent ev) {
                try {
                    cnt.setCameraPosition(new Coord(41.889, -87.622));
                    cnt.addMarker(EncodedImage.create("/maps-pin.png"), new Coord(41.889, -87.622), "Hi marker", "Optional long description", new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            //Dialog.show("Marker Clicked!", "You clicked the marker", "OK", null);
                        }
                    });
                } catch (IOException err) {
                    // since the image is iin the jar this is unlikely
                    err.printStackTrace();
                }
            }
        });
        hi.addCommand(new Command("Add Marker Here") {
            public void actionPerformed(ActionEvent ev) {
                try {
                    cnt.addMarker(EncodedImage.create("/maps-pin.png"), cnt.getCameraPosition(), "Marker At", "Lat: " + cnt.getCameraPosition().getLatitude() + ", " + cnt.getCameraPosition().getLongitude(), new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            Dialog.show("Marker Clicked!", "You clicked the marker", "OK", null);
                        }
                    });
                } catch (IOException err) {
                    // since the image is iin the jar this is unlikely
                    err.printStackTrace();
                }
            }
        });
        hi.addCommand(new Command("Add Path") {
            public void actionPerformed(ActionEvent ev) {
                cnt.setCameraPosition(new Coord(-18.142, 178.431));
                cnt.addPath(new Coord(-33.866, 151.195), // Sydney
                        new Coord(-18.142, 178.431), // Fiji
                        new Coord(21.291, -157.821), // Hawaii
                        new Coord(37.423, -122.091) // Mountain View
                );
            }
        });
        hi.addCommand(new Command("Clear All") {
            public void actionPerformed(ActionEvent ev) {
                cnt.clearMapLayers();
            }
        });

        hi.show();
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }

    public void destroy() {
    }

    private void zlayerDemo() {
        Form f = new Form("Native Maps", new BorderLayout());

        Toolbar tb = new Toolbar();
        f.setToolbar(tb);
        tb.setTitle("Native Maps");

        MapContainer map = new MapContainer(HTML_API_KEY);

        MapInfoPanel infoPanel = new MapInfoPanel(map);

        Command cmdHideInfo = new Command("Hide Map Info") {
            public void actionPerformed(ActionEvent evt) {
                tb.removeOverflowCommand(this);

            }
        };

        Command cmdShowInfo = new Command("Show Map Info") {
            public void actionPerformed(ActionEvent evt) {
                tb.removeOverflowCommand(this);
                tb.addCommandToOverflowMenu(cmdHideInfo);
                infoPanel.show();
            }
        };

        tb.addCommandToOverflowMenu(cmdShowInfo);

        FloatingActionButton addBtn = FloatingActionButton.createFAB(FontImage.MATERIAL_ADD_LOCATION);
        addBtn.addActionListener(e -> {

            InteractionDialog enterLocationNameDlg = new InteractionDialog();
            TextField locationName = new TextField();
            boolean nameEntered[] = new boolean[1];

            locationName.setDoneListener(doneEvt -> {
                enterLocationNameDlg.dispose();

                Coord sw = map.getCoordAtPosition(map.getAbsoluteX(), map.getAbsoluteY() + map.getHeight());
                Coord ne = map.getCoordAtPosition(map.getAbsoluteX() + map.getWidth(), map.getAbsoluteY());

                Coord c = map.getCameraPosition();
                int cX = map.getAbsoluteX() + map.getWidth() / 2;
                int cY = map.getAbsoluteY() + map.getHeight() / 2;

                int startX = addBtn.getAbsoluteX();
                int startY = addBtn.getAbsoluteY();

                Style style = new Style();
                style.setFgColor(0xff0000);
                style.setBgTransparency(0);

                FontImage markerImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, style);

                Label markerLabel = new Label(markerImg);
                markerLabel.getAllStyles().setMarginUnit(Style.UNIT_TYPE_PIXELS, Style.UNIT_TYPE_PIXELS);

                markerLabel.setX(addBtn.getAbsoluteX());
                markerLabel.setY(addBtn.getAbsoluteY() - addBtn.getComponentForm().getContentPane().getAbsoluteY());
                markerLabel.getAllStyles().setMarginUnit(Style.UNIT_TYPE_PIXELS, Style.UNIT_TYPE_PIXELS, Style.UNIT_TYPE_PIXELS, Style.UNIT_TYPE_PIXELS);
                markerLabel.getAllStyles().setMargin(
                        map.getAbsoluteY() + map.getHeight() / 2 - map.getComponentForm().getContentPane().getAbsoluteY() - markerImg.getHeight(),
                        0,
                        map.getAbsoluteX() + map.getWidth() / 2 - map.getComponentForm().getContentPane().getAbsoluteX() - markerImg.getWidth() / 2,
                        0
                );

                addBtn.getComponentForm().getLayeredPane().add(markerLabel);
                addBtn.getComponentForm().getLayeredPane().animateLayoutAndWait(500);

                MapObject mapObj = map.addMarker(EncodedImage.createFromImage(markerImg, false), c, locationName.getText(), "My Super location", e2 -> {
                    ToastBar.showMessage("You clicked it!", FontImage.MATERIAL_PLACE);
                });
                markerLabel.remove();
                addBtn.getComponentForm().getLayeredPane().revalidate();

                infoPanel.addMarker(mapObj, locationName.getText(), markerImg, c);
            });

            locationName.setHint("Enter location name...");
            enterLocationNameDlg.add(locationName);

            enterLocationNameDlg.showPopupDialog(addBtn);
            locationName.startEditingAsync();

            //BoundingBox bbox = new BoundingBox(sw, ne);
            //ServerAccess.getEntriesFromFlickrService("art", bbox);
        });

        Container newRoot = addBtn.bindFabToContainer(map);
        f.add(BorderLayout.CENTER, newRoot);
        f.show();

    }
    
    private void showNativeMapsTestForm() {
        Form hi = new Form("Native Maps Test");
        hi.setLayout(new BorderLayout());
        final MapContainer cnt = new MapContainer(HTML_API_KEY);
        
        Button btnMoveCamera = new Button("Move Camera");
        btnMoveCamera.addActionListener(e->{
            cnt.setCameraPosition(new Coord(-33.867, 151.206));
        });
        Style s = new Style();
        s.setFgColor(0xff0000);
        s.setBgTransparency(0);
        FontImage markerImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, s, Display.getInstance().convertToPixels(3));
        
        Button btnAddMarker = new Button("Add Marker");
        btnAddMarker.addActionListener(e->{
           
            cnt.setCameraPosition(new Coord(41.889, -87.622));
            cnt.addMarker(EncodedImage.createFromImage(markerImg, false), cnt.getCameraPosition(), "Hi marker", "Optional long description", new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
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
        
        Button btnClearAll = new Button("Clear All");
        btnClearAll.addActionListener(e->{
            cnt.clearMapLayers();
        });
        
        cnt.addTapListener(e->{
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
            });
            dlg.showPopupDialog(new Rectangle(e.getX(), e.getY(), 10, 10));
            enterName.startEditingAsync();
        });
        
        Container root = LayeredLayout.encloseIn(
                BorderLayout.center(cnt),
                BorderLayout.south(
                        FlowLayout.encloseBottom(btnMoveCamera, btnAddMarker, btnAddPath, btnClearAll)
                )
        );
        
        hi.add(BorderLayout.CENTER, root);
        hi.show();
    }

}
