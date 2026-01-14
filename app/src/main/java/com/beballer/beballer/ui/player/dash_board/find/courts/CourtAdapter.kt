package com.beballer.beballer.ui.player.dash_board.find.courts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.data.model.GetCourtData
import com.beballer.beballer.databinding.ItemLoadingBinding
import com.beballer.beballer.databinding.RecyclerCourtItemBinding


class CourtAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<ViewItem> = mutableListOf()

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_LOADER = 1
    }

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return when (listItem[position]) {
            is ViewItem.Post -> TYPE_TEXT
            is ViewItem.Loader -> TYPE_LOADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_TEXT -> {
                val binding: RecyclerCourtItemBinding = DataBindingUtil.inflate(
                    inflater, R.layout.recycler_court_item, parent, false
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

            is ViewItem.Post -> {
                if (holder is TextPostViewHolder) {
                    holder.bind(item.post, listener, position)
                }
            }

            is ViewItem.Loader -> Unit
        }
    }

    fun setList(newList: List<ViewItem>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    fun addToList(newList: List<ViewItem>) {
        hideLoader()

        val start = listItem.size
        listItem.addAll(newList)
        notifyItemRangeInserted(start, newList.size)
    }

    fun showLoader() {
        if (listItem.none { it is ViewItem.Loader }) {
            listItem.add(ViewItem.Loader)
            notifyItemInserted(listItem.size - 1)
        }
    }

    fun hideLoader() {
        val index = listItem.indexOfFirst { it is ViewItem.Loader }
        if (index != -1) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    fun getList(): MutableList<ViewItem> = listItem


    class LoaderViewHolder(val binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)

    inner class TextPostViewHolder(private val binding: RecyclerCourtItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(
            item: GetCourtData?, listener: OnItemClickListener, position: Int
        ) {
            binding.bean = item
            binding.pos = position

            binding.clMain.setOnClickListener {
                listener.onItemClick(item, binding.clMain.id, position)
            }

            binding.executePendingBindings()
        }

    }

    interface OnItemClickListener {
        fun onItemClick(item: GetCourtData?, clickedViewId: Int, position: Int)
    }
}