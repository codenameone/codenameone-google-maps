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

#import "com_codename1_googlemaps_InternalNativeMapsImpl.h"
#include "com_codename1_googlemaps_MapContainer.h"
#import "CodenameOne_GLViewController.h"

extern float scaleValue;

@implementation com_codename1_googlemaps_InternalNativeMapsImpl

-(void)setMarkerSize:(int)param param1:(int)param1 {
    // Not needed right now.
    // Only used by Javascript port
}

-(long long)addMarker:(NSData*)param param1:(double)param1 param2:(double)param2 param3:(NSString*)param3 param4:(NSString*)param4 param5:(BOOL)param5 param6:(float)param6 param7:(float)param7{
    __block GMSMarker *marker = nil;
    dispatch_sync(dispatch_get_main_queue(), ^{
        NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
        marker = [[GMSMarker alloc] init];
        marker.position = CLLocationCoordinate2DMake(param1, param2);
        marker.title = param3;
        marker.snippet = param4;
        marker.groundAnchor = CGPointMake(param6, param7);
        if(param != nil) {
            UIImage* img = nil;
            if ([[UIImage class] respondsToSelector:@selector(imageWithData:scale:)]){
                // If we are on retina we need to provide scale, or the images will be too big and 
                // blurry.
                // Scale version available only in iOS 6 and later so check here.
                img = [UIImage imageWithData:param scale:scaleValue];
            } else {
                img = [UIImage imageWithData:param];
            }
            marker.icon = img;
        }
        marker.map = mapView;
        marker.tappable = YES;
        if(param5) {
            marker.userData = @"";
        } else {
            marker.userData = nil;
        }
        
        [marker retain];
        [pool release];
    });
    
    return marker;
}

- (void) mapView:(GMSMapView *)mapView didTapAtCoordinate:(CLLocationCoordinate2D)coordinate {
    CGPoint pp = [mapView.projection pointForCoordinate:coordinate];
    com_codename1_googlemaps_MapContainer_fireTapEventStatic___int_int_int(CN1_THREAD_GET_STATE_PASS_ARG mapId, pp.x * scaleValue, pp.y * scaleValue);
}

- (void) mapView:(GMSMapView *)mapView didLongPressAtCoordinate:(CLLocationCoordinate2D)coordinate {
    CGPoint pp = [mapView.projection pointForCoordinate:coordinate];
    com_codename1_googlemaps_MapContainer_fireLongPressEventStatic___int_int_int(CN1_THREAD_GET_STATE_PASS_ARG mapId, pp.x * scaleValue, pp.y * scaleValue);
}
-(void)mapView:(GMSMapView *)mapView didChangeCameraPosition:(GMSCameraPosition *)position {
    com_codename1_googlemaps_MapContainer_fireMapChangeEvent___int_int_double_double(CN1_THREAD_GET_STATE_PASS_ARG mapId, (int)mapView.camera.zoom, mapView.camera.target.latitude, mapView.camera.target.longitude);
}

-(BOOL)mapView:(GMSMapView *)mapView didTapMarker:(GMSMarker *)marker {
    if(marker.userData != nil) {
        com_codename1_googlemaps_MapContainer_fireMarkerEvent___int_long(CN1_THREAD_GET_STATE_PASS_ARG mapId, marker);
        return YES;
    }
    return NO;
}

-(long long)beginPath{
    GMSMutablePath *path = [GMSMutablePath path];
    [path retain];
    return path;
}

-(void)setPosition:(double)param param1:(double)param1{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
        GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:param
                                                                longitude:param1
                                                                     zoom:mapView.camera.zoom];
        mapView.camera = camera;
        [mapView retain];
        [pool release];
    });
}

-(float)getZoom{
    return mapView.camera.zoom;
}

-(void)setZoom:(double)param param1:(double)param1 param2:(float)param2{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
        GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:param
                                                                longitude:param1
                                                                     zoom:param2];
        mapView.camera = camera;
        [mapView retain];
        [pool release];
    });
}

-(void)addToPath:(long long)param param1:(double)param1 param2:(double)param2{
    GMSMutablePath *path = (GMSMutablePath*) param;
    [path addLatitude:param1 longitude:param2];
}

-(void)removeAllMarkers{
    dispatch_async(dispatch_get_main_queue(), ^{
        [mapView clear];
    });
}

-(int)getMinZoom{
    return (int)mapView.minZoom;
}

-(void)removeMapElement:(long long)param{
    NSObject* n = (NSObject*)param;
    if([n isKindOfClass:[GMSMarker class]]) {
        dispatch_sync(dispatch_get_main_queue(), ^{
            GMSMarker* marker = (GMSMarker*)n;
            marker.map = nil;
        });
        return;
    }
    dispatch_sync(dispatch_get_main_queue(), ^{
        GMSPolyline *polyline = (GMSPolyline *)param;
        polyline.map = nil;
    });
}

