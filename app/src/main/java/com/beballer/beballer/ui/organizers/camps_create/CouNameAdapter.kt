package com.beballer.beballer.ui.organizers.camps_create


import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beballer.beballer.R
import com.beballer.beballer.data.model.CreateTournamentModel


class CouNameAdapter(
    private val mContext: Context,
    private val eventCourtsList: ArrayList<CreateTournamentModel>
) : RecyclerView.Adapter<CouNameAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.rv_create_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = eventCourtsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val court = eventCourtsList[position]

        // ✅ Remove old listener first
        holder.courtNameEditText.setTag(null)

        // ✅ Set text safely
        if (holder.courtNameEditText.text.toString() != court.name) {
            holder.courtNameEditText.setText(court.name ?: "")
        }

        holder.courtNumberTV.text =
            mContext.getString(R.string.create_court, (position + 1).toString())

        holder.courtNameEditText.hint =
            mContext.getString(R.string.hint_court, (position + 1).toString())

        // ✅ Create fresh watcher
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    eventCourtsList[pos].name = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        holder.courtNameEditText.addTextChangedListener(watcher)

        // ✅ Save watcher reference
        holder.courtNameEditText.setTag(watcher)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        // ✅ Remove old watcher when view recycled
        val watcher = holder.courtNameEditText.tag as? TextWatcher
        if (watcher != null) {
            holder.courtNameEditText.removeTextChangedListener(watcher)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courtNumberTV: TextView = itemView.findViewById(R.id.tvTournamentCourtName)
        val courtNameEditText: EditText = itemView.findViewById(R.id.etTournamentCourtsName)
    }
}