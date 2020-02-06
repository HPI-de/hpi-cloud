package crawler

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.news.entities.Article
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URI
import java.net.URL

abstract class ArticleCrawler {
    abstract val baseUri: URI
    abstract val serviceName: String
    abstract val crawlerVersion: String
    val serviceString
        get() = "[$serviceName]/$crawlerVersion"
    val userAgent
        get() = USER_AGENT_TEMPLATE.format(serviceString)

    data class Metrics(val requestCount: Int = 0)

    var metrics = Metrics()
        private set

    fun createDocumentQuery(url: URL): Connection {
        metrics = metrics.copy(requestCount = metrics.requestCount + 1)
        return Jsoup
            .connect(url.toString())
            .userAgent(userAgent)
    }

    abstract fun query(indexOffset: Int = 0): Sequence<Pair<Id<Article>, Article>>
}
