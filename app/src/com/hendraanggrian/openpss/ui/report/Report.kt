package com.hendraanggrian.openpss.ui.report

import com.hendraanggrian.openpss.db.schemas.Payment
import com.hendraanggrian.openpss.db.schemas.Payment.Method.CASH
import com.hendraanggrian.openpss.db.schemas.Payment.Method.TRANSFER
import javafx.collections.ObservableList
import ktfx.collections.toObservableList
import org.joda.time.LocalDate

data class Report(
    val date: LocalDate,
    val cash: Double,
    val transfer: Double
) {
    val total: Double get() = cash + transfer

    companion object {
        fun listAll(payments: Iterable<Payment>): ObservableList<Report> = payments
            .groupBy { it.dateTime.toLocalDate() }
            .flatMap { (dateTime, payments) ->
                listOf(Report(dateTime,
                    payments.filter { it.method == CASH }.sumByDouble { it.value },
                    payments.filter { it.method == TRANSFER }.sumByDouble { it.value }))
            }
            .toObservableList()
    }
}