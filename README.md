# AsmPluginDemo
## ASM
ASM 是一个通用的Java字节码操作和分析框架。它可以直接以二进制形式用于修改现有类或动态生成类。 ASM提供了一些常见的字节码转换和分析算法，可以从中构建定制的复杂转换和代码分析工具。 ASM提供与其他Java字节码框架类似的功能，但侧重于性能。因为它的设计和实现是尽可能的小和尽可能快，所以它非常适合在动态系统中使用（但当然也可以以静态方式使用，例如在编译器中）。

## 自定义plugin开发
Gradle从1.5开始，Gradle插件包含了一个叫Transform的API，这个API允许第三方插件在class文件转为为dex文件前操作编译好的class文件，这个API的目标是简化自定义类操作，而不必处理Task，并且在操作上提供更大的灵活性。并且可以更加灵活地进行操作。官方文档：http://google.github.io/android-gradle-dsl/javadoc/

## 运行注意事项
* 首先要生成plugin，所以要先注释`apply plugin: 'asmplugin'`及`classpath 'com.demo:asm-plugin:1.0.0'`,还有`AsmConfig {...}`