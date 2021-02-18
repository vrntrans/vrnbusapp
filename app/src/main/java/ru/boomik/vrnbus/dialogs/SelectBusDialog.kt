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
import androidx.appcompat.widget.AppCompatImageButton
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.adapters.AutoCompleteContainArrayAdapter
import ru.boomik.vrnbus.adapters.RoutesAdapter
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.FaveManager
import ru.boomik.vrnbus.managers.SettingsManager


class SelectBusDialog {

    companion object {
        fun show(activity: Activity, mRoutes: String, mInsets: WindowInsetsCompat, selected: (String) -> Unit) {

            val routesList = DataManager.routeNames
            if (routesList == null) {
                Toast.makeText(activity, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
                return
            }

            val decorView = activity.window.decorView as FrameLayout

            AsyncLayoutInflater(activity).inflate(R.layout.select_bus_dialog, decorView) { view, _, _ ->
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

/*
                val routeEditText = dialogView.findViewById<ClearableMultiAutoCompleteTextView>(R.id.route_edit_text)
                routeEditText.imeOptions = EditorInfo.IME_ACTION_SEARCH
                routeEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)

                fun createChip(text: String) : ChipDrawable {
                    val chip = ChipDrawable.createFromResource(activity, R.xml.chip)
                    chip.text=text
                    chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
                    return chip
                }

                var spannedLength = 0
                val chipLength = 4

                routeEditText.doOnTextChanged { text, start, before, count ->
                    if (text==null) return@doOnTextChanged
                    if (text.length == spannedLength - chipLength)
                        spannedLength = text.length
                }

                routeEditText.doAfterTextChanged {
                    if (it==null) return@doAfterTextChanged
                    val text = it.toString()
                    val splitByComma = text.split(',')
                    val splitBySpace = text.split(' ')
                    val splitBySemicolon = text.split(';')
                    val texts : List<String>
                    texts = when {
                        splitByComma.size>1 -> splitByComma.filter { item -> item.isNotBlank() }.toList()
                        splitBySpace.size>1 -> splitBySpace.filter { item -> item.isNotBlank() }.toList()
                        splitBySemicolon.size>1 -> splitBySemicolon.filter { item -> item.isNotBlank() }.toList()
                        else -> emptyList()
                    }
                    if (texts.size < 2) return@doAfterTextChanged

                    texts.forEach { chitText ->
                        val chip = createChip(chitText)
                        val span = ImageSpan(chip)
                        it.setSpan(span, spannedLength, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        spannedLength = text.length
                    }
                }
                routeEditText.setAdapter(nachosAdapter)
*/


                val favesButton = mutableListOf<AppCompatImageButton>()
                favesButton.add(dialogView.findViewById(R.id.fave_home))
                favesButton.add(dialogView.findViewById(R.id.fave_work))
                favesButton.add(dialogView.findViewById(R.id.fave_man))
                favesButton.add(dialogView.findViewById(R.id.fave_one))
                favesButton.add(dialogView.findViewById(R.id.fave_two))
                favesButton.add(dialogView.findViewById(R.id.fave_three))
                favesButton.add(dialogView.findViewById(R.id.fave_four))
                favesButton.add(dialogView.findViewById(R.id.fave_five))

                val faveClick = View.OnClickListener { v -> if (v!=null) {
                    hide(activity)
                    FaveManager.faveClick(v.tag as String)
                }}
                val faveLongClick = View.OnLongClickListener {
                    hide(activity)
                    return@OnLongClickListener FaveManager.faveLongClick(it.tag as String)
                }
                favesButton.forEach {
                    it.setOnClickListener(faveClick)
                    it.setOnLongClickListener(faveLongClick)
                }



                val nachosAdapter = AutoCompleteContainArrayAdapter(activity, R.layout.bus_complete_view, routesList)
                val nachos = dialogView.findViewById<NachoTextView>(R.id.nachoRoutes)
                nachos.setAdapter(nachosAdapter)
                nachos.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
                nachos.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
                nachos.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
                nachos.enableEditChipOnTouch(false, true)
                if (mRoutes.isNotEmpty()) {
                    val routes = mRoutes.split(',').asSequence().distinct().map { it.trim() }.toList()
                    nachos.setText(routes)
                }

                nachos.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) (v as NachoTextView).showDropDown()
                }

                nachos.imeOptions = EditorInfo.IME_ACTION_SEARCH
                nachos.setRawInputType(InputType.TYPE_CLASS_TEXT)
                nachos.chipifyAllUnterminatedTokens()

                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(nachos.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)

                nachos.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        selectRoutes(nachos, routesList, selected)
                        imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                        hide(activity)
                        true
                    } else
                        false
                }

