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


import com.codename1.googlemaps.MapContainer;
import com.codename1.maps.Coord;
import com.codename1.ui.Command;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class GoogleMapsTestApp {

    private Form current;

    public void init(Object context) {
        try {
            Resources theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void start() {
        if(current != null){
            current.show();
            return;
        }
        Form hi = new Form("Native Maps Test");
        hi.setLayout(new BorderLayout());
        final MapContainer cnt = new MapContainer();
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
                            Dialog.show("Marker Clicked!", "You clicked the marker", "OK", null);
                        }
                    });
                } catch(IOException err) {
                    // since the image is iin the jar this is unlikely
                    err.printStackTrace();
                }
            }
        });
        hi.addCommand(new Command("Add Path") {
            public void actionPerformed(ActionEvent ev) {
                cnt.setCameraPosition(new Coord(-18.142, 178.431));
                cnt.addPath(new Coord(-33.866, 151.195), // Sydney
                    new Coord(-18.142, 178.431),  // Fiji
                    new Coord(21.291, -157.821),  // Hawaii
                    new Coord(37.423, -122.091)  // Mountain View
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

}
