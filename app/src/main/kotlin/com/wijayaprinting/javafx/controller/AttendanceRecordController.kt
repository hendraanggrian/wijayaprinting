package com.wijayaprinting.javafx.controller

import com.wijayaprinting.javafx.data.Employee
import com.wijayaprinting.javafx.data.Record
import com.wijayaprinting.javafx.getExtra
import javafx.application.Platform.runLater
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.print.PrinterJob
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import kotfx.bindings.plus
import kotfx.bindings.stringBindingOf
import kotfx.dialogs.warningAlert
import kotfx.stringConverterOf

class AttendanceRecordController {

    @FXML lateinit var grandTotalFlow: TextFlow
    @FXML lateinit var treeTableView: TreeTableView<Record>
    @FXML lateinit var treeTableColumnEmployee: TreeTableColumn<Record, Employee>
    @FXML lateinit var treeTableColumnStart: TreeTableColumn<Record, String>
    @FXML lateinit var treeTableColumnEnd: TreeTableColumn<Record, String>
    @FXML lateinit var treeTableColumnDaily: TreeTableColumn<Record, Double>
    @FXML lateinit var treeTableColumnDailyIncome: TreeTableColumn<Record, Double>
    @FXML lateinit var treeTableColumnOvertime: TreeTableColumn<Record, Double>
    @FXML lateinit var treeTableColumnOvertimeIncome: TreeTableColumn<Record, Double>
    @FXML lateinit var treeTableColumnTotal: TreeTableColumn<Record, Double>

    @FXML
    @Suppress("UNCHECKED_CAST")
    fun initialize() = runLater {
        val totals = mutableListOf<DoubleProperty>()

        treeTableView.root = TreeItem(Record.ROOT) // dummy for invisible root
        treeTableView.isShowRoot = false
        getExtra<Set<Employee>>().forEach { employee ->
            val node = employee.toNodeRecord()
            val childs = employee.toChildRecords()
            val total = employee.toTotalRecords(childs)
            totals.add(total.total)
            treeTableView.root.children.add(TreeItem(node).apply {
                isExpanded = true
                children.addAll(*childs.map { TreeItem(it) }.toTypedArray(), TreeItem(total))
            })
        }

        treeTableColumnEmployee.setCellValueFactory { ReadOnlyObjectWrapper(it.value.value.employee) }
        treeTableColumnStart.setCellValueFactory { ReadOnlyObjectWrapper(it.value.value.start) }
        treeTableColumnEnd.setCellValueFactory { ReadOnlyObjectWrapper(it.value.value.end) }
        treeTableColumnDaily.setCellValueFactory { it.value.value.daily as ObservableValue<Double> }
        treeTableColumnDaily.cellFactory = TextFieldTreeTableCell.forTreeTableColumn<Record, Double>(stringConverterOf { it.toDouble() })
        treeTableColumnDaily.setOnEditStart {
            if (it.rowValue.value.type != Record.TYPE_CHILD) {
                it.consume()
                warningAlert("Can't edit!").show()
            }
        }
        treeTableColumnDaily.setOnEditCommit { it.rowValue.value.daily.set(it.newValue) }
        treeTableColumnDailyIncome.setCellValueFactory { it.value.value.dailyIncome as ObservableValue<Double> }
        treeTableColumnOvertime.setCellValueFactory { it.value.value.overtime as ObservableValue<Double> }
        treeTableColumnOvertime.cellFactory = TextFieldTreeTableCell.forTreeTableColumn<Record, Double>(stringConverterOf { it.toDouble() })
        treeTableColumnOvertime.setOnEditStart {
            if (it.rowValue.value.type != Record.TYPE_CHILD) {
                it.consume()
                warningAlert("Can't edit!").show()
            }
        }
        treeTableColumnOvertime.setOnEditCommit { it.rowValue.value.overtime.set(it.newValue) }
        treeTableColumnOvertimeIncome.setCellValueFactory { it.value.value.overtimeIncome as ObservableValue<Double> }
        treeTableColumnTotal.setCellValueFactory { it.value.value.total as ObservableValue<Double> }

        val first = totals.firstOrNull() ?: return@runLater
        var binding = first + SimpleDoubleProperty(0.0)
        (1 until totals.size).forEach { binding += totals[it] }
        grandTotalFlow.children.add(Text().apply { textProperty().bind(stringBindingOf(binding) { binding.get().toString() }) })
    }

    @FXML
    fun buttonCancelOnAction() = (treeTableView.scene.window as Stage).close()

    @FXML
    fun buttonPrintOnAction() {
        val printerJob = PrinterJob.createPrinterJob()
        if (printerJob.showPrintDialog(treeTableView.scene.window) && printerJob.printPage(treeTableView)) {
            printerJob.endJob()
        }
    }
}