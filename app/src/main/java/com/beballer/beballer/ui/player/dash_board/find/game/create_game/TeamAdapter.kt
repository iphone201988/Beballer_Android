package com.beballer.beballer.ui.player.dash_board.find.game.create_game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.data.model.Player
import com.beballer.beballer.data.model.TeamSlotModel
import com.beballer.beballer.databinding.RvTeamItemBinding
import com.bumptech.glide.Glide

class TeamAdapter(
    private val isHomeTeam: Boolean,
    private val listener: OnInviteClickListener,
    private var isEditable: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<TeamSlotModel>()

    companion object {
        private const val TYPE_PLAYER = 1
        private const val TYPE_INVITE = 2
    }

    interface OnInviteClickListener {
        fun onInviteClick(isHomeTeam: Boolean)
        fun onRemoveClick(player: Player, isHomeTeam: Boolean)
    }

    fun submitList(newList: List<TeamSlotModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


    fun updateEditPermission(isEditable: Boolean) {
        this.isEditable = isEditable
        notifyDataSetChanged()
    }



    override fun getItemViewType(position: Int): Int {
        return if (list[position].player != null) TYPE_PLAYER else TYPE_INVITE
    }

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_PLAYER) {

            val binding = RvTeamItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            PlayerViewHolder(binding)

        } else {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_invite_player, parent, false)

            InviteViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is PlayerViewHolder -> holder.bind(list[position])
            is InviteViewHolder -> holder.bind()
        }
    }

    inner class PlayerViewHolder(
        private val binding: RvTeamItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeamSlotModel) {

            val player = item.player ?: return

            binding.bean = player
            binding.executePendingBindings()

            // ✅ Status Text Logic
            binding.tvStatus.apply {

                if (isHomeTeam && adapterPosition == 0) {
                    text = "Ready"
                }
                else if (player.accepted == true) {
                    text = "Ready"
                }
                else {
                    text = "Invited"
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
            }
            // 👇 Only show cross if editable
            if (!isEditable) {
                binding.ivCross.visibility = View.GONE
            } else {
                if (isHomeTeam && adapterPosition == 0) {
                    binding.ivCross.visibility = View.GONE
                } else {
                    binding.ivCross.visibility = View.VISIBLE
                }
            }

            binding.ivCross.setOnClickListener {
                if (isEditable) {
                    listener.onRemoveClick(player, isHomeTeam)

                }
            }
        }
    }

    inner class InviteViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind() {
            itemView.setOnClickListener {
                listener.onInviteClick(isHomeTeam)
            }
        }
    }
}

