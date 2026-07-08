# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep line numbers for readable release crash reports, hide original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Gson — model classes deserialized by reflection from bundled JSON assets.
# Their field names must survive obfuscation so JSON keys still map.
# ---------------------------------------------------------------------------
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

-keep class com.shoktuk.shoktukkeyboard.keyboard.KeyboardLayout { *; }
-keep class com.shoktuk.shoktukkeyboard.keyboard.KeyEntry { *; }

# Settings/enums are looked up by name (enumValueOf / .name) and some are
# (de)serialized, so keep the whole data package.
-keep class com.shoktuk.shoktukkeyboard.project.data.** { *; }
-keep class com.shoktuk.shoktukkeyboard.emoji.** { *; }

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ---------------------------------------------------------------------------
# QuickJS — JNI / native bridge, accessed reflectively.
# ---------------------------------------------------------------------------
-keep class com.whl.quickjs.** { *; }
-dontwarn com.whl.quickjs.**

# ---------------------------------------------------------------------------
# Enums used by name (SettingsManager.getEnum -> enumValueOf).
# ---------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}