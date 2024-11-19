package com.nest.kanxue.statprocpath

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nest.kanxue.FileStat
import com.nest.kanxue_data.R
import java.text.SimpleDateFormat
import java.util.Locale

// FileStatsAdapter.kt
class FileStatsAdapter : RecyclerView.Adapter<StatsViewHolder>() {
    private var statsList: List<FileStat> = emptyList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun submitList(list: List<FileStat>) {
        statsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(statsList[position])
    }

    override fun getItemCount() = statsList.size



}