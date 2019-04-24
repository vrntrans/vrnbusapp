package ru.boomik.vrnbus.whatsnew.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import kotlinx.android.synthetic.main.whatsnew_item.view.*
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
                with(itemView) {
                    mData[position].imageRes?.let {
                        val drawable =  ContextCompat.getDrawable(mContext, it)
                        drawable?.setColorFilter(titleColor, PorterDuff.Mode.SRC_ATOP)
                        itemTitleTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                    }
                    itemTitleTextView.compoundDrawablePadding = 16
                    itemTitleTextView.text = mData[position].title
                    itemTitleTextView.setTextColor(titleColor)


                    itemContentTextView.text = mData[position].content
                    itemContentTextView.setTextColor(contentColor)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.whatsnew_item, parent, false))
    }

    override fun getItemCount(): Int = mData.size

    inner class ItemViewHolder(mItemView: View) : RecyclerView.ViewHolder(mItemView)
}