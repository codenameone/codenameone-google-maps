(function(exports){

    /**
     * Map type for native maps
     */
    var MAP_TYPE_TERRAIN = 1;

    /**
     * Map type for native maps
     */
    var MAP_TYPE_HYBRID = 2;

    /**
     * Map type for native maps
     */
    var MAP_TYPE_NONE = 3;
    
    var uniqueIdCounter = 0;
    
    function triggerResize(self) {
        var offset = jQuery(self.el).offset();
        var width = jQuery(self.el).width();
        var height = jQuery(self.el).height();
        
        if (self._lastOffset === undefined) {
            self._lastOffset = offset;
            self._lastWidth = width;
            self._lastHeight = height;
        } else {
            if (self._lastOffset.x != offset.x || self._lastOffset.y != offset.y || self._lastWidth != width || self._lastHeight != height) {
                google.maps.event.trigger(self.map, 'resize');
            }
        }
        
    }
    
    var rgbToHex = function (rgb) { 
        var hex = Number(rgb).toString(16);
        while (hex.length < 6) {
             hex = "0" + hex;
        }
        return '#'+hex;
    };
    
    function fromLatLngToPoint(latLng, map) {
        var topRight = map.getProjection().fromLatLngToPoint(map.getBounds().getNorthEast());
        var bottomLeft = map.getProjection().fromLatLngToPoint(map.getBounds().getSouthWest());
        var scale = Math.pow(2, map.getZoom());
        var worldPoint = map.getProjection().fromLatLngToPoint(latLng);
        return new google.maps.Point((worldPoint.x - bottomLeft.x) * scale, (worldPoint.y - topRight.y) * scale);
    }
    
    // We seem to get a race condition in chrome if we 
    // initialize the map before the element is added to the dom.
    // Therefore we set a timeout when first initializing the map 
    // But now we need to wait until this initialization happens before
    // subsequent calls on the map will work so we wrap any calls
    // that need to access the map in this function
    function ready(self, callback) {
        if (self.initialized || callback === undefined) {
            if (self.onInitialized !== undefined) {
                while (self.onInitialized.length > 0) {
                    (self.onInitialized.shift()).apply(self);
                }
            }
            if (callback !== undefined) {
                callback.apply(self);
            }
        } else {
            self.onInitialized = self.onInitialized || [];
            
            self.onInitialized.push(callback);
        }
    }

var o = {};

    o.initialize_ = function(callback) {
        this.pathStrokeColor = 0;
        this.pathStrokeWidth = 1;
        ready(this, function() {
            window.theMapEl = this.el;
            callback.complete();
        });
    };

    o.calcScreenPosition__double_double = function(param1, param2, callback) {
        ready(this, function() {
            var unscaleCoord = window.cn1UnscaleCoord !== undefined ? window.cn1UnscaleCoord : function(x){return x};
            triggerResize(this);
            var topRight=this.map.getProjection().fromLatLngToPoint(this.map.getBounds().getNorthEast()); 
            var bottomLeft=this.map.getProjection().fromLatLngToPoint(this.map.getBounds().getSouthWest()); 
            var scale=Math.pow(2,this.map.getZoom());
            this.lastPoint = this.map.getProjection().fromLatLngToPoint(new google.maps.LatLng(param1, param2));
            this.lastPoint = new google.maps.Point(unscaleCoord((this.lastPoint.x-bottomLeft.x)*scale),unscaleCoord((this.lastPoint.y-topRight.y)*scale));
            callback.complete();
        });
    };

    o.getLatitude_ = function(callback) {
        ready(this, function() {
            triggerResize(this);
            callback.complete(this.map.getCenter().lat());
        });
    };

    o.removeMapElement__long = function(param1, callback) {
        ready(this, function() {
            this.paths = this.paths || {};
            var line = this.paths[param1];
            if (line) {
                delete this.paths[param1];
                line.setMap(null);
            }
            this.markerLookup = this.markerLookup || {};
            var marker = this.markerLookup[param1];
            if (marker) {
                delete this.markerLookup[param1];
                marker.setMap(null);
            }
            callback.complete();
        });
    };

    o.getMinZoom_ = function(callback) {
        ready(this, function() {
            callback.complete(this.map.mapTypes.get(this.map.getMapTypeId()).minZoom);
        });
    };

    o.getScreenLon_ = function(callback) {
        ready( this, function() {
            callback.complete(this.lastPosition.lng());
        });
    };

    o.getLongitude_ = function(callback) {
        ready(this, function() { 
            callback.complete(this.map.getCenter().lng());
        });
    };

    o.getScreenX_ = function(callback) {
        ready(this, function() {
            callback.complete(this.lastPoint.x);
        });
    };

    o.setMapType__int = function(param1, callback) {
        ready(this, function() {
            switch (param1) {
                case MAP_TYPE_HYBRID :
                    this.map.setMapTypeId(google.maps.MapTypeId.HYBRID); break;
                case MAP_TYPE_TERRAIN :
                    this.map.setMapTypeId(google.maps.MapTypeId.TERRAIN); break;
                default :
                    this.map.setMapTypeId(google.maps.MapTypeId.ROADMAP); break;
            }
            callback.complete();
        });
    };

    o.calcLatLongPosition__int_int = function(param1, param2, callback) {
        ready(this, function() {
            triggerResize(this);
            // First convert these coordinates from cn1 coords
            param1 = window.cn1ScaleCoord !== undefined ? window.cn1ScaleCoord(param1) : param1;
            param2 = window.cn1ScaleCoord !== undefined ? window.cn1ScaleCoord(param2) : param2;
            
            // retrieve the lat lng for the far extremities of the (visible) map
            var latLngBounds = this.map.getBounds();
            var neBound = latLngBounds.getNorthEast();
            var swBound = latLngBounds.getSouthWest();
            //console.log("neBound = "+neBound+", swBound="+swBound);
    
            // convert the bounds in pixels
            var neBoundInPx = this.map.getProjection().fromLatLngToPoint(neBound);
            var swBoundInPx = this.map.getProjection().fromLatLngToPoint(swBound);
    
            // compute the percent of x and y coordinates related to the div containing the map; in my case the screen
            var procX = param1/jQuery(this.el).width();
            var procY = param2/jQuery(this.el).height();
    
            // compute new coordinates in pixels for lat and lng;
            // for lng : subtract from the right edge of the container the left edge, 
            // multiply it by the percentage where the x coordinate was on the screen
            // related to the container in which the map is placed and add back the left boundary
            // you should now have the Lng coordinate in pixels
            // do the same for lat
            var newLngInPx = (neBoundInPx.x - swBoundInPx.x) * procX + swBoundInPx.x;
            var newLatInPx = (swBoundInPx.y - neBoundInPx.y) * procY + neBoundInPx.y;
    
            // convert from google point in lat lng and have fun :)
            var newLatLng = this.map.getProjection().fromPointToLatLng(new google.maps.Point(newLngInPx, newLatInPx));
            
            //this.lastPosition = this.map.getProjection().fromPointToLatLng(new google.maps.Point(param1, param2));
            this.lastPosition = newLatLng;
            callback.complete();
        });
    };

    o.setShowMyLocation__boolean = function(param1, callback) {
        ready(this, function() {
            console.log("Show my location not implemented yet in Javascript port");
            callback.complete();
        });
    };

    o.createNativeMap__int = function(param1, callback) {
        var self = this;
        //jQuery(document).ready(function() {
        self.el = jQuery('<div id=\"cn1-googlemaps-canvas\"></div>').get(0);
        if (self.el.parentNode !== null) {
            self.el.parentNode.removeChild(self.el);
        }
        var initialize = function(){
            
            self.mapId = param1;

            var mapOptions = {
                zoom: 11,
                center: new google.maps.LatLng(-34.397, 150.644)
            };
            self.map = new google.maps.Map(self.el, mapOptions);

            //var self = this;
            var fireTapEventStatic = self.$GLOBAL$.com_codename1_googlemaps_MapContainer.fireTapEventStatic__int_int_int$async;
            var fireLongPressEventStatic = self.$GLOBAL$.com_codename1_googlemaps_MapContainer.fireLongPressEventStatic__int_int_int$async;
            google.maps.event.addListener(self.map, 'click', function(evt) {
                //Point p = mapInstance.getProjection().toScreenLocation(point);
                //MapContainer.fireTapEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
                //var p = self.map.getProjection().fromLatLngToPoint(evt.latLng);
                if (cancelClick) {
                    cancelClick = false;
                    return;
                }
                var p = fromLatLngToPoint(evt.latLng, self.map);
                fireTapEventStatic(self.mapId, p.x, p.y);

            });
            
            var inLongPress = false;
            var cancelClick = false;
            google.maps.event.addListener(self.map, 'mousedown', function(evt) {
                var p = fromLatLngToPoint(evt.latLng, self.map);
                inLongPress = true;
                setTimeout(function() {
                    if (inLongPress) {
                        cancelClick = true;
                        fireLongPressEventStatic(self.mapId, p.x, p.y);
                    }
                }, 500);
                

            });
            google.maps.event.addListener(self.map, 'mouseup', function(evt) {
                inLongPress = false;

            });
            google.maps.event.addListener(self.map, 'dragstart', function(evt) {
                inLongPress = false;

            });

            var fireMapChangeEvent = self.$GLOBAL$.com_codename1_googlemaps_MapContainer.fireMapChangeEvent__int_int_double_double$async;
            var pendingBoundsEvent = null;
            google.maps.event.addListener(self.map, 'bounds_changed', function() {
                if (!self.initialized) {
                    callback.complete(self.el);
                    self.initialized = true;
                    ready(self, function() {
                        setTimeout(function() {
                            if (self.center) {
                                self.map.setCenter(self.center);
                            }
                            google.maps.event.trigger(self.map, 'resize');
                        }, 500);
                    });
                }
                if (pendingBoundsEvent === null) {
                    setTimeout(function() {
                        var evt = pendingBoundsEvent;
                        pendingBoundsEvent = null;
                        fireMapChangeEvent(self.mapId, evt.zoom, evt.lat, evt.lng);
                    }, 100);
                }
                pendingBoundsEvent = {zoom: self.map.getZoom(), lat: self.map.getCenter().lat(), lng: self.map.getCenter().lng()};
                
            });
            var pendingCenterEvent = null;
            
            google.maps.event.addListener(self.map, 'center_changed', function() {
                if (pendingCenterEvent === null) {
                    setTimeout(function() {
                        var evt = pendingCenterEvent;
                        pendingCenterEvent = null;
                        fireMapChangeEvent(self.mapId, evt.zoom, evt.lat, evt.lng);
                    }, 100);
                }
                pendingCenterEvent = {zoom: self.map.getZoom(), lat: self.map.getCenter().lat(), lng: self.map.getCenter().lng()};
            });
            google.maps.event.addListener(self.map, 'zoom_changed', function() {
                fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
            });
            google.maps.event.addListener(self.map, 'tilt_changed', function() {
                fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
            });
            google.maps.event.addListener(self.map, 'heading_changed', function() {
                fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
            });

            
        };
        initialize();
        setTimeout(function() {
            // If this still isn't initialized after 5 seconds, then something went really wrong
            // Rather than deadlock EDT, we'll return here
            if (!self.initialized) {
                console.log("Failed to initialize GoogleMaps.  Check network or your Javascript Key");
                callback.complete(self.el);
            }
        }, 5000);
        
    };

    o.setMarkerSize__int_int = function(w, h, callback) {
        this.markerWidth = w;
        this.markerHeight = h;
        callback.complete(null);
    };

    o.addMarker__byte_1ARRAY_double_double_java_lang_String_java_lang_String_boolean_float_float = function(param1, lat, lon, text, snippet, cb, anchorU, anchorV, callback) {
        ready(this, function() {
            triggerResize(this);
            var self = this;
            var uint8 = param1 !== null ? new Uint8Array(param1) : null;
            var url = uint8 !== null ? ('data:image/png;base64,' + window.arrayBufferToBase64(uint8.buffer)) : null;
            
            var icon = url == null ? url : new google.maps.MarkerImage(
                                // URL
                                url,
                                // (width,height)
                                new google.maps.Size(self.markerWidth, self.markerHeight),
                                // The origin point (x,y)
                                new google.maps.Point(0, 0),
                                // The anchor point (x,y)
                                new google.maps.Point(anchorU * self.markerWidth, anchorV * self.markerHeight)
                            );
            var markerOpts = {
                icon : icon,
                map : this.map,
                position : new google.maps.LatLng(lat, lon),
                title : text
            };
            
            var key = uniqueIdCounter++;
            this.markerLookup = this.markerLookup || {};
            
            var marker = new google.maps.Marker(markerOpts);
            
            var fireMarkerEvent = this.$GLOBAL$.com_codename1_googlemaps_MapContainer.fireMarkerEvent__int_long$async;
            google.maps.event.addListener(marker, 'click', function() {
                fireMarkerEvent(self.mapId, key);
            });
            
            this.markerLookup[key] = marker;
            
            callback.complete(key);
        });
    };

    o.setRotateGestureEnabled__boolean = function(param1, callback) {
        ready(this, function() {
            console.log("setRotateGestureEnabled not implemented yet in Javascript");
            callback.complete();
        });
    };

    o.finishPath__long = function(param1, callback) {
        ready(this, function() {
            var id = uniqueIdCounter++;
            this.paths = this.paths || {};
            this.paths[id] = new google.maps.Polyline(this.currentPath);
            this.paths[id].setMap(this.map);
            callback.complete(id);
        });
    };

    o.getMaxZoom_ = function(callback) {
        ready(this, function() {
            callback.complete(this.map.mapTypes.get(this.map.getMapTypeId()).maxZoom);
        });
    };

    o.getMapType_ = function(callback) {
        ready(this, function() {
            var type;
            switch (this.map.getMapTypeId()) {
                case google.maps.MapTypeId.HYBRID :
                    type = MAP_TYPE_HYBRID; break;
                case google.maps.MapTypeId.TERRAIN :
                case google.maps.MapTypeId.SATELLITE:
                    type = MAP_TYPE_TERRAIN; break;
                default :
                    type = MAP_TYPE_NONE;
                    
            }
            callback.complete(type);
        });
        
    };

    o.getScreenLat_ = function(callback) {
        ready(this, function() {
            callback.complete(this.lastPosition.lat());
        });
    };

    o.beginPath_ = function(callback) {
        ready(this, function() {
            this.currentPath = {path : [], strokeColor:rgbToHex(this.pathStrokeColor), strokeWeight: this.pathStrokeWidth};//new google.maps.PolylineOptions();
            callback.complete(1);
        });
    };

    o.setPosition__double_double = function(param1, param2, callback) {
        ready(this, function() {
        //console.log("Setting position");
            this.center = new google.maps.LatLng(param1, param2);
            this.map.setCenter(new google.maps.LatLng(param1, param2));
            callback.complete();
        });
    };

    o.deinitialize_ = function(callback) {
        ready(this, function() {
            //jQuery(this.el).remove();
            console.log("Deinitializing map");
            if (this.el.parentNode) {
                this.el.parentNode.removeChild(this.el);
            }
            callback.complete();
        });
    };

    /*
     * -(void)setPathStrokeColor:(int)param;
-(int)getPathStrokeColor;
-(void)setPathStrokeWidth:(int)param;
-(int)getPathStrokeWidth;
     */

    o.setPathStrokeColor__int = function(param, callback) {
        this.pathStrokeColor = param;
        callback.complete();
    };
    
    o.getPathStrokeColor_ = function(callback) {
        
        callback.complete(this.pathStrokeColor);
    };
    
    o.setPathStrokeWidth__int = function(param, callback) {
        this.pathStrokeWidth = param;
        callback.complete();
    };
    
    o.getPathStrokeWidth_ = function(callback) {
        callback.complete(this.pathStrokeWidth);
    };

    o.getZoom_ = function(callback) {
        ready(this, function() {
            callback.complete(this.map.getZoom());
        });
    };

    o.setZoom__double_double_float = function(param1, param2, param3, callback) {
        ready(this, function() {
            this.center = new google.maps.LatLng(param1, param2);
            this.map.setZoom(param3);
            this.map.setCenter(new google.maps.LatLng(param1, param2));
            callback.complete();
        });
    };

    o.getScreenY_ = function(callback) {
        ready(this, function() {
            callback.complete(this.lastPoint.y);
        });
    };

    o.addToPath__long_double_double = function(param1, param2, param3, callback) {
        ready(this, function() {
            this.currentPath.path.push(new google.maps.LatLng(param2, param3));
            callback.complete();
        });
    };

    o.removeAllMarkers_ = function(callback) {
        ready(this, function() {
            var toRemove = [];
            var self = this;
            if (this.markerLookup) {
                for (var key in this.markerLookup) {
                    self.markerLookup[key].setMap(null);
                    toRemove.push(key);
                }
            }
            for (var i=0; i<toRemove.length; i++) {
                delete this.markerLookup[toRemove[i]];
            }
            
            toRemove = [];
            if (this.paths) {
                for (var key in this.paths) {
                    self.paths[key].setMap(null);
                    toRemove.push(key);
                }
            }
            for (var i=0; i<toRemove.length; i++) {
                delete this.paths[toRemove[i]];
            }
            
            callback.complete();
        });
    };

    o.isSupported_ = function(callback) {
        callback.complete(true);
    };

exports.com_codename1_googlemaps_InternalNativeMaps= o;

})(cn1_get_native_interfaces());