package de.hpi.cloud.course

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.dsl.Expression.s
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.query.dsl.Sort.asc
import com.couchbase.client.java.query.dsl.Sort.desc
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.buildWith
import de.hpi.cloud.common.utils.grpc.buildWithDocument
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.course.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun main(args: Array<String>) {
    val service = Service("course", args.firstOrNull()?.toInt()) { CourseServiceImpl(it) }
    service.blockUntilShutdown()
}

class CourseServiceImpl(private val bucket: Bucket) : CourseServiceGrpc.CourseServiceImplBase() {
    companion object {
        const val DESIGN_COURSE_SERIES = "courseSeries"
        const val DESIGN_SEMESTER = "semester"
        const val DESIGN_COURSE = "course"
        const val DESIGN_COURSE_DETAIL = "courseDetail"
    }

    // region CourseSeries
    override fun listCourseSeries(
        request: ListCourseSeriesRequest?,
        responseObserver: StreamObserver<ListCourseSeriesResponse>?
    ) = unary(request, responseObserver, "listCourseSeries") { req ->
        val (courseSeries, newToken) = ViewQuery.from(DESIGN_COURSE_SERIES, VIEW_BY_ID)
            .paginate(bucket, req.pageSize, req.pageToken) { it.parseCourseSeries() }

        ListCourseSeriesResponse.newBuilder().buildWith {
            addAllCourseSeries(courseSeries)
            nextPageToken = newToken
        }
    }

    override fun getCourseSeries(request: GetCourseSeriesRequest?, responseObserver: StreamObserver<CourseSeries>?) =
        unary(request, responseObserver, "getCourseSeries") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("CourseSeries ID is required")

            bucket.get(DESIGN_COURSE_SERIES, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseCourseSeries()
                ?: Status.NOT_FOUND.throwException("CourseSeries with ID ${req.id} not found")
        }

    private fun JsonObject.parseCourseSeries() =
        CourseSeries.newBuilder().buildWithDocument<CourseSeries, CourseSeries.Builder>(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title")
            shortTitle = it.getI18nString("shortTitle")
            abbreviation = it.getI18nString("abbreviation")
            ects = it.getInt("ects")
            hoursPerWeek = it.getInt("hoursPerWeek")
            mandatory = it.getBoolean("mandatory")
            language = it.getString("language")
            addAllTypes(it.getStringArray("types").mapNotNull { t -> t?.parseCourseSeriesType() })
        }

    private fun String.parseCourseSeriesType(): CourseSeries.Type {
        return CourseSeries.Type.values().first { it.name.equals(this, true) }
    }
    // endregion

    // region Semester
    override fun listSemesters(
        request: ListSemestersRequest?,
        responseObserver: StreamObserver<ListSemestersResponse>?
    ) = unary(request, responseObserver, "listSemesters") { req ->
        val (semesters, newToken) = ViewQuery.from(DESIGN_SEMESTER, VIEW_BY_ID)
            .paginate(bucket, req.pageSize, req.pageToken) { it.parseSemester() }

        ListSemestersResponse.newBuilder().buildWith {
            addAllSemesters(semesters)
            nextPageToken = newToken
        }
    }

    override fun getSemester(request: GetSemesterRequest?, responseObserver: StreamObserver<Semester>?) =
        unary(request, responseObserver, "getSemester") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_SEMESTER, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseSemester()
                ?: Status.NOT_FOUND.throwException("Semester with ID ${req.id} not found")
        }

    private fun JsonObject.parseSemester() = Semester.newBuilder().buildWithDocument<Semester, Semester.Builder>(this) {
        id = getString(KEY_ID)
        term = it.getString("term").parseSemesterTerm()
        year = it.getInt("year")
    }

    private fun String.parseSemesterTerm(): Semester.Term {
        return Semester.Term.values().first { it.name.equals(this, true) }
    }
    // endregion

    // region Course
    override fun listCourses(request: ListCoursesRequest?, responseObserver: StreamObserver<ListCoursesResponse>?) =
        unary(request, responseObserver, "listCourses") { req ->
            val courseSeriesId = req.courseSeriesId?.trim()?.takeIf { it.isNotEmpty() }
            val semesterId = req.semesterId?.trim()?.takeIf { it.isNotEmpty() }

            val (courses, newToken) = paginate(bucket, {
                where(
                    and(
                        x(KEY_TYPE).eq(s("course")),
                        v("courseSeriesId").eq(s(courseSeriesId)).takeIf { courseSeriesId != null },
                        v("semesterId").eq(s(semesterId)).takeIf { semesterId != null })
                )
                    .orderBy(
                        asc(v("courseSeriesId")),
                        desc(v("semesterId"))
                    )
            }, req.pageSize, req.pageToken) { it.parseCourse() }

            ListCoursesResponse.newBuilder().buildWith {
                addAllCourses(courses)
                nextPageToken = newToken
            }
        }

    override fun getCourse(request: GetCourseRequest?, responseObserver: StreamObserver<Course>?) =
        unary(request, responseObserver, "getCourse") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Course ID is required")

            bucket.get(DESIGN_COURSE, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseCourse()
                ?: Status.NOT_FOUND.throwException("Course with ID ${req.id} not found")
        }

    private fun JsonObject.parseCourse() = Course.newBuilder().buildWithDocument<Course, Course.Builder>(this) {
        id = getString(KEY_ID)
        courseSeriesId = it.getString("courseSeriesId")
        semesterId = it.getString("semesterId")
        addLecturers(it.getString("lecturer"))
        addAllAssistants(it.getStringArray("assistants").filterNotNull())
        it.getString("website")?.let { w -> website = w }
    }
    // endregion

    // region CourseDetail
    override fun getCourseDetail(request: GetCourseDetailRequest?, responseObserver: StreamObserver<CourseDetail>?) =
        unary(request, responseObserver, "getCourseDetail") { req ->
            if (req.courseId.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument course_id is required")

            bucket.get(DESIGN_COURSE_DETAIL, VIEW_BY_ID, req.courseId)
                ?.document()?.content()?.parseCourseDetail()
                ?: Status.NOT_FOUND.throwException("CourseDetail with ID ${req.courseId} not found")
        }

    private fun JsonObject.parseCourseDetail() =
        CourseDetail.newBuilder().buildWithDocument<CourseDetail, CourseDetail.Builder>(this) {
            courseId = getString(KEY_ID)
            it.getString("teletask")?.let { t -> teletask = t }
            putAllPrograms(it.getObject("programs").toMap()
                .mapValues { p ->
                    @Suppress("UNCHECKED_CAST")
                    CourseDetail.ProgramList.newBuilder().addAllPrograms(p.value as List<String>).build()
                })
            it.getI18nString("description").let { d -> description = d }
            it.getI18nString("requirements").let { r -> requirements = r }
            it.getI18nString("learning").let { l -> learning = l }
            it.getI18nString("examination").let { e -> examination = e }
            it.getI18nString("dates").let { d -> dates = d }
            it.getI18nString("literature").let { l -> literature = l }
        }
    // endregion
}
