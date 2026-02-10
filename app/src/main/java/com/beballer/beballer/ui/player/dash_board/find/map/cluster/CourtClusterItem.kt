package com.beballer.beballer.ui.player.dash_board.find.map.cluster

import com.beballer.beballer.ui.interfacess.MapMarkerItem
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MapClusterItem(
    val item: MapMarkerItem
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(item.lat, item.lng)
    override fun getTitle(): String = item.title
    override fun getSnippet(): String? = item.address
}


