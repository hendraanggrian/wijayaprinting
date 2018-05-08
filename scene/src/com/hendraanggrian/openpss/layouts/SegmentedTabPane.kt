package com.hendraanggrian.openpss.layouts

import com.hendraanggrian.openpss.scene.R
import com.hendraanggrian.openpss.util.adaptableText
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Pane
import ktfx.application.later
import ktfx.coroutines.listener
import ktfx.scene.layout.paddingTop
import org.controlsfx.control.SegmentedButton

class SegmentedTabPane : TabPane() {

    var header: SegmentedButton = SegmentedButton()
    var isAdaptableText: Boolean = false

    init {
        stylesheets += javaClass.getResource(R.style.hiddentabpane).toExternalForm()
        later { paddingTop = -(lookup(".tab-header-area") as Pane).height }
        header.toggleGroup.run {
            selectedToggleProperty().listener { _, oldValue, value ->
                when (value) {
                    null -> selectToggle(oldValue)
                    else -> selectionModel.select(toggles.indexOf(value))
                }
            }
        }
        populate(tabs)
        tabs.listener<Tab> { change ->
            change.next()
            when {
                change.wasAdded() -> {
                    populate(change.addedSubList)
                    if (change.from == 0) header.buttons.first().isSelected = true
                }
                else -> header.buttons -= header.buttons.filter { it.text in change.addedSubList.map { it.text } }
            }
        }
    }

    private fun populate(tabs: Collection<Tab>) {
        header.buttons += tabs.map {
            ToggleButton(it.text, it.graphic).apply {
                if (isAdaptableText) adaptableText()
            }
        }
    }
}