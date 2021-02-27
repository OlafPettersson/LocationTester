package org.pettersson.locationtester.ui.main

import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.pettersson.locationtester.BuildConfig
import org.pettersson.locationtester.R
import org.pettersson.locationtester.viewModels.LocationProviderViewModel
import org.pettersson.locationtester.viewModels.LocationProvidersViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A placeholder fragment containing a simple view.
 */
class MapFragment : Fragment() {

    private val viewModel : LocationProvidersViewModel by activityViewModels()

    private var currentList : List<LocationProviderViewModel>? = null
    private var markerIconTemplate : Drawable? = null

    private val markerIcons = mutableMapOf<Int, Drawable>()

    val enabledProvidersObserver = Observer<List<LocationProviderViewModel>> {
        currentList?.forEach { it.locationValue.removeObserver(locationChangedObserver)}
        it.forEach { it.locationValue.observe(viewLifecycleOwner, locationChangedObserver)}
        currentList = it
    }

    val locationChangedObserver = Observer<Location?> {
        val map = this.view?.findViewById<MapView>(R.id.map)
        val locations = viewModel.enabledProviders.value!!
                            .map { Pair(it.locationValue.value, it) }
                            .filter { it.first != null}

        // remove all but the first and second overlay (minimap, scaleBar)
        for(n in (map?.overlayManager?.size  ?: 1) - 1 downTo 2)
            map?.overlayManager?.removeAt(n)

        if(locations.any()){

            val markers = mutableListOf<Marker>()

            for (item in locations.sortedByDescending { it.first!!.accuracy }) {
                val location : Location = item.first!!
                val viewModel = item.second

                val circlePoints: MutableList<GeoPoint> = makeLocationCircle(location)

                if(circlePoints.any {abs(it.latitude) > 85}) {
                    // apparently the map can not display values larger than
                    continue
                }

                var polygon = Polygon()
                polygon.points = circlePoints

                val intColor = viewModel.displayColor.value ?: 0xC0C0C0
                val color = Color.rgb(Color.red(intColor), Color.green(intColor), Color.blue(intColor))

                polygon.fillPaint.color = Color.parseColor("#1EFFE70E")
                polygon.outlinePaint.strokeWidth = 5f
                polygon.outlinePaint.color = color
                map!!.overlayManager?.add(polygon)

                val marker = Marker(map)
                marker.position = GeoPoint(location)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.title = viewModel.displayName.value

                if(markerIcons.containsKey(color)){
                    marker.icon = markerIcons[color]
                }else{
                    marker.icon = markerIconTemplate!!.constantState!!.newDrawable().mutate()
                    marker.icon.alpha = 0xC0
                    marker.icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
                    markerIcons[color] = marker.icon
                }
                markers.add(marker)
            }

            for (marker in markers) {
                map!!.overlayManager.add(marker)
            }

            val bestLocation = locations.minBy { it.first!!.accuracy }?.first!!
            map?.controller?.setCenter(GeoPoint(bestLocation))
            map?.invalidate()
        }
    }

    private fun makeLocationCircle(location: Location): MutableList<GeoPoint> {

        val circlePoints: MutableList<GeoPoint> = ArrayList()
        val iSteps = 180
        val fStepSize = Math.PI * 2 / iSteps;
        var fStepPos = 0.0


        val circleAtLatitudeLengthInMeters = cos(location.latitude / 180 * Math.PI) * 6370000.0 * 2.0 * Math.PI

        // not sure why the *1.5 is required, it does't seem to make sense.
        // but with it the circle diameter matches the shown scale bar of the map.
        val radiusLon = location.accuracy / 40007863.0 * 360.0 * 1.5
        val radiusLat = location.accuracy / circleAtLatitudeLengthInMeters * 180.0 * 1.5

        for (step in 0 until iSteps) {
            circlePoints.add(
                    GeoPoint(location.latitude + radiusLat * sin(fStepPos),
                            location.longitude + radiusLon * cos(fStepPos)
                            ))
            fStepPos += fStepSize
        }
        circlePoints.add(circlePoints[0])
        return circlePoints
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        val map = root.findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
        map.setMultiTouchControls(true)
        map.isTilesScaledToDpi = true // on high DPI devices (i.e. most modern ones)
                                      // this makes readable, but blurry text...
        map.controller.setZoom(16.0)

        val scaleBar = ScaleBarOverlay(map)
        map.overlayManager.add(scaleBar)

        val miniMap = MinimapOverlay(requireContext(), map.handler)
        miniMap.zoomDifference = 6
        map.overlayManager.add(miniMap)

        markerIconTemplate = requireContext().getDrawable(R.drawable.ic_fixed_location)

        viewModel.enabledProviders.observe(viewLifecycleOwner, enabledProvidersObserver)

        return root
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<MapView>(R.id.map)?.onResume()
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<MapView>(R.id.map)?.onPause()
    }
}