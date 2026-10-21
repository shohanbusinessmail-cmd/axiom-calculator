# Axiom Calculator release shrinking/obfuscation rules.
# The app ships no reflection-heavy third-party SDKs, so default R8 rules
# for AndroidX, Compose, Room and Kotlin coroutines are sufficient.

# Keep Room entities' no-arg constructors and fields used via reflection-free codegen.
-keep class com.axiom.calculator.data.db.** { *; }

# Keep line numbers for readable crash traces in local debugging builds.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
