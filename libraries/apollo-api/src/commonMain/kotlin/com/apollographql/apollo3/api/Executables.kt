@file:JvmName("Executables")

package com.apollographql.apollo3.api

import com.apollographql.apollo3.annotations.ApolloInternal
import com.apollographql.apollo3.api.json.BufferedSinkJsonWriter
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.api.json.MapJsonWriter
import okio.Buffer
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads


/**
 * Returns a map of the variables as they would be sent over the wire. Use this to construct your own HTTP requests
 */
fun <D : Executable.Data> Executable<D>.variables(customScalarAdapters: CustomScalarAdapters): Executable.Variables {
  return variables(customScalarAdapters, false)
}

/**
 * Returns the variables as they would be sent over the wire. Use this to construct your own HTTP requests
 */
fun <D : Executable.Data> Executable<D>.variablesJson(customScalarAdapters: CustomScalarAdapters): String {
  val buffer = Buffer()
  BufferedSinkJsonWriter(buffer).apply {
    beginObject()
    serializeVariables(this, customScalarAdapters, false)
    endObject()
  }
  return buffer.readUtf8()
}

/**
 * Returns the [Set] of boolean variables that are **false** either explicitly or because there is a default value
 *
 * This inversion is needed for `@defer(if: Boolean! = true)` that default to true so absence of a value in the set
 * denotes true
 *
 * Order of precedence for a query like this: query($foo: Boolean = false)
 * - variables: {"foo": true} => bit not set
 * - variables: {"foo": false} => bit set
 * - variables: {} => bit set
 * Order of precedence for a query like this: query($foo: Boolean)
 * - variables: {"foo": true} => bit not set
 * - variables: {"foo": false} => bit set
 * - variables: {} => bit not set
 */
@ApolloInternal
fun <D : Executable.Data> Executable<D>.falseVariables(customScalarAdapters: CustomScalarAdapters): Set<String> {
  return variables(customScalarAdapters, true).valueMap.filter { it.value == false }.keys
}

@Suppress("UNCHECKED_CAST")
@ApolloInternal
fun <D : Executable.Data> Executable<D>.variables(
    customScalarAdapters: CustomScalarAdapters,
    withBooleanDefaultValues: Boolean,
): Executable.Variables {
  val valueMap = MapJsonWriter().apply {
    beginObject()
    serializeVariables(this, customScalarAdapters, withBooleanDefaultValues)
    endObject()
  }.root() as Map<String, Any?>
  return Executable.Variables(valueMap)
}

@JvmOverloads
fun <D : Executable.Data> Executable<D>.parseData(
    jsonReader: JsonReader,
    customScalarAdapters: CustomScalarAdapters = CustomScalarAdapters.Empty,
    falseVariables: Set<String>? = null,
    deferredFragmentIds: Set<DeferredFragmentIdentifier>? = null,
): D? {
  val adapterContext = CompositeAdapterContext.Builder()
      .customScalarAdapters(customScalarAdapters)
      .falseVariables(falseVariables)
      .deferredFragmentIdentifiers(deferredFragmentIds)
      .build()
  return adapter().nullable().fromJson(jsonReader, adapterContext)
}


fun <D : Executable.Data> Executable<D>.composeData(
    jsonWriter: JsonWriter,
    customScalarAdapters: CustomScalarAdapters,
    value: D
) {
  val adapterContext = CompositeAdapterContext.Builder()
      .customScalarAdapters(customScalarAdapters)
      .build()
  adapter().toJson(jsonWriter, value, adapterContext)
}