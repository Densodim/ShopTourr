# Add project specific ProGuard rules here.
# Keep enough for KMP / Ktor / SQLDelight / Firebase / Compose.

-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,EnclosingMethod,Signature

# Kotlin / coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlin.Metadata { *; }

# Ktor / serialization
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# SQLDelight
-keep class com.example.shoptourr.db.** { *; }
-keep class app.cash.sqldelight.** { *; }

# SQLCipher
-keep class net.zetetic.** { *; }
-dontwarn net.zetetic.**

# Koin
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}

# Glance widgets
-keep class com.example.shoptourr.widget.** { *; }
-dontwarn androidx.glance.**
