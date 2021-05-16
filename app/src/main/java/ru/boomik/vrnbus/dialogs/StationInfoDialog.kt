package ru.boomik.vrnbus.dialogs

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
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
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.adapters.StationRoutesAdapter
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.dal.businessObjects.BusesOnStationObject
import ru.boomik.vrnbus.dal.businessObjects.RoutesObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.remote.RequestResultWithData
import ru.boomik.vrnbus.dal.remote.RequestStatus
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.managers.FaveManager
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Fave
import ru.boomik.vrnbus.objects.FaveFull
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.calculateDistanceBetweenPoints
import java.text.SimpleDateFormat
import java.util.*
import ru.boomik.vrnbus.utils.addCircleRipple

class StationInfoDialog {
    companion object {
        fun show(activity: Activity, mInsets: WindowInsetsCompat, station: StationOnMap, fave: Fave? = null) {
            StationInfoDialog().show(activity, mInsets, station, fave)
        }
    }

    fun show(activity: Activity, mInsets: WindowInsetsCompat, station: StationOnMap, fave: Fave? = null) {

        first = true
        mFave = fave
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
        val progressIndeterminate: ProgressBar =
            dialogView.findViewById(R.id.progressIndeterminate)
        val favesContainer: LinearLayout = dialogView.findViewById(R.id.faves_container)
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            title,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
        title.text = station.name
        progress.isIndeterminate = false


        val wheelchair: Drawable = ContextCompat.getDrawable(activity, R.drawable.ic_close)!!
        wheelchair.setColorFilter(
            ContextCompat.getColor(activity, R.color.textColor),
            PorterDuff.Mode.SRC_ATOP
        )
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
            Toast.makeText(
                activity,
                "Прибывающие автобусы отобразились на карте",
                Toast.LENGTH_SHORT
            ).show()
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


        mTime = time
        mAdapter = adapter
        mProgress = progress
        mProgressIndeterminate = progressIndeterminate
        activity.runOnUiThread {
            startUpdateStationInfo(
                activity,
                station,
                dialog
            )
        }

        val faveClick = View.OnClickListener { v ->
            Log.e("click1")
            if (v != null) {
                Log.e("click2")
                val faveFull = v.tag as? FaveFull ?: return@OnClickListener
                val fave = faveFull.fave
                if (mFave == fave) {
                    mFave = null
                    v.addCircleRipple()
                } else {
                    mFave = fave
                    v.setBackgroundResource(R.drawable.circle_background)
                }

                Log.e("click3")
                sortRoutes()
            }
        }

        val faves = FaveManager.getAvailableFaveForStation(station.id)
        faves.forEach { fave ->
            AsyncLayoutInflater(activity).inflate(
                R.layout.fave_station_button,
                favesContainer
            ) { view, _, _ ->
                val button = view as ImageButton
                button.tag = fave
                button.setImageResource(fave.icon)
                button.contentDescription = fave.name
                button.layoutParams = LinearLayout.LayoutParams(
                    (52 * activity.resources.displayMetrics.density).toInt(),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                button.setOnClickListener(faveClick)
                if (mFave == fave.fave)
                    button.setBackgroundResource(R.drawable.circle_background)
                favesContainer.addView(button)
            }
        }

    }


    private var mFave: Fave? = null
    private lateinit var mRoutesForList: MutableList<Bus>
    private var first: Boolean = true
    private lateinit var mRoutes: RequestResultWithData<List<RoutesObject>?>
    private lateinit var mStations: RequestResultWithData<List<StationObject>?>
    private lateinit var mStation: StationObject
    private lateinit var mProgressIndeterminate: ProgressBar
    private lateinit var mProgress: ProgressBar
    private lateinit var mAdapter: StationRoutesAdapter
    private lateinit var mTime: TextView
    private var mAutoUpdateRoutes: Boolean = true

    private lateinit var mBuses: List<Bus>

    private fun startUpdateStationInfo(
        activity: Activity,
        stationOnMap: StationOnMap,
        dialog: DialogPlus?
    ) {

        GlobalScope.async(Dispatchers.Main) {
            var first = true
            var ok = true
            val anim = ValueAnimator.ofInt(30)
            anim.addUpdateListener { mProgress.progress = it.animatedValue as Int }
            anim.duration = 30000

            mProgressIndeterminate.visibility = View.GONE

            do {
                try {
                    if (dialog == null || !dialog.isShowing || DataManager.activeStationId != stationOnMap.id) {
                        ok = false
                    } else {
                        mProgressIndeterminate.visibility = View.VISIBLE
                        mProgress.visibility = View.GONE

                        val stations = DataServices.CoddPersistentDataService.stations()
                        val routes = DataServices.CoddPersistentDataService.routes()

                        val station = stations.data?.firstOrNull { s -> s.id == stationOnMap.id }

                        if (stations.data == null || routes.data == null || station == null) {
                            delay(15000)
                            continue
                        }

                        mStation = station
                        mStations = stations
                        mRoutes = routes

                        val stationInfoResult =
                            DataServices.CoddDataService.getBusesByStationId(stationOnMap.id)
                        if (stationInfoResult.status == RequestStatus.Ok && stationInfoResult.data != null) {
                            val stationInfo = stationInfoResult.data!!
                            updateWithStationInfo(station, stationInfo)
                        } else {
                            if (DataManager.activeStationId == mStation.id) DataManager.activeStationId =
                                0
                            Toast.makeText(
                                activity,
                                "По выбранной остановке не найдено маршрутов",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                            anim.cancel()
                            return@async
                        }
                        mProgressIndeterminate.visibility = View.GONE
                        mProgress.visibility = View.VISIBLE
                        anim.start()
                        anim.resume()

                        delay(15000)

                    }
                } catch (e: Exception) {
                    if (first) {
                        Toast.makeText(activity, "Ошибка загрузки данных", Toast.LENGTH_SHORT)
                            .show()
                        dialog?.dismiss()
                        anim.cancel()
                        if (DataManager.activeStationId == stationOnMap.id) DataManager.activeStationId =
                            0
                    }
                    delay(10000)
                    mProgressIndeterminate.visibility = View.GONE
                    mProgress.visibility = View.VISIBLE
                    anim.start()
                    anim.resume()

                }
                first = false
            } while (ok)
        }
    }

    private fun updateWithStationInfo(station: StationObject, stationInfo: BusesOnStationObject) {
        val format1 = SimpleDateFormat("dd MMM yyyy в kk:mm:ss")
        val formatted = format1.format(stationInfo.time.time)

        mTime.text = "Время обновления:\n$formatted"

        stationInfo.id = station.id
        stationInfo.title = station.title

        val possibleRoutes = DataManager.stationRoutes?.filter { it.key == station.id }
            ?.flatMap { it.value }?.map {
                Bus().apply {
                    bus = BusObject().apply {
                        routeId = it.id
                        routeName = it.name
                    }
                }
            }?.toList() ?: listOf()

        val timeInMills = stationInfo.time.timeInMillis
        val calendarNow = Calendar.getInstance()
        val difference = (calendarNow.timeInMillis - timeInMills) / 1000


        val buses = stationInfo.buses.asSequence().map {
            val toStation = mStations.data?.firstOrNull { s -> s.id == station.id }
            if (it.routeName.isNotBlank()) {
                val busRoute = mRoutes.data?.firstOrNull { s -> s.name == it.routeName }
                if (busRoute != null) it.routeId = busRoute.id
            }
            val distance =
                if (toStation != null) calculateDistanceBetweenPoints(
                    station.latitude,
                    station.longitude,
                    it.lastLatitude,
                    it.lastLongitude
                ) else .0

            val bus = Bus()
            bus.bus = it
            bus.timeLeft = it.minutesLeftToBusStop
            bus.distance = distance
            bus.timeDifference =
                (timeInMills - (it.lastTime?.timeInMillis ?: 0)) / 1000
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

        mRoutesForList = routesForList

        sortRoutes()

        if (first) mAutoUpdateRoutes = false
    }

    private fun sortRoutes() {

        val favorites =
            SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
        val sortedRoutes: MutableList<Bus>
        if (favorites != null) {
            val favRoutes =
                mRoutesForList.filter { favorites.contains(it.bus.routeName) && it.arrivalTime != null }
            val noFavRoutes =
                mRoutesForList.filterNot { favorites.contains(it.bus.routeName) && it.arrivalTime != null }
                    .sortedByDescending { it.arrivalTime != null }
            sortedRoutes = mutableListOf()
            sortedRoutes.addAll(favRoutes)
            sortedRoutes.addAll(noFavRoutes)
        } else sortedRoutes = mRoutesForList.toMutableList()

        val fave = mFave
        if (fave != null) {
            val routes = fave.routes

            val favRoutes =
                mRoutesForList.filter { routes.contains(it.bus.routeName) && it.arrivalTime != null }
            val noFavRoutes =
                mRoutesForList.filterNot { routes.contains(it.bus.routeName) && it.arrivalTime != null }
                    .sortedByDescending { it.arrivalTime != null }
            val sortedFaveRoutes = mutableListOf<Bus>()
            sortedFaveRoutes.addAll(favRoutes)
            sortedFaveRoutes.addAll(noFavRoutes)

            sortedRoutes.clear()
            sortedRoutes.addAll(sortedFaveRoutes)
        }

        mAdapter.setRoutes(sortedRoutes)
    }

}
