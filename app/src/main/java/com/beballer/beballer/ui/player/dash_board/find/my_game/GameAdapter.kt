package com.beballer.beballer.ui.player.dash_board.find.my_game

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.data.model.MyGame
import com.beballer.beballer.databinding.ItemLoadingBinding
import com.beballer.beballer.databinding.MyGameRvItemBinding


class GameAdapter(
    private val userId: String, private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<GameViewItem> = mutableListOf()

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_LOADER = 1
    }

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return when (listItem[position]) {
            is GameViewItem.Post -> TYPE_TEXT
            is GameViewItem.Loader -> TYPE_LOADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_TEXT -> {
                val binding: MyGameRvItemBinding = DataBindingUtil.inflate(
                    inflater, R.layout.my_game_rv_item, parent, false
                )
                TextPostViewHolder(binding)
            }

            TYPE_LOADER -> {
                val binding: ItemLoadingBinding = DataBindingUtil.inflate(
                    inflater, R.layout.item_loading, parent, false
                )
                LoaderViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItem[position]) {

            is GameViewItem.Post -> {
                if (holder is TextPostViewHolder) {
                    holder.bind(item.post, listener, position)
                }
            }

            is GameViewItem.Loader -> Unit
        }
    }

    fun setList(newList: List<GameViewItem>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    fun addToList(newList: List<GameViewItem>) {
        hideLoader()
        val start = listItem.size
        listItem.addAll(newList)
        notifyItemRangeInserted(start, newList.size)
    }

    fun showLoader() {
        if (listItem.none { it is GameViewItem.Loader }) {
            listItem.add(GameViewItem.Loader)
            notifyItemInserted(listItem.size - 1)
        }
    }

    fun hideLoader() {
        val index = listItem.indexOfFirst { it is GameViewItem.Loader }
        if (index != -1) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getList(): MutableList<GameViewItem> = listItem

    class LoaderViewHolder(val binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)

    inner class TextPostViewHolder(
        private val binding: MyGameRvItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MyGame?, listener: OnItemClickListener, position: Int
        ) {

            binding.bean = item
            binding.userId = userId
            binding.pos = position

            binding.clMain.setOnClickListener {
                listener.onItemClick(item, binding.clMain.id, position)
            }

            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: MyGame?, clickedViewId: Int, position: Int)
    }
}