package ru.boomik.vrnbus

import android.animation.ValueAnimator
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import java.lang.StringBuilder
import java.util.*

class RoutesAdapter(private val context: Context, BussList: List<Bus>) : BaseAdapter() {

    private var busesList: List<Bus> = BussList
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var favorites: List<String>?

    init {
        favorites = SettingsManager.instance.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
        DataBus.subscribe<Pair<String, Boolean>>(DataBus.FavoriteRoute) {
            favorites = SettingsManager.instance.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
            notifyDataSetChanged()
        }
    }

    fun dataEquals(routes: String): Boolean {
        if (routes.isBlank()) return true
        val dataRoutes = busesList.asSequence().map { it.route }.map { it.trim() }.distinct().toList()
        val routesList = routes.split(',').asSequence().map { it.trim() }.distinct().toList()
        if (routesList.size != dataRoutes.size) return false
        return dataRoutes.firstOrNull { !routesList.contains(it) } == null
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val view: View?
        val vh: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.bus_cell, parent, false)
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        val bus = getItem(position) as Bus

        vh.tvTitle.text = bus.route

        vh.tvAbsoluteTime.includeFontPadding = false
        vh.tvContent.includeFontPadding = false

        val time = bus.timeLeft.toInt()
        val timeToArrival = bus.timeToArrival

        val arrival = bus.arrivalTime
        if (arrival != null) {
            val absoluteTime = DateFormat.format("kk:mm", arrival).toString()
            vh.tvAbsoluteTime.text = "${context.getString(R.string.arrival_at)}$absoluteTime"
        } else vh.tvAbsoluteTime.text = null

        val sb = StringBuilder()
        if (bus.lowFloor) sb.append("Низкопольный.\n")
        if (bus.busType > 0) {
            sb.append(when {
                bus.busType == 3 -> context.getString(R.string.medium_capacity)
                bus.busType == 4 -> context.getString(R.string.big_capacity)
                else -> context.getString(R.string.small_capacity)
            })
        }
        if (sb.isBlank()) {
            vh.tvAdditional.visibility = View.GONE
        } else {
            vh.tvAdditional.visibility = View.VISIBLE
            vh.tvAdditional.text = sb.toString()
        }

        (vh.tvTitle.tag as? ValueAnimator)?.cancel()

        if (time >= 1000) {
            vh.tvContent.text = null
            vh.tvContent.tag = null
        } else {
            vh.tvContent.tag = timeToArrival
            updateTimeLeft(vh.tvContent)
            val anim = ValueAnimator.ofInt(30)
            anim.addUpdateListener { updateTimeLeft(vh.tvContent) }
            anim.duration = 30000
            anim.start()
            vh.tvTitle.tag = anim
        }

        var inFavorite = favorites?.contains(bus.route) ?: false
        vh.btnFavorite.setImageResource(if (inFavorite) R.drawable.ic_star else R.drawable.ic_no_star)

        vh.btnFavorite.setOnClickListener {
            inFavorite=!inFavorite
            DataBus.sendEvent(DataBus.FavoriteRoute, Pair(bus.route,inFavorite))
            vh.btnFavorite.setImageResource(if (inFavorite) R.drawable.ic_star else R.drawable.ic_no_star)
        }


        return view
    }

    private fun updateTimeLeft(tvContent: TextView) {

        val timeString: String
        val timeToArrival = tvContent.tag as? Long
        if (timeToArrival == null) {
            tvContent.text = ""
            return
        }
        val timeLeft = timeToArrival - System.currentTimeMillis()
        if (timeLeft <= 10 * 1000) {
            tvContent.text = "Ждем"
            return
        }
        val cal = Calendar.getInstance()
        cal.clear()
        cal.add(Calendar.MILLISECOND, timeLeft.toInt())
        var min = cal.get(Calendar.MINUTE)
        val sec = cal.get(Calendar.SECOND)
        if (min > 1 && sec > 30) min++
        timeString = if (min < 1 && sec < 30) "Прибывает"
        else if (min < 1) "менее минуты"
        else "$min мин."

        tvContent.text = timeString
    }

    override fun getItem(position: Int): Any {
        return busesList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return busesList.size
    }

    fun setRoutes(routes: List<Bus>) {
        busesList = routes
        notifyDataSetChanged()
    }
}

private class ViewHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.title) as TextView
    val tvContent: TextView = view?.findViewById(R.id.time) as TextView
    val tvAbsoluteTime: TextView = view?.findViewById(R.id.absoluteTime) as TextView
    val tvAdditional: TextView = view?.findViewById(R.id.additional) as TextView
    val btnFavorite: ImageButton = view?.findViewById(R.id.favorite) as ImageButton

}