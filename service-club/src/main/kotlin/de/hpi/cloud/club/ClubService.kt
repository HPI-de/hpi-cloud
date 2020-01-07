package de.hpi.cloud.club

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.club.entities.Club
import de.hpi.cloud.club.entities.toProto
import de.hpi.cloud.club.v1test.ClubServiceGrpc
import de.hpi.cloud.club.v1test.GetClubRequest
import de.hpi.cloud.club.v1test.ListClubsRequest
import de.hpi.cloud.club.v1test.ListClubsResponse
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.couchbase.VIEW_BY_ID
import de.hpi.cloud.common.couchbase.get
import de.hpi.cloud.common.couchbase.paginate
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.grpc.checkArgRequired
import de.hpi.cloud.common.grpc.notFound
import de.hpi.cloud.common.grpc.unary
import de.hpi.cloud.common.protobuf.build
import io.grpc.stub.StreamObserver
import de.hpi.cloud.club.v1test.Club as ProtoClub

fun main(args: Array<String>) {
    val service = Service("club", args.firstOrNull()?.toInt()) { ClubServiceImpl(it) }
    service.blockUntilShutdown()
}

class ClubServiceImpl(private val bucket: Bucket) : ClubServiceGrpc.ClubServiceImplBase() {
    // region Club
    override fun listClubs(request: ListClubsRequest?, responseObserver: StreamObserver<ListClubsResponse>?) =
        unary(request, responseObserver, "listClubs") { req ->
            val (clubs, newToken) = ViewQuery.from(Club.type, VIEW_BY_ID)
                .paginate<Club>(bucket, req.pageSize, req.pageToken)

            ListClubsResponse.newBuilder().build {
                addAllClubs(clubs.map { it.toProto(this@unary) })
                nextPageToken = newToken
            }
        }

    override fun getClub(request: GetClubRequest?, responseObserver: StreamObserver<ProtoClub>?) =
        unary(request, responseObserver, "getClub") { req ->
            checkArgRequired(req.id, "id")

            bucket.get<Club>(Id(req.id))?.toProto(this)
                ?: notFound<ProtoClub>(req.id)
        }
    // endregion
}
