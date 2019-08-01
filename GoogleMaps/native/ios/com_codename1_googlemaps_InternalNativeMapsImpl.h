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
#import <Foundation/Foundation.h>
#import "GoogleMaps/GoogleMaps.h"

@interface com_codename1_googlemaps_InternalNativeMapsImpl : NSObject<GMSMapViewDelegate> {
GMSMapView *mapView;
    int mapId;
    CGPoint currentPoint;
    CLLocationCoordinate2D currentCoordinate;
    BOOL showMyLocation;
    BOOL rotateGesture;
    int pathStrokeWidth;
    int pathStrokeColor;
}

-(long long)addMarker:(NSData*)param param1:(double)param1 param2:(double)param2 param3:(NSString*)param3 param4:(NSString*)param4 param5:(BOOL)param5 param6:(float)param6 param7:(float)param7;
-(long long)beginPath;
-(void)setPosition:(double)param param1:(double)param1;
-(float)getZoom;
-(void)setZoom:(double)param param1:(double)param1 param2:(float)param2;
-(void)addToPath:(long long)param param1:(double)param1 param2:(double)param2;
-(void)removeAllMarkers;
-(int)getMinZoom;
-(void)removeMapElement:(long long)param;
-(double)getLatitude;
-(double)getLongitude;
-(void)setMapType:(int)param;
-(void*)createNativeMap:(int)param;
-(int)getMapType;
-(int)getMaxZoom;
-(long long)finishPath:(long long)param;
-(BOOL)isSupported;
-(void)deinitialize;
-(void)initialize;

-(void)setMarkerSize:(int)param param1:(int)param1;
-(void)calcScreenPosition:(double)param  param1:(double)param1;
-(int)getScreenX;
-(int) getScreenY;
-(void) calcLatLongPosition:(int)param param1:(int)param1;
-(double) getScreenLat;
-(double) getScreenLon;
-(void)setShowMyLocation:(BOOL)param;
-(void)setRotateGestureEnabled:(BOOL)param;
/*
 public void setPathStrokeColor(int color) {
        
    }
    public int getPathStrokeColor() {
        return 0;
    }
    public void setPathStrokeWidth(int width) {
        
    }
    public int getPathStrokeWidth() {
        return 1;
    }
 */
-(void)setPathStrokeColor:(int)param;
-(int)getPathStrokeColor;
-(void)setPathStrokeWidth:(int)param;
-(int)getPathStrokeWidth;

@end
