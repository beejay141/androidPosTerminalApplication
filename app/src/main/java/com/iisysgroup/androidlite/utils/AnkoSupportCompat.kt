/**
 * Anko support.v4 compatibility shim.
 * Provides Fragment.alert() equivalent to org.jetbrains.anko.support.v4.alert.
 */
package org.jetbrains.anko.support.v4

import androidx.fragment.app.Fragment
import org.jetbrains.anko.AlertDialogBuilder

fun Fragment.alert(init: AlertDialogBuilder.() -> Unit): AlertDialogBuilder {
    val builder = AlertDialogBuilder(requireContext())
    builder.init()
    return builder
}
