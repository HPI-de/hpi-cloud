package de.hpi.cloud.food

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.dsl.Expression.s
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.query.dsl.Sort.asc
import com.couchbase.client.java.query.dsl.functions.DateFunctions.millisToStr
import com.couchbase.client.java.view.ViewQuery
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.getI18nString
import de.hpi.cloud.common.utils.grpc.buildWith
import de.hpi.cloud.common.utils.grpc.buildWithDocument
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.common.utils.protobuf.getDateUsingMillis
import de.hpi.cloud.common.utils.protobuf.getMoney
import de.hpi.cloud.common.utils.protobuf.toIsoString
import de.hpi.cloud.food.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun main(args: Array<String>) {
    val service = Service("food", args.firstOrNull()?.toInt()) { FoodServiceImpl(it) }
    service.blockUntilShutdown()
}

class FoodServiceImpl(private val bucket: Bucket) : FoodServiceGrpc.FoodServiceImplBase() {
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
        val (restaurants, newToken) = ViewQuery.from(DESIGN_RESTAURANT, VIEW_BY_ID)
            .paginate(bucket, req.pageSize, req.pageToken) { it.parseRestaurant(req) }

        ListRestaurantsResponse.newBuilder().buildWith {
            addAllRestaurants(restaurants)
            nextPageToken = newToken
        }
    }

    override fun getRestaurant(request: GetRestaurantRequest?, responseObserver: StreamObserver<Restaurant>?) =
        unary(request, responseObserver, "getRestaurant") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Restaurant ID is required")

            bucket.get(DESIGN_RESTAURANT, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseRestaurant(req)
                ?: Status.NOT_FOUND.throwException("Restaurant with ID ${req.id} not found")
        }

    private fun JsonObject.parseRestaurant(request: GeneratedMessageV3) =
        Restaurant.newBuilder().buildWithDocument<Restaurant, Restaurant.Builder>(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
        }
    // endregion

    // region MenuItem
    override fun listMenuItems(
        request: ListMenuItemsRequest?,
        responseObserver: StreamObserver<ListMenuItemsResponse>?
    ) = unary(request, responseObserver, "listMenuItems") { req ->
        val restaurantId = req.restaurantId?.trim()?.takeIf { it.isNotEmpty() }
        val date = if (req.hasDate()) req.date else null

        val (menuItems, newToken) = paginate(bucket, {
            where(
                and(
                    x(KEY_TYPE).eq(s("menuItem")),
                    restaurantId?.let { v("restaurantId").eq(s(restaurantId)) },
                    date?.let {
                        millisToStr(v("date", "millis"), "1111-11-11")
                            .eq(s(it.toIsoString()))
                    }
                )
            )
                .orderBy(
                    *descTimestamp(v("date")),
                    asc(v("offerName"))
                )
        }, req.pageSize, req.pageToken) { it.parseMenuItem(req) }

        ListMenuItemsResponse.newBuilder().buildWith {
            addAllItems(menuItems)
            nextPageToken = newToken
        }
    }

    override fun getMenuItem(request: GetMenuItemRequest?, responseObserver: StreamObserver<MenuItem>?) =
        unary(request, responseObserver, "getMenuItem") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_MENU_ITEM, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseMenuItem(req)
                ?: Status.NOT_FOUND.throwException("MenuItem with ID ${req.id} not found")
        }

    private fun JsonObject.parseMenuItem(request: GeneratedMessageV3) =
        MenuItem.newBuilder().buildWithDocument<MenuItem, MenuItem.Builder>(this) {
            id = getString(KEY_ID)
            restaurantId = it.getString("restaurantId")
            it.getDateUsingMillis("date")?.let { d -> date = d }
            it.getI18nString("counter", request)?.let { c -> counter = c }
            it.getObject("prices").let { prices ->
                putAllPrices(prices.names.map { p -> p to prices.getMoney(p) }.toMap())
            }
            title = it.getI18nString("title", request)
            addAllLabelIds(it.getStringArray("labelIds").filterNotNull())
        }
    // endregion

    // region Label
    override fun listLabels(request: ListLabelsRequest?, responseObserver: StreamObserver<ListLabelsResponse>?) =
        unary(request, responseObserver, "listLabels") { req ->
            val (labels, newToken) = ViewQuery.from(DESIGN_LABEL, VIEW_BY_ID)
                .paginate(bucket, req.pageSize, req.pageToken) { it.parseLabel(req) }

            ListLabelsResponse.newBuilder().buildWith {
                addAllLabels(labels)
                nextPageToken = newToken
            }
        }

    override fun getLabel(request: GetLabelRequest?, responseObserver: StreamObserver<Label>?) =
        unary(request, responseObserver, "getLabel") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Label ID is required")

            bucket.get(DESIGN_LABEL, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseLabel(req)
                ?: Status.NOT_FOUND.throwException("Label with ID ${req.id} not found")
        }

    private fun JsonObject.parseLabel(request: GeneratedMessageV3) =
        Label.newBuilder().buildWithDocument<Label, Label.Builder>(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
            it.getString("icon")?.let { i -> icon = i }
        }
    // endregion
}
