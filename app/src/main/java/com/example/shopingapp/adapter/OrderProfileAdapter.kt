package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopingapp.R
import com.example.shopingapp.databinding.ItemOrderBinding
import com.example.shopingapp.databinding.ItemOrderHistoryBinding
import com.example.shopingapp.model.OrderResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class OrderProfileAdapter(
    private val list: List<OrderResponse>,
    private val listener: OrderActionListener
) : RecyclerView.Adapter<OrderProfileAdapter.Vh>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        val order = list[position]
        val b = holder.b

        // Buyurtmadagi birinchi mahsulot (rasm uchun)
        val firstItem = order.items.firstOrNull()

        b.apply {

            // 📷 Mahsulot rasmi
            firstItem?.let {
                Glide.with(holder.itemView.context)
                    .load(it.imageUrl)
                    .into(ivProduct)
            }

            // 📦 Status (koreyscha)
            val statusText = mapStatus(order.status)

            // 📅 Yetkazib berish sanasi (+3 ~ +5 kun)
            val deliveryDateText = formatDeliveryRange(order.createdAt)

            // 🔹 Status + sana
            tvArrivalDate.text = "$statusText · $deliveryDateText"

            // 🎨 Statusga qarab rang berish (Coupang style)
            setStatusColor(tvArrivalDate, order.status)

            // 🔘 Item bosilganda
            root.setOnClickListener {
                listener.onOrderClick(order)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    /**
     * Backenddan kelgan statusni UI uchun chiroyli ko‘rinishga o‘tkazadi
     */
    private fun mapStatus(status: String): String {
        return when (status.uppercase()) {
            "PENDING"   -> "Qabul qilindi"
            "SHIPPED"   -> "Yo‘lda"
            "DELIVERED" -> "Yetkazildi"
            "CANCELLED" -> "Bekor qilindi"

            else -> status
        }
    }

    /**
     * Statusga qarab rang berish (Coupang uslubi)
     */
    private fun setStatusColor(textView: TextView, status: String) {
        val colorRes = when (status.uppercase()) {
            "PENDING" -> R.color.blue_500
            "SHIPPED" -> R.color.green_500
            "DELIVERED" -> R.color.gray_500
            "CANCELLED" -> R.color.red_500
            else -> R.color.gray_500
        }

        textView.setTextColor(
            ContextCompat.getColor(textView.context, colorRes)
        )
    }

    /**
     * Order yaratilgan sanadan kelib chiqib
     * +3 ~ +5 kun oralig‘ida yetkazib berish sanasini chiqaradi
     *
     * Misol:
     * 2025-12-22T10:43:00.43243343
     * → 25/12 ~ 27/12 (shanba)
     */
    private fun formatDeliveryRange(createdAt: String): String {

        // ✅ UNIVERSAL formatter (eng to‘g‘ri)
        val orderDate = LocalDateTime.parse(
            createdAt,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        // Yetkazib berish oralig‘i
        val fromDate = orderDate.plusDays(3)
        val toDate = orderDate.plusDays(5)

        // UI formatlari
        val dayMonthFormatter = DateTimeFormatter.ofPattern("dd/MM")
        val dayNameFormatter =
            DateTimeFormatter.ofPattern("EEEE", Locale("uz"))

        val from = fromDate.format(dayMonthFormatter)
        val to = toDate.format(dayMonthFormatter)
        val dayName = toDate.format(dayNameFormatter)

        return "$from ~ $to ($dayName)"
    }

    inner class Vh(val b: ItemOrderBinding) :
        RecyclerView.ViewHolder(b.root)

    interface OrderActionListener {
        fun onOrderClick(order: OrderResponse)
    }
}
