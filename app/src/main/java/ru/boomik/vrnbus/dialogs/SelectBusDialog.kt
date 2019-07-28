package ru.boomik.vrnbus.dialogs

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.adapters.RouteItem
import ru.boomik.vrnbus.managers.DataStorageManager
import ru.boomik.vrnbus.managers.SettingsManager


class SelectBusDialog {

    companion object {


        fun show(activity: Activity, mRoutes: String, mInsets: WindowInsetsCompat, selected: (String) -> Unit) {

            val routesList = DataStorageManager.routeNames
            if (routesList == null) {
                Toast.makeText(activity, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
                return
            }

            val decorView = activity.window.decorView as FrameLayout

            AsyncLayoutInflater(activity).inflate(R.layout.select_bus_dialog_second, decorView) { view, _, _ ->
            val dialogView = view  as LinearLayout
           // val dialogView = View.inflate(activity, R.layout.select_bus_dialog_second, null) as LinearLayout
                dialogView.tag = "dialog"
                val params = dialogView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
                val paramsLast = dialogView.getChildAt(dialogView.childCount - 1).layoutParams as ViewGroup.MarginLayoutParams
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    params.topMargin += mInsets.systemWindowInsetTop
                    paramsLast.bottomMargin = activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt() + mInsets.systemWindowInsetBottom
                } else {
                    params.topMargin += activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt()
                    paramsLast.bottomMargin += activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt()
                }
                dialogView.setOnClickListener {
                    decorView.removeView(dialogView)
                }


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
                nachos.chipifyAllUnterminatedTokens()

                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


/*
            val dialog = DialogPlus.newDialog(activity)
                    .setExpanded(true)
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
                    .setContentHeight(ViewGroup.LayoutParams.MATCH_PARENT)
                    .create()
*/
                nachos.setOnFocusChangeListener { _, _ ->
                    nachos.chipifyAllUnterminatedTokens()
                }

                nachos.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        nachos.chipifyAllUnterminatedTokens()
                        val routes = nachos.chipValues.asSequence().distinct().joinToString(",")
                        selected(routes)
                        //dialog.dismiss()
                        decorView.removeView(dialogView)
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
                    // dialog.dismiss()
                    decorView.removeView(dialogView)
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                }
                val allButton = dialogView.findViewById<Button>(R.id.all)
                allButton.setOnClickListener {
                    val routes = "*"
                    selected(routes)
                    //  dialog.dismiss()
                    decorView.removeView(dialogView)
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                }
                val favoritesButton = dialogView.findViewById<Button>(R.id.favorites)
                favoritesButton.setOnClickListener {

                    val favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)

                    if (favorites == null || favorites.count() == 0) {
                        Toast.makeText(activity, "Нет избранных маршрутов. Для добавления нажмите на звездочку у нужного маршрута", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    val routes = favorites.asSequence().distinct().joinToString(",")
                    selected(routes)
                    // dialog.dismiss()
                    decorView.removeView(dialogView)
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                }

                val activeRoutes = nachos.chipValues.asSequence().distinct()
                val favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
                val flexibleRoutesList = routesList.map {
                    RouteItem(it, activeRoutes.contains(it), favorites?.contains(it) ?: false)
                }.sortedByDescending { it.inFavorites }
                val routesRecycler = dialogView.findViewById<RecyclerView>(R.id.routesList)
                val routesAdapter = FlexibleAdapter(flexibleRoutesList)
                routesRecycler.layoutManager = SmoothScrollLinearLayoutManager(activity)
                routesRecycler.adapter = routesAdapter
                routesRecycler.setHasFixedSize(true)

                DataBus.subscribe<Pair<String, Boolean>>(DataBus.FavoriteRoute) { notification ->
                    flexibleRoutesList.firstOrNull { it.route == notification.data.first }?.inFavorites=notification.data.second
                }

                DataBus.unsubscribe<Pair<String, Boolean>>(DataBus.ClickRoute)
                DataBus.subscribe<Pair<String, Boolean>>(DataBus.ClickRoute) {
                    val buses = nachos.chipValues
                    if (it.data.second) buses.add(it.data.first)
                    else buses.remove(it.data.first)
                    nachos.setText(buses.distinct())
                }

                decorView.postDelayed({
                    val t = Slide(Gravity.TOP)
                    TransitionManager.beginDelayedTransition(decorView, t)
                    decorView.addView(dialogView)
                    //nachos.requestFocus()
                    //imm.showSoftInput(nachos, InputMethodManager.SHOW_FORCED)
                }, 0)
           }
        }

        fun Hide() {
            val decorView = window.decorView as FrameLayout?
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

