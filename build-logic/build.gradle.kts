import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin

plugins {
  `embedded-kotlin`
  id("java-gradle-plugin")
}

plugins.apply(SamWithReceiverGradleSubplugin::class.java)
extensions.configure(SamWithReceiverExtension::class.java) {
  annotations(HasImplicitReceiver::class.qualifiedName!!)
}

group = "com.apollographql.apollo3.build"

dependencies {
  compileOnly(gradleApi())
  compileOnly(libs.gegp)

  implementation(libs.okhttp)

  implementation(libs.kotlinx.benchmark)
  implementation(libs.dokka.plugin)
  implementation(libs.dokka.base)

  // We add all the plugins to the classpath here so that they are loaded with proper conflict resolution
  // See https://github.com/gradle/gradle/issues/4741
  implementation(libs.android.plugin)
  implementation(libs.gradle.japicmp.plugin)
  implementation(libs.vespene)
  implementation(libs.poet.java)
  implementation(libs.poet.kotlin)
  implementation(libs.intellij.plugin)
  implementation(libs.intellij.changelog)

  // We want the KSP plugin to use the version from the classpath and not force a newer version
  // of the Gradle plugin
  implementation(libs.kotlin.plugin)
  runtimeOnly(libs.ksp)
  // XXX: This is only needed for tests. We could have different build logic for different
  // builds but this seems just overkill for now
  runtimeOnly(libs.kotlin.allopen)
  runtimeOnly(libs.kotlinx.serialization.plugin)

  runtimeOnly(libs.sqldelight.plugin)
  runtimeOnly(libs.gradle.publish.plugin)
  runtimeOnly(libs.benmanes.versions)
  runtimeOnly(libs.gr8)
  runtimeOnly(libs.kotlinx.binarycompatibilityvalidator)
}

// Keep in sync with CompilerOptions.kt
java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
tasks.withType<JavaCompile>().configureEach {
  options.release.set(11)
}
tasks.withType(KotlinJvmCompile::class.java).configureEach {
  kotlinOptions.jvmTarget = "11"
}

gradlePlugin {
  plugins {
    register("apollo.library") {
      id = "apollo.library"
      implementationClass = "com.apollographql.apollo3.buildlogic.plugin.LibraryConventionPlugin"
    }

    register("apollo.test") {
      id = "apollo.test"
      implementationClass = "com.apollographql.apollo3.buildlogic.plugin.TestConventionPlugin"
    }
  }
}
