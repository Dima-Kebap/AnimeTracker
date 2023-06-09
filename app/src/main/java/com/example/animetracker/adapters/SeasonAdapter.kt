package com.example.animetracker.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.animetracker.R
import com.example.animetracker.databinding.ListSeasonBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SeasonAdapter(private val listener: OnChipClickListener?) : ListAdapter<String, SeasonAdapter.Holder>(Comparator()) {
    class Holder(view: View, private val listener: OnChipClickListener?) : RecyclerView.ViewHolder(view) {
        val binding = ListSeasonBinding.bind(view)
        private var itemTemp: ChipGroup? = null
        private var lastCheckedChip: Chip? = null

        init {
           binding.chipGroup.setOnCheckedChangeListener {group, checkedId ->
               if (checkedId != (-1)) {
                   val chip: Chip = group.findViewById(checkedId)
                   itemTemp?.let { listener?.onChipClick(chip.text.toString(), binding.year.text.toString()) }
                   lastCheckedChip = chip// Зберігаємо поточний вибраний Chip
               }
               else
                   itemTemp?.let { listener?.onChipClick(lastCheckedChip?.text.toString(), binding.year.text.toString()) }
            }
        }


        @SuppressLint("SetTextI18n")
        fun bind(item: String) = with(binding) {
                year.text = item
            itemTemp = chipGroup
        }
    }

    class Comparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_season, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface OnChipClickListener {
        fun onChipClick(chipText: String, year: String)
    }


}