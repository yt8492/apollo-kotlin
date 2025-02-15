plugins {
  id("org.jetbrains.kotlin.jvm")
  id("apollo.test")
  id("com.apollographql.apollo3")
}

dependencies {
  implementation(libs.apollo.runtime)
  implementation(project(":multi-module-2:root"))
  testImplementation(libs.kotlin.test.junit)
}

apollo {
  service("multimodule2") {
    packageName.set("multimodule2.child")
    flattenModels.set(false)
    dependsOn(project(":multi-module-2:root"))
  }
}
