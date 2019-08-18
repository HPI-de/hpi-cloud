package de.hpi.cloud.food

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.N1qlParams
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.Select
import com.couchbase.client.java.query.dsl.Expression.s
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.query.dsl.functions.DateFunctions.millisToStr
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.food.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

const val PORT_DEFAULT = 50051

fun main(args: Array<String>) {
    val service = Service("food", args.firstOrNull()?.toInt() ?: PORT_DEFAULT) { FoodServiceImpl(it) }
    service.blockUntilShutdown()
}

class FoodServiceImpl(val bucket: Bucket) : FoodServiceGrpc.FoodServiceImplBase() {
    companion object {
        const val DESIGN_RESTAURANT = "restaurant"
        const val DESIGN_MENU_ITEM = "menuItem"
        const val DESIGN_LABEL = "label"
    }

    // region Restaurant
    override fun listRestaurants(
        request: ListRestaurantsRequest?,
        responseObserver: StreamObserver<ListRestaurantsResponse>?
    ) = unary(request, responseObserver, "listRestaurants") { req ->
        val infoBits = bucket.query(ViewQuery.from(DESIGN_RESTAURANT, VIEW_BY_ID)).allRows()
            .map { it.document().content().parseRestaurant() }
        ListRestaurantsResponse.newBuilder()
            .addAllRestaurants(infoBits)
            .build()
    }

    override fun getRestaurant(request: GetRestaurantRequest?, responseObserver: StreamObserver<Restaurant>?) =
        unary(request, responseObserver, "getRestaurant") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Restaurant ID is required")

            bucket.get(DESIGN_RESTAURANT, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseRestaurant()
                ?: Status.NOT_FOUND.throwException("Restaurant with ID ${req.id} not found")
        }

    private fun JsonObject.parseRestaurant(): Restaurant {
        val value = getObject(KEY_VALUE)
        return Restaurant.newBuilder().apply {
            id = getString(KEY_ID)
            title = value.getI18nString("title")
        }.build()
    }
    // endregion

    // region MenuItem
    override fun listMenuItems(
        request: ListMenuItemsRequest?,
        responseObserver: StreamObserver<ListMenuItemsResponse>?
    ) = unary(request, responseObserver, "listMenuItems") { req ->
        val restaurantId = req.restaurantId?.trim()?.takeIf { it.isNotEmpty() }
        val date = if (req.hasDate()) req.date else null

        val menuItems =
            if (restaurantId == null && date == null)
                bucket.query(ViewQuery.from(DESIGN_MENU_ITEM, VIEW_BY_ID)).allRows()
                    .map { it.document().content() }
            else {
                val statement = Select.select("*")
                    .from(bucket.name())
                    .where(
                        and(
                            x(KEY_TYPE).eq(s("menuItem")),
                            restaurantId?.let { n(KEY_VALUE, "restaurantId").eq(s(restaurantId)) },
                            date?.let {
                                millisToStr(
                                    n(KEY_VALUE, "date", "millis"),
                                    "1111-11-11"
                                ).eq(s(it.toQueryString()))
                            }
                        )
                    )
                    .orderBy(*descTimestamp(n(KEY_VALUE, "publishedAt")))
                bucket.query(N1qlQuery.simple(statement, N1qlParams.build().adhoc(false))).allRows()
                    .map { it.value().getObject(bucket.name()) }
            }


        ListMenuItemsResponse.newBuilder()
            .addAllItems(menuItems.map { it.parseMenuItem() })
            .build()
    }

    override fun getMenuItem(request: GetMenuItemRequest?, responseObserver: StreamObserver<MenuItem>?) =
        unary(request, responseObserver, "getMenuItem") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_MENU_ITEM, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseMenuItem()
                ?: Status.NOT_FOUND.throwException("MenuItem with ID ${req.id} not found")
        }

    private fun JsonObject.parseMenuItem(): MenuItem? {
        val value = getObject(KEY_VALUE)
        return MenuItem.newBuilder().apply {
            id = getString(KEY_ID)
            restaurantId = value.getString("restaurantId")
            value.getDate("date")?.let { date = it }
            value.getI18nString("counter")?.let { counter = it }
            value.getObject("prices").let { prices ->
                putAllPrices(prices.names.map { it to prices.getMoney(it) }.toMap())
            }
            title = value.getI18nString("title")
            addAllLabelIds(value.getStringArray("labelIds").filterNotNull())
            value.getObject("substitution")?.also { sub ->
                substitution = MenuItem.Substitution.newBuilder().apply {
                    title = sub.getI18nString("title")
                    addAllLabelIds(sub.getStringArray("labelIds").filterNotNull())
                }.build()
            }
        }.build()
    }
    // endregion

    // region Label
    override fun listLabels(request: ListLabelsRequest?, responseObserver: StreamObserver<ListLabelsResponse>?) =
        unary(request, responseObserver, "listLabels") { req ->
            val infoBits = bucket.query(ViewQuery.from(DESIGN_LABEL, VIEW_BY_ID)).allRows()
                .map { it.document().content().parseLabel() }
            ListLabelsResponse.newBuilder()
                .addAllLabels(infoBits)
                .build()
        }

    override fun getLabel(request: GetLabelRequest?, responseObserver: StreamObserver<Label>?) =
        unary(request, responseObserver, "getLabel") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Label ID is required")

            bucket.get(DESIGN_LABEL, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseLabel()
                ?: Status.NOT_FOUND.throwException("Label with ID ${req.id} not found")
        }

    private fun JsonObject.parseLabel(): Label {
        val value = getObject(KEY_VALUE)
        return Label.newBuilder().apply {
            id = getString(KEY_ID)
            title = value.getI18nString("title")
            value.getString("icon")?.let { icon = it }
        }.build()
    }
    // endregion
}
