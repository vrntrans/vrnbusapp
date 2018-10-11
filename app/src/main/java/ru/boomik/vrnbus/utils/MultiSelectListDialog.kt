package ru.boomik.vrnbus.utils

import android.app.Activity
import androidx.appcompat.app.AlertDialog


fun alertMultipleChoiceItems(activity: Activity, items: List<String>, selected: (ArrayList<String>?) -> Unit) {
    val selectedItems = ArrayList<String>()

    AlertDialog.Builder(activity)
            .setMultiChoiceItems(items.toTypedArray(), null) { _, which, isChecked ->
                if (isChecked) {
                    // if the user checked the item, add it to the selected items
                    selectedItems.add(items[which])
                } else if (selectedItems.contains(items[which])) {
                    // else if the item is already in the array, remove it
                    selectedItems.remove(items[which])
                }
            }
            .setPositiveButton("OK") { _, _ -> selected(selectedItems) }
            .setNegativeButton("Cancel") { _, _ -> selected(null) }
            .setOnCancelListener {
                selected(null)
            }
            .show()

}
