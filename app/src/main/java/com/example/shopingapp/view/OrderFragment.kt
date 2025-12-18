package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopingapp.R
import com.example.shopingapp.adapter.OrderProductAdapter
import com.example.shopingapp.databinding.FragmentOrderBinding
import com.example.shopingapp.model.CartItem
import com.example.shopingapp.model.Order
import com.example.shopingapp.network.OrderRequest
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderFragment : Fragment(),
    OrderProductAdapter.CartActionListener {

    private lateinit var adapter: OrderProductAdapter
    private lateinit var binding: FragmentOrderBinding
    private lateinit var sessionManager: SessionManager
    private var cartList = mutableListOf<CartItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler()

        binding.cbSelectAll.setOnCheckedChangeListener { _, checked ->
            adapter.selectAll(checked)
            updateSelectCount()
        }

        binding.tvDeleteSelected.setOnClickListener {
            deleteSelected()
        }
    }

    private fun setupRecycler() {
        adapter = OrderProductAdapter(mutableListOf(), this)
        binding.rvOrderProducts.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvOrderProducts.adapter = adapter
    }

    private fun deleteSelected() {
        val selected = adapter.getSelectedItems().toList()
        selected.forEach { adapter.removeItem(it) }
        updateSelectCount()
    }

    private fun updateSelectCount() {
        val selected = cartList.count { it.isSelected }
        binding.cbSelectAll.text =
            "전체선택 ($selected/${cartList.size})"
    }

    // ✅ MUHIM — interface talab qilgan method
    override fun onQuantityChanged(item: CartItem) {
        // hozircha bo‘sh
    }
    override fun onSelectionChanged() {
        // hozircha bo‘sh
    }
}
