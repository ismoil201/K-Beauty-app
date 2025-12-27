package com.example.shopingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopingapp.databinding.ItemCartProductBinding
import com.example.shopingapp.model.CartItem
import com.example.shopingapp.model.Product

class CartProductAdapter(
    private val list: MutableList<CartItem> = mutableListOf(),
    private val listener: CartActionListener,
    private val onProductClick: (Product) -> Unit,
    private val onProductDelete: (Product) -> Unit

) : RecyclerView.Adapter<CartProductAdapter.VH>() {

    inner class VH(val b: ItemCartProductBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        val b = holder.b

        //b.tv.text = item.product.category
        b.tvProductName.text = item.product.name
        b.tvOption.text = item.product.brand
        b.tvQty.text = item.quantity.toString()
        b.tvFinalPrice.text =
            "${item.product.price * item.quantity} so'm"

        Glide.with(holder.itemView.context)
            .load(item.product.images[position].main)
            .into(b.imgProduct)

        // ✅ CHECKBOX
        b.cbSelect.setOnCheckedChangeListener(null)

        b.cbSelect.isChecked = item.isSelected
        b.cbSelect.setOnCheckedChangeListener { _, checked ->
            item.isSelected = checked
            listener.onSelectionChanged()
        }
        b.imgProduct.setOnClickListener {
            onProductClick(item.product)
        }
        b.tvProductName.setOnClickListener {

            onProductClick(item.product)
        }
        b.tvOption.setOnClickListener {
            onProductClick(item.product)
        }

        b.btnCancel.setOnClickListener {
            b.btnCancel.isEnabled = false
            onProductDelete(item.product)
        }



        // ➕
        b.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(holder.bindingAdapterPosition)
            listener.onQuantityChanged(item)
        }

        // ➖
        b.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(holder.bindingAdapterPosition)
                listener.onQuantityChanged(item)
            }
        }
    }

    fun removeItem(item: CartItem) {
        val index = list.indexOf(item)
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    fun getItemByProductId(productId: Long): CartItem? {
        return list.firstOrNull { it.product.id == productId }
    }


    override fun getItemCount(): Int = list.size

    fun submitData(newList: List<CartItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<CartItem> =
        list.filter { it.isSelected }

    fun selectAll(select: Boolean) {
        list.forEach { it.isSelected = select }
        notifyDataSetChanged()
    }
    interface CartActionListener {
        fun onQuantityChanged(item: CartItem)
        fun onSelectionChanged()
    }


}
