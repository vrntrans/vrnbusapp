package ru.boomik.vrnbus.dialogs

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.hootsuite.nachos.ClearableAutoCompleteTextView
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.adapters.AutoCompleteContainArrayAdapter
import ru.boomik.vrnbus.adapters.StationsAdapter
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.StationOnMap


class SelectStationDialog {

    companion object {

        fun show(activity: Activity, mInsets: WindowInsetsCompat, selected: (StationOnMap) -> Unit) {

            val stationsList = DataManager.stations
            if (stationsList == null) {
                Toast.makeText(activity, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
                return
            }

            val decorView = activity.window.decorView as FrameLayout

            AsyncLayoutInflater(activity).inflate(R.layout.select_station_dialog, decorView) { view, _, _ ->
                val dialogView = view  as LinearLayout
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


                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                val namesAdapter = AutoCompleteContainArrayAdapter(activity, R.layout.bus_complete_view, stationsList.map { it.title })
                val nameEdit = dialogView.findViewById<ClearableAutoCompleteTextView>(R.id.autoComplete)
                nameEdit.setAdapter(namesAdapter)
                imm.hideSoftInputFromWindow(nameEdit.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)

                nameEdit.imeOptions = EditorInfo.IME_ACTION_SEARCH
                nameEdit.setRawInputType(InputType.TYPE_CLASS_TEXT)

                nameEdit.setOnItemClickListener { _, view, _, _ ->
                    run {
                        val name = view.findViewById<TextView>(android.R.id.text1)
                        val stationName = name.text
                        val station = stationsList.firstOrNull { it.title == stationName }
                        imm.hideSoftInputFromWindow(nameEdit.windowToken, 0)
                        hide(activity)
                        station?.let { nameEdit.postDelayed({selected(StationOnMap(station.title, station.id, station.latitude, station.longitude))}, 250)}
                    }
                }

                nameEdit.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        val stations = stationsList.filter { it.title.toLowerCase().contains(nameEdit.text.toString().toLowerCase()) }
                        if (stations.size==1) {
                            imm.hideSoftInputFromWindow(nameEdit.windowToken, 0)
                            hide(activity)
                            var f= stations.first()
                            nameEdit.postDelayed({selected(StationOnMap(f.title, f.id, f.latitude, f.longitude))}, 250)
                        }
                        true
                    } else
                        false
                }


                var favorites = SettingsManager.getIntArray(Consts.SETTINGS_FAVORITE_STATIONS)
                if (favorites==null) favorites = listOf()
                val routesAdapter = StationsAdapter(activity, stationsList.sortedByDescending { r -> favorites.contains(r.id) }.map { s-> StationOnMap(s.title, s.id, s.latitude, s.longitude) }, favorites) {
                    imm.hideSoftInputFromWindow(nameEdit.windowToken, 0)
                    hide(activity)
                    it?.let { nameEdit.postDelayed({selected(it)}, 250)}
                }

                val stationsRecycler = dialogView.findViewById<RecyclerView>(R.id.stationsList)
                stationsRecycler.layoutManager = LinearLayoutManager(activity)
                stationsRecycler.adapter = routesAdapter
                stationsRecycler.setHasFixedSize(true)


                decorView.postDelayed({
                    val t = Slide(Gravity.TOP)
                    TransitionManager.beginDelayedTransition(decorView, t)
                    decorView.addView(dialogView)
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