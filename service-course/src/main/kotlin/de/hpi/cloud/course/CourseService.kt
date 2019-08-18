package de.hpi.cloud.course

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.N1qlParams
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.Select
import com.couchbase.client.java.query.dsl.Expression.s
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.course.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

const val PORT_DEFAULT = 50050

fun main(args: Array<String>) {
    val service = Service("course", args.firstOrNull()?.toInt() ?: PORT_DEFAULT) { CourseServiceImpl(it) }
    service.blockUntilShutdown()
}

class CourseServiceImpl(val bucket: Bucket) : CourseServiceGrpc.CourseServiceImplBase() {
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
        val courseSeries = bucket.query(ViewQuery.from(DESIGN_COURSE_SERIES, VIEW_BY_ID)).allRows()
            .map { it.document().content().parseCourseSeries() }
        ListCourseSeriesResponse.newBuilder()
            .addAllCourseSeries(courseSeries)
            .build()
    }

    override fun getCourseSeries(request: GetCourseSeriesRequest?, responseObserver: StreamObserver<CourseSeries>?) =
        unary(request, responseObserver, "getCourseSeries") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("CourseSeries ID is required")

            bucket.get(DESIGN_COURSE_SERIES, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseCourseSeries()
                ?: Status.NOT_FOUND.throwException("CourseSeries with ID ${req.id} not found")
        }

    private fun JsonObject.parseCourseSeries(): CourseSeries {
        val value = getObject(KEY_VALUE)
        return CourseSeries.newBuilder()
            .setId(getString(KEY_ID))
            .setTitle(value.getI18nString("title"))
            .setShortTitle(value.getI18nString("shortTitle"))
            .setAbbreviation(value.getI18nString("abbreviation"))
            .setEcts(value.getInt("ects"))
            .setHoursPerWeek(value.getInt("hoursPerWeek"))
            .setMandatory(value.getBoolean("mandatory"))
            .setLanguage(value.getString("language"))
            .addAllTypes(value.getStringArray("types").mapNotNull { it?.parseCourseSeriesType() })
            .build()
    }

    private fun String.parseCourseSeriesType(): CourseSeries.Type {
        return CourseSeries.Type.values().first { it.name.equals(this, true) }
    }
    // endregion

    // region Semester
    override fun listSemesters(
        request: ListSemestersRequest?,
        responseObserver: StreamObserver<ListSemestersResponse>?
    ) =
        unary(request, responseObserver, "listSemesters") { req ->
            val semesters = bucket.query(ViewQuery.from(DESIGN_SEMESTER, VIEW_BY_ID)).allRows()
                .map { it.document().content().parseSemester() }
            ListSemestersResponse.newBuilder()
                .addAllSemesters(semesters)
                .build()
        }

    override fun getSemester(
        request: GetSemesterRequest?,
        responseObserver: StreamObserver<Semester>?
    ) =
        unary(request, responseObserver, "getSemester") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_SEMESTER, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseSemester()
                ?: Status.NOT_FOUND.throwException("Semester with ID ${req.id} not found")
        }

    private fun JsonObject.parseSemester(): Semester? {
        val value = getObject(KEY_VALUE)
        return Semester.newBuilder()
            .setId(getString(KEY_ID))
            .setTerm(value.getString("term").parseSemesterTerm())
            .setYear(value.getInt("year"))
            .build()
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

            val courses =
                if (courseSeriesId == null && semesterId == null)
                    bucket.query(ViewQuery.from(DESIGN_COURSE, VIEW_BY_ID)).allRows()
                        .map { it.document().content() }
                else {
                    val filters = listOfNotNull(
                        x(KEY_TYPE).eq(s("course")),
                        n(KEY_VALUE, "courseSeriesId").eq(s(courseSeriesId)).takeIf { courseSeriesId != null },
                        n(KEY_VALUE, "semesterId").eq(s(semesterId)).takeIf { semesterId != null }
                    )
                    val statement = Select.select("*")
                        .from(bucket.name())
                        .where(and(filters))
                    bucket.query(N1qlQuery.simple(statement, N1qlParams.build().adhoc(false))).allRows()
                        .map { it.value().getObject(bucket.name()) }
                }

            ListCoursesResponse.newBuilder()
                .addAllCourses(courses.map { it.parseCourse() })
                .build()
        }

    override fun getCourse(request: GetCourseRequest?, responseObserver: StreamObserver<Course>?) =
        unary(request, responseObserver, "getCourse") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Course ID is required")

            bucket.get(DESIGN_COURSE, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseCourse()
                ?: Status.NOT_FOUND.throwException("Course with ID ${req.id} not found")
        }

    private fun JsonObject.parseCourse(): Course {
        val value = getObject(KEY_VALUE)
        return Course.newBuilder()
            .setId(getString(KEY_ID))
            .setCourseSeriesId(value.getString("courseSeriesId"))
            .setSemesterId(value.getString("semesterId"))
            .setLecturer(value.getString("lecturer"))
            .addAllAssistants(value.getStringArray("assistants"))
            .setWebsite(value.getString("website"))
            .build()
    }
    // endregion

    // region CourseDetail
    override fun listCourseDetails(
        request: ListCourseDetailsRequest?,
        responseObserver: StreamObserver<ListCourseDetailsResponse>?
    ) =
        unary(request, responseObserver, "listCourseDetails") { req ->
            val courseDetails = bucket.query(ViewQuery.from(DESIGN_COURSE_DETAIL, VIEW_BY_ID)).allRows()
                .map { it.document().content().parseCourseDetail() }
            ListCourseDetailsResponse.newBuilder()
                .addAllDetails(courseDetails)
                .build()
        }

    override fun getCourseDetail(request: GetCourseDetailRequest?, responseObserver: StreamObserver<CourseDetail>?) =
        unary(request, responseObserver, "getCourseDetail") { req ->
            if (req.courseId.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument course_id is required")

            bucket.get(DESIGN_COURSE_DETAIL, VIEW_BY_ID, req.courseId)
                ?.document()?.content()?.parseCourseDetail()
                ?: Status.NOT_FOUND.throwException("CourseDetail with ID ${req.courseId} not found")
        }

    private fun JsonObject.parseCourseDetail(): CourseDetail? {
        val value = getObject(KEY_VALUE)
        return CourseDetail.newBuilder()
            .setCourseId(getString(KEY_ID))
            .setTeletask(value.getString("teletask"))
            .putAllPrograms(value.getObject("programs").toMap()
                .mapValues {
                    @Suppress("UNCHECKED_CAST")
                    CourseDetail.ProgramList.newBuilder().addAllPrograms(it.value as List<String>).build()
                })
            .setDescription(value.getI18nString("description"))
            .setRequirements(value.getI18nString("requirements"))
            .setLearning(value.getI18nString("learning"))
            .setExamination(value.getI18nString("examination"))
            .setDates(value.getI18nString("dates"))
            .setLiterature(value.getI18nString("literature"))
            .build()
    }
    // endregion
}
