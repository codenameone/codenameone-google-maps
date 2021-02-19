using com.codename1.impl;
using Windows.Devices.Geolocation;
using Windows.UI.Xaml.Controls.Maps;
using System;
namespace com.codename1.googlemaps{
   

    public class InternalNativeMapsImpl : IInternalNativeMapsImpl {

        const string TOKEN_KEY = "windows.bingmaps.token";

        MapControl mapControl;
        int mapId;
        int currPx;
        int currPy;
        double currLat;
        double currLng;

    public void initialize() {
    }

    public int getMaxZoom() {
            int res = 0;
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                res = (int)mapControl.MaxZoomLevel;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
            return res;
    }

    public long finishPath(long param) {
        return 0;
    }

    public void calcLatLongPosition(int param, int param1) {
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                Windows.Devices.Geolocation.BasicGeoposition cityPosition = new BasicGeoposition() { Latitude = 0, Longitude = 0 };
                Geopoint pt = new Geopoint(cityPosition);
                Windows.Foundation.Point p = new Windows.Foundation.Point(param/SilverlightImplementation.scaleFactor, param1/SilverlightImplementation.scaleFactor);
                mapControl.GetLocationFromOffset(p, out pt);
                currLat = pt.Position.Latitude;
                currLng = pt.Position.Longitude;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
           
            
        }

    public double getScreenLon() {
            return currLng;
    }

    public double getScreenLat() {
        return currLat;
    }

    public int getScreenY() {
            return currPy;
    }

    public void removeMapElement(long param) {
    }

    public double getLatitude() {
            double res = 0;
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
               res = mapControl.Center.Position.Latitude;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
            return res;
    }

    public int getScreenX() {
            return currPx;
    }

    public void setRotateGestureEnabled(bool param) {
    }

    public void removeAllMarkers() {
    }

    public void calcScreenPosition(double param, double param1) {
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                Windows.Devices.Geolocation.BasicGeoposition cityPosition = new BasicGeoposition() { Latitude = param, Longitude = param1 };
                Geopoint pt = new Geopoint(cityPosition);
                Windows.Foundation.Point p = new Windows.Foundation.Point();
                mapControl.GetOffsetFromLocation(pt, out p);
                currPx = (int)(p.X*SilverlightImplementation.scaleFactor);
                currPy = (int)(p.Y*SilverlightImplementation.scaleFactor);
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
           

    }

    public object createNativeMap(int param) {
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                mapControl = new MapControl();
                mapId = param;
                mapControl.ZoomInteractionMode = MapInteractionMode.GestureAndControl;
                mapControl.TiltInteractionMode = MapInteractionMode.GestureAndControl;
                string token = com.codename1.ui.Display.getInstance().getProperty(TOKEN_KEY, "");
            
                mapControl.MapServiceToken = token;
                mapControl.ActualCameraChanged += MapControl_ActualCameraChanged;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();

            return mapControl;
    }

        private void MapControl_ActualCameraChanged(MapControl sender, MapActualCameraChangedEventArgs args)
        {
            com.codename1.googlemaps.MapContainer.fireMapChangeEvent(mapId, (int)mapControl.ZoomLevel, mapControl.Center.Position.Latitude, mapControl.Center.Position.Longitude);
        }

        public double getLongitude() {
            double res = 0;
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                res = mapControl.Center.Position.Longitude;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
            return res;
    }

    public long beginPath() {
        return 0;
    }

    public void setPosition(double param, double param1) {
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                Windows.Devices.Geolocation.BasicGeoposition cityPosition = new BasicGeoposition() { Latitude = param, Longitude = param1 };
                Geopoint cityCenter = new Geopoint(cityPosition);
                mapControl.Center = cityCenter;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
           
    }

    public float getZoom() {
            float res = 0;
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                res = (float)mapControl.ZoomLevel;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
            return res;
    }

    public void setZoom(double param, double param1, float param2) {
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                Windows.Devices.Geolocation.BasicGeoposition cityPosition = new BasicGeoposition() { Latitude = param, Longitude = param1 };
                Geopoint cityCenter = new Geopoint(cityPosition);
                mapControl.Center = cityCenter;
                mapControl.ZoomLevel = param2;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
           
        }

    public void deinitialize() {
    }

    public int getMapType() {
        return 0;
    }

    public int getMinZoom() {
            int res = 0;
            SilverlightImplementation.dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
               res = (int)mapControl.MinZoomLevel;
            }).AsTask().ConfigureAwait(false).GetAwaiter().GetResult();
            return res;
    }

    public void addToPath(long param, double param1, double param2) {
    }

    public void setMapType(int param) {
    }

    public void setShowMyLocation(bool param) {
    }

    public long addMarker(byte[] param, double param1, double param2, string param3, string param4, bool param5, float param6, float param7) {
        return 0;
    }

    public void setMarkerSize(int w, int h) {

    }

    public bool isSupported() {
        return com.codename1.ui.Display.getInstance().getProperty(TOKEN_KEY, null) != null ;
    }

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

}
}
