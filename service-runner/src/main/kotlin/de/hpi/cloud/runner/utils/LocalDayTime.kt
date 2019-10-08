package de.hpi.cloud.runner.utils

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.*

infix fun DayOfWeek.at(time: LocalTime) = LocalDayTime(this, time)
val TemporalField.maximumValue get() = this.range().maximum

class LocalDayTime(
    val dayOfWeek: DayOfWeek,
    val localTime: LocalTime
) { // TODO: implement Temporal
    override fun toString() = "$dayOfWeek $localTime"

    fun nextOrSameAdjuster(): TemporalAdjuster {
        val wantedValue = SecondsOfWeek.getFrom(this)
        return TemporalAdjuster { temporal: Temporal ->
            val currentValue = temporal.getLong(SecondsOfWeek)
            if (wantedValue == currentValue) {
                temporal
            } else {
                val diff = currentValue - wantedValue
                temporal.plus(
                    if (diff >= 0) SecondsOfWeek.SECONDS_IN_WEEK - diff else -diff,
                    ChronoUnit.SECONDS
                )
            }
        }
    }

    object SecondsOfWeek : TemporalField {
        val SECONDS_IN_DAY = (ChronoField.SECOND_OF_DAY.maximumValue + 1)

        override fun isTimeBased() = true
        override fun isDateBased() = true

        override fun getBaseUnit() = ChronoUnit.SECONDS
        override fun getRangeUnit() = ChronoUnit.WEEKS
        override fun range(): ValueRange = ValueRange.of(0, ChronoField.DAY_OF_WEEK.maximumValue * SECONDS_IN_DAY - 1)
        override fun rangeRefinedBy(temporal: TemporalAccessor) = range()
        val SECONDS_IN_WEEK = maximumValue + 1

        override fun isSupportedBy(temporal: TemporalAccessor) =
            temporal.isSupported(ChronoField.SECOND_OF_DAY) && temporal.isSupported(ChronoField.DAY_OF_WEEK)

        private fun getFrom(dayOfWeek: Int, localTime: Int): Long = (
                localTime
                        + SECONDS_IN_DAY
                        * (dayOfWeek - 1)
                )

        fun getFrom(dayOfWeek: DayOfWeek, localTime: LocalTime) = getFrom(dayOfWeek.value, localTime.toSecondOfDay())
        fun getFrom(localDayTime: LocalDayTime) = getFrom(localDayTime.dayOfWeek, localDayTime.localTime)
        override fun getFrom(temporal: TemporalAccessor) =
            getFrom(temporal[ChronoField.DAY_OF_WEEK], temporal[ChronoField.SECOND_OF_DAY])

        @Suppress("UNCHECKED_CAST")
        override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R = (
                temporal
                    .with(ChronoField.DAY_OF_WEEK, 1 + newValue / SECONDS_IN_DAY)
                    .with(ChronoField.SECOND_OF_DAY, newValue % SECONDS_IN_DAY)
                ) as R
    }
}
