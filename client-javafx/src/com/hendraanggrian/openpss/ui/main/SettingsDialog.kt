package com.hendraanggrian.openpss.ui.main

import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.content.Language
import com.hendraanggrian.openpss.control.Space
import com.hendraanggrian.openpss.data.GlobalSetting.Companion.KEY_INVOICE_HEADERS
import com.hendraanggrian.openpss.data.GlobalSetting.Companion.KEY_LANGUAGE
import com.hendraanggrian.openpss.io.ReaderFile
import com.hendraanggrian.openpss.io.SettingsFile
import com.hendraanggrian.openpss.ui.FxComponent
import com.hendraanggrian.openpss.ui.OpenPSSDialog
import com.hendraanggrian.openpss.ui.wage.WageReader
import com.jfoenix.controls.JFXButton
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import ktfx.bindings.and
import ktfx.boolean
import ktfx.collections.toObservableList
import ktfx.controls.gap
import ktfx.coroutines.listener
import ktfx.jfoenix.jfxButton
import ktfx.jfoenix.jfxComboBox
import ktfx.layouts.LayoutDsl
import ktfx.layouts.NodeInvokable
import ktfx.layouts._HBox
import ktfx.layouts._VBox
import ktfx.layouts.borderPane
import ktfx.layouts.gridPane
import ktfx.layouts.hbox
import ktfx.layouts.label
import ktfx.layouts.textArea
import ktfx.layouts.vbox
import ktfx.listeners.converter
import kotlin.coroutines.CoroutineContext

class SettingsDialog(component: FxComponent) : OpenPSSDialog(component, R.string.settings) {

    private var isLocalChanged = boolean()
    private var isGlobalChanged = boolean()

    private lateinit var invoiceHeadersArea: TextArea
    private lateinit var wageReaderChoice: ComboBox<WageReader>
    private lateinit var languageBox: ComboBox<Language>

    init {
        borderPane {
            left = ktfx.layouts.vbox {
                spacing = getDouble(R.value.padding_large)
                group(R.string.wage) {
                    item {
                        label(getString(R.string.reader))
                        wageReaderChoice = jfxComboBox(WageReader.listAll()) {
                            value = WageReader.of(ReaderFile.WAGE_READER)
                            valueProperty().listener { _, _, value ->
                                isLocalChanged.set(true)
                                ReaderFile.WAGE_READER = (value as WageReader).name
                            }
                        }
                    }
                }
            }
            Space(getDouble(R.value.padding_large))()
            right = this@SettingsDialog.group(R.string.global_settings) {
                GlobalScope.launch(Dispatchers.JavaFx) {
                    isDisable = !api.isAdmin(login)
                }
                gridPane {
                    gap = getDouble(R.value.padding_medium)
                    label(getString(R.string.server_language)) row 0 col 0
                    languageBox = jfxComboBox(Language.values().toObservableList()) {
                        converter { toString { it!!.toString(true) } }
                        GlobalScope.launch(Dispatchers.JavaFx) {
                            selectionModel.select(
                                Language.ofFullCode(
                                    api.getGlobalSetting(
                                        KEY_LANGUAGE
                                    ).value
                                )
                            )
                        }
                        valueProperty().listener { isGlobalChanged.set(true) }
                    } row 0 col 1
                    label(getString(R.string.invoice_headers)) row 1 col 0
                    GlobalScope.launch(Dispatchers.JavaFx) {
                        invoiceHeadersArea = textArea(
                            api.getGlobalSetting(KEY_INVOICE_HEADERS).valueList.joinToString("\n").trim()
                        ) {
                            promptText = getString(R.string.invoice_headers)
                            setMaxSize(256.0, 88.0)
                            textProperty().listener { _, oldValue, value ->
                                when (INVOICE_HEADERS_DIVIDER) {
                                    in value -> text = oldValue
                                    else -> isGlobalChanged.set(true)
                                }
                            }
                        } row 1 col 1
                    }
                }
            }
        }
        buttonInvokable.run {
            jfxButton(getString(R.string.ok)) {
                isDefaultButton = true
                styleClass += R.style.raised
                buttonType = JFXButton.ButtonType.RAISED
                disableProperty().bind(!isLocalChanged and !isGlobalChanged)
                onActionFilter {
                    if (isLocalChanged.value) {
                        SettingsFile.save()
                    }
                    if (isGlobalChanged.value) GlobalScope.launch(Dispatchers.JavaFx) {
                        api.setGlobalSetting(KEY_LANGUAGE, languageBox.value.fullCode)
                        api.setGlobalSetting(
                            KEY_INVOICE_HEADERS,
                            invoiceHeadersArea.text.trim().replace("\n", "|")
                        )
                    }
                    close()
                }
            }
        }
    }

    private fun group(
        titleId: String,
        init: (@LayoutDsl _VBox).() -> Unit
    ): VBox = ktfx.layouts.vbox(getDouble(R.value.padding_small)) {
        label(getString(titleId)) {
            styleClass += R.style.bold
        }
        init()
    }

    private fun NodeInvokable.group(
        titleId: String,
        init: (@LayoutDsl _VBox).() -> Unit
    ): VBox = vbox(getDouble(R.value.padding_small)) {
        label(getString(titleId)) {
            styleClass += R.style.bold
        }
        init()
    }

    private fun NodeInvokable.item(
        labelId: String? = null,
        init: (@LayoutDsl _HBox).() -> Unit
    ): HBox = hbox(getDouble(R.value.padding_medium)) {
        alignment = Pos.CENTER_LEFT
        if (labelId != null) label(getString(labelId))
        init()
    }

    private companion object {

        const val INVOICE_HEADERS_DIVIDER = "|"

        /**
         * Can't use `javafxx-coroutines` because by the time `consume`
         * is called in coroutine context, it is already too late.
         */
        fun Node.onActionFilter(
            context: CoroutineContext = Dispatchers.JavaFx,
            action: suspend CoroutineScope.() -> Unit
        ) = addEventFilter(ActionEvent.ACTION) {
            it.consume()
            GlobalScope.launch(context) { action() }
        }
    }
}