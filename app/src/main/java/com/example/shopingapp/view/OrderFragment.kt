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
import com.example.shopingapp.model.OrderRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderFragment : Fragment(), OrderProductAdapter.CartActionListener {

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
        loadCart()
        // ☑️ 전체선택
        binding.cbSelectAll.setOnCheckedChangeListener { _, checked ->
            adapter.selectAll(checked)
            updateSelectCount()
            calculatePrice()
        }

        // 🗑 선택삭제
        binding.tvDeleteSelected.setOnClickListener {
            deleteSelected()
        }
    }
    // 🗑 선택삭제

    private fun deleteSelected() {
        val selected = adapter.getSelectedItems().toList()

        selected.forEach { item ->

            // 🔥 1) UI dan darhol olib tashla
            adapter.removeItem(item)

            // 🔥 2) Backend delete
            RetrofitClient.instance.deleteCartItem(item.id)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(
                        call: Call<Void>,
                        response: Response<Void>
                    ) {}

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        // optional: xatolik bo‘lsa qayta qo‘shish mumkin
                    }
                })
        }

        updateSelectCount()
        calculatePrice()
    }


    private fun setupRecycler() {
        adapter = OrderProductAdapter(mutableListOf(), this)
        binding.rvOrderProducts.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvOrderProducts.adapter = adapter
    }
    private fun updateSelectCount() {
        val selected = cartList.count { it.isSelected }
        binding.cbSelectAll.text =
            "전체선택 ($selected/${cartList.size})"
    }

    // ☑️ change
    override fun onSelectionChanged() {
        updateSelectCount()
        calculatePrice()
    }


    private fun loadCart() {
        RetrofitClient.instance.getUserCart(sessionManager.getUserId())
            .enqueue(object : Callback<List<CartItem>> {

                override fun onResponse(
                    call: Call<List<CartItem>>,
                    response: Response<List<CartItem>>
                ) {
                    Log.d("CART", "server size=${response.body()!!.size}")

                    if (response.isSuccessful && response.body() != null) {
                        cartList.clear()

                        response.body()!!.forEach {
                            it.isSelected = false   // 🔥 MUHIM
                            cartList.add(it)
                        }

                        adapter.submitData(cartList)
                        updateSelectCount()
                        calculatePrice()

                    }
                }

                override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                    toast(t.message ?: "Error")
                }
            })
    }

    private fun calculatePrice() {
        val selected = adapter.getSelectedItems()

        val total = selected.sumOf {
            it.product.price * it.quantity
        }

        binding.itemPriceRow.tvValue.text = "${total}원"
        binding.itemPriceFinally.tvValue.text = "${total}원"
        binding.btnOrder.text = "${total}원 주문하기"
    }

    override fun onQuantityChanged(item: CartItem) {
        calculatePrice()

        RetrofitClient.instance.updateCart(
            cartId = item.id,
            qty = item.quantity
        ).enqueue(object : Callback<CartItem> {
            override fun onResponse(call: Call<CartItem>, response: Response<CartItem>) {}
            override fun onFailure(call: Call<CartItem>, t: Throwable) {}
        })
    }


    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
