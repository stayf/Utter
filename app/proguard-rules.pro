-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep class !android.support.v7.internal.view.menu.**,android.support.** { *; }

-keep class org.drinkless.td.libcore.telegram.** { *; }
-keep class com.google.webp.** { *; }

-keepattributes SourceFile,LineNumberTable

-keep class com.stayfprod.utter.ui.** {
    void set*(***);
    *** get*();
}