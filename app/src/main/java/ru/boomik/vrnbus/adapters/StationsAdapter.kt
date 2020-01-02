package ru.boomik.vrnbus.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.StationOnMap


class StationsAdapter(val context: Context, var routes: List<StationOnMap>, var favorites: List<Int>, private val clickListener: (StationOnMap?) -> Unit) : RecyclerView.Adapter<StationsAdapter.StationViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return routes.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        return StationViewHolder(LayoutInflater.from(context).inflate(R.layout.station_cell, parent, false), clickListener)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {

        val route = routes[position]

        holder.cellView.tag = route
        holder.mNameView.let {
            it.tag = route
            it.text = route.name
        }

        val inFavorites = favorites.contains(route.id)
        holder.btnFavorite.let { btnFavorite ->
            btnFavorite.setImageResource(if (inFavorites) R.drawable.ic_favorite else R.drawable.ic_no_favorite)
            btnFavorite.tag = inFavorites
            btnFavorite.isFocusable = false
        }
    }


    class StationViewHolder(view: View, clickListener: (StationOnMap?) -> Unit) : RecyclerView.ViewHolder(view) {

        var mNameView = view.findViewById<TextView>(R.id.nameView)!!
        var btnFavorite = view.findViewById<ImageButton>(R.id.favorite)!!
        var cellView = view

        init {
            view.tag = null
            cellView.setOnClickListener{
                clickListener(view.tag as? StationOnMap)
            }
            TextViewCompat.setAutoSizeTextTypeWithDefaults(mNameView,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            btnFavorite.setOnClickListener {
                val station: StationOnMap = view.tag as? StationOnMap ?: return@setOnClickListener
                var inFav = (btnFavorite.tag as Boolean)
                inFav = !inFav
                btnFavorite.tag = inFav
                DataBus.sendEvent(DataBus.FavoriteStation, Pair(station.id, inFav))
                (it as ImageButton).setImageResource(if (inFav) R.drawable.ic_favorite else R.drawable.ic_no_favorite)
            }
        }

    }
}
