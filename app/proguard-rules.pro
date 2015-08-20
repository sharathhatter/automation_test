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
# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}
-keep interface android.support.** { *; }

# Retrofit & OkHttp
-keep class retrofit.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn rx.**
-dontwarn retrofit.appengine.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

# Facebook
-keep class com.facebook.** { *; }
-keep class sun.misc.Unsafe { *; }

# Gson
-keep class com.google.gson.stream.** { *; }

# Moengage
-dontwarn com.google.android.gms.location.**
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


# Http classes
-keep class org.apache.http.entity.** {
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


# Mobikwik
-keepclassmembers class com.paymentsdk.android.PGWebView$MyJavaScriptInterface{
   public *;
}

-keep class com.paymentsdk.android.model.** { *; }
-keep class * extends android.app.Activity { *; }
-keep class android.support.v4.** { *; }