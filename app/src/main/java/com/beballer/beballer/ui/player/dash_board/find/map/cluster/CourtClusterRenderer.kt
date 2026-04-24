package com.beballer.beballer.ui.player.dash_board.find.map.cluster

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.beballer.beballer.R
import com.beballer.beballer.ui.interfacess.MapMarkerItem
import com.beballer.beballer.utils.BindingUtils.vectorToBitmapDescriptor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class CourtClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    private val clusterManager: ClusterManager<MapClusterItem>
) : DefaultClusterRenderer<MapClusterItem>(context, map, clusterManager) {

    var selectedItemId: String? = null
    var currentZoom: Float = 0f

    private val markerMap = mutableMapOf<String, Marker>()
    private val mClusterIconGenerator = IconGenerator(context)
    private val clusterTextView: AppCompatTextView = AppCompatTextView(context)

    init {
        clusterTextView.id = com.google.maps.android.R.id.amu_text
        clusterTextView.gravity = Gravity.CENTER

        val density = context.resources.displayMetrics.density
        val size = (40 * density).toInt()
        clusterTextView.layoutParams = ViewGroup.LayoutParams(size, size)

        clusterTextView.setBackgroundResource(R.drawable.cluster_court)
        clusterTextView.setTextAppearance(R.style.ClusterTextAppearance)

        mClusterIconGenerator.setContentView(clusterTextView)
        mClusterIconGenerator.setBackground(null)
    }

    var currentMapType: MapType = MapType.COURT

    override fun onBeforeClusterItemRendered(
        item: MapClusterItem, markerOptions: MarkerOptions
    ) {
        markerOptions.icon(getIcon(item.item)).title(item.item.title).snippet(item.item.address)
    }

    override fun onClusterItemRendered(
        item: MapClusterItem, marker: Marker
    ) {
        marker.tag = item.item.id
        marker.title = item.item.title
        marker.snippet = item.item.address
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

    override fun onBeforeClusterRendered(
        cluster: Cluster<MapClusterItem>, markerOptions: MarkerOptions
    ) {
        val bgRes = when (currentMapType) {
            MapType.GAME -> R.drawable.cluster_game
            else -> R.drawable.cluster_court
        }
        clusterTextView.setBackgroundResource(bgRes)
        val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onClusterRendered(cluster: Cluster<MapClusterItem>, marker: Marker) {
        super.onClusterRendered(cluster, marker)
        updateClusterIcon(cluster, marker)
    }

    override fun onClusterUpdated(cluster: Cluster<MapClusterItem>, marker: Marker) {
        super.onClusterUpdated(cluster, marker)
        updateClusterIcon(cluster, marker)
    }

    private fun updateClusterIcon(cluster: Cluster<MapClusterItem>, marker: Marker) {
        val bgRes = when (currentMapType) {
            MapType.GAME -> R.drawable.cluster_game
            MapType.TOURNAMENT -> R.drawable.cluster_tournament
            MapType.CAMP -> R.drawable.cluster_camps
            else -> R.drawable.cluster_court
        }
        clusterTextView.setBackgroundResource(bgRes)

        val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
    }

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

    fun showMarkerInfoWindow(id: String) {
        // First try the local cache
        markerMap[id]?.showInfoWindow()

        // Then try the cluster manager collection
        clusterManager.markerCollection.markers.forEach { marker ->
            val itemId = marker.tag as? String
            if (itemId == id) {
                marker.showInfoWindow()
            }
        }
    }

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
        return when (currentMapType) {
            MapType.COURT -> if (selected) R.drawable.pinfilledcourt else R.drawable.findcourticon
            MapType.GAME -> if (selected) R.drawable.pin_filled_game else R.drawable.pin_game
            MapType.TICKET -> if (selected) R.drawable.ic_round_polyline_40 else R.drawable.ic_pro_game_24
            MapType.TOURNAMENT -> if (selected) R.drawable.pin_filled_tournament else R.drawable.pin_tournament
            MapType.CAMP -> if (selected) R.drawable.pin_filled_camp else R.drawable.pin_camp
            else -> if (selected) R.drawable.pinfilledcourt else R.drawable.findcourticon
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
