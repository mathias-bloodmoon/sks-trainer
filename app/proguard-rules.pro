# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Bewahrt die Modellklassen für kotlinx.serialization, da diese sonst beim
# JSON-Parsen von sks.json oder user_stats_v2.json nicht mehr gefunden werden.
-keep class com.sks.trainer.model.** { *; }
-keep class com.sks.trainer.data.UserStats { *; }
-keep class com.sks.trainer.data.CategoryStats { *; }

# kotlinx.serialization spezifische Regeln
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
