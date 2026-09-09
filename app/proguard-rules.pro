# ============================================
# PRODUCTION PROGUARD RULES FOR AIZAYA
# ============================================

# Target actual package name
-keep class com.zayaai.app.** { *; }
-keep enum com.zayaai.app.** { *; }
-keepclassmembers class com.zayaai.app.** { *; }

# Keep AI Service Models and Serializers
-keep class com.zayaai.app.OpenRouter** { *; }
-keep class com.zayaai.app.*Message { *; }
-keep class com.zayaai.app.*ContentPart { *; }
-keep class com.zayaai.app.AIService** { *; }
-keep class com.zayaai.app.*Serializer { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Retrofit & OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature, Exceptions, *Annotation*

# Gson
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keepattributes EnclosingMethod
-keepclassmembers class * implements java.io.Serializable { *; }

# PDFBox / JPX Filter Support
-dontwarn com.gemalto.jp2.JP2Decoder
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
