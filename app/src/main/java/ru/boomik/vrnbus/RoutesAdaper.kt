package ru.boomik.vrnbus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ru.boomik.vrnbus.objects.Bus
import java.util.*

class RoutesAdapter(context: Context, BussList: List<Bus>) : BaseAdapter() {

    private var BusesList : List<Bus> = BussList
    private var context: Context? = context
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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

        vh.tvTitle.text = BusesList[position].route

        val time = BusesList[position].timeLeft
        val timeString: String
        if (time == Double.MAX_VALUE) timeString = ""
        else {
            val cal = Calendar.getInstance()
            cal.clear()
            cal.add(Calendar.SECOND, (time * 60).toInt())
            val min = cal.get(Calendar.MINUTE)
            val sec = cal.get(Calendar.SECOND)
            timeString = if (min > 10) "$min мин."
            else if (min < 1 && sec < 30) "Прибывает"
            else if (min < 1) "$sec сек."
            else if (min >= 1 && sec==0) "$min мин."
            else if (sec<10) "$min мин. 0$sec сек."
            else "$min мин. $sec сек."
        }
        vh.tvContent.text = timeString


        return view
    }

    override fun getItem(position: Int): Any {
        return BusesList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return BusesList.size
    }
}

private class ViewHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.title) as TextView
    val tvContent: TextView = view?.findViewById(R.id.time) as TextView
}