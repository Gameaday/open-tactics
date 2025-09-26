# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all model classes that use serialization
-keep @kotlinx.serialization.Serializable class com.gameaday.opentactics.** { *; }
-keep class com.gameaday.opentactics.model.** { *; }
-keep class com.gameaday.opentactics.data.** { *; }

# Keep enum classes
-keepclassmembers enum com.gameaday.opentactics.** {
    **[] $VALUES;
    public *;
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep ViewBinding classes
-keep class com.gameaday.opentactics.databinding.** { *; }

# Keep serialization-related classes
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep security crypto classes
-keep class androidx.security.crypto.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Optimization settings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 3
-allowaccessmodification
-dontpreverify

# Keep custom view classes
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}