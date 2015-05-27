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

var o = {};

    o.initialize_ = function(callback) {
        //jQuery('body').append(this.el);
        callback.complete();
    };

    o.calcScreenPosition__double_double = function(param1, param2, callback) {
        this.lastPoint = this.map.getProjection().fromLatLngToPoint(new google.maps.LatLng(param1, param2));
        callback.complete();
    };

    o.getLatitude_ = function(callback) {
        callback.complete(this.map.getCenter().lat());
    };

    o.removeMapElement__long = function(param1, callback) {
        this.paths = this.paths || {};
        var line = this.paths[param1];
        if (line) {
            delete this.paths[param1];
            line.setMap(null);
        }
        
        
        callback.complete();
    };

    o.getMinZoom_ = function(callback) {
        callback.complete(this.map.mapTypes.get(this.map.getMapTypeId()).minZoom);
    };

    o.getScreenLon_ = function(callback) {
        callback.complete(this.lastPosition.lng());
    };

    o.getLongitude_ = function(callback) {
        callback.complete(this.map.getCenter().lng());
    };

    o.getScreenX_ = function(callback) {
        callback.complete(this.lastPoint.x);
    };

    o.setMapType__int = function(param1, callback) {
        switch (param1) {
            case MAP_TYPE_HYBRID :
                this.map.setMapTypeId(google.maps.MapTypeId.HYBRID); break;
            case MAP_TYPE_TERRAIN :
                this.map.setMapTypeId(google.maps.MapTypeId.TERRAIN); break;
            default :
                this.map.setMapTypeId(google.maps.MapTypeId.ROADMAP); break;
        }
        callback.complete();
    };

    o.calcLatLongPosition__int_int = function(param1, param2, callback) {
        this.lastPosition = this.map.getProjection().fromPointToLatLng(new google.maps.Point(param1, param2));
        callback.complete();
    };

    o.setShowMyLocation__boolean = function(param1, callback) {
        callback.error(new Error("Not implemented yet"));
    };

    o.createNativeMap__int = function(param1, callback) {
        this.el = jQuery('<div id=\"cn1-googlemaps-canvas\"></div>').get(0);
        this.mapId = param1;
        
        var mapOptions = {
            zoom: 8,
            center: new google.maps.LatLng(-34.397, 150.644)
        };
        this.map = new google.maps.Map(this.el, mapOptions);
        
        var self = this;
        var fireTapEventStatic = this.$GLOBAL$.com_codename1_googlemaps_MapContainer.fireTapEventStatic__int_int_int$async;
        google.maps.event.addListener(this.map, 'click', function(evt) {
            //Point p = mapInstance.getProjection().toScreenLocation(point);
            //MapContainer.fireTapEventStatic(InternalNativeMapsImpl.this.mapId, p.x, p.y);
            var p = self.map.getProjection().fromLatLngToPoint(evt.latLng);
            fireTapEventStatic(self.mapId, p.x, p.y);
            
        });
        
        var fireMapChangeEvent = this.$GLOBAL$.com_codename1_googlemaps_MapContainer.fireMapChangeEvent__int_int_double_double$async;
        google.maps.event.addListener(this.map, 'bounds_changed', function() {
            fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
        });
        google.maps.event.addListener(this.map, 'center_changed', function() {
            fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
        });
        google.maps.event.addListener(this.map, 'zoom_changed', function() {
            fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
        });
        google.maps.event.addListener(this.map, 'tilt_changed', function() {
            fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
        });
        google.maps.event.addListener(this.map, 'heading_changed', function() {
            fireMapChangeEvent(self.mapId, self.map.getZoom(), self.map.getCenter().lat(), self.map.getCenter().lng());
        });
        
        
        
        callback.complete(this.el);
    };

    o.addMarker__byte_1ARRAY_double_double_java_lang_String_java_lang_String_boolean = function(param1, param2, param3, param4, param5, param6, callback) {
        callback.error(new Error("Not implemented yet"));
    };

    o.setRotateGestureEnabled__boolean = function(param1, callback) {
        callback.error(new Error("Not implemented yet"));
    };

    o.finishPath__long = function(param1, callback) {
        var id = uniqueIdCounter++;
        this.paths = this.paths || {};
        this.paths[id] = new google.maps.Polyline(this.currentPath);
        this.paths[id].setMap(this.map);
        callback.complete(id);
    };

    o.getMaxZoom_ = function(callback) {
        callback.complete(this.map.mapTypes.get(this.map.getMapTypeId()).maxZoom);
    };

    o.getMapType_ = function(callback) {
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
        
    };

    o.getScreenLat_ = function(callback) {
        callback.complete(this.lastPosition.lat());
    };

    o.beginPath_ = function(callback) {
        this.currentPath = {path : []};//new google.maps.PolylineOptions();
        callback.complete(1);
    };

    o.setPosition__double_double = function(param1, param2, callback) {
        this.map.setCenter(new google.maps.LatLng(param1, param2));
        callback.complete();
    };

    o.deinitialize_ = function(callback) {
        //jQuery(this.el).remove();
        callback.complete();
    };

    o.getZoom_ = function(callback) {
        callback.complete(this.map.getZoom());
    };

    o.setZoom__double_double_float = function(param1, param2, param3, callback) {
        this.map.setCenter(new google.maps.LatLng(param1, param2));
        this.map.setZoom(param3);
        callback.complete();
    };

    o.getScreenY_ = function(callback) {
        callback.complete(this.lastPoint.y);
    };

    o.addToPath__long_double_double = function(param1, param2, param3, callback) {
        this.currentPath.path.push(new google.maps.LatLng(param2, param3));
        callback.complete();
    };

    o.removeAllMarkers_ = function(callback) {
        callback.error(new Error("Not implemented yet"));
    };

    o.isSupported_ = function(callback) {
        callback.complete(true);
    };

exports.com_codename1_googlemaps_InternalNativeMaps= o;

})(cn1_get_native_interfaces());
