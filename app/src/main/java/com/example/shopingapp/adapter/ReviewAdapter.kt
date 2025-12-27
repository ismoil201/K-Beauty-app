package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopingapp.databinding.ItemReviewBinding
import com.example.shopingapp.model.ReviewResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.VH>() {

    private val list = mutableListOf<ReviewResponse>()

    fun submit(data: List<ReviewResponse>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemReviewBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val r = list[pos]
        val b = h.b

        // 👤 USER
        b.tvUser.text = r.userName

        // ⭐ RATING
        b.ratingBar.rating = r.rating.toFloat()

        // 📝 CONTENT
        b.tvContent.text = r.content

        // 📅 DATE (2025-12-26)
        b.tvDate.text = formatDate(r.createdAt)
    }

    override fun getItemCount() = list.size

    private fun formatDate(date: String): String {
        return try {
            val dt = LocalDateTime.parse(date)
            dt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            date.take(10) // fallback
        }
    }
}
