package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopingapp.adapter.OrderProductAdapter
import com.example.shopingapp.databinding.FragmentCartBinding
import com.example.shopingapp.model.CartItem
import com.example.shopingapp.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment(),
    OrderProductAdapter.CartActionListener {
    private var isBottomBarVisible = true

    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: OrderProductAdapter
    private lateinit var sessionManager: SessionManager

    // debounce uchun
    private var updateRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler()
        loadCart()

        // Select all
        binding.cbSelectAll.setOnCheckedChangeListener { _, checked ->
            adapter.selectAll(checked)
            updateSelectCount()
            calculatePrices()
        }

        // Delete selected
        binding.tvDeleteSelected.setOnClickListener {
            deleteSelected()
        }

        // Default prices
        binding.itemPriceFinally.tvValue.text = "0원"
        binding.itemPriceDiscoutRow.tvValue.text = "-0원"
        binding.itemProductDelivery.tvValue.text = "0원"

        binding.btnOrder.setOnClickListener {



            // TODO: checkout / order API
        }


        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && isBottomBarVisible) {
                hideBottomBar()
                isBottomBarVisible = false
            } else if (scrollY < oldScrollY && !isBottomBarVisible) {
                showBottomBar()
                isBottomBarVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    // =======================
    // RecyclerView
    // =======================
    private fun setupRecycler() {
        adapter = OrderProductAdapter(mutableListOf(), this)
        binding.rvOrderProducts.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvOrderProducts.adapter = adapter
    }

    // =======================
    // Load cart
    // =======================
    private fun loadCart() {
        RetrofitClient.instance(requireContext())
            .getMyCart()
            .enqueue(object : Callback<List<CartItem>> {

                override fun onResponse(
                    call: Call<List<CartItem>>,
                    response: Response<List<CartItem>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        adapter.submitData(response.body()!!)
                        updateSelectCount()
                        calculatePrices()
                    } else {
                        Log.e("CART", "response code=${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                    Log.e("CART", "load error", t)
                }
            })
    }

    // =======================
    // Delete selected
    // =======================
    private fun deleteSelected() {
        val selectedItems = adapter.getSelectedItems().toList()

        selectedItems.forEach { item ->
            RetrofitClient.instance(requireContext())
                .deleteCartItem(item.id)
                .enqueue(object : Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        adapter.removeItem(item)
                        updateSelectCount()
                        calculatePrices()
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("CART", "delete error", t)
                    }
                })
        }
    }

    // =======================
    // Select count
    // =======================
    private fun updateSelectCount() {
        val selected = adapter.getSelectedItems().size
        val total = adapter.itemCount

        binding.cbSelectAll.text = "전체선택 ($selected/$total)"
        binding.cbSelectAll.isChecked =
            total > 0 && selected == total
    }

    // =======================
    // Quantity + / −
    // =======================
    override fun onQuantityChanged(item: CartItem) {

        // UI darhol yangilansin
        calculatePrices()

        // eski PUT ni bekor qilamiz
        updateRunnable?.let { handler.removeCallbacks(it) }

        updateRunnable = Runnable {
            Log.d(
                "CART_PUT",
                "PUT cartItemId=${item.id}, quantity=${item.quantity}"
            )

            RetrofitClient.instance(requireContext())
                .updateCartQuantity(item.id, item.quantity)
                .enqueue(object : Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.d("CART_PUT", "responseCode=${response.code()}")
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("CART_PUT", "onFailure", t)
                    }
                })
        }

        handler.postDelayed(updateRunnable!!, 500)
    }

    override fun onSelectionChanged() {
        updateSelectCount()
        calculatePrices()
    }

    // =======================
    // PRICE CALCULATION 🔥
    // =======================
    private fun calculatePrices() {

        val selectedItems = adapter.getSelectedItems()

        var totalPrice = 0.0
        var discountTotal = 0.0

        selectedItems.forEach { item ->

            val price = item.product.price
            val discountPrice = item.product.discountPrice

            // 🔹 umumiy narx
            totalPrice += price * item.quantity

            // 🔥 FAAT REAL DISCOUNT BO‘LSA
            if (
                discountPrice != null &&
                discountPrice > 0 &&
                discountPrice < price
            ) {
                discountTotal += (price - discountPrice) * item.quantity
            }
        }

        // 🚚 DELIVERY
        val deliveryFee =
            if (selectedItems.isEmpty()) {
                0.0
            } else if (totalPrice - discountTotal >= 250.0) {
                0.0
            } else {
                5.0
            }

        val finalPrice = totalPrice - discountTotal + deliveryFee

        // 💲 FORMAT
        fun dollar(value: Double): String =
            "$" + String.format("%.2f", value)

        // UI UPDATE

        binding.itemPriceRow.tvValue.text = dollar(finalPrice)
        binding.itemPriceFinally.tvValue.text = dollar(finalPrice)
        binding.itemPriceDiscoutRow.tvValue.text =
            if (discountTotal > 0) "-${dollar(discountTotal)}" else "$0.00"

        binding.itemProductDelivery.tvValue.text = dollar(deliveryFee)
        binding.btnOrder.text = "${dollar(finalPrice)} Order"
    }
    private fun hideBottomBar() {
        binding.bottomBar.animate()
            .translationY(binding.bottomBar.height.toFloat())
            .setDuration(200)
            .start()
    }

    private fun showBottomBar() {
        binding.bottomBar.animate()
            .translationY(0f)
            .setDuration(200)
            .start()
    }


}
