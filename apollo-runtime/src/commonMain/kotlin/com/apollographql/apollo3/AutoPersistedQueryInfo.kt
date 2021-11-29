package com.apollographql.apollo3

import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.ExecutionContext
import com.apollographql.apollo3.api.HasMutableExecutionContext
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.api.http.HttpMethod
import com.apollographql.apollo3.api.http.sendApqExtensions
import com.apollographql.apollo3.api.http.sendDocument

internal class AutoPersistedQueryConfiguration(
    val httpMethodForHashedQueries: HttpMethod,
) : ExecutionContext.Element {
  override val key: ExecutionContext.Key<*>
    get() = Key

  companion object Key : ExecutionContext.Key<AutoPersistedQueryConfiguration>
}

internal class AutoPersistedQueryContext(
    val enabled: Boolean,
) : ExecutionContext.Element {
  override val key: ExecutionContext.Key<*>
    get() = Key

  companion object Key : ExecutionContext.Key<AutoPersistedQueryContext>
}

/**
 * Information about auto persisted queries
 *
 * @param hit: whether the query was hit or required another round trip to send the document
 */
class AutoPersistedQueryInfo(
    val hit: Boolean,
) : ExecutionContext.Element {
  override val key: ExecutionContext.Key<*>
    get() = Key

  companion object Key : ExecutionContext.Key<AutoPersistedQueryInfo>
}

val <D : Operation.Data> ApolloResponse<D>.autoPersistedQueryInfo
  get() = executionContext[AutoPersistedQueryInfo]

/**
 * Enable autoPersistedQueries
 */
fun <T> HasMutableExecutionContext<T>.enableAutoPersistedQueries(enable: Boolean): T where T : HasMutableExecutionContext<T>  {
  return addExecutionContext(AutoPersistedQueryContext(enable))
}

@Deprecated("Please use ApolloClient.Builder methods instead. This will be removed in v3.0.0.", level = DeprecationLevel.ERROR)
@Suppress("UNUSED_PARAMETER")
fun ApolloClient.withHashedQuery(hashed: Boolean): Nothing = throw NotImplementedError()

@Deprecated("Please use ApolloRequest.Builder methods instead. This will be removed in v3.0.0.", level = DeprecationLevel.ERROR)
@Suppress("UNUSED_PARAMETER")
fun <D : Operation.Data> ApolloRequest<D>.withHashedQuery(hashed: Boolean): Nothing = throw NotImplementedError()
