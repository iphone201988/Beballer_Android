package com.beballer.beballer.ui.player.dash_board.find.map.cluster

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.beballer.beballer.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CourtInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    private val view: View =
        LayoutInflater.from(context).inflate(R.layout.map_info_window, null)

    override fun getInfoWindow(marker: Marker): View {
        bindData(marker)
        return view
    }

    override fun getInfoContents(marker: Marker): View? = null
    private fun bindData(marker: Marker) {
        val title = view.findViewById<AppCompatTextView>(R.id.tvTitle)
        val address = view.findViewById<AppCompatTextView>(R.id.tvAddress)

        title.text = marker.title
        address.text = marker.snippet
    }
}