package ru.boomik.vrnbus.dialogs

import android.app.Activity
import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.WindowInsetsCompat
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.managers.DataStorageManager

class SelectBusDialog {

    companion object {
        fun show(activity: Activity, mRoutes: String, mInsets: WindowInsetsCompat, selected: (String) -> Unit) {

            val routesList = DataStorageManager.routeNames
            if (routesList == null) {
                Toast.makeText(activity, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
                return
            }

            val dialogView = View.inflate(activity, R.layout.select_bus_dialog, null) as LinearLayout
            val params = dialogView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin += mInsets.systemWindowInsetTop


            val adapter = ArrayAdapter(activity, R.layout.bus_complete_view, routesList)
            val nachos = dialogView.findViewById<NachoTextView>(R.id.nacho_text_view)
            nachos.setAdapter(adapter)
            nachos.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            nachos.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            nachos.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            nachos.enableEditChipOnTouch(false, true)
            if (mRoutes.isNotEmpty()) {
                val routes = mRoutes.split(',').asSequence().distinct().map { it.trim() }.toList()
                nachos.setText(routes)
            }

            nachos.imeOptions = EditorInfo.IME_ACTION_SEARCH
            nachos.setRawInputType(InputType.TYPE_CLASS_TEXT)

            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            val dialog = DialogPlus.newDialog(activity)
                    .setGravity(Gravity.TOP)
                    .setCancelable(true)
                    .setOnDismissListener {
                        imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                        nachos.clearFocus()
                    }
                    .setOnCancelListener {
                        imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                        nachos.clearFocus()
                    }
                    .setContentHolder(ViewHolder(dialogView))
                    .setContentBackgroundResource(android.R.color.transparent)
                    .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .create()

            nachos.setOnFocusChangeListener { _, _ ->
                nachos.chipifyAllUnterminatedTokens()
            }

            nachos.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    nachos.chipifyAllUnterminatedTokens()
                    val routes = nachos.chipValues.asSequence().distinct().joinToString(",")
                    selected(routes)
                    dialog.dismiss()
                    nachos.clearFocus()
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    true
                } else
                    false
            }

            val searchButton = dialogView.findViewById<Button>(R.id.search)
            searchButton.setOnClickListener {
                nachos.chipifyAllUnterminatedTokens()
                val routes = nachos.chipValues.asSequence().distinct().joinToString(",")
                selected(routes)
                dialog.dismiss()
                imm.hideSoftInputFromWindow(nachos.windowToken, 0)
            }


            val showList = dialogView.findViewById<ImageButton>(R.id.showList)
            showList.setOnClickListener { _ ->
                nachos.chipifyAllUnterminatedTokens()
                alertMultipleChoiceItems(activity, routesList) {
                    if (it != null) {
                        val buses = nachos.chipValues
                        buses.addAll(it)
                        nachos.setText(buses.distinct())
                    }
                }
            }

            dialog.show()

            nachos.requestFocus()
            imm.showSoftInput(nachos, InputMethodManager.SHOW_FORCED)
        }
    }
}