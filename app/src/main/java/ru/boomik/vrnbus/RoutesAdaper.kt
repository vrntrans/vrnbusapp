package ru.boomik.vrnbus

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ru.boomik.vrnbus.objects.Bus
import java.util.*

class RoutesAdapter(context: Context, BussList: List<Bus>) : BaseAdapter() {

    var busesList: List<Bus> = BussList
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


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

        vh.tvTitle.text = busesList[position].route


        val time = busesList[position].timeLeft.toInt()
        val timeToArrival = busesList[position].timeToArrival

        (vh.tvTitle.tag as? ValueAnimator)?.cancel()

        if (time >= 1000) {
            vh.tvContent.text = ""
            vh.tvContent.tag = null
        } else {
            vh.tvContent.tag = timeToArrival
            UpdateTimeLeft(vh.tvContent)
            val anim = ValueAnimator.ofInt(30)
            anim.addUpdateListener { UpdateTimeLeft(vh.tvContent) }
            anim.duration = 30000
            anim.start()
            vh.tvTitle.tag = anim
        }
        return view
    }

    private fun UpdateTimeLeft(tvContent: TextView) {

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
}

private class ViewHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.title) as TextView
    val tvContent: TextView = view?.findViewById(R.id.time) as TextView
}