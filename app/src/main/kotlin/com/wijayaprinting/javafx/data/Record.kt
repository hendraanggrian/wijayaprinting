package com.wijayaprinting.javafx.data

import com.wijayaprinting.javafx.utils.round
import com.wijayaprinting.mysql.utils.PATTERN_DATETIME
import com.wijayaprinting.mysql.utils.PATTERN_TIME
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import kotfx.bindings.doubleBindingOf
import kotfx.bindings.plus
import kotfx.bindings.stringBindingOf
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Period
import java.lang.Math.abs

data class Record(
        val type: Int,
        val actualEmployee: Employee,
        val start: SimpleObjectProperty<DateTime>,
        val end: SimpleObjectProperty<DateTime>,

        val daily: DoubleProperty = SimpleDoubleProperty(),
        val overtime: DoubleProperty = SimpleDoubleProperty(),

        val dailyIncome: DoubleProperty = SimpleDoubleProperty(),
        val overtimeIncome: DoubleProperty = SimpleDoubleProperty(),

        val total: DoubleProperty = SimpleDoubleProperty()
) {
    val employee: Employee?
        get() = when (type) {
            TYPE_NODE -> actualEmployee
            TYPE_CHILD -> null
            TYPE_TOTAL -> null
            else -> throw UnsupportedOperationException()
        }

    val startString: SimpleStringProperty
        get() = SimpleStringProperty().apply {
            bind(stringBindingOf(start) {
                when (type) {
                    TYPE_NODE -> start.value.toString(PATTERN_TIME)
                    TYPE_CHILD -> start.value.toString(PATTERN_DATETIME)
                    TYPE_TOTAL -> ""
                    else -> throw UnsupportedOperationException()
                }
            })
        }

    val endString: SimpleStringProperty
        get() = SimpleStringProperty().apply {
            bind(stringBindingOf(end) {
                when (type) {
                    TYPE_NODE -> end.value.toString(PATTERN_TIME)
                    TYPE_CHILD -> end.value.toString(PATTERN_DATETIME)
                    TYPE_TOTAL -> ""
                    else -> throw UnsupportedOperationException()
                }
            })
        }

    fun cloneStart(time: LocalTime) = DateTime(start.value.year, start.value.monthOfYear, start.value.dayOfMonth, time.hourOfDay, time.minuteOfHour)

    fun cloneEnd(time: LocalTime) = DateTime(end.value.year, end.value.monthOfYear, end.value.dayOfMonth, time.hourOfDay, time.minuteOfHour)

    init {
        if (type != TYPE_ROOT) {
            dailyIncome.bind(doubleBindingOf(daily, actualEmployee.daily) { (daily.value * actualEmployee.daily.value / Employee.WORKING_HOURS).round })
            overtimeIncome.bind(doubleBindingOf(overtime, actualEmployee.hourlyOvertime) { (actualEmployee.hourlyOvertime.value * overtime.value).round })
            when (type) {
                TYPE_NODE -> {
                    daily.set(Employee.WORKING_HOURS)
                    overtime.set(actualEmployee.recess.value)
                    total.set(0.0)
                }
                TYPE_CHILD -> {
                    val workingHours = (abs(Period(start.value, end.value).toStandardMinutes().minutes) / 60.0) - actualEmployee.recess.value
                    if (workingHours <= Employee.WORKING_HOURS) {
                        daily.set(workingHours.round)
                        overtime.set(0.0)
                    } else {
                        daily.set(Employee.WORKING_HOURS)
                        overtime.set((workingHours - Employee.WORKING_HOURS).round)
                    }
                    total.bind(dailyIncome + overtimeIncome)
                }
                TYPE_TOTAL -> total.bind(dailyIncome + overtimeIncome)
            }
        }
    }

    companion object {
        /** Dummy since [javafx.scene.control.TreeTableView] must have a root item. */
        const val TYPE_ROOT = 0
        /** Parent row displaying employee and its preferences. */
        const val TYPE_NODE = 1
        /** Child row of a node, displaying an actual record data. */
        const val TYPE_CHILD = 2
        /** Last child row of a node, displaying calculated total. */
        const val TYPE_TOTAL = 3

        val ROOT: Record = Record(TYPE_ROOT, Employee(0, ""), SimpleObjectProperty(DateTime(0)), SimpleObjectProperty(DateTime(0)))
    }
}