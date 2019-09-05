package ru.boomik.vrnbus.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R

class RoutesAdapter(val context: Context, var routes : List<String>, var selected : List<String>, var favorites : List<String>) : RecyclerView.Adapter<ViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return routes.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.route_cell, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val route = routes[position]

        holder.cellView.tag = route
        holder.mCheckBoxSelected.let {
            it.tag = route
            it.text = route
            it.isChecked = selected.contains(route)
        }

        val inFavorites = favorites.contains(route)
        holder.btnFavorite.let { btnFavorite ->
            btnFavorite.setImageResource(if (inFavorites) R.drawable.ic_star else R.drawable.ic_no_star)
            btnFavorite.tag = inFavorites
            btnFavorite.isFocusable = false
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    var mCheckBoxSelected = view.findViewById<CheckBox>(R.id.checkBox)!!
    var btnFavorite = view.findViewById<ImageButton>(R.id.favorite)!!
    var cellView = view

    init {
        view.tag = null
        view.setOnClickListener { _ ->
            mCheckBoxSelected.let{it.isChecked = !it.isChecked}
            DataBus.sendEvent(DataBus.ClickRoute, Pair(mCheckBoxSelected.tag as String, mCheckBoxSelected.isChecked))
        }

        mCheckBoxSelected.setOnCheckedChangeListener { checkBox, isChecked ->
            if(!checkBox.isPressed) return@setOnCheckedChangeListener
            DataBus.sendEvent(DataBus.ClickRoute, Pair(checkBox.tag as String, isChecked))
        }

        btnFavorite.setOnClickListener {
            var inFav = (btnFavorite.tag as Boolean)
            inFav = !inFav
            btnFavorite.tag = inFav
            DataBus.sendEvent(DataBus.FavoriteRoute, Pair(view.tag, inFav))
            (it as ImageButton).setImageResource(if (inFav) R.drawable.ic_star else R.drawable.ic_no_star)
        }
    }
}