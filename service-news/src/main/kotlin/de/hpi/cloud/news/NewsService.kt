package de.hpi.cloud.news

import com.google.protobuf.Timestamp
import de.hpi.cloud.common.v1test.Image
import de.hpi.cloud.news.NewsService.Companion.ARTICLES
import de.hpi.cloud.news.v1test.*
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.stub.StreamObserver

class NewsService {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = NewsService()
            service.start(args[0].toInt())
            service.blockUntilShutdown()
        }

        val ARTICLES = listOf(
            Article.newBuilder().apply {
                id = "1"
                sourceId = "hpi"
                link =
                    "https://hpi.de/news/jahrgaenge/2019/die-schul-cloud-fuer-brandenburg-bildungsministerin-britta-ernst-unterzeichnet-absichtserklaerung-zur-nutzung-der-hpi-schul-cloud.html"
                title =
                    "Die Schul-Cloud für Brandenburg: Bildungsministerin Britta Ernst unterzeichnet Absichtserklärung zur Nutzung der HPI Schul-Cloud"
                publishDate = Timestamp.newBuilder().apply {
                    seconds = 1551470400
                }.build()
                cover = Image.newBuilder().apply {
                    source =
                        "https://hpi.de/fileadmin/user_upload/hpi/bilder/teaser_news/2019/HPI_Schul_Cloud_2019_1020x420.jpg"
                    alt = "HPI Schul-Cloud für Brandenburg"
                }.build()
                teaser =
                    "Künftig sollen viel mehr Schulen in Brandenburg die HPI Schul-Cloud nutzen und sich miteinander vernetzen können. Eine entsprechende Absichtserklärung unterzeichneten am 01. März 2019 die Bildungsministerin Britta Ernst, HPI-Direktor Professor Christoph Meinel und die Projektpartner in Potsdam."
                content = """
                        Seit 2016 entwickelt das Hasso-Plattner-Institut unter der Leitung von Professor Christoph Meinel zusammen mit dem nationalen Excellence-Schulnetzwerk MINT-EC und gefördert durch das Bundesministerium für Bildung und Forschung die HPI Schul-Cloud. Sie soll die technische Grundlage schaffen, dass Lehrkräfte und Schüler in jedem Unterrichtsfach auch moderne digitale Lehr- und Lerninhalte nutzen können, und zwar so einfach, wie Apps über Smartphones oder Tablets nutzbar sind. Von August 2019 bis Juli 2021 wird nun auch eine Brandenburgische Version der Schul-Cloud entwickelt und evaluiert werden. Dazu haben heute in Potsdam Bildungsministerin Britta Ernst, der Gründungsgeschäftsführer der DigitalAgentur Brandenburg GmbH, Olav Wilms, und der Direktor des Hasso-Plattner-Instituts (HPI) und Leiter des Lehrstuhls Internet-Technologien und Systeme, Prof. Christoph Meinel, eine Absichtserklärung zur Pilotierung einer Schul-Cloud im Land Brandenburg unterzeichnet. Für die Pilotierung haben sich bereits 27 „medienfit sek I“-Schulen angemeldet. Weitere medienfit-Grundschulen werden folgen. Die schrittweise Inbetriebnahme der Cloud ist bis zum Schuljahr 2021/22 angestrebt.

                        Bildungsministerin Britta Ernst betonte: „Das Lernen mit digitalen Medien ist die Herausforderung mit der größten Dynamik. Dazu braucht es eine leistungsfähige digitale Infrastruktur. Zu den wichtigsten Vorhaben gehört die im Hasso-Plattner-Institut entwickelte Schul-Cloud. Sie soll die Schulen des Landes vernetzen, so dass Lehrkräfte, Schülerinnen und Schüler jederzeit und überall Zugang zu Lern- und Lehrmaterialien haben. Die professionelle zentrale Wartung für alle Schulen verringert deutlich den Verwaltungs- und Betreuungsaufwand. Wir versprechen uns von der Schul-Cloud einen deutlichen Schub in der digitalen Bildung und freuen uns auf den Start des Projekts.“

                        Auch HPI-Direktor Professor Christoph Meinel freut sich über die Kooperation mit dem Land Brandenburg. „Mit der HPI Schul-Cloud können Lehrkräfte und Schüler in jedem Unterrichtsfach sehr einfach digitale Lehr- und Lerninhalte nutzen und das unter Einhaltung der hohen gesetzlichen Datenschutzregelungen. Ich freue mich daher sehr, dass ab dem neuen Schuljahr weitere Schulen in Brandenburg mit der Schul-Cloud arbeiten werden.“

                        Derzeit arbeiten bundesweit bereits 100 ausgewählte MINT-EC-Schulen mit der HPI Schul-Cloud. Dazu kommen 43 Schulen der Niedersächsischen Bildungscloud (NBC).
                    """.trimIndent()
            }.build(),
            Article.newBuilder().apply {
                id = "2"
                sourceId = "hpi-mgzn"
                link = "https://hpimgzn.de/2019/von-wurmmehlkeksen-bis-hin-zu-kompostieranlagen/"
                title = "Von Wurmmehlkeksen bis hin zu Kompostieranlagen"
                publishDate = Timestamp.newBuilder().apply {
                    seconds = 1550256548
                }.build()
                addAuthorIds("Lilith Diringer")
                cover = Image.newBuilder().apply {
                    source = "https://hpimgzn.de/wp-content/uploads/2018/12/photo_2018-12-16_20-19-43.jpg"
                }.build()
                teaser =
                    "Am 16.12 hat der Nachhaltigkeitsklub seinen ersten Workshop angeboten. Von Bokashianlagenbau bis hin zur Herstellung selbstgemachter Weihnachtsgeschenke. Lest hier mehr."
                content = """
                        „Wollt ihr auch einmal Heuschrecken kosten?“ Diese Frage kursiert am 16.12. im HPI Hauptgebäude. Woher sie kommt? Aus Richtung der Teeküche, in der sich für den heutigen Tag eine Mischung aus Nachhaltigkeitsklubmitgliedern und externen Interessierten eingefunden hat. Welcher Anlass? Unser erster Nachhaltigkeitsworkshoptag." +

                        "Warum muss man immer alles kaufen? Heute wollen wir selbst Hand anlegen. Und so geht es gleich mit den ersten selbst gemachten Müsliriegeln los. Schon bald wird das Erdgeschoss des Hauptgebäudes mit Müsliriegelduft durchströmt. Aus dem Ofen können wir nach einiger Backzeit nicht nur ansehnliche, sondern auch sehr leckere Müslischnitten entnehmen." +

                        "Die Nachwärme der Herdplatte, den wir zum Nüsserösten verwendet haben, nutzen wir gleich weiter, um etwas anderes zu rösten: Heuschrecken und Mehlwürmer, sowie Buffalowürmer. Als diese fertig sind, können wir diese etwas ungewöhnliche Proteinquelle verspeisen sowie die von einer Teilnehmerin mitgebrachten Buffalomehlkekse probieren. Interessant!" +


                        "Natürlich gibt es zwischendurch auch etwas Leckeres zu essen – unter anderem Biogemüse." +
                        "Der Workshop geht weiter mit der Herstellung von Badekugeln und Waschmittel, sowie dem Bau einer Bokashi-Anlage – einem Innenkomposter. Für alle, die Lust haben davon etwas nachzumachen gibt es die Anleitungen hier." +


                        "Einer dieser Innenkomposter steht inzwischen auch in der Teeküche für euch bereit. Ihr seid herzlich dazu eingeladen, eure Bioabfälle in diesem zu entsorgen und auch, ab in ca. zwei Wochen, wenn der erste Dünger entstanden sein sollte, euch daran zu bedienen – eure Küchenpflanzen werden sich freuen." +


                        "Bei der Herstellung unserer Kompostieranlagen hat uns die Kreativität gepackt." +



                        "Geplant ist es, auch in Zukunft, einmal monatlich einen Workshoptag anzubieten. Dabei werden wir Trinkschokoladensticks sowie vegane Milchalternativen selbst herstellen, Shampoo produzieren, das Stricken von Hausschuhen erlernen…" +

                        "Besonders sollen diese Workshops als Austauschplattform und zum Ausprobieren von Alternativen dienen, sowie Freiräume zum Diskutieren bieten." +

                        "Kommt bei Ideen einfach auf uns zu oder schreibt uns an:" +
                        "Lilith.Diringer@student.hpi.de" +
                        "Malte.Barth@student.hpi.de
                    """.trimIndent()
                addAllCategories(
                    listOf(
                        Category.newBuilder().apply {
                            id = "allgemein"
                            name = "Allgemein"
                        }.build(),
                        Category.newBuilder().apply {
                            id = "klubs"
                            name = "Klubs"
                        }.build(),
                        Category.newBuilder().apply {
                            id = "klubs/nachhaltigkeitsklub"
                            name = "Nachhaltigkeitsklub"
                        }.build()
                    )
                )
                addAllTags(
                    listOf(
                        Tag.newBuilder().apply {
                            id = "essen"
                            name = "Essen"
                            articleCount = 1
                        }.build(),
                        Tag.newBuilder().apply {
                            id = "nachhaltigkeit"
                            name = "Nachhaltigkeit"
                            articleCount = 3
                        }.build(),
                        Tag.newBuilder().apply {
                            id = "nachhaltigkeitsklub"
                            name = "Nachhaltigkeitsklub"
                            articleCount = 5
                        }.build(),
                        Tag.newBuilder().apply {
                            id = "selbstgemacht"
                            name = "selbstgemacht"
                            articleCount = 2
                        }.build(),
                        Tag.newBuilder().apply {
                            id = "workshop"
                            name = "Workshop"
                            articleCount = 8
                        }.build()
                    )
                )
                viewCount = 107
            }.build()
        )
    }

    private var server: Server? = null
    val isRunning
        get() = server != null

    fun start(port: Int) {
        if (server != null) throw IllegalStateException("Server is already running")

        println("Starting ${NewsService::class.java.simpleName} on port $port")
        server = ServerBuilder.forPort(port)
            .addService(NewsServiceImpl())
            .build()
        server?.start()
        println("${NewsService::class.java.simpleName} started")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("Stopping ${NewsService::class.java.simpleName} due to runtime shutdown")
                this@NewsService.stop()
            }
        })
    }

    fun stop() {
        val server = server ?: throw IllegalStateException("Server is not running")

        println("Stopping ${NewsService::class.java.simpleName}")
        server.shutdown()
        this.server = null
        println("${NewsService::class.java.simpleName} stopped")
    }

    fun blockUntilShutdown() {
        val server = server ?: throw IllegalStateException("Server is not running")
        server.awaitTermination()
    }
}

