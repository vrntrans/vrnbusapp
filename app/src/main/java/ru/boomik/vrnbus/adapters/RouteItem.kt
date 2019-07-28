package ru.boomik.vrnbus.adapters

import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R

/**
 * Where AbstractFlexibleItem implements IFlexible!
 */
class RouteItem(val route: String, var checked: Boolean,var inFavorites: Boolean) : AbstractFlexibleItem<RouteItem.MyViewHolder/*(2)*/>() {


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
        holder.view?.tag = route
        holder.mCheckBox?.let {
            it.tag = route
            it.text = route
            it.isChecked = checked


        }
        holder.btnFavorite?.let { btnFavorite ->
            btnFavorite.setImageResource(if (inFavorites) R.drawable.ic_star else R.drawable.ic_no_star)
            btnFavorite.tag = inFavorites

            btnFavorite.isFocusable = false
        }


    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    inner class MyViewHolder(var view: View?, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?) : FlexibleViewHolder(view, adapter) {

        var mCheckBox = view?.findViewById<CheckBox>(R.id.checkBox)
        var btnFavorite = view?.findViewById<ImageButton>(R.id.favorite)

        init {
            view?.tag = null
            view?.setOnClickListener { _ ->
                mCheckBox?.let{it.isChecked = !it.isChecked}
            }

            mCheckBox?.setOnCheckedChangeListener {checkBox, isChecked ->
                DataBus.sendEvent(DataBus.ClickRoute, Pair(checkBox.tag as String, isChecked))
            }

            btnFavorite?.setOnClickListener {
                var inFav = (btnFavorite?.tag as Boolean)
                inFav = !inFav
                btnFavorite?.tag = inFav
                DataBus.sendEvent(DataBus.FavoriteRoute, Pair(view?.tag, inFav))
                (it as ImageButton).setImageResource(if (inFav) R.drawable.ic_star else R.drawable.ic_no_star)
            }
        }
    }
}
