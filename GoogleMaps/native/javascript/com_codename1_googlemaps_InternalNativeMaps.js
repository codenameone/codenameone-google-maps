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
        //jQuery('body').append(this.el);
        callback.complete();
    };

    o.calcScreenPosition__double_double = function(param1, param2, callback) {
        ready(this, function() {
            this.lastPoint = this.map.getProjection().fromLatLngToPoint(new google.maps.LatLng(param1, param2));
            callback.complete();
        });
    };

    o.getLatitude_ = function(callback) {
        ready(this, function() {
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
            // retrieve the lat lng for the far extremities of the (visible) map
            var latLngBounds = this.map.getBounds();
            var neBound = latLngBounds.getNorthEast();
            var swBound = latLngBounds.getSouthWest();
    
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
                var p = self.map.getProjection().fromLatLngToPoint(evt.latLng);
                fireTapEventStatic(self.mapId, p.x, p.y);

            });
            
            var inLongPress = false;
            google.maps.event.addListener(self.map, 'mousedown', function(evt) {
                var p = self.map.getProjection().fromLatLngToPoint(evt.latLng);
                inLongPress = true;
                setTimeout(function() {
                    if (inLongPress) {
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
            google.maps.event.addListener(self.map, 'bounds_changed', function() {
                fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
            });
            google.maps.event.addListener(self.map, 'center_changed', function() {
                //console.log("Center changed");
                fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
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

            self.initialized = true;
            ready(self);
            
        };
        setTimeout(initialize, 500);
        callback.complete(self.el);
        
        //initialize();
        //google.maps.event.addDomListener(window, 'load', initialize);
        
    };

    o.addMarker__byte_1ARRAY_double_double_java_lang_String_java_lang_String_boolean = function(param1, lat, lon, text, snippet, cb, callback) {
        ready(this, function() {
            var self = this;
            var uint8 = new Uint8Array(param1);
            var url = 'data:image/png;base64,' + window.arrayBufferToBase64(uint8.buffer);
            var markerOpts = {
                icon : url,
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
            this.currentPath = {path : []};//new google.maps.PolylineOptions();
            callback.complete(1);
        });
    };

    o.setPosition__double_double = function(param1, param2, callback) {
        ready(this, function() {
        //console.log("Setting position");
            this.map.setCenter(new google.maps.LatLng(param1, param2));
            callback.complete();
        });
    };

    o.deinitialize_ = function(callback) {
        ready(this, function() {
            //jQuery(this.el).remove();
            callback.complete();
        });
    };

    o.getZoom_ = function(callback) {
        ready(this, function() {
            callback.complete(this.map.getZoom());
        });
    };

    o.setZoom__double_double_float = function(param1, param2, param3, callback) {
        ready(this, function() {
        //console.log("Setting zoom");
            this.map.setCenter(new google.maps.LatLng(param1, param2));
            this.map.setZoom(param3);
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
            for (var key in this.markerLookup) {
                self.markerLookup[key].setMap(null);
                toRemove.push(key);
            }
            for (var i=0; i<toRemove.length; i++) {
                delete this.markerLookup[toRemove[i]];
            }
            
            callback.complete();
        });
    };

    o.isSupported_ = function(callback) {
        callback.complete(true);
    };

exports.com_codename1_googlemaps_InternalNativeMaps= o;

})(cn1_get_native_interfaces());