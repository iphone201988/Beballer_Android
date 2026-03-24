package com.beballer.beballer.ui.player.dash_board.find.tournament

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.data.model.PlaceDetails

class PlaceAdapter(private var places: List<PlaceDetails>, private val onPlaceClicked: (PlaceDetails) -> Unit) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_location, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        val combinedNameAndAddress = "${place.name}, ${place.address}".trim(',').trim()

        holder.placeName.text = combinedNameAndAddress
        holder.itemView.setOnClickListener {
            onPlaceClicked(place)
        }
    }

    override fun getItemCount() = places.size

    fun updatePlaces(newPlaces: List<PlaceDetails>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.tvTitle)
    }
}