package com.heallinkapp.ui.medical

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.heallinkapp.databinding.ItemHospitalBinding


class HospitalAdapter(
    private val onItemClick: (Hospital) -> Unit
) : ListAdapter<Hospital, HospitalAdapter.ViewHolder>(HospitalDiffCallback()) {

    inner class ViewHolder(private val binding: ItemHospitalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hospital: Hospital) {
            binding.apply {
                tvHospitalName.text = hospital.name
                tvDistance.text = "${hospital.distance} Km"
                ivHospital.setImageResource(hospital.image)
                btnGo.setOnClickListener { onItemClick(hospital) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHospitalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class HospitalDiffCallback : DiffUtil.ItemCallback<Hospital>() {
    override fun areItemsTheSame(oldItem: Hospital, newItem: Hospital): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Hospital, newItem: Hospital): Boolean {
        return oldItem == newItem
    }
}