package ru.boomik.vrnbus.managers

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.boomik.vrnbus.BuildConfig
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.StationOnMap


class OSMapManager(val activity: Activity) {


    private var mMap: MapView? = null

    private var fusedLocationClient: FusedLocationProviderClient

    init {

        val ctx = activity.applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    fun resume() {
        mMap?.onResume()
    }

    fun pause() {
        mMap?.onPause()
    }

    fun setView(mapview: MapView) {
        mMap = mapview

        mapview.setBuiltInZoomControls(false)
        mapview.setMultiTouchControls(true)

        val mapController = mapview.controller
        mapController.setZoom(Consts.INITIAL_ZOOM_OSM)
        mapController.setCenter(Consts.INITIAL_POSITION_OSM)

        goToMyLocation()
    }

    fun goToMyLocation() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.continueWith {
                val location = it.result
                if (location != null && it.isSuccessful) {
                    mMap?.controller?.setCenter(GeoPoint(location.latitude, location.longitude))
                }
            }
        }
    }

    private lateinit var mLocationOverlay: MyLocationNewOverlay

    fun enableMyLocation() {
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity), mMap)
        mLocationOverlay.enableMyLocation()
        mMap?.overlays?.add(this.mLocationOverlay)
    }

    fun restoreInstanceStateBundle(savedInstanceState: Bundle?) {

    }

    fun getInstanceStateBundle(outState: Bundle?) {

    }

    private lateinit var mAllStations: List<StationOnMap>

    fun showStations(stations: List<StationOnMap>) {
/*
        //val items = mutableListOf<OverlayItem>()
        val items = stations.map { LabelledGeoPoint( it.lat, it.lon, it.getTitle()) }

// wrap them in a theme
        val pt = SimplePointTheme(items, false)



// set some visual options for the overlay
// we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        val opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7f).setIsClickable(true).setCellSize(15)

// create the overlay with the theme
        val sfpo = SimpleFastPointOverlay(pt, opt)

// onClick callback
        sfpo.setOnClickListener { points, point -> Toast.makeText(activity, "You clicked " + (points.get(point!!) as LabelledGeoPoint).label, Toast.LENGTH_SHORT).show() }

// add overlay
        mMap?.overlays?.add(sfpo)
*/




        val items = stations.map { OverlayItem(it.getTitle(), it.getSnippet(), GeoPoint(it.lat, it.lon)) }
        //the overlay
/*
        fun ItemizedOverlayWithFocus(aList: List<Item>, pMarker: Drawable,
                                     pMarkerFocused: Drawable?, pFocusedBackgroundColor: Int,
                                     aOnItemTapListener: ItemizedIconOverlay.OnItemGestureListener<Item>, pContext: Context): ???
*/
        val icon = activity.resources.getDrawable( R.drawable.ic_station, activity.theme)
        val mOverlay = ItemizedOverlayWithFocus<OverlayItem>(items, icon, null, Color.RED,
                object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                        //do something
                        Toast.makeText(activity, "You clicked " + item.title, Toast.LENGTH_SHORT).show()
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                        return false
                    }
                }, activity)

        mOverlay.setFocusItemsOnTap(true)


        mMap?.overlays?.add(mOverlay)



        mAllStations = stations
/*

        // val showBig = mMap.cameraPosition.zoom >= stationVisibleZoomSmall
        // val showSmall = mMap.cameraPosition.zoom >= stationVisibleZoom

        val icon = activity.resources.getDrawable(R.drawable.ic_station, activity.theme)
//        val iconSmall: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_small)

        val newStationMarkers = stations.map {
            val marker = Marker(mMap!!);
            marker.position = GeoPoint(it.lat, it.lon)
            marker.title = it.getTitle()
            marker.icon = icon
            marker.setAnchor(.5f, .5f)
            marker.setVisible(true)
            marker.isFlat=true
            marker.setInfoWindow(null)
            //marker.tag = it
            marker.id= it.id.toString()
            marker
        }
        newStationMarkers.forEach { mMap?.overlays?.add(it) }
        /*
        val newStationSmallMarkers = stations.map {
            val marker = mMap.addMarker(Marker().position(it.getPosition()).title(it.getTitle()).icon(iconSmall).zIndex(0.8f).anchor(.5f, .5f).visible(showSmall))
            marker.tag = it
            marker
        }
        stationVisible = showBig
        stationVisibleSmall = showSmall

        mStationMarkers = newStationMarkers
        mStationMarkersSmall = newStationSmallMarkers*/
*/
    }


}