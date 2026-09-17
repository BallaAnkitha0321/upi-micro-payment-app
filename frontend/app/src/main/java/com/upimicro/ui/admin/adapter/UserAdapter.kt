package com.upimicro.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.upimicro.data.model.UserResponse
import com.upimicro.databinding.ItemUserBinding

class UserAdapter(
    private var users: List<UserResponse>,
    private val onBlock: (UserResponse) -> Unit,
    private val onUnblock: (UserResponse) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.binding.tvName.text = user.name
        holder.binding.tvPhone.text = user.phone

        holder.binding.btnBlock.setOnClickListener {
            onBlock(user)
        }

        holder.binding.btnUnblock.setOnClickListener {
            onUnblock(user)
        }
    }

    fun updateData(newList: List<UserResponse>) {
        users = newList
        notifyDataSetChanged()
    }
}