-(double)getLatitude{
    __block double lat = 0;
    dispatch_sync(dispatch_get_main_queue(), ^{
        lat = mapView.camera.target.latitude;
    });
    return lat;
}

-(double)getLongitude{
    __block double lon = 0;
    dispatch_sync(dispatch_get_main_queue(), ^{
        lon = mapView.camera.target.longitude;
    });
    return lon;
}

-(void)setMapType:(int)param{
    dispatch_async(dispatch_get_main_queue(), ^{
        switch(param) {
            case 1:
                mapView.mapType = kGMSTypeSatellite;
                return;
            case 2:
                mapView.mapType = kGMSTypeHybrid;
                return;
        }
        mapView.mapType = kGMSTypeNormal;
    });
}

-(void*)createNativeMap:(int)param{
    mapId = param;
    dispatch_sync(dispatch_get_main_queue(), ^{
        NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
        GMSCameraPosition *camera = [GMSCameraPosition cameraWithLatitude:-33.86
                                                                longitude:151.20
                                                                     zoom:6];
        mapView = [GMSMapView mapWithFrame:CGRectZero camera:camera];
        mapView.myLocationEnabled = showMyLocation;
        mapView.settings.compassButton = showMyLocation;
        mapView.settings.myLocationButton = showMyLocation;
        mapView.settings.rotateGestures = rotateGesture;
        mapView.delegate = self;
        [mapView retain];
        [pool release];
    });
    return mapView;
}

-(void)setRotateGestureEnabled:(BOOL)param {
    dispatch_sync(dispatch_get_main_queue(), ^{
        rotateGesture = param;
        if(mapView != nil) {
            mapView.settings.rotateGestures = rotateGesture;
        } 
    });
}

-(void)setShowMyLocation:(BOOL)param {
    dispatch_sync(dispatch_get_main_queue(), ^{
        showMyLocation = param;
        if(mapView != nil) {
            mapView.myLocationEnabled = showMyLocation;
            mapView.settings.compassButton = showMyLocation;
            mapView.settings.myLocationButton = showMyLocation;
        } 
    });
}

-(int)getMapType{
    GMSMapViewType t = mapView.mapType;
    if(t == kGMSTypeSatellite) {
        return 1;
    }
    if(t == kGMSTypeHybrid) {
        return 2;
    }
    if(t == kGMSTypeTerrain) {
        return 1;
    }
    return 3;
}

-(int)getMaxZoom{
    return (int)mapView.maxZoom;
}

-(long long)finishPath:(long long)param{
    __block GMSPolyline *polyline = nil;
    int color = pathStrokeColor;
    int width = pathStrokeWidth;
    dispatch_async(dispatch_get_main_queue(), ^{
        GMSMutablePath *path = (GMSMutablePath*)param;
        polyline = [GMSPolyline polylineWithPath:path];
        [path release];
        polyline.strokeColor = UIColorFromRGB(color, 255);
        polyline.strokeWidth = width;
        polyline.map = mapView;
    });
    return polyline;
}

-(void)calcScreenPosition:(double)param  param1:(double)param1 {
    dispatch_sync(dispatch_get_main_queue(), ^{
        NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
        currentPoint = [mapView.projection pointForCoordinate:CLLocationCoordinate2DMake(param, param1)];
        [pool release];
    });
}
    
-(int)getScreenX {
    return currentPoint.x * scaleValue;
}
    
-(int) getScreenY {
    return currentPoint.y * scaleValue;
}

-(void) calcLatLongPosition:(int)param param1:(int)param1 {
    dispatch_sync(dispatch_get_main_queue(), ^{
        NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
        currentCoordinate = [mapView.projection coordinateForPoint:CGPointMake(param / scaleValue, param1 / scaleValue)];
        [pool release];
    });
}
    
-(double) getScreenLat {
    return currentCoordinate.latitude;
}
    
-(double) getScreenLon {
    return currentCoordinate.longitude;
}


-(BOOL)isSupported{
    return YES;
}

-(void)deinitialize {}
-(void)initialize {
    pathStrokeColor = 0;
    pathStrokeWidth = 1;
}

-(void)setPathStrokeColor:(int)param {
    pathStrokeColor = param;
}
-(int)getPathStrokeColor {
    return pathStrokeColor;
}
-(void)setPathStrokeWidth:(int)param {
    pathStrokeWidth = param;
}
-(int)getPathStrokeWidth {
    return pathStrokeWidth;
}
@end
