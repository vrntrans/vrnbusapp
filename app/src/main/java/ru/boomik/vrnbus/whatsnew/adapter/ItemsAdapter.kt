package ru.boomik.vrnbus.whatsnew.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import ru.boomik.vrnbus.R

/**
 * Created by lizhaotailang on 30/11/2017.
 */
class ItemsAdapter(private val mData: Array<WhatsNewItem>, private val mContext: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var titleColor: Int = Color.parseColor("#FF0000")
    var contentColor: Int = Color.parseColor("#808080")

override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    if (position <= mData.size) {
            with(holder as ItemViewHolder) {
                mData[position].imageRes?.let {
                    val drawable =  ContextCompat.getDrawable(mContext, it)
                    drawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(titleColor, BlendModeCompat.SRC_ATOP)
                    title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
                title.compoundDrawablePadding = 16
                title.text = mData[position].title
                title.setTextColor(titleColor)

                content.text = mData[position].content
                content.setTextColor(contentColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.whatsnew_item, parent, false)
        val title = view.findViewById<TextView>(R.id.itemTitleTextView)
        val content = view.findViewById<TextView>(R.id.itemContentTextView)
        return ItemViewHolder(view, title, content)
    }

    override fun getItemCount(): Int = mData.size

    inner class ItemViewHolder(val view: View, val title: TextView, val content: TextView) : RecyclerView.ViewHolder(view)
}