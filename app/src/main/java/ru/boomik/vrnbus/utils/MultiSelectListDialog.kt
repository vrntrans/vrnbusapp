package ru.boomik.vrnbus.utils

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout
import android.widget.EditText
import android.view.ViewGroup
import android.util.DisplayMetrics
import android.content.res.Resources.Theme
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import ru.boomik.vrnbus.R


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
            .setPositiveButton(activity.getString(R.string.Ок)) { _, _ -> selected(selectedItems) }
            .setNegativeButton(activity.getString(R.string.Cancel)) { _, _ -> selected(null) }
            .setOnCancelListener {
                selected(null)
            }
            .show()

}

fun alertEnterText(activity: Activity, title: String, selected: (String?) -> Unit) {
    val input = EditText(activity)

    val typedValue = TypedValue()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        activity.theme.resolveAttribute(android.R.attr.dialogPreferredPadding, typedValue, true)
    }
    val resId = typedValue.resourceId
    val margin = if (resId <= 0) 0 else activity.resources.getDimensionPixelSize(resId) + (-4 * activity.resources.displayMetrics.density).toInt()


    val layout = LinearLayout(activity)
    layout.setPadding(margin, 0, margin, 0)
    layout.addView(input, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

    AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(activity.getString(R.string.Ок)) { _, _ -> selected(input.text.toString()) }
            .setNegativeButton(activity.getString(R.string.Cancel)) { _, _ -> selected(null) }
            .setOnCancelListener {
                selected(null)
            }
            .show()

}
