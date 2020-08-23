package ru.boomik.vrnbus.dialogs

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.*
import ru.boomik.vrnbus.*
import ru.boomik.vrnbus.adapters.StationRoutesAdapter
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dal.remote.RequestStatus
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap

class StationInfoDialog {
    companion object {
        fun show(activity: Activity, mInsets: WindowInsetsCompat, station: StationOnMap, selected: (String) -> Unit) {

            DataManager.activeStationId = station.id
            val dialogView = View.inflate(activity, R.layout.station_view, null) as LinearLayout
            mBuses = listOf()
            val params = dialogView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin += mInsets.systemWindowInsetBottom

            val time: TextView = dialogView.findViewById(R.id.time)
            val title: TextView = dialogView.findViewById(R.id.title)
            val list: ListView = dialogView.findViewById(R.id.list)
            val close: ImageButton = dialogView.findViewById(R.id.close)
            val favorite: ImageButton = dialogView.findViewById(R.id.favorite)
            val showBuses: ImageButton = dialogView.findViewById(R.id.showBuses)
            val stationImage: ImageView = dialogView.findViewById(R.id.stationImage)
            val progress: ProgressBar = dialogView.findViewById(R.id.station_progress)
            val progressIndeterminate: ProgressBar = dialogView.findViewById(R.id.progressIndeterminate)
            TextViewCompat.setAutoSizeTextTypeWithDefaults(title, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            title.text = station.name
            progress.isIndeterminate = false


            val wheelchair: Drawable = ContextCompat.getDrawable(activity,R.drawable.ic_close)!!
            wheelchair.setColorFilter(ContextCompat.getColor(activity, R.color.textColor), PorterDuff.Mode.SRC_ATOP)
            close.setImageDrawable(wheelchair)

            val dialog = DialogPlus.newDialog(activity)
                    .setGravity(Gravity.BOTTOM)
                    .setCancelable(true)
                    .setContentHolder(ViewHolder(dialogView))
                    .setContentBackgroundResource(android.R.color.transparent)
                    .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .create()
            dialog.show()

            close.setOnClickListener {
                dialog.dismiss()
                DataManager.activeStationId = 0
            }
            showBuses.setOnClickListener {
                val buses = mBuses.toMutableList()

                if (buses.isEmpty()) {
                    Toast.makeText(activity, "Нет прибывающих автобусов", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                DataManager.searchStationId = station.id
                Toast.makeText(activity, "Прибывающие автобусы отобразились на карте", Toast.LENGTH_SHORT).show()
                DataBus.sendEvent(DataBus.BusToMap, buses)
                dialog.dismiss()
                DataManager.activeStationId = 0
            }

            val adapter = StationRoutesAdapter(activity, listOf())
            list.adapter = adapter

            list.setOnItemClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as Bus
                DataBus.sendEvent(DataBus.AddRoutes, item.bus.routeName)
            }
            list.setOnItemLongClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as Bus
                DataBus.sendEvent(DataBus.ResetRoutes, item.bus.routeName)
                dialog.dismiss()
                DataManager.activeStationId = 0
                true
            }


            val favorites = SettingsManager.getIntArray(Consts.SETTINGS_FAVORITE_STATIONS)

            var inFavorite = favorites?.contains(station.id) ?: false
            favorite.setImageResource(if (inFavorite) R.drawable.ic_favorite else R.drawable.ic_no_favorite)
            stationImage.setImageResource(if (inFavorite) R.drawable.ic_station_favorite else R.drawable.ic_station)

            favorite.setOnClickListener {
                inFavorite = !inFavorite
                DataBus.sendEvent(DataBus.FavoriteStation, Pair(station.id, inFavorite))
                favorite.setImageResource(if (inFavorite) R.drawable.ic_favorite else R.drawable.ic_no_favorite)
                stationImage.setImageResource(if (inFavorite) R.drawable.ic_station_favorite else R.drawable.ic_station)
            }


            activity.runOnUiThread {
                startUpdateStationInfo(activity, station, dialog, time, adapter, progress, progressIndeterminate)
            }
        }


        private var mAutoUpdateRoutes: Boolean = true

        private lateinit var mBuses: List<Bus>

        private fun startUpdateStationInfo(activity: Activity, station: StationOnMap, dialog: DialogPlus?, time: TextView, adapter: StationRoutesAdapter, progress: ProgressBar, progressIndeterminate: ProgressBar) {

            GlobalScope.async(Dispatchers.Main) {
                var first = true
                var ok = true
                val anim = ValueAnimator.ofInt(30)
                anim.addUpdateListener { progress.progress = it.animatedValue as Int }
                anim.duration = 30000

                progressIndeterminate.visibility = View.GONE

                do {
                    try {
                        if (dialog == null || !dialog.isShowing || DataManager.activeStationId != station.id) {
                            ok = false
                        } else {
                            progressIndeterminate.visibility = View.VISIBLE
                            progress.visibility = View.GONE

                            val stationInfoResult = DataServices.CoddDataService.getBusesByStationId(station.id)
                            if (stationInfoResult.status == RequestStatus.Ok && stationInfoResult.data!=null) {
                                val stationInfo = stationInfoResult.data
                                time.text = stationInfo.time

                                mBuses = stationInfo.buses
                                val routes = stationInfo.routes.toMutableList()
                                val favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
                                var sortedRoutes: MutableList<Bus>
                                if (favorites != null) {
                                    val favRoutes = routes.filter { favorites.contains(it.bus.routeName) && it.arrivalTime != null }
                                    val noFavRoutes = routes.filterNot { favorites.contains(it.bus.routeName) && it.arrivalTime != null }.sortedByDescending { it.arrivalTime != null }
                                    sortedRoutes = mutableListOf()
                                    sortedRoutes.addAll(favRoutes)
                                    sortedRoutes.addAll(noFavRoutes)
                                } else sortedRoutes = routes.toMutableList()

                                adapter.setRoutes(sortedRoutes)
                                if (first) mAutoUpdateRoutes = false
                            } else {
                                if (DataManager.activeStationId == station.id) DataManager.activeStationId = 0
                                    Toast.makeText(activity, "По выбранной остановке не найдено маршрутов", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                    anim.cancel()

                            }
                                progressIndeterminate.visibility = View.GONE
                                progress.visibility = View.VISIBLE
                                anim.start()
                                anim.resume()

                                delay(30000)

                        }
                    } catch (e: Exception) {
                        if (first) {
                                Toast.makeText(activity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                                dialog?.dismiss()
                                anim.cancel()

                            if (DataManager.activeStationId == station.id) DataManager.activeStationId = 0
                        }
                        delay(10000)
                            progressIndeterminate.visibility = View.GONE
                            progress.visibility = View.VISIBLE
                            anim.start()
                            anim.resume()

                    }
                    first = false
                } while (ok)
            }
        }
    }
}