private class NewsServiceImpl : NewsServiceGrpc.NewsServiceImplBase() {
    // region Article
    override fun listArticles(
        request: ListArticlesRequest?,
        responseObserver: StreamObserver<ListArticlesResponse>?
    ) {
        println("${NewsService::class.java.simpleName}.listArticles called")
        responseObserver ?: throw IllegalArgumentException("responseObserver is null")

        responseObserver.onNext(ListArticlesResponse.newBuilder().apply {
            addAllArticles(ARTICLES)
        }.build())
        responseObserver.onCompleted()
    }

    override fun getArticle(request: GetArticleRequest?, responseObserver: StreamObserver<Article>?) {
        println("${NewsService::class.java.simpleName}.getArticle called")
        responseObserver ?: throw IllegalArgumentException("responseObserver is null")

        if (request == null) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("request is null").asRuntimeException())
            return
        }
        if (request.id.isNullOrEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id must be set").asRuntimeException())
            return
        }

        val article = ARTICLES.firstOrNull { it.id == request.id }
        if (article == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Article with ID ${request.id} not found").asRuntimeException())
            return
        }

        responseObserver.onNext(article)
        responseObserver.onCompleted()
    }
    // endregion

    // region Source
    override fun listSources(request: ListSourcesRequest?, responseObserver: StreamObserver<ListSourcesResponse>?) {
        super.listSources(request, responseObserver)
    }

    override fun getSource(request: GetSourceRequest?, responseObserver: StreamObserver<Source>?) {
        super.getSource(request, responseObserver)
    }
    // endregion

    // region Category
    override fun listCategories(
        request: ListCategoriesRequest?,
        responseObserver: StreamObserver<ListCategoriesResponse>?
    ) {
        super.listCategories(request, responseObserver)
    }

    override fun getCategory(request: GetCategoryRequest?, responseObserver: StreamObserver<Category>?) {
        super.getCategory(request, responseObserver)
    }
    // endregion

    // region Tag
    override fun listTags(request: ListTagsRequest?, responseObserver: StreamObserver<ListTagsResponse>?) {
        super.listTags(request, responseObserver)
    }

    override fun getTag(request: GetTagRequest?, responseObserver: StreamObserver<Tag>?) {
        super.getTag(request, responseObserver)
    }
    // endregion
}
