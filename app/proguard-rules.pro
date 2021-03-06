#（Basic 包名不混合大小写
-dontusemixedcaseclassnames
#（Basic）不忽略非公共的库类
-dontskipnonpubliclibraryclasses
#（Basic）输出混淆日志
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#（Basic）不进行优化
-dontoptimize
#（Basic）不进行预检验
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

#混淆注意事项第一条，保留四大组件及Android的其它组件
-keep public class * extends android.app.Activity
#（Basic）
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
#（Basic）
-keep public class com.google.vending.licensing.ILicensingService
#（Basic）
-keep public class com.android.vending.licensing.ILicensingService
#（Basic）混淆注意事项第二条，保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
# 混淆注意事项第四条，保持WebView中JavaScript调用的方法
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
#＃混淆注意事项第五条 自定义View （Basic）
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
# （Basic）混淆注意事项第七条，保持 Parcelable 不被混淆
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}
#（Basic） 混淆注意事项第八条，保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#（Basic）
-keepclassmembers class **.R$* {
    public static <fields>;
}
#（Basic）保留注解
-keepattributes *Annotation*
# （Basic）排除警告
-dontwarn android.support.**
# Understand the @Keep support annotation.
# （Basic）不混淆指定的类及其类成员
-keep class android.support.annotation.Keep
# （Basic）不混淆使用注解的类及其类成员
-keep @android.support.annotation.Keep class * {*;}
# （Basic）不混淆所有类及其类成员中的使用注解的方法
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
# （Basic）不混淆所有类及其类成员中的使用注解的字段
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}
# 不混淆所有类及其类成员中的使用注解的初始化方法
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
#保留源文件以及行号 方便查看具体的崩溃信息
-keepattributes SourceFile,LineNumberTable

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
# glide 的混淆代码
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# banner 的混淆代码
-keep class com.youth.banner.** {
    *;
 }
##---------------Begin: proguard configuration for Gson  ----------
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.weihuagu.receiptnotice.beans.** { *; }
##---------------End: proguard configuration for Gson  ----------
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature-keepattributes
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-ignorewarnings