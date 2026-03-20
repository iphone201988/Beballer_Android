package com.beballer.beballer.ui.player.dash_board.find.game.invite_player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.databinding.FindGameRvItemBinding
import com.beballer.beballer.databinding.ItemLayoutPlayersBinding
import com.beballer.beballer.databinding.ItemLoadingBinding
import com.beballer.beballer.ui.player.dash_board.find.courts.ViewItem


class InvitePlayerAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<PlayerItem> = mutableListOf()

    private val selectedPlayers = mutableListOf<Player>()
    private var side: String? = null   // "referee" or "players"
    private var maxSelectionCount: Int = 1

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_LOADER = 1
    }

    // ---------------------------------
    // Selection Configuration
    // ---------------------------------

    fun setSelectionType(type: String?) {
        side = type
    }

    fun setMaxSelection(count: Int) {
        maxSelectionCount = count
    }

    fun getSelectedPlayers(): List<Player> = selectedPlayers

    fun clearSelection() {
        selectedPlayers.clear()
        notifyDataSetChanged()
    }

    // ---------------------------------
    // Adapter Basics
    // ---------------------------------

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return when (listItem[position]) {
            is PlayerItem.Post -> TYPE_TEXT
            is PlayerItem.Loader -> TYPE_LOADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_TEXT -> {
                val binding: ItemLayoutPlayersBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.item_layout_players,
                        parent,
                        false
                    )
                TextPostViewHolder(binding)
            }

            TYPE_LOADER -> {
                val binding: ItemLoadingBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.item_loading,
                        parent,
                        false
                    )
                LoaderViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItem[position]) {

            is PlayerItem.Post -> {
                if (holder is TextPostViewHolder) {
                    holder.bind(item.players, position)
                }
            }

            is PlayerItem.Loader -> Unit
        }
    }

    // ---------------------------------
    // List Management
    // ---------------------------------

    fun setList(newList: List<PlayerItem>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    fun addToList(newList: List<PlayerItem>) {
        hideLoader()
        val start = listItem.size
        listItem.addAll(newList)
        notifyItemRangeInserted(start, newList.size)
    }

    fun showLoader() {
        if (listItem.none { it is PlayerItem.Loader }) {
            listItem.add(PlayerItem.Loader)
            notifyItemInserted(listItem.size - 1)
        }
    }

    fun hideLoader() {
        val index = listItem.indexOfFirst { it is PlayerItem.Loader }
        if (index != -1) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getList(): MutableList<PlayerItem> = listItem

    // ---------------------------------
    // ViewHolders
    // ---------------------------------

    class LoaderViewHolder(val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class TextPostViewHolder(
        private val binding: ItemLayoutPlayersBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Player?, position: Int) {

            binding.bean = item
            binding.pos = position

            val isSelected = selectedPlayers.any {
                it._id == item?._id
            }

            // ✅ Highlight Selection
            if (isSelected) {
                binding.clMain.setBackgroundResource(R.drawable.blue_strock_color_bg)
                binding.clMain.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
            } else {
                binding.clMain.setBackgroundResource(R.drawable.bg_blue_btn)
                binding.clMain.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
            }

            binding.clMain.setOnClickListener {

                item ?: return@setOnClickListener

                when (side) {

                    // ✅ SINGLE SELECTION (Referee)
                    "referee" -> {
                        selectedPlayers.clear()
                        selectedPlayers.add(item)
                    }

                    // ✅ MULTIPLE SELECTION (Dynamic)
                    "players" -> {

                        val alreadySelected = selectedPlayers.any {
                            it._id == item._id
                        }

                        if (alreadySelected) {

                            // 🔹 Deselect
                            selectedPlayers.removeAll {
                                it._id == item._id
                            }

                        } else {

                            if (selectedPlayers.size < maxSelectionCount) {

                                selectedPlayers.add(item)

                            } else {

                                Toast.makeText(
                                    binding.root.context,
                                    "You can select only $maxSelectionCount players",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@setOnClickListener
                            }
                        }


                    }

                    "gameDetail" ->{
                        selectedPlayers.clear()
                        selectedPlayers.add(item)
                    }
                    "changeReferee" -> {
                        selectedPlayers.clear()
                        selectedPlayers.add(item)
                    }
                }

                notifyDataSetChanged()
                listener.onItemClick(item, binding.clMain.id, position)
            }

            binding.executePendingBindings()
        }
    }
    // ---------------------------------
    // Click Interface
    // ---------------------------------

    interface OnItemClickListener {
        fun onItemClick(item: Player?, clickedViewId: Int, position: Int)
    }
}




