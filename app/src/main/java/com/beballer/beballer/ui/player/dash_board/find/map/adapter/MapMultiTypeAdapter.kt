package com.beballer.beballer.ui.player.dash_board.find.map.adapter

import MapListItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.databinding.RvCampsMapBoundItemBinding
import com.beballer.beballer.databinding.RvCourtMapBoundItemBinding
import com.beballer.beballer.databinding.RvGameMapBoundItemBinding
import com.beballer.beballer.databinding.RvTicketMapBoundItemBinding
import com.beballer.beballer.databinding.RvTournamentsMapBoundItemBinding

class MapMultiTypeAdapter(
    private val onItemClick: (MapListItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = mutableListOf<MapListItem>()
    companion object {
        private const val COURT = 1
        private const val GAME = 2
        private const val TICKET = 3
        private const val TOURNAMENT = 4
        private const val CAMP = 5
    }
    fun submitList(list: List<MapListItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is MapListItem.Court -> COURT
        is MapListItem.Game -> GAME
        is MapListItem.Ticket -> TICKET
        is MapListItem.Tournament -> TOURNAMENT
        is MapListItem.Camp -> CAMP
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            COURT -> CourtVH(RvCourtMapBoundItemBinding.inflate(inflater, parent, false), onItemClick)
            GAME -> GameVH(RvGameMapBoundItemBinding.inflate(inflater, parent, false), onItemClick)
            TICKET -> TicketVH(RvTicketMapBoundItemBinding.inflate(inflater, parent, false), onItemClick)
            TOURNAMENT -> TournamentVH(RvTournamentsMapBoundItemBinding.inflate(inflater, parent, false), onItemClick)
            else -> CampVH(RvCampsMapBoundItemBinding.inflate(inflater, parent, false), onItemClick)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {

            is CourtVH -> holder.bind(item as MapListItem.Court)
            is GameVH -> holder.bind(item as MapListItem.Game)
            is TicketVH -> holder.bind(item as MapListItem.Ticket)
            is TournamentVH -> holder.bind(item as MapListItem.Tournament)
            is CampVH -> holder.bind(item as MapListItem.Camp)
        }
    }
    override fun getItemCount() = items.size
    class CourtVH(
        private val binding: RvCourtMapBoundItemBinding,
        private val onItemClick: (MapListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MapListItem.Court) {
            binding.bean = item.data
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    class GameVH(
        private val binding: RvGameMapBoundItemBinding,
        private val onItemClick: (MapListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MapListItem.Game) {
            binding.bean = item.data
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    class TicketVH(
        private val binding: RvTicketMapBoundItemBinding,
        private val onItemClick: (MapListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MapListItem.Ticket) {
            binding.bean = item.data
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    class TournamentVH(
        private val binding: RvTournamentsMapBoundItemBinding,
        private val onItemClick: (MapListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MapListItem.Tournament) {
            binding.bean = item.data
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    class CampVH(
        private val binding: RvCampsMapBoundItemBinding,
        private val onItemClick: (MapListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MapListItem.Camp) {
            binding.bean = item.data
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    fun getItemAt(position: Int): MapListItem? {
        return items.getOrNull(position)
    }


}