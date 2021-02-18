package ru.boomik.vrnbus.dialogs

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.hootsuite.nachos.ClearableAutoCompleteTextView
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.adapters.AutoCompleteContainArrayAdapter
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.FaveManager
import ru.boomik.vrnbus.objects.Fave
import java.util.*

class FaveParamsDialog {
    companion object {
        fun show(
            activity: Activity,
            type: String,
            name: String,
            @DrawableRes icon: Int,
            mInsets: WindowInsetsCompat,
            fave: Fave? = null
        ) {

            val routesList = DataManager.routes
            val stationsList = DataManager.stations
            if (stationsList == null || routesList == null) {
                Toast.makeText(activity, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
                return
            }

            val decorView = activity.window.decorView as FrameLayout

            AsyncLayoutInflater(activity).inflate(R.layout.fave_params_dialog, decorView) { view, _, _ ->
                val dialogView = view as LinearLayout
                dialogView.tag = "dialog"
                val paramsFirst = dialogView.layoutParams as ViewGroup.MarginLayoutParams
                val params = dialogView.layoutParams as ViewGroup.MarginLayoutParams
                val paramsLast = dialogView.getChildAt(dialogView.childCount - 1).layoutParams as ViewGroup.MarginLayoutParams
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    paramsFirst.topMargin += mInsets.systemWindowInsetTop
                    params.leftMargin += mInsets.systemWindowInsetLeft
                    params.rightMargin += mInsets.systemWindowInsetRight
                    paramsLast.bottomMargin = activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt() + mInsets.systemWindowInsetBottom
                } else {
                    paramsFirst.topMargin += activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt()
                    paramsLast.bottomMargin += activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt()
                }
                dialogView.setOnClickListener {
                    decorView.removeView(dialogView)
                }

                val delete = view.findViewById<TextView>(R.id.delete);
                if (fave==null) delete.visibility = View.GONE
                delete.setOnClickListener {
                    alertQuestion(activity, "Избранное", "Удалить пункт ибранного \"$name\".", "Да", "Нет") {
                        if (it) {
                            FaveManager.deleteFave(type)
                            hide(activity)
                            return@alertQuestion
                        }
                    }
                }

                val titleView = view.findViewById<TextView>(R.id.title)
                val iconView = view.findViewById<ImageView>(R.id.icon)
                titleView.text = name
                iconView.setImageResource(icon)
                view.findViewById<Button>(R.id.cancel).setOnClickListener {hide(activity)}
                val nachosRoutes = dialogView.findViewById<NachoTextView>(R.id.nachoRoutes)
                val stationNameEdit = dialogView.findViewById<ClearableAutoCompleteTextView>(R.id.stationAutoComplete)

                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                stationNameEdit.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) (v as ClearableAutoCompleteTextView).showDropDown()
                }

                val routesNames = routesList.map { r->r.name }.toList()
                val nachosAdapter = AutoCompleteContainArrayAdapter(activity, R.layout.bus_complete_view, routesNames)

                val namesAdapter = AutoCompleteContainArrayAdapter(activity, R.layout.bus_complete_view, stationsList.map { it.title })
                stationNameEdit.setAdapter(namesAdapter)
                imm.hideSoftInputFromWindow(stationNameEdit.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)

                stationNameEdit.imeOptions = EditorInfo.IME_ACTION_NEXT
                stationNameEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)


                fun findStation(name: String) : StationObject? {
                    val stations = stationsList.filter {
                        it.title.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
                    }
                    return if (stations.size == 1) stations.first()
                    else null
                }

                fun selectStation(name: String) {
                    val station = findStation(name) ?: return
                    val id = station.id
                    val routesForStation =
                        routesList.filter { it.backward.contains(id) || it.forward.contains(id) }
                            .map { it.name }.toList()
                    nachosAdapter.setNewData(routesForStation)
                    // nameEdit.postDelayed({selected(StationOnMap(f.title, f.id, f.latitude, f.longitude))}, 250)
                    nachosRoutes.requestFocus()
                    nachosRoutes.postDelayed({ nachosRoutes.showDropDown() }, 300)

                }

                stationNameEdit.setOnItemClickListener { _, view, _, _ ->
                    run {
                        val stationNameView = view.findViewById<TextView>(android.R.id.text1)
                        val stationName = stationNameView.text.toString()
                        selectStation(stationName)
                    }
                }

                stationNameEdit.setOnEditorActionListener { tv, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        selectStation(tv.text.toString())
                        true
                    } else
                        false
                }


                nachosRoutes.setAdapter(nachosAdapter)
                nachosRoutes.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
                nachosRoutes.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
                nachosRoutes.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
                nachosRoutes.enableEditChipOnTouch(false, true)


                nachosRoutes.imeOptions = EditorInfo.IME_ACTION_SEARCH
                nachosRoutes.setRawInputType(InputType.TYPE_CLASS_TEXT)
                nachosRoutes.chipifyAllUnterminatedTokens()

                imm.hideSoftInputFromWindow(nachosRoutes.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)

                nachosRoutes.setOnEditorActionListener { v, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                       // SelectBusDialog.selectRoutes(nachos, routesList, selected)
                        imm.hideSoftInputFromWindow(nachosRoutes.windowToken, 0)
                        //SelectBusDialog.hide(activity)
                        (v as NachoTextView).showDropDown()
                        true
                    } else
                        false
                }


                fun getNachos(nachos: NachoTextView, routesList: List<String>): List<String> {
                    nachos.chipifyAllUnterminatedTokens()
                    val routes = nachos.chipValues.asSequence().distinct().toMutableList()
                    return routes.filter { routesList.contains(it) }.toList()
                }


                fave?.let {
                    nachosRoutes.setText(fave.routes)
                    val station = stationsList.firstOrNull { s -> s.id == it.stationId }
                    if (station != null) stationNameEdit.setText(station.title)
                }

                view.findViewById<Button>(R.id.save).setOnClickListener {
                    val station = findStation(stationNameEdit.text.toString())
                    if (station==null) {
                        Toast.makeText(activity, "Выберите остановку", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val routes = getNachos(nachosRoutes, routesNames)
                    if (!routes.any()) {
                        Toast.makeText(activity, "Выберите маршруты", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    FaveManager.save(type, station.id, routes)
                    hide(activity)
                }

                decorView.postDelayed({
                    val t = Slide(Gravity.TOP)
                    TransitionManager.beginDelayedTransition(decorView, t)
                    decorView.addView(dialogView)

                    decorView.postDelayed({
                        stationNameEdit.requestFocus()
                        stationNameEdit.showDropDown()
                    }, 500)
                    //nachos.requestFocus()
                    //imm.showSoftInput(nachos, InputMethodManager.SHOW_FORCED)
                }, 0)
            }
        }


        fun hide(activity: Activity) {
            val decorView = activity.window.decorView as FrameLayout?
            if (decorView != null && decorView.childCount > 0) {
                val last = decorView.getChildAt(decorView.childCount - 1)
                if (last.tag == "dialog") {
                    val t = Slide(Gravity.TOP)
                    TransitionManager.beginDelayedTransition(decorView, t)
                    decorView.removeView(last)
                    return
                }
            }
        }
    }
}