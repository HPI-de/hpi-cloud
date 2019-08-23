package de.hpi.cloud.news

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment

object CouchbaseSyncer {

    const val COUCHBASE_CONNECT_TIMEOUT = 10000L
    const val COUCHBASE_ENVVAR_USERNAME = "HPI_CLOUD_COUCHBASE_USERNAME"
    const val COUCHBASE_ENVVAR_PASSWORD = "HPI_CLOUD_COUCHBASE_PASSWORD"
    const val COUCHBASE_ENVVAR_HOST = "HPI_CLOUD_COUCHBASE_HOSTNAME"
    const val BUCKET_NAME = "news"

    fun dbOps(bucket: String, runnable: (Bucket) -> Unit) {
        val hostname = System.getenv(COUCHBASE_ENVVAR_HOST) ?: error("Couchbase host must be provided via the environment variable $COUCHBASE_ENVVAR_HOST")
        println("Connecting to Couchbase at $hostname")
        CouchbaseCluster
            .create(
                DefaultCouchbaseEnvironment.Builder()
                    .connectTimeout(COUCHBASE_CONNECT_TIMEOUT)
                    .build(),
                hostname
            )
            .apply {
                val username = System.getenv(COUCHBASE_ENVVAR_USERNAME)
                    ?: error("Couchbase username must be provided via the environment variable $COUCHBASE_ENVVAR_USERNAME")
                val password = System.getenv(COUCHBASE_ENVVAR_PASSWORD)
                    ?: error("Couchbase password must be provided via the environment variable $COUCHBASE_ENVVAR_PASSWORD")
                authenticate(username, password)
            }
            .also { cluster ->
                cluster
                    .openBucket(bucket)
                    .also(runnable)
                    .close()
            }
            .disconnect()
    }


    //"https://hpi.de/news/jahrgaenge/2019/schluesselbund-ade-hpi-studierende-ermoeglichen-verhaltensbasierte-zugangskontrolle.html" IN OBJECT_INNER_VALUES(`value`.link)

//        val articles = HpiMediaArchiveCrawler().query(1, 2)
//        articles
//            .map {
//                it
//                    .put("type", "article")
//                    .put("version", 1)
//            }
//            .forEach { article ->
//                val type = "article"
//                val links = (article["value"] as JsonObject)["link"].toString()
//                val query = N1qlQuery.simple(
//                    "SELECT id FROM ${bucket.name()} WHERE type=\"$type\" AND ANY link IN OBJECT_INNER_VALUES($links) SATISFIES link IN OBJECT_INNER_VALUES(`value`.link) END",
//                    N1qlParams.build().adhoc(false)
//                )
//                println(query)
//                bucket.query(query).allRows().forEach {
//                    println(it)
//                }
//            }


//            if (bucket.exists(documentId(article))) {
//                println("id exists: " + article["id"])
//                var i = 2
//                var uid: String
//                do {
//                    uid = "${article["id"]}_${i++}"
//                    println("trying: " + uid)
//                } while (bucket.exists(documentId(article, uid)))
//                println("success with: " + uid)
//                article.put("id", uid)
//            }
//            val document = JsonDocument.create(
//                "${article["type"]}:${article["id"]}",
//                article
//            )
//            println(bucket.upsert(document)
}
