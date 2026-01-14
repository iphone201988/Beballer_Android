package com.beballer.beballer.ui.player.dash_board.find.map.cluster

import com.beballer.beballer.data.model.MapCourt
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class CourtClusterItem(
    val court: MapCourt
) : ClusterItem {

    override fun getPosition(): LatLng =
        LatLng(court.lat!!, court.long!!)

    override fun getTitle(): String = court.name ?: ""

    override fun getSnippet(): String? = court.address
}