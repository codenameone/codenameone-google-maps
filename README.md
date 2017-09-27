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

If you are using the [beta version](https://github.com/codenameone/codenameone-google-maps/blob/master/GoogleMaps.cn1lib?raw=true) (installed manually - not through the CN1 Extensions manager), then you'll also need to add:

~~~
android.playServicesVersion=11.0.4
~~~~

**Android Note**

Currently (as of September 27, 2017) the Codename One extensions repository (through Codename One settings) includes version 22, which works with the default version of Google Play Services (8.3.0) on the build server.  The [GoogleMaps.cn1lib](https://github.com/codenameone/codenameone-google-maps/blob/master/GoogleMaps.cn1lib?raw=true) on the master branch includes some new features that require Google Play Services 9.4.0 or higher.  Once the build server has been updated to support the new play services version by default, the new version of GoogleMaps.cn1lib will be added to the repository.  Until then, you can install new new version by downloading it manually [from here](https://github.com/codenameone/codenameone-google-maps/blob/master/GoogleMaps.cn1lib?raw=true), copying it to your "libs" directory, and selecting "Refresh CN1Libs" from the Codename One Menu in the IDE.  If you do use this new version, you'll need to add the following build hint:

~~~~
android.playServicesVersion=9.4.0
~~~~

NOTE: Version 23 and higher require google play services 9.4.0 or higher.  If you require compatibility with the older version (8.3.0), you'll need to install GoogleMaps.cn1lib v22 or earlier.  You can download v22 [here](https://github.com/codenameone/codenameone-google-maps/releases/tag/v22).

This project was migrated from an old project on Google code http://code.google.com/p/codenameone-google-maps
