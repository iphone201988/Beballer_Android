package com.beballer.beballer.ui.player.dash_board.find.map.cluster


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.beballer.beballer.R
import com.beballer.beballer.utils.BindingUtils.vectorToBitmapDescriptor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CourtClusterRenderer(
    private val context: Context, map: GoogleMap, clusterManager: ClusterManager<CourtClusterItem>
) : DefaultClusterRenderer<CourtClusterItem>(context, map, clusterManager)
{
    var selectedCourtId: String? = null
    private val markerMap = mutableMapOf<String, Marker>()

    override fun onBeforeClusterItemRendered(
        item: CourtClusterItem, markerOptions: MarkerOptions
    ) {
        markerOptions.icon(getIcon(item.court.id)).title(item.court.name)
            .snippet(item.court.address).alpha(1f)
    }

    override fun onClusterItemRendered(item: CourtClusterItem, marker: Marker) {

        marker.tag = item.court.id
        marker.title = item.court.name
        marker.snippet = item.court.address
        markerMap[item.court.id!!] = marker
        animateMarker(marker)


    }

    override fun onClusterItemUpdated(item: CourtClusterItem, marker: Marker) {
        marker.setIcon(getIcon(item.court.id))
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<CourtClusterItem>, markerOptions: MarkerOptions
    ) {
        markerOptions.icon(createClusterIcon(cluster.size))
    }


    override fun onClusterRendered(
        cluster: Cluster<CourtClusterItem>, marker: Marker
    ) {
        marker.setIcon(createClusterIcon(cluster.size))
    }

    @SuppressLint("UseKtx")
    private fun createClusterIcon(clusterSize: Int): BitmapDescriptor {
        val diameter = 100
        val bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.courts_light_color)
        }

        canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, paint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val x = diameter / 2f
        val y = (diameter / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(clusterSize.toString(), x, y, textPaint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    fun selectMarker(courtId: String?) {
        selectedCourtId = courtId
        markerMap.forEach { (id, marker) ->
            marker.setIcon(getIcon(id))
            if (id == courtId) marker.showInfoWindow() else marker.hideInfoWindow()
        }
    }

    fun unselectMarker() {
        selectedCourtId = null
        markerMap.forEach { (_, marker) ->
            marker.setIcon(getIconForMarker(marker))
            marker.hideInfoWindow()
        }
    }

    private fun getIconForMarker(marker: Marker): BitmapDescriptor {
        val courtId = marker.tag as? String
        return getIcon(courtId)
    }


    override fun shouldRenderAsCluster(cluster: Cluster<CourtClusterItem>): Boolean {
        return cluster.size > 1
    }



    private fun getIcon(courtId: String?): BitmapDescriptor {
        return vectorToBitmapDescriptor(
            context,
            if (courtId == selectedCourtId) R.drawable.pinfilledcourt
            else R.drawable.findcourticon,
            if (courtId == selectedCourtId) 50 else 40,
            if (courtId == selectedCourtId) 50 else 40
        )
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
}


//class CourtClusterRenderer(
//    private val context: Context, map: GoogleMap, clusterManager: ClusterManager<CourtClusterItem>
//) : DefaultClusterRenderer<CourtClusterItem>(context, map, clusterManager)
//{
//
//    var selectedCourtId: String? = null
//    private val markerMap = mutableMapOf<String, Marker>()
//
//    override fun onBeforeClusterItemRendered(
//        item: CourtClusterItem, markerOptions: MarkerOptions
//    ) {
//        markerOptions.icon(getIcon(item.court.id)).title(item.court.name)
//            .snippet(item.court.address).alpha(0f)
//    }
//
//    override fun onClusterItemRendered(
//        item: CourtClusterItem, marker: Marker
//    ) {
//        marker.tag = item.court.id
//        markerMap[item.court.id!!] = marker
//        animateMarker(marker)
//    }
//
//    override fun onClusterItemUpdated(item: CourtClusterItem, marker: Marker) {
//        marker.setIcon(getIcon(item.court.id))
//    }
//
//    fun selectMarker(courtId: String?) {
//        selectedCourtId = courtId
//        markerMap.forEach { (id, marker) ->
//            marker.setIcon(getIcon(id))
//            if (id == courtId) marker.showInfoWindow()
//            else marker.hideInfoWindow()
//        }
//    }
//
//    fun getMarker(courtId: String?): Marker? = markerMap[courtId]
//
//    private fun getIcon(courtId: String?): BitmapDescriptor {
//        return vectorToBitmapDescriptor(
//            context,
//            if (courtId == selectedCourtId) R.drawable.pinfilledcourt
//            else R.drawable.findcourticon,
//            if (courtId == selectedCourtId) 50 else 40,
//            if (courtId == selectedCourtId) 50 else 40
//        )
//    }
//
//    private fun animateMarker(marker: Marker) {
//        val start = System.currentTimeMillis()
//        val duration = 400L
//        val handler = Handler(Looper.getMainLooper())
//
//        handler.post(object : Runnable {
//            override fun run() {
//                val t = ((System.currentTimeMillis() - start).toFloat() / duration).coerceAtMost(1f)
//
//                marker.alpha = t
//                marker.setAnchor(0.5f, 1f + (1f - t) * 0.3f)
//
//                if (t < 1f) handler.postDelayed(this, 16)
//            }
//        })
//    }
//}
