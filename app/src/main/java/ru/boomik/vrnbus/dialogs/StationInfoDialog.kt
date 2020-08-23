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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.adapters.StationRoutesAdapter
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.dal.remote.RequestStatus
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.calculateDistanceBetweenPoints
import java.text.SimpleDateFormat
import java.util.*

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


            val wheelchair: Drawable = ContextCompat.getDrawable(activity, R.drawable.ic_close)!!
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

        private fun startUpdateStationInfo(activity: Activity, stationOnMap: StationOnMap, dialog: DialogPlus?, time: TextView, adapter: StationRoutesAdapter, progress: ProgressBar, progressIndeterminate: ProgressBar) {

            GlobalScope.async(Dispatchers.Main) {
                var first = true
                var ok = true
                val anim = ValueAnimator.ofInt(30)
                anim.addUpdateListener { progress.progress = it.animatedValue as Int }
                anim.duration = 30000

                progressIndeterminate.visibility = View.GONE

                do {
                    try {
                        if (dialog == null || !dialog.isShowing || DataManager.activeStationId != stationOnMap.id) {
                            ok = false
                        } else {
                            progressIndeterminate.visibility = View.VISIBLE
                            progress.visibility = View.GONE

                            val stations = DataServices.CoddPersistentDataService.stations()
                            val routes = DataServices.CoddPersistentDataService.routes()

                            val station = stations.data?.firstOrNull { s->s.id == stationOnMap.id }

                            if (stations.data==null || routes.data==null || station==null) return@async

                            val stationInfoResult = DataServices.CoddDataService.getBusesByStationId(stationOnMap.id)
                            if (stationInfoResult.status == RequestStatus.Ok && stationInfoResult.data!=null) {
                                val stationInfo = stationInfoResult.data!!
                                val format1 = SimpleDateFormat("dd.MMM.yyyy в kk.mm.ss")
                                  val formatted = format1.format(stationInfo.time.time)

                                time.text = "Время обновления: $formatted"

                                stationInfo.id = station.id
                                stationInfo.title = station.title



                                val possibleRoutes = stationInfo.routeIds.mapNotNull {
                                    routes.data!!.firstOrNull { rId -> it == rId.id }
                                }.map {
                                    Bus().apply {
                                        bus = BusObject().apply {
                                            routeId = it.id
                                            routeName = it.name
                                        }
                                    }
                                }

                                val timeInMills =  stationInfo.time.timeInMillis
                                val calendarNow = Calendar.getInstance()
                                val difference = (calendarNow.timeInMillis-timeInMills)/1000


                                val buses = stationInfo.buses.asSequence().map {

                                    val toStation  = stations.data?.firstOrNull { s->s.id == it.lastStationId }
                                    val busRoute  = routes.data?.firstOrNull { s->s.id == it.routeId }
                                    if (busRoute!=null) it.routeName = busRoute.name

                                    val distance = if (toStation!=null) calculateDistanceBetweenPoints(station.latitude, station.longitude, it.lastLatitude, it.lastLongitude) else .0

                                    val bus = Bus()
                                    bus.bus = it
                                    bus.timeLeft = it.minutesLeftToBusStop
                                    bus.distance = distance
                                    bus.timeDifference = (timeInMills - (it.lastTime?.timeInMillis ?: 0)) / 1000
                                    bus.localServerTimeDifference = difference
                                    bus.init()
                                    bus
                                }.toMutableList()
                                val routesForList = buses.toMutableList()

                                mBuses = buses

                                possibleRoutes.forEach {
                                    val nextBus = routesForList.firstOrNull { bus -> it.bus.routeId == bus.bus.routeId }
                                    if (nextBus == null) routesForList.add(it)
                                }
                                routesForList.sortBy { it.timeLeft }


                                val favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
                                var sortedRoutes: MutableList<Bus>
                                if (favorites != null) {
                                    val favRoutes = routesForList.filter { favorites.contains(it.bus.routeName) && it.arrivalTime != null }
                                    val noFavRoutes = routesForList.filterNot { favorites.contains(it.bus.routeName) && it.arrivalTime != null }.sortedByDescending { it.arrivalTime != null }
                                    sortedRoutes = mutableListOf()
                                    sortedRoutes.addAll(favRoutes)
                                    sortedRoutes.addAll(noFavRoutes)
                                } else sortedRoutes = routesForList.toMutableList()

                                adapter.setRoutes(sortedRoutes)
                                if (first) mAutoUpdateRoutes = false
                            } else {
                                if (DataManager.activeStationId == stationOnMap.id) DataManager.activeStationId = 0
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

                            if (DataManager.activeStationId == stationOnMap.id) DataManager.activeStationId = 0
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