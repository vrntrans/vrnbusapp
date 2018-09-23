package ru.boomik.vrnbus.ui_utils

import android.app.Activity
import android.content.DialogInterface
import android.support.v7.app.AlertDialog


fun alertMultipleChoiceItems(activity: Activity, items: List<String>, selected: (ArrayList<String>?) -> Unit) {

    val builder = AlertDialog.Builder(activity)
    val selectedItems = ArrayList<String>()

    builder.setMultiChoiceItems(items.toTypedArray(), null) { _, which, isChecked ->
        if (isChecked) {
            // if the user checked the item, add it to the selected items
            selectedItems.add(items[which])
        } else if (selectedItems.contains(items[which])) {
            // else if the item is already in the array, remove it
            selectedItems.remove(items[which])
        }
    }
            // Set the action buttons
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                selected(selectedItems)
            })

            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                selected(null)
            })
            .setOnCancelListener {
                selected(null)
            }
            .show()

}
