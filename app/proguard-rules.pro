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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn okio.**

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase


-keep public class top.imlk.oneword.systemui.injecter.AppInjecter{*;}

-keep public class top.imlk.oneword.util.AppStatus{*;}

-keep public class top.imlk.oneword.bean.ApiBean{*;}
-keep public class top.imlk.oneword.bean.WordBean{*;}
-keep public class top.imlk.oneword.bean.WordViewConfig{*;}
-keep public class top.imlk.oneword.bean.WordViewConfig$*{*;}

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.** -keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }

-keep public class com.zqc.opencc.android.lib.ChineseConverter{*;}
