package com.hendraanggrian.openpss.popup.dialog

import com.hendraanggrian.openpss.content.FxComponent
import com.hendraanggrian.openpss.popup.Popup
import com.jfoenix.controls.JFXDialog
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import ktfx.beans.value.getValue
import ktfx.beans.value.setValue
import ktfx.layouts.NodeInvokable

@Suppress("LeakingThis")
open class Dialog(
    component: FxComponent,
    override val titleId: String
) : JFXDialog(), Popup, FxComponent by component {

    private companion object {
        const val MAX_OPENED_DIALOGS = 3
    }

    override fun setActualContent(region: Region) {
        content = region
    }

    override fun setOnShown(onShown: () -> Unit) = setOnDialogOpened { onShown() }

    override fun dismiss() = close()

    override lateinit var contentPane: VBox
    override lateinit var buttonInvokable: NodeInvokable
    override lateinit var cancelButton: Button

    private val graphicProperty = SimpleObjectProperty<Node>()
    override fun graphicProperty(): ObjectProperty<Node> = graphicProperty
    var graphic: Node? by graphicProperty

    init {
        initialize()
        dialogContainer = rootLayout
    }

    override fun show() {
        val openedDialogs = rootLayout.children.filterIsInstance<Dialog>()
        if (openedDialogs.size > MAX_OPENED_DIALOGS) {
            rootLayout.children -= openedDialogs
        }
        super.show()
    }
}