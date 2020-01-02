package ru.boomik.vrnbus.utils

import android.annotation.SuppressLint
import android.view.View
import android.widget.*
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.BusType
@SuppressLint("NewApi")
class BusViewHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.title) as TextView
    val tvContent: TextView = view?.findViewById(R.id.time) as TextView
    val tvAbsoluteTime: TextView = view?.findViewById(R.id.absoluteTime) as TextView
    val btnFavorite: ImageButton = view?.findViewById(R.id.favorite) as ImageButton
    val ivLowFloor: ImageView = view?.findViewById(R.id.low_floor) as ImageView
    val ivBusType: ImageView = view?.findViewById(R.id.bus_type) as ImageView

  init {
     // TextViewCompat.setAutoSizeTextTypeWithDefaults(tvTitle, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
       ivLowFloor.setOnClickListener { Toast.makeText(ivLowFloor.context, R.string.low_floor, Toast.LENGTH_SHORT).show() }
        ivBusType.setOnClickListener {
            val type: BusType? = ivBusType.tag as? BusType ?: return@setOnClickListener
            val stringRes = when (type) {
                BusType.Big -> R.string.big_capacity
                BusType.BigLowFloor -> R.string.big_capacity
                BusType.Medium -> R.string.medium_capacity
                BusType.Small -> R.string.small_capacity
                BusType.Trolleybus -> R.string.trolleybus
                else -> 0
            }
            if (stringRes>0) Toast.makeText(ivLowFloor.context, stringRes, Toast.LENGTH_SHORT).show()
        }
      ivLowFloor.isFocusable = false
      ivBusType.isFocusable = false
    }
}