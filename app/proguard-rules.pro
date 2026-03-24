# Proguard rules for Order App
-keep class com.orderapp.** { *; }
-keep class com.orderapp.model.** { *; }
-keep class com.orderapp.api.** { *; }
-keep class com.orderapp.validator.** { *; }

# Retrofit
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.** { *; }

# Material Components
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }
