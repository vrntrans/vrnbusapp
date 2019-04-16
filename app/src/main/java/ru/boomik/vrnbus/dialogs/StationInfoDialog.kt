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
import ru.boomik.vrnbus.managers.DataStorageManager
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap

class StationInfoDialog {
    companion object {
        fun show(activity: Activity, mInsets: WindowInsetsCompat, station: StationOnMap, selected: (String) -> Unit) {

            DataStorageManager.activeStationId = station.id
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
            val progress: ProgressBar = dialogView.findViewById(R.id.progress)
            val progressIndeterminate: ProgressBar = dialogView.findViewById(R.id.progressIndeterminate)
            TextViewCompat.setAutoSizeTextTypeWithDefaults(title, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            title.text = station.name
            progress.isIndeterminate = false


            val wheelchair: Drawable = activity.resources.getDrawable(R.drawable.ic_close, activity.theme)
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
                DataStorageManager.activeStationId = 0
            }
            showBuses.setOnClickListener {
                val buses = mBuses.toMutableList()
/*
                val bus1 = Bus("Big")
                bus1.busType = 4
                bus1.timeLeft = 9.0
                bus1.lowFloor = true
                bus1.lastLat = 51.670138
                bus1.lastLon = 39.154952
                bus1.init()
                val bus2 = Bus("Big")
                bus2.busType = 4
                bus2.timeLeft = 9.0
                bus2.lowFloor = false
                bus2.lastLat = 51.669138
                bus2.lastLon = 39.154952
                bus2.init()
                val bus = Bus("Medium")
                bus.busType = 3
                bus.timeLeft = 3.0
                bus.lastLat = 51.668138
                bus.lastLon = 39.154952
                bus.init()
                val bus3 = Bus("Small")
                bus3.busType = 1
                bus3.timeLeft = 15.0
                bus3.lowFloor = false
                bus3.lastLat = 51.667138
                bus3.lastLon = 39.154952
                bus3.init()
                val bus4 = Bus("Тр. bus")
                bus4.busType = 4
                bus4.timeLeft = 15.0
                bus4.lowFloor = false
                bus4.lastLat = 51.666138
                bus4.lastLon = 39.154952
                bus4.init()

                buses.add(bus)
                buses.add(bus1)
                buses.add(bus2)
                buses.add(bus3)
                buses.add(bus4)*/

                if (buses.isEmpty()) {
                    Toast.makeText(activity, "Нет прибывающих автобусов", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                DataStorageManager.searchStationId = station.id
                Toast.makeText(activity, "Прибывающие автобусы отобразились на карте", Toast.LENGTH_SHORT).show()
                DataBus.sendEvent(DataBus.BusToMap, buses)
                dialog.dismiss()
                DataStorageManager.activeStationId = 0
            }

            val adapter = RoutesAdapter(activity, listOf())
            list.adapter = adapter

            list.setOnItemClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as Bus
                DataBus.sendEvent(DataBus.AddRoutes, item.route)
            }
            list.setOnItemLongClickListener { parent, _, position, _ ->
                val item = parent.adapter.getItem(position) as Bus
                DataBus.sendEvent(DataBus.ResetRoutes, item.route)
                dialog.dismiss()
                DataStorageManager.activeStationId = 0
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

        private fun startUpdateStationInfo(activity: Activity, station: StationOnMap, dialog: DialogPlus?, time: TextView, adapter: RoutesAdapter, progress: ProgressBar, progressIndeterminate: ProgressBar) {

            GlobalScope.async(Dispatchers.Main) {
                var first = true
                var ok = true
                val anim = ValueAnimator.ofInt(30)
                anim.addUpdateListener { progress.progress = it.animatedValue as Int }
                anim.duration = 30000

                progressIndeterminate.visibility = View.GONE

                do {
                    try {
                        if (dialog == null || !dialog.isShowing || DataStorageManager.activeStationId != station.id) {
                            ok = false
                        } else {
                            progressIndeterminate.visibility = View.VISIBLE
                            progress.visibility = View.GONE

                            val stationInfo = DataService.loadArrivalInfoAsync(station.id).await()
                            if (stationInfo != null) {
                                time.text = stationInfo.time

                                mBuses = stationInfo.buses
                                val routes = stationInfo.routes.toMutableList()
/*
                                val bus1 = Bus("Big")
                                bus1.busType = 4
                                bus1.timeLeft = 9.0
                                bus1.lowFloor = true
                                bus1.lastLat = 51.670138
                                bus1.lastLon = 39.154952
                                bus1.init()
                                val bus2 = Bus("Big")
                                bus2.busType = 4
                                bus2.timeLeft = 9.0
                                bus2.lowFloor = false
                                bus2.lastLat = 51.669138
                                bus2.lastLon = 39.154952
                                bus2.init()
                                val bus = Bus("Medium")
                                bus.busType = 3
                                bus.timeLeft = 3.0
                                bus.lastLat = 51.668138
                                bus.lastLon = 39.154952
                                bus.init()
                                val bus3 = Bus("Small")
                                bus3.busType = 1
                                bus3.timeLeft = 15.0
                                bus3.lowFloor = false
                                bus3.lastLat = 51.667138
                                bus3.lastLon = 39.154952
                                bus3.init()
                                val bus4 = Bus("Тр. bus")
                                bus4.busType = 4
                                bus4.timeLeft = 15.0
                                bus4.lowFloor = false
                                bus4.lastLat = 51.666138
                                bus4.lastLon = 39.154952
                                bus4.init()

                                routes.add(bus)
                                routes.add(bus1)
                                routes.add(bus2)
                                routes.add(bus3)
                                routes.add(bus4)*/


                                val favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
                                var sortedRoutes: MutableList<Bus>
                                if (favorites != null) {
                                    val favRoutes = routes.filter { favorites.contains(it.route) && it.arrivalTime != null }
                                    val noFavRoutes = routes.filterNot { favorites.contains(it.route) && it.arrivalTime != null }.sortedByDescending { it.arrivalTime != null }
                                    sortedRoutes = mutableListOf()
                                    sortedRoutes.addAll(favRoutes)
                                    sortedRoutes.addAll(noFavRoutes)
                                } else sortedRoutes = routes.toMutableList()

                                adapter.setRoutes(sortedRoutes)

                                if (first) {
                                    /*
                                    mapManager.clearBusesOnMap()
                                    mapManager.showBusesOnMap(stationInfo.buses)
                                    mRoutes = stationInfo.routes.joinToString(", ") { it.route }*/
                                    mAutoUpdateRoutes = false
                                } else {
                                    /*  val containAll = adapter.dataEquals(mRoutes)
                                      if (containAll) {
                                          mRoutes = stationInfo.routes.joinToString(", ") { it.route }
                                          mapManager.clearBusesOnMap()
                                          mapManager.showBusesOnMap(stationInfo.buses)
                                          mAutoUpdateRoutes = false
                                      }*/
                                }
                            } else {
                                if (DataStorageManager.activeStationId == station.id) DataStorageManager.activeStationId = 0
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

                            if (DataStorageManager.activeStationId == station.id) DataStorageManager.activeStationId = 0
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