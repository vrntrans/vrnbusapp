package ru.boomik.vrnbus.dialogs

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.R
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

            AsyncLayoutInflater(activity).inflate(R.layout.select_bus_dialog_second, null) { view, _, _ ->
                // val dialogView = View.inflate(activity, R.layout.select_bus_dialog_second, null) as LinearLayout
                val dialogView = view  as LinearLayout
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
                val flexibleRoutesList = routesList.map { RouteItem(it, activeRoutes.contains(it)) }
                val routesRecycler = dialogView.findViewById<RecyclerView>(R.id.routesList)
                val routesAdapter = FlexibleAdapter(flexibleRoutesList)
                routesRecycler.layoutManager = SmoothScrollLinearLayoutManager(activity)
                routesRecycler.adapter = routesAdapter
                routesRecycler.setHasFixedSize(true)

                /*
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
                }*/

                //dialog.show()

                decorView.postDelayed({
                    val t = Slide(Gravity.TOP)
                    TransitionManager.beginDelayedTransition(decorView, t)
                    decorView.addView(dialogView)
                    //nachos.requestFocus()
                    //imm.showSoftInput(nachos, InputMethodManager.SHOW_FORCED)
                }, 0)

            }
        }

    }
}

/**
 * Where AbstractFlexibleItem implements IFlexible!
 */
class RouteItem(private val route: String, var checked: Boolean) : AbstractFlexibleItem<RouteItem.MyViewHolder/*(2)*/>() {


    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    override fun equals(other: Any?): Boolean {
        return if (other is RouteItem) {
            this.route == other.route
        } else false
    }

    /**
     * For the item type we need an int value: the layoutResID is sufficient.
     */
    override fun getLayoutRes(): Int {
        return R.layout.route_cell
    }


    override fun hashCode(): Int {
        return route.hashCode()
    }

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * The inflated view is already provided as well as the Adapter.
     */
    override fun createViewHolder(view: View?, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?): MyViewHolder {
        return MyViewHolder(view, adapter)
    }

    /**
     * The Adapter and the Payload are provided to perform and get more specific
     * information.
     */
    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?, holder: MyViewHolder?, position: Int, payloads: MutableList<Any>?) {
        if (holder == null) return
        holder.mCheckBox?.let {
            it.text = route
            it.isChecked = checked
        }
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    inner class MyViewHolder(view: View?, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?) : FlexibleViewHolder(view, adapter) {

        var mCheckBox = view?.findViewById<CheckBox>(R.id.checkBox)

    }
}