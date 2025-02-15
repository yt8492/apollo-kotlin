import com.apollographql.apollo.sample.server.SampleServer
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.ws.KtorWebSocketEngine
import com.apollographql.apollo3.network.ws.WebSocketNetworkTransport
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import sample.server.CountSubscription

class SampleServerCustomTest {
  private fun websocketReopenWhenDoesNotPile(customizeTransport: WebSocketNetworkTransport.Builder.() -> WebSocketNetworkTransport.Builder) {

    val port = 56678
    val url = "http://localhost:$port/subscriptions"
    var sampleServer: SampleServer? = SampleServer(port)

    var reopenCount = 0

    println("serverUrl=${sampleServer?.subscriptionsUrl()}")
    val apolloClient = ApolloClient.Builder()
        .subscriptionNetworkTransport(
            WebSocketNetworkTransport.Builder()
                .serverUrl(url)
                .reopenWhen { _, _ ->
                  println("reopen ${System.currentTimeMillis() / 1000}")

                  delay(1000)
                  reopenCount++
                  true
                }
                .customizeTransport()
                .build()
        )
        .serverUrl("https://unused.com/")
        .build()

    runBlocking {
      repeat(50) { id ->
        launch {
          try {
            withTimeout(30_000) {
              apolloClient.subscription(CountSubscription(Int.MAX_VALUE, 3600 * 1000))
                  .toFlow()
                  .onEach {
                    println("#$id ${it.data}")
                  }
                  .take(2)
                  .collect()
            }
          } catch (e: TimeoutCancellationException) {
            error("timeout")
          }
        }
      }
      launch {
        delay(1000)
        println("closing server")
        sampleServer!!.close()
        println("server closed")
        delay(10_000)
        println("reopening server")
        sampleServer = SampleServer(port)
        println("server reopened")
      }
    }
    sampleServer?.close()
  }

  @Test
  fun websocketReopenWhenDoesNotPileDefault() = websocketReopenWhenDoesNotPile { this }

  @Test
  fun websocketReopenWhenDoesNotPileKtor() = websocketReopenWhenDoesNotPile { webSocketEngine(KtorWebSocketEngine()) }
}
