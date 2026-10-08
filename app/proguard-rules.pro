# TexPro ERP release shrinking (R8)
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room / ledger models used by Moshi and Firestore maps
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }

# Google Sign-In / Credential Manager
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