                val searchButton = dialogView.findViewById<Button>(R.id.search)
                searchButton.setOnClickListener {
                    selectRoutes(nachos, routesList, selected)

                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    hide(activity)
                }
                val allButton = dialogView.findViewById<Button>(R.id.all)
                allButton.setOnClickListener {
                    val routes = "*"
                    selected(routes)
                    nachos.clearFocus()
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    hide(activity)
                }
                /*
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
                    nachos.clearFocus()
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    hide(activity)
                }*/

                val selectedRoutes = nachos.chipValues.asSequence().distinct().toList()
                var favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
                if (favorites==null) favorites = listOf()
                val routesAdapter = RoutesAdapter(activity, routesList.sortedByDescending { r -> favorites.contains(r) }, selectedRoutes, favorites)

                val routesRecycler = dialogView.findViewById<RecyclerView>(R.id.routesList)
                routesRecycler.layoutManager = LinearLayoutManager(activity)
                routesRecycler.adapter = routesAdapter
                routesRecycler.setHasFixedSize(true)

                DataBus.subscribe<Pair<String, Boolean>>(DataBus.FavoriteRoute) { _ ->
                    routesAdapter.favorites =  SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE) ?: listOf()
                    routesAdapter.notifyDataSetChanged()
                }

                nachos.setOnFocusChangeListener { _, _ ->
                    nachos.chipifyAllUnterminatedTokens()
                    updateSelectedInAdapter(routesAdapter, nachos)
                }

                nachos.setOnChipAddListener {
                    val text = nachos.chipValues
                    val nachosText = getNachos(nachos, routesList)
                    if (text.count()!=nachosText.count())
                        nachos.post {
                            nachos.setText(nachosText)
                        }
                    updateSelectedInAdapter(routesAdapter, nachos)
                }
                nachos.setOnChipRemoveListener {
                    nachos.chipifyAllUnterminatedTokens()
                    updateSelectedInAdapter(routesAdapter, nachos)
                }


                DataBus.unsubscribe<Pair<String, Boolean>>(DataBus.ClickRoute)
                DataBus.unsubscribe<Pair<String, Boolean>>(DataBus.LongClickRoute)
                DataBus.subscribe<Pair<String, Boolean>>(DataBus.ClickRoute) {
                    val buses = nachos.chipValues
                    if (it.data.second) buses.add(it.data.first)
                    else buses.remove(it.data.first)
                    if (buses.contains("*")) buses.remove("*")
                    nachos.setText(buses.distinct())
                    updateSelectedInAdapter(routesAdapter, nachos)
                }
                DataBus.subscribe<String>(DataBus.LongClickRoute) {
                    val routes = it.data
                    selected(routes)
                    nachos.clearFocus()
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    hide(activity)
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

        private fun selectRoutes(nachos: NachoTextView, routesList: List<String>, selected: (String) -> Unit) {
            selected(getNachos(nachos, routesList).joinToString(","))
            nachos.clearFocus()
        }

        private fun getNachos(nachos: NachoTextView, routesList: List<String>): List<String> {
            nachos.chipifyAllUnterminatedTokens()
            val routes = nachos.chipValues.asSequence().distinct().toMutableList()
            return routes.filter { routesList.contains(it) }.toList()
        }

        private fun updateSelectedInAdapter(adapter: RoutesAdapter, nachos: NachoTextView) {
            nachos.post {
                val selectedRoutes = nachos.chipValues.asSequence().distinct().toList()
                if (selectedRoutes.toTypedArray().contentEquals(adapter.selected.toTypedArray())) return@post
                adapter.selected = selectedRoutes
                adapter.notifyDataSetChanged()
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