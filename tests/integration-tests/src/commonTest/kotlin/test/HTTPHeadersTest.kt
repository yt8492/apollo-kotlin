package test

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.composeJsonResponse
import com.apollographql.apollo3.api.http.valueOf
import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.integration.normalizer.HeroNameQuery
import com.apollographql.apollo3.mockserver.MockResponse
import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.network.http.HttpInfo
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.apollographql.apollo3.network.http.KtorHttpEngine
import com.apollographql.apollo3.network.ws.DefaultWebSocketEngine
import com.apollographql.apollo3.network.ws.KtorWebSocketEngine
import com.apollographql.apollo3.testing.enqueue
import com.apollographql.apollo3.testing.internal.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class HTTPHeadersTest {
  private lateinit var mockServer: MockServer
  private lateinit var apolloClient: ApolloClient

  private suspend fun setUp(builder: ApolloClient.Builder.() -> Unit) {
    mockServer = MockServer()
    apolloClient = ApolloClient.Builder().serverUrl(mockServer.url()).apply(builder).build()
  }

  private suspend fun tearDown() {
    mockServer.stop()
  }

  @Test
  fun defaultEngineMakeSureHeadersAreSet() {
    makeSureHeadersAreSet {
      httpEngine(DefaultHttpEngine())
      webSocketEngine(DefaultWebSocketEngine())
    }
  }

  @Test
  fun ktorEngineMakeSureHeadersAreSet() {
    makeSureHeadersAreSet {
      httpEngine(KtorHttpEngine())
      webSocketEngine(KtorWebSocketEngine())
    }
  }

  private fun makeSureHeadersAreSet(
      builder: ApolloClient.Builder.() -> Unit
  ) = runTest(before = { setUp(builder) }, after = { tearDown() }) {
    val query = HeroNameQuery()
    val data = HeroNameQuery.Data(HeroNameQuery.Hero("R2-D2"))

    mockServer.enqueue(query, data)

    val response = apolloClient.query(query).execute()

    assertNotNull(response.data)

    val recordedRequest = mockServer.takeRequest()
    assertEquals("POST", recordedRequest.method)
    assertNotEquals(null, recordedRequest.headers["Content-Length"])
    assertNotEquals("0", recordedRequest.headers["Content-Length"])
  }

  @Test
  fun defaultEngineHeadersCanBeReadInResponseExecutionContext() {
    headersCanBeReadInResponseExecutionContext {
      httpEngine(DefaultHttpEngine())
      webSocketEngine(DefaultWebSocketEngine())
    }
  }

  @Test
  fun ktorEngineHeadersCanBeReadInResponseExecutionContext() {
    headersCanBeReadInResponseExecutionContext {
      httpEngine(KtorHttpEngine())
      webSocketEngine(KtorWebSocketEngine())
    }
  }

  private fun headersCanBeReadInResponseExecutionContext(
      builder: ApolloClient.Builder.() -> Unit
  ) = runTest(before = { setUp(builder) }, after = { tearDown() }) {
    val query = HeroNameQuery()
    val data = HeroNameQuery.Data(HeroNameQuery.Hero("R2-D2"))

    val json = buildJsonString {
      query.composeJsonResponse(this, data)
    }

    mockServer.enqueue(
        MockResponse.Builder()
            .body(json)
            .addHeader("Header1", "Value1")
            .addHeader("Header2", "Value2")
            .build()
    )

    val response = apolloClient.query(query).execute()

    assertEquals(response.executionContext[HttpInfo]?.headers?.valueOf("Header1"), "Value1")
    assertEquals(response.executionContext[HttpInfo]?.headers?.valueOf("Header2"), "Value2")
  }
}
