package com.hendraanggrian.openpss.ui.wage.record

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.ui.wage.IntervalWrapper
import com.hendraanggrian.openpss.util.PATTERN_DATETIME
import com.hendraanggrian.openpss.util.START_OF_TIME
import com.hendraanggrian.openpss.i18n.Resourced
import com.hendraanggrian.openpss.ui.wage.Attendee
import com.hendraanggrian.openpss.util.round
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import ktfx.beans.binding.doubleBindingOf
import ktfx.beans.binding.plus
import ktfx.beans.binding.stringBindingOf
import ktfx.beans.property.toProperty
import ktfx.beans.value.getValue
import ktfx.beans.value.setValue
import org.joda.time.DateTime
import org.joda.time.LocalTime
import kotlin.math.absoluteValue

class Record(
    resourced: Resourced,

    val index: Int,
    val attendee: Attendee,

    val startProperty: ObjectProperty<DateTime>,
    val endProperty: ObjectProperty<DateTime>,

    val dailyDisabledProperty: BooleanProperty = SimpleBooleanProperty(),

    val dailyProperty: DoubleProperty = SimpleDoubleProperty(),
    val overtimeProperty: DoubleProperty = SimpleDoubleProperty(),

    val dailyIncomeProperty: DoubleProperty = SimpleDoubleProperty(),
    val overtimeIncomeProperty: DoubleProperty = SimpleDoubleProperty(),

    val totalProperty: DoubleProperty = SimpleDoubleProperty()
) : Resourced by resourced {

    var start: DateTime by startProperty
    var end: DateTime by endProperty
    var isDailyDisabled: Boolean by dailyDisabledProperty
    var daily: Double by dailyProperty
    var overtime: Double by overtimeProperty
    var dailyIncome: Double by dailyIncomeProperty
    var overtimeIncome: Double by overtimeIncomeProperty
    var total: Double by totalProperty

    companion object {
        const val WORKING_HOURS = 8

        /** Parent row displaying name and its preferences. */
        const val INDEX_NODE = -2
        /** Last child row of a node, displaying calculated total. */
        const val INDEX_TOTAL = -1

        /** Dummy for invisible [javafx.scene.control.TreeTableView] root. */
        fun getDummy(resourced: Resourced) = Record(resourced, Int.MIN_VALUE, Attendee.DUMMY,
            START_OF_TIME.toProperty(), START_OF_TIME.toProperty())
    }

    init {
        dailyDisabledProperty.set(false)
        if (isNode()) {
            dailyProperty.set(0.0)
            overtimeProperty.set(0.0)
            dailyIncomeProperty.set(attendee.daily.toDouble())
            overtimeIncomeProperty.set(attendee.hourlyOvertime.toDouble())
            totalProperty.set(0.0)
        }
        if (isChild()) {
            dailyProperty.bind(doubleBindingOf(startProperty, endProperty, dailyDisabledProperty) {
                if (isDailyDisabled) 0.0 else {
                    val hours = workingHours
                    when {
                        hours <= WORKING_HOURS -> hours.round()
                        else -> WORKING_HOURS.toDouble()
                    }
                }
            })
            overtimeProperty.bind(doubleBindingOf(startProperty, endProperty) {
                val hours = workingHours
                val overtime = (hours - WORKING_HOURS).round()
                when {
                    hours <= WORKING_HOURS -> 0.0
                    else -> overtime
                }
            })
        }
        if (isChild() || isTotal()) {
            dailyIncomeProperty.bind(doubleBindingOf(dailyProperty) {
                (daily * attendee.daily / WORKING_HOURS).round()
            })
            overtimeIncomeProperty.bind(doubleBindingOf(overtimeProperty) {
                (overtime * attendee.hourlyOvertime).round()
            })
            totalProperty.bind(dailyIncomeProperty + overtimeIncomeProperty)
        }
    }

    fun isNode(): Boolean = index == INDEX_NODE

    fun isTotal(): Boolean = index == INDEX_TOTAL

    fun isChild(): Boolean = index >= 0

    val displayedName: String
        get() = when {
            isNode() -> attendee.toString()
            isChild() -> attendee.recesses.getOrNull(index)?.toString().orEmpty()
            isTotal() -> ""
            else -> throw UnsupportedOperationException()
        }

    val displayedStart: StringProperty
        get() = SimpleStringProperty().apply {
            bind(stringBindingOf(startProperty, dailyDisabledProperty) {
                when {
                    isNode() -> attendee.role.orEmpty()
                    isChild() -> start.toString(PATTERN_DATETIME).let { if (isDailyDisabled) "($it)" else it }
                    isTotal() -> ""
                    else -> throw UnsupportedOperationException()
                }
            })
        }

    val displayedEnd: StringProperty
        get() = SimpleStringProperty().apply {
            bind(stringBindingOf(endProperty, dailyDisabledProperty) {
                when {
                    isNode() -> "${attendee.attendances.size / 2} ${getString(R.string.day)}"
                    isChild() -> end.toString(PATTERN_DATETIME).let { if (isDailyDisabled) "($it)" else it }
                    isTotal() -> getString(R.string.total)
                    else -> throw UnsupportedOperationException()
                }
            })
        }

    fun cloneStart(time: LocalTime): DateTime =
        DateTime(start.year, start.monthOfYear, start.dayOfMonth, time.hourOfDay, time.minuteOfHour)

    fun cloneEnd(time: LocalTime): DateTime =
        DateTime(end.year, end.monthOfYear, end.dayOfMonth, time.hourOfDay, time.minuteOfHour)

    private val workingHours: Double
        get() {
            val interval = IntervalWrapper.of(start, end)
            var minutes = interval.minutes
            attendee.recesses
                .map { it.getInterval(start) }
                .forEach {
                    minutes -= interval.overlap(it)?.toDuration()?.toStandardMinutes()?.minutes?.absoluteValue ?: 0
                }
            return minutes / 60.0
        }
}