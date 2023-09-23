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
-optimizationpasses 5   #指定代码的压缩级别 0 - 7
-dontusemixedcaseclassnames  #是否使用大小写混合
-dontskipnonpubliclibraryclasses #如果应用程序引入的有jar包，并且想混淆jar包里面的class
-dontpreverify #混淆时是否做预校验（可去掉加快混淆速度）
-verbose #混淆时是否记录日志（混淆后生产映射文件 map 类名 -> 转化后类名的映射
#-ignorewarnings #忽略警告
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  #混淆采用的算法
-printmapping proguardMapping.txt #混淆映射文件
-keepattributes *Annotation*,InnerClasses  #假如项目中有用到注解，应加入这行配置
-keepattributes Signature #过滤泛型（不写可能会出现类型转换错误，一般情况把这个加上就是了）
-keepattributes SourceFile,LineNumberTable

-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

#（可选）避免Log打印输出
-assumenosideeffects class android.util.Log {
        public static *** v(...);
        public static *** d(...);
        public static *** i(...);
        public static *** w(...);
        public static *** e(...);
}

# ==================gson start=====================
#Gson
-keepclassmembers public class com.google.gson.**
-keepclassmembers public class com.google.gson.** {public private protected *;}

-keepclassmembers enum * { *; }
-keep class com.google.gson.* { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-keepattributes *Annotation*, Signature, Exception
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
#实体类单独配置
# ==================gson end=====================

-keep class com.welcome.browser.model.** {*;}
