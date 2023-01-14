package com.example.humaranagar.ui.signup.signup_or_login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.humaranagar.databinding.ItemWelcomeBannerBinding
import com.example.humaranagar.ui.signup.signup_or_login.model.WelcomeBannerModel

class WelcomeBannerAdapter : RecyclerView.Adapter<WelcomeBannerAdapter.WelcomeBannerViewHolder>() {
    private var list: List<WelcomeBannerModel> = listOf()

    fun setData(list: List<WelcomeBannerModel>) {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WelcomeBannerViewHolder {
        return WelcomeBannerViewHolder(
            ItemWelcomeBannerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WelcomeBannerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class WelcomeBannerViewHolder(private val binding: ItemWelcomeBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WelcomeBannerModel) {
            binding.run {
                ivBanner.setImageResource(item.drawable)
                tvTitle.text = item.title
            }
        }
    }
}