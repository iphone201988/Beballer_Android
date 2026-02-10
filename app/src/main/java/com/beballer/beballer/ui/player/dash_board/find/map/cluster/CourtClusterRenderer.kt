package com.beballer.beballer.ui.player.dash_board.find.map.cluster


import MapListItem
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.beballer.beballer.R
import com.beballer.beballer.ui.interfacess.MapMarkerItem
import com.beballer.beballer.utils.BindingUtils.vectorToBitmapDescriptor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CourtClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    private val clusterManager: ClusterManager<MapClusterItem>
) : DefaultClusterRenderer<MapClusterItem>(context, map, clusterManager) {

    var selectedItemId: String? = null
    var currentZoom: Float = 0f

    private val markerMap = mutableMapOf<String, Marker>()

    override fun onBeforeClusterItemRendered(
        item: MapClusterItem, markerOptions: MarkerOptions
    ) {
        markerOptions.icon(getIcon(item.item)).title(item.item.title).snippet(item.item.address)
    }

    override fun onClusterItemRendered(
        item: MapClusterItem, marker: Marker
    ) {
        marker.tag = item.item.id
        marker.title = item.item.title ?: "Unknown"
        marker.snippet = item.item.address ?: "No address"
        markerMap[item.item.id] = marker
        animateMarker(marker)

    }


    override fun onClusterItemUpdated(
        item: MapClusterItem, marker: Marker
    ) {
        marker.setIcon(getIcon(item.item))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<MapClusterItem>): Boolean {
        return cluster.size > 1 && currentZoom < 17f
    }


    /*    private fun getIcon(item: MapMarkerItem): BitmapDescriptor {
            val isSelected = item.id == selectedItemId

            val iconRes = when (item) {
                is MapListItem.Court -> {
                    if (isSelected) R.drawable.pinfilledcourt
                    else R.drawable.findcourticon
                }

                is MapListItem.Game -> {
                    if (isSelected) R.drawable.ic_game_24
                    else R.drawable.game
                }

                is MapListItem.Ticket -> {
                    if (isSelected) R.drawable.ic_round_polyline_40
                    else R.drawable.ic_pro_game_24
                }

                is MapListItem.Tournament -> {
                    if (isSelected) R.drawable.ic_tournament_24
                    else R.drawable.ic_tournament_24
                }

                is MapListItem.Camp -> {
                    if (isSelected) R.drawable.ic_camp_24
                    else R.drawable.ic_camp_24
                }

                else -> R.drawable.findcourticon
            }

            return vectorToBitmapDescriptor(
                context, iconRes, if (isSelected) 50 else 40, if (isSelected) 50 else 40
            )
        }*/

    fun selectMarker(id: String?) {
        selectedItemId = id

        markerMap.forEach { (key, marker) ->
            val item = clusterManager.algorithm.items.firstOrNull { it.item.id == key }?.item
                ?: return@forEach

            if (key == id) {
                marker.showInfoWindow()
                animateIconChange(marker, item)
                marker.zIndex = 10f
            } else {
                marker.hideInfoWindow()
                marker.zIndex = 0f
                marker.setIcon(getIcon(item))
            }

        }
    }

    // Show info window for a specific marker
    fun showMarkerInfoWindow(id: String) {
        // Iterate all markers in the collection
        clusterManager.markerCollection.markers.forEach { marker ->
            val itemId = marker.tag as? String
            if (itemId == id) {
                marker.showInfoWindow() // show the info window
            }
        }
    }

//    fun selectMarker(id: String?) {
//        selectedItemId = id
//
//        markerMap.forEach { (key, marker) ->
//            val item = clusterManager.algorithm.items
//                .firstOrNull { it.item.id == key }
//                ?.item
//                ?: return@forEach
//
//            marker.setIcon(getIcon(item))
//
//            if (key == id) {
//                marker.showInfoWindow()
//            } else {
//                marker.hideInfoWindow()
//            }
//        }
//    }


    fun clearSelection() {
        selectedItemId = null
        markerMap.forEach { (_, marker) ->
            marker.hideInfoWindow()
        }
        clusterManager.cluster()
    }


    private fun animateMarker(marker: Marker) {
        val start = System.currentTimeMillis()
        val duration = 400L
        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {
                val t = ((System.currentTimeMillis() - start).toFloat() / duration).coerceAtMost(1f)
                marker.alpha = t
                marker.setAnchor(0.5f, 1f + (1f - t) * 0.3f)
                if (t < 1f) handler.postDelayed(this, 16)
            }
        })
    }

    private fun animateIconChange(marker: Marker, item: MapMarkerItem) {
        val handler = Handler(Looper.getMainLooper())
        val start = System.currentTimeMillis()
        val duration = 200L

        val startSize = 40
        val endSize = 50

        handler.post(object : Runnable {
            override fun run() {
                val t = ((System.currentTimeMillis() - start).toFloat() / duration).coerceAtMost(1f)

                val size = (startSize + (endSize - startSize) * t).toInt()

                marker.setIcon(
                    vectorToBitmapDescriptor(
                        context, getIconRes(item, true), size, size
                    )
                )

                if (t < 1f) handler.postDelayed(this, 16)
            }
        })
    }


    private fun getIconRes(item: MapMarkerItem, selected: Boolean): Int {
        return when (item) {
            is MapListItem.Court -> if (selected) R.drawable.pinfilledcourt else R.drawable.findcourticon

            is MapListItem.Game -> if (selected) R.drawable.ic_game_24 else R.drawable.game

            is MapListItem.Ticket -> if (selected) R.drawable.ic_round_polyline_40 else R.drawable.ic_pro_game_24

            is MapListItem.Tournament -> R.drawable.ic_tournament_24

            is MapListItem.Camp -> R.drawable.ic_camp_24

            else -> R.drawable.findcourticon
        }
    }

    private fun getIcon(item: MapMarkerItem): BitmapDescriptor {
        val isSelected = item.id == selectedItemId
        val size = if (isSelected) 50 else 40

        return vectorToBitmapDescriptor(
            context, getIconRes(item, isSelected), size, size
        )
    }

}




