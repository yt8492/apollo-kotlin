plugins {
  id("org.jetbrains.kotlin.jvm")
  id("apollo.library")

  // Depend on a published version of the plugin to avoid a "chicken and egg" problem
  id("com.apollographql.apollo3") version "4.0.0-alpha.1"
}

dependencies {
  api(project(":apollo-compiler"))

  api(project(":apollo-annotations")) {
    because("Allows to opt-in to experimental features")
  }

  implementation(project(":apollo-ast"))
  implementation(libs.apollo.runtime.published)
  implementation(libs.okhttp)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(libs.junit)
  testImplementation(libs.truth)
  testImplementation(libs.apollo.mockserver.published)
  testImplementation(libs.apollo.testingsupport.published)
}

apollo {
  // https://spec.graphql.org/June2018/#sec-Schema-Introspection
  service("graphql-june2018") {
    packageName.set("com.apollographql.apollo3.tooling.graphql.june2018")
    sourceFolder.set("graphql/june2018")
    generateAsInternal.set(true)
  }

  // https://spec.graphql.org/October2021/#sec-Schema-Introspection.Schema-Introspection-Schema
  service("graphql-october2021") {
    packageName.set("com.apollographql.apollo3.tooling.graphql.october2021")
    sourceFolder.set("graphql/october2021")
    generateAsInternal.set(true)
  }

  // https://spec.graphql.org/draft/#sec-Schema-Introspection.Schema-Introspection-Schema
  service("graphql-draft") {
    packageName.set("com.apollographql.apollo3.tooling.graphql.draft")
    sourceFolder.set("graphql/draft")
    generateAsInternal.set(true)
  }

  // https://studio.apollographql.com/public/apollo-platform/variant/main/home
  service("platform-api-public") {
    packageName.set("com.apollographql.apollo3.tooling.platformapi.public")
    sourceFolder.set("platform-api/public")
    generateAsInternal.set(true)
    mapScalarToKotlinString("GraphQLDocument")
    registry {
      graph.set("apollo-platform")
      graphVariant.set("main")
      key.set("")
      schemaFile.set(file("src/main/graphql/platform-api/public/schema.graphqls"))
    }
  }

  // https://studio-staging.apollographql.com/graph/engine/variant/prod/home
  service("platform-api-internal") {
    packageName.set("com.apollographql.apollo3.tooling.platformapi.internal")
    sourceFolder.set("platform-api/internal")
    generateAsInternal.set(true)
    introspection {
      endpointUrl.set("https://graphql.api.apollographql.com/api/graphql")
      schemaFile.set(file("src/main/graphql/platform-api/internal/schema.graphqls"))
    }
  }

}

// We're using project(":apollo-compiler") and the published "apollo-runtime" which do not have the same version
// TODO: exclude apollo-compiler from the version check as it's not used at runtime.
tasks.configureEach {
  if (name == "checkApolloVersions") {
    enabled = false
  }
}
