# Codename One Google Native Maps Support

Allows [Codename One](https://www.codenameone.com/) developers to embed native Google Maps on iOS/Android or
fallback to Codename One MapComponent on other platforms.
Check out a brief tutorial on using this project here:
https://www.codenameone.com/blog/new-improved-native-google-maps.html

## Installation

### Via Codename One Preferences

1. Open Codename One Preferences (i.e. Control Center)
2. Go to "Advanced Settings" > "Extensions" in the menu.
3. Find the "Codename One Google Native Maps" option.
4. Press "Download"

Back in your IDE (e.g. IntelliJ, NetBeans, Etc..) select the "Refresh Cn1libs" option in the Codename One menu.

### Maven Dependency

If you are using Maven as your build tool, then you can simply paste the following snippet into your common/pom.xml file:

~~~~
<dependency>
  <groupId>com.codenameone</groupId>
  <artifactId>googlemaps-lib</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
~~~~

NOTE: You should replace the `version` with the [latest on Maven central](https://search.maven.org/artifact/com.codenameone/googlemaps-lib).

## Limitations
1. The native maps are only supported on Android devices that have the Google Play store (e.g. not on Amazon Kindle)
and on iOS devices. All other devices will show the MapComponent by default.
Map component will be used on the simulator as well.

2. Since a native component is used placing overlays is problematic. You will need to use Dialogs and the API's of the MapContainer class to implement this.

## Configuration
The configuration portion is the hardest part, Google made it especially painful in the Google typical way.
You can follow the instructions from Google to get started for [Android](https://developers.google.com/maps/documentation/android/start), for [iOS](https://developers.google.com/maps/documentation/ios/start/), and
for [Javascript](https://developers.google.com/maps/documentation/javascript/).  UWP uses https://code.msdn.microsoft.com/windowsapps/Bing-Maps-for-Windows-10-d7ae3e44[BingMaps] instead of Google maps.  

You will need to follow their instructions to generate your map keys. Then define the following build arguments
within your project:

```
javascript.googlemaps.key=YOUR_JAVASCRIPT_API_KEY
android.xapplication=<meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="YOUR_ANDROID_API_KEY"/>
ios.afterFinishLaunching=[GMSServices provideAPIKey:@"YOUR_IOS_API_KEY"];
```

Make sure to replace the values `YOUR_ANDROID_API_KEY`, `YOUR_IOS_API_KEY``, and YOUR_JAVASCRIPT_API_KEY` with the values you
obtained from the Google Cloud console by following the instructions for [Android](https://developers.google.com/maps/documentation/android/start)
, for [iOS](https://developers.google.com/maps/documentation/ios/start/), and for [Javascript](https://developers.google.com/maps/documentation/javascript/).

NOTE: You can specify the iOS Google Maps version by setting the `var.ios.pods.GoogleMaps.version` build hint, using Cocoapods version syntax.  The default value is currently `~> 3.8`.  If you wish to use a newer version (e.g. `3.8`), then you'll need to set your `ios.pods.platform` build hint to a higher version, as newer versions of GoogleMaps have higher iOS version requirements.  `~> 2.0` requires iOS 8 or higher.  `~> 3.8` requires iOS 9. 

For UWP, you'll need to define the `windows.bingmaps.token` display property inside your app's init() method to your Bing Maps token.  See [instructons on generating a BingMaps token](https://code.msdn.microsoft.com/windowsapps/Bing-Maps-for-Windows-10-d7ae3e44).  E.g.

```
Display.getInstance().setProperty("windows.bingmaps.token", "xxxxxxxxx");
```

NOTE: Version 23 and higher require google play services 9.4.0 or higher, this library will automatically cause your app to build against 9.4.0 or higher.  If you require compatibility with the older version (8.3.0), you'll need to install GoogleMaps.cn1lib v22 or earlier.  You can download v22 [here](https://github.com/codenameone/codenameone-google-maps/releases/tag/v22).

This project was migrated from an old project on Google code http://code.google.com/p/codenameone-google-maps

## Documentation

- [Javadocs](https://www.javadoc.io/doc/com.codenameone/googlemaps-common/latest/index.html)
- [Sample Maven Project](https://github.com/shannah/googlemaps-maven-demo)

## Building From Source

This project uses Maven as its build tool.

~~~~
git clone https://github.com/codenameone/codenameone-google-maps
cd codenameone-google-maps/GoogleMaps
mvn install
~~~~

This will install the library into your local maven repository so that you'll be able to add it as a dependency to any of your projects with the snippet:

~~~~
<dependency>
  <groupId>com.codenameone</groupId>
  <artifactId>googlemaps-lib</artifactId>
  <version>THE_VERSION</version>
  <type>pom</type>
</dependency>
~~~~

Just replace `THE_VERSION` with the version in the GoogleMaps/pom.xml file.

IMPORTANT: Notice that you include the `googlemaps-lib` artifact and not the `googlemaps` artifact when using it as a dependency.  The root "googlemaps" artifact is just a wrapper project for the multi-module project.  The "lib" module is the actual cn1lib dependency.

### Building Legacy .cn1lib File

Maven projects no longer use .cn1lib file format, however the project still builds this format in case you want to distribute the cn1lib without using Maven's dependency mechanisms (or if you want to use the library with the legacy Ant project type).  When you run `mvn install` it will automatically build the cn1lib file.  **You'll find it inside the common/target** directory after performing a build.

