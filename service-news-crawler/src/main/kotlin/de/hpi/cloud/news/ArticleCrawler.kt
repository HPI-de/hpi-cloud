package de.hpi.cloud.news

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URI
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

abstract class ArticleCrawler {

    abstract val baseUri: URI
    abstract val serviceName: String
    abstract val crawlerVersion: String
    val serviceString get() = "[$serviceName]/$crawlerVersion"
    val userAgent: String by lazy {
        USER_AGENT_TEMPLATE.format(serviceString)
    }

    private object Metrics {
        val requestCounter = AtomicInteger()
    }

    val requestCount
        get() = Metrics.requestCounter.get()

    fun createDocumentQuery(url: URL): Connection {
        Metrics.requestCounter.incrementAndGet()
        return Jsoup
            .connect(url.toString())
            .userAgent(userAgent)
    }

    abstract fun query(indexOffset: Int = 0): Sequence<Article>
}
