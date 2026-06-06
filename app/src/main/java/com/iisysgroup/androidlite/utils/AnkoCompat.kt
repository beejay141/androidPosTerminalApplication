/**
 * Anko compatibility shim.
 *
 * Provides drop-in replacements for the Anko library functions used in this project.
 * The original Anko library (org.jetbrains.anko) is no longer available on any public
 * Maven repository after the JCenter shutdown in 2021.
 *
 * Functions provided:
 *  - Context.toast(message)
 *  - Activity.contentView
 *  - Context.doAsync(block)
 *  - Context.alert(init) -> AlertDialogBuilder
 *  - AlertDialogBuilder.okButton(handler)
 *  - AlertDialogBuilder.cancelButton(handler)
 *  - Context.indeterminateProgressDialog(message, title?, init?) -> ProgressDialog
 */
@file:Suppress("DEPRECATION")
package org.jetbrains.anko

import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.concurrent.Executors

// ---------- toast ----------

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.toast(messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageResId, duration).show()
}

// ---------- Activity.contentView ----------

val android.app.Activity.contentView: View?
    get() = window?.decorView?.rootView

// ---------- doAsync ----------

private val ankoThreadPool = Executors.newCachedThreadPool()

fun Context.doAsync(task: () -> Unit) {
    ankoThreadPool.execute(task)
}

// ---------- AlertDialogBuilder DSL ----------

class AlertDialogBuilder(val ctx: Context) {
    var title: String? = null
    var message: String? = null
    private var positiveButtonText: String = "OK"
    private var positiveButtonHandler: (() -> Unit)? = null
    private var negativeButtonText: String = "Cancel"
    private var negativeButtonHandler: (() -> Unit)? = null

    fun okButton(handler: () -> Unit) {
        positiveButtonText = "OK"
        positiveButtonHandler = handler
    }

    fun cancelButton(handler: () -> Unit) {
        negativeButtonText = "Cancel"
        negativeButtonHandler = handler
    }

    fun show(): AlertDialog {
        val builder = AlertDialog.Builder(ctx)
        title?.let { builder.setTitle(it) }
        message?.let { builder.setMessage(it) }
        if (positiveButtonHandler != null) {
            builder.setPositiveButton(positiveButtonText) { _, _ -> positiveButtonHandler?.invoke() }
        }
        if (negativeButtonHandler != null) {
            builder.setNegativeButton(negativeButtonText) { _, _ -> negativeButtonHandler?.invoke() }
        }
        val dialog = builder.create()
        dialog.show()
        return dialog
    }
}

fun Context.alert(init: AlertDialogBuilder.() -> Unit): AlertDialogBuilder {
    val builder = AlertDialogBuilder(this)
    builder.init()
    return builder
}

// ---------- indeterminateProgressDialog ----------

fun Context.indeterminateProgressDialog(
    message: CharSequence = "",
    title: CharSequence? = null,
    init: (ProgressDialog.() -> Unit)? = null
): ProgressDialog {
    val dialog = ProgressDialog(this)
    dialog.isIndeterminate = true
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
    if (title != null) dialog.setTitle(title)
    if (message.isNotEmpty()) dialog.setMessage(message)
    init?.invoke(dialog)
    dialog.show()
    return dialog
}
