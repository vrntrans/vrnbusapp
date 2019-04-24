package ru.boomik.vrnbus.managers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import io.github.tonnyl.whatsnew.WhatsNew
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import io.github.tonnyl.whatsnew.util.PresentationOption
import ru.boomik.vrnbus.R


fun showWhatsNew(activity: AppCompatActivity, insets: WindowInsetsCompat) {
    try {
        var nowVersionCode: Int
        activity.packageManager
                .getPackageInfo(activity.packageName, 0)
                .let {
                    nowVersionCode = it.versionCode
                }
        if (nowVersionCode==12) showWhatsNewFor12(activity, insets)
        if (nowVersionCode==11) showWhatsNewFor11(activity, insets)

    } catch (t: IllegalStateException) {

    }
}

private fun showWhatsNewFor12(activity: AppCompatActivity, insets: WindowInsetsCompat) {
    val whatsNew = WhatsNew.newInstance(
            itemFromRes(R.string.map_title, R.string.map_desc, R.drawable.ic_map, activity),
            itemFromRes(R.string.bus_type_title, R.string.bus_type_desc, R.drawable.ic_bus, activity),
            itemFromRes(R.string.favorite_title, R.string.favorite_desc, R.drawable.ic_no_star, activity),
            itemFromRes(R.string.night_title, R.string.night_desc, R.drawable.ic_night, activity),
            itemFromRes(R.string.zoom_title, R.string.zoom_desc, R.drawable.ic_zoom, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
    with(whatsNew) {
        buttonText = activity.getString(R.string.cont)
        buttonTextColor = ContextCompat.getColor(activity, R.color.background)
        buttonBackground = ContextCompat.getColor(activity, R.color.textColor)
        itemTitleColor = ContextCompat.getColor(activity, R.color.textColor)
        itemContentColor = ContextCompat.getColor(activity, R.color.textColor)
        titleColor = ContextCompat.getColor(activity, R.color.textColor)
        titleText = activity.getString(R.string.whatsnew)
        presentationOption = PresentationOption.IF_NEEDED
        windowInsets = insets
    }
    whatsNew.presentAutomatically(activity)
}
private fun showWhatsNewFor11(activity: AppCompatActivity, insets: WindowInsetsCompat) {
    val whatsNew = WhatsNew.newInstance(
            itemFromRes(R.string.map_title, R.string.map_desc, R.drawable.ic_map, activity),
            itemFromRes(R.string.bus_type_title, R.string.bus_type_desc, R.drawable.ic_bus, activity),
            itemFromRes(R.string.favorite_title, R.string.favorite_desc, R.drawable.ic_no_star, activity),
            itemFromRes(R.string.night_title, R.string.night_desc, R.drawable.ic_night, activity),
            itemFromRes(R.string.zoom_title, R.string.zoom_desc, R.drawable.ic_zoom, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
    with(whatsNew) {
        buttonText = activity.getString(R.string.cont)
        buttonTextColor = ContextCompat.getColor(activity, R.color.background)
        buttonBackground = ContextCompat.getColor(activity, R.color.textColor)
        itemTitleColor = ContextCompat.getColor(activity, R.color.textColor)
        itemContentColor = ContextCompat.getColor(activity, R.color.textColor)
        titleColor = ContextCompat.getColor(activity, R.color.textColor)
        titleText = activity.getString(R.string.whatsnew)
        presentationOption = PresentationOption.IF_NEEDED
        windowInsets = insets
    }
    whatsNew.presentAutomatically(activity)
}

private fun itemFromRes(@StringRes title : Int, @StringRes desc : Int, @DrawableRes image : Int, activity: AppCompatActivity): WhatsNewItem {
   return WhatsNewItem(activity.getString(title), activity.getString(desc), image)
}
