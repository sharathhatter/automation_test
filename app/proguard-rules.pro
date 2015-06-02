# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/siddharthsrivastava/libs/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes *Annotation*,EnclosingMethod
-keepattributes Exceptions, Signature, InnerClasses
-keepattributes SourceFile,LineNumberTable

#Project
-keep class com.bigbasket.mobileapp.BuildConfig { *; }
-keep class com.bigbasket.mobileapp.model.** { *; }
-keep class com.bigbasket.mobileapp.apiservice.models.response.** { *; }

# Wibmo
-keep class com.enstage.wibmo.sdk.** { *; }
-keep class com.enstage.wibmo.sdk.inapp.InAppBrowserActivity$* { *; }
-keep class com.enstage.wibmo.util.** { *; }
-keepclassmembers class com.enstage.wibmo.sdk.inapp.pojo.** { *; }

# New Relic
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**

# Crashlytics
-keep class com.crashlytics.** { *; }

# Support library
-keep class android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
-keep class android.support.v7.widget.RoundRectDrawable { *; }

# Retrofit & OkHttp
-keep class retrofit.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn rx.**
-dontwarn retrofit.appengine.**
-dontwarn com.moe.pushlibrary.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

# Facebook
-keep class com.facebook.** { *; }
-keep class sun.misc.Unsafe { *; }

# Gson
-keep class com.google.gson.stream.** { *; }

# Moengage
-keep class com.moe.** { *; }
-keep class com.moengage.** { *; }

# Google licensing files
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# Localytics
-keep class com.localytics.android.** { *; }

# Required for attribution
-keep class com.google.android.gms.ads.** { *; }

# Required for Google Play Services (see http://developer.android.com/google/play-services/setup.html)
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep class com.google.android.gms.gcm.**{ *; }


-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Konotor
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.demach.** {
    <fields>;
    <methods>;
}

# Http classes
-keep class org.apache.http.entity.** {
    <fields>;
    <methods>;
}

# Demach GSON files
-keep,allowshrinking class com.google.gson.demach.** {
    <fields>;
    <methods>;
}

# Demach model
-keep,allowshrinking class com.demach.konotor.model.** {
 <fields>;
 <methods>;
}

# Konotor MAIN class
-keep,allowshrinking class com.demach.konotor.Konotor {
 <fields>;
 <methods>;
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

# Sqlite
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }

# Disable logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}