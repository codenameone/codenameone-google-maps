# Codename One Google Native Maps Support

Allows [Codename One](https://www.codenameone.com/) developers to embed native Google Maps on iOS/Android or
fallback to Codename One MapComponent on other platforms.
Check out a brief tutorial on using this project here:
http://www.codenameone.com/blog/mapping-natively.html

# Limitations
1. The native maps are only supported on Android devices that have the Google Play store (e.g. not on Amazon Kindle)
and on iOS devices. All other devices will show the MapComponent by default.
Map component will be used on the simulator as well.

2. Since a native component is used placing overlays is problematic. You will need to use Dialogs and the API's of the MapContainer class to implement this.

# Configuration
The configuration portion is the hardest part, Google made it especially painful in the Google typical way.
You can follow the instructions from Google to get started for [Android](https://developers.google.com/maps/documentation/android/start), for [iOS](https://developers.google.com/maps/documentation/ios/start/), and
for [Javascript](https://developers.google.com/maps/documentation/javascript/).

You will need to follow their instructions to generate your map keys. Then define the following build arguments
within your project:

```
javascript.googlemaps.key=YOUR_JAVASCRIPT_API_KEY
android.xapplication=<meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="YOUR_ANDROID_API_KEY"/>
ios.afterFinishLaunching=[GMSServices provideAPIKey:@"YOUR_IOS_API_KEY"];
```

Make sure to replace the values YOUR_ANDROID_API_KEY, YOUR_IOS_API_KEY, and YOUR_JAVASCRIPT_API_KEY with the values you
obtained from the Google Cloud console by following the instructions for [Android](https://developers.google.com/maps/documentation/android/start)
, for [iOS](https://developers.google.com/maps/documentation/ios/start/), and for [Javascript](https://developers.google.com/maps/documentation/javascript/).

NOTE: Version 23 and higher require google play services 9.4.0 or higher, this library will automatically cause your app to build against 9.4.0 or higher.  If you require compatibility with the older version (8.3.0), you'll need to install GoogleMaps.cn1lib v22 or earlier.  You can download v22 [here](https://github.com/codenameone/codenameone-google-maps/releases/tag/v22).

This project was migrated from an old project on Google code http://code.google.com/p/codenameone-google-maps
