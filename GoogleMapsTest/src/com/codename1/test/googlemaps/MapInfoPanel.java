/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.test.googlemaps;

import com.codename1.components.InteractionDialog;
import com.codename1.components.Progress;
import com.codename1.googlemaps.MapContainer;
import com.codename1.googlemaps.MapContainer.MapObject;
import com.codename1.l10n.L10NManager;
import com.codename1.maps.BoundingBox;
import com.codename1.maps.Coord;
import com.codename1.maps.MapListener;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
//import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Slider;
import com.codename1.ui.Tabs;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.table.TableLayout;
import com.codename1.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author shannah
 */
public class MapInfoPanel extends Container {
    final MapContainer map;
    Slider zoomSlider;
    Label centerCoords;
    Label swCoord;
    Label neCoord;
    Container markers;
    
    Container paths;
    
    
    
private void initStarRankStyle(Style s, Image star) {
    s.setBackgroundType(Style.BACKGROUND_IMAGE_TILE_BOTH);
    s.setBorder(Border.createEmpty());
    s.setBgImage(star);
    s.setBgTransparency(0);
}
    
    public MapInfoPanel(MapContainer map) {
        this.map = map;
        /*
        Slider starRank = new Slider();
    starRank.setEditable(true);
    starRank.setMinValue(0);
    starRank.setMaxValue(10);
    Font fnt = Font.createTrueTypeFont("native:MainLight", "native:MainLight").
            derive(Display.getInstance().convertToPixels(5, true), Font.STYLE_PLAIN);
    Style s = new Style(0xffff33, 0, fnt, (byte)0);
    Image fullStar = FontImage.createMaterial(FontImage.MATERIAL_STAR, s).toImage();
    s.setOpacity(100);
    s.setFgColor(0);
    Image emptyStar = FontImage.createMaterial(FontImage.MATERIAL_STAR, s).toImage();
    initStarRankStyle(starRank.getSliderEmptySelectedStyle(), emptyStar);
    initStarRankStyle(starRank.getSliderEmptyUnselectedStyle(), emptyStar);
    initStarRankStyle(starRank.getSliderFullSelectedStyle(), fullStar);
    initStarRankStyle(starRank.getSliderFullUnselectedStyle(), fullStar);
    starRank.setPreferredSize(new Dimension(fullStar.getWidth() * 5, fullStar.getHeight()));
    
        //getAllStyles().setOpacity(128);
        //getAllStyles().setBgTransparency(128);
    */
        zoomSlider =new Slider();
        zoomSlider.setThumbImage(FontImage.createMaterial(FontImage.MATERIAL_FIBER_MANUAL_RECORD, 
                new Style(),
                Display.getInstance().convertToPixels(2)));
        zoomSlider.setEditable(true);
        //$(zoomSlider).setPaddingMillimeters(2);
        zoomSlider.setMinValue(0);
        zoomSlider.setMaxValue(100);
        zoomSlider.setProgress(50);
        zoomSlider.addActionListener(ze->{
            map.zoom(map.getCameraPosition(), zoomSlider.getProgress());
        });
        
        Tabs tabs = new Tabs();
        tabs.getAllStyles().setBgTransparency(0);
        this.getAllStyles().setBgTransparency(0);
        centerCoords = new Label();
        swCoord = new Label();
        neCoord = new Label();
        
        markers = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        paths = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        
        TableLayout infoTable = new TableLayout(4, 2);
        Container infoTableC = new Container(infoTable);
        
        infoTableC.add("Center").add(centerCoords).add("SW:").add(swCoord).add("NE").add(neCoord)
                .add("Zoom").add(BorderLayout.center(zoomSlider));
        
        tabs.addTab("Coords", infoTableC);
        tabs.addTab("Markes", markers);
        
        map.addMapListener(new MapListener() {

            @Override
            public void mapPositionUpdated(Component source, int zoom, Coord center) {
                updateMapPosition(zoom, center);
            }
            
        });
        
        
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, tabs);
        styleLabels(infoTableC);
        
    }
    
    private String coordString(Coord coord) {
        return L10NManager.getInstance().format(coord.getLatitude(), 3)+", "+L10NManager.getInstance().format(coord.getLongitude(), 3);
    }
    
    public void updateMapPosition() {
        updateMapPosition((int)map.getZoom(), map.getCameraPosition());
    }
    
    public void updateMapPosition(int zoom, Coord center) {
        centerCoords.setText(coordString(center));
        BoundingBox bbox = map.getBoundingBox();
        swCoord.setText(coordString(bbox.getSouthWest()));
        neCoord.setText(coordString(bbox.getNorthEast()));
        zoomSlider.setMinValue(map.getMinZoom());
        zoomSlider.setMaxValue(map.getMaxZoom());
        
        zoomSlider.setProgress(zoom);
        revalidate();
    }
    
    public void addMarker(MapObject mo, String label, Image icon, Coord location) {
        markers.add(createMarkerButton(mo, label, icon, location));
        markers.revalidate();
    }
    
    private Button createMarkerButton(MapObject mo, String label, Image icon, final Coord location) {
        Button b = new Button(label, icon);
        b.addActionListener(e->{
            map.zoom(location, (int)map.getZoom());
        });
        
        return b;
    }
    
    
    public void show() {
        if (getParent() != null) {
            return;
        }
        Display disp = Display.getInstance();
        if (disp.isTablet()) {
            if (disp.isPortrait()) {
                markers.setLayout(new BoxLayout(BoxLayout.X_AXIS));
                for (Component marker : markers) {
                    if (marker instanceof Button) {
                        Button btnMarker = (Button)marker;
                        btnMarker.setTextPosition(Label.BOTTOM);
                    }
                }
                
                InteractionDialog dlg = new InteractionDialog("Map Info");
                dlg.setLayout(new BorderLayout());
                dlg.add(BorderLayout.CENTER, this);
                int dh = disp.getDisplayHeight();
                int dw = disp.getDisplayWidth();
                dlg.show(3 * dh / 4 , 0, 0, 0);
                updateMapPosition();
                
            } else {
                markers.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
                for (Component marker : markers) {
                    if (marker instanceof Button) {
                        Button btnMarker = (Button)marker;
                        btnMarker.setTextPosition(Label.RIGHT);
                    }
                }
                InteractionDialog dlg = new InteractionDialog("Map Info");
                dlg.setLayout(new BorderLayout());
                dlg.add(BorderLayout.CENTER, this);
                int dh = disp.getDisplayHeight();
                int dw = disp.getDisplayWidth();
                //makeTransparent(dlg);
                dlg.getAllStyles().setBorder(Border.createEmpty());
                dlg.getAllStyles().setBgColor(0x0);
                dlg.getAllStyles().setBgTransparency(128);
                
                List<Component> dialogTitle = findByUIID("DialogTitle", dlg);
                for (Component c : dialogTitle) {
                    c.getAllStyles().setFgColor(0xffffff);
                }
                List<Component> tabsContainer = findByUIID("TabsContainer", dlg);
                for (Component c : tabsContainer) {
                    c.getAllStyles().setBgColor(0xEAEAEA);
                    //c.getAllStyles().setBackgroundType(Style.BACKGROUND_NONE);
                    c.getAllStyles().setBgTransparency(255);
                }
                //$("TabsContainer", dlg)
                //        .setBgColor(0x0)
                //        .setBorder(Border.createEmpty())
                //        .setBgTransparency((byte)0);
               
                        ;
                dlg.show(0, 0, 0, dw * 3/4);
                
                updateMapPosition();
                System.out.println("Making transparent");
                //makeTransparent(dlg);
            }
            
        } else {
            if (disp.isPortrait()) {
                markers.setLayout(new BoxLayout(BoxLayout.X_AXIS));
                for (Component marker : markers) {
                    if (marker instanceof Button) {
                        Button btnMarker = (Button)marker;
                        btnMarker.setTextPosition(Label.BOTTOM);
                    }
                }
                
                InteractionDialog dlg = new InteractionDialog("Map Info");
                dlg.setLayout(new BorderLayout());
                dlg.add(BorderLayout.CENTER, this);
                int dh = disp.getDisplayHeight();
                int dw = disp.getDisplayWidth();
                
                
                dlg.show(dh/2, 0, 0, 0);
                updateMapPosition();
            } else {
                markers.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
                for (Component marker : markers) {
                    if (marker instanceof Button) {
                        Button btnMarker = (Button)marker;
                        btnMarker.setTextPosition(Label.RIGHT);
                    }
                }
                InteractionDialog dlg = new InteractionDialog("Map Info");
                dlg.setLayout(new BorderLayout());
                dlg.add(BorderLayout.CENTER, this);
                int dh = disp.getDisplayHeight();
                int dw = disp.getDisplayWidth();
                dlg.show(0, dw/2, 0, 0);
                updateMapPosition();
            }
        }
    }
    
    private void styleLabels(Component... roots) {
        for (Component c : roots) {
            if (c instanceof Label) {
                Label l = (Label)c;
                l.getAllStyles().setFgColor(0xffffff);
                l.getAllStyles().setBgTransparency(0);
                l.getAllStyles().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            }
            if (c instanceof Container) {
                Container cnt = (Container)c;
                for (Component child : cnt) {
                    styleLabels(child);
                }
            }
        }
    }
    
    List<Component> findByUIID(String uiid, Component root) {
        return findByUIID(new ArrayList<Component>(), uiid, root);        
    }
    
    List<Component> findByUIID(List<Component> out, String uiid, Component root) {
        if (uiid.equals(root.getUIID())) {
            out.add(root);
        }
        if (root instanceof Container) {
            Container cnt = (Container)root;
            for (Component child : cnt) {
                findByUIID(out, uiid, child);
            }
        }
        return out;
    }
    
    private static void makeTransparent(Component c) {
        if (c instanceof Container) {
            Container cnt = (Container)c;
            for (Component child : cnt) {
                makeTransparent(child);
            }
        } 
        c.getAllStyles().setBgTransparency(0);
        c.getAllStyles().setBorder(Border.createEmpty());

        
    }
    
    
    
}
