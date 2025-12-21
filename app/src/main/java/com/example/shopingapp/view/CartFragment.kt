package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopingapp.R
import com.example.shopingapp.adapter.OrderProductAdapter
import com.example.shopingapp.adapter.ShimmerAdapter
import com.example.shopingapp.databinding.FragmentCartBinding
import com.example.shopingapp.model.CartItem
import com.example.shopingapp.model.Product
import com.example.shopingapp.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment(),
    OrderProductAdapter.CartActionListener {

    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: OrderProductAdapter
    private lateinit var shimmerAdapter: ShimmerAdapter
    private lateinit var sessionManager: SessionManager

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isBottomBarVisible = true

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

        setupShimmer()
        setupRecycler()
        setupListeners()
        setupScrollBehavior()

        showLoading()
        loadCart()

        binding.btnGoShopping.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    // ================= SETUP =================
    private fun setupShimmer() {
        shimmerAdapter = ShimmerAdapter()
        binding.rvShimmer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShimmer.adapter = shimmerAdapter
    }

    private fun setupRecycler() {
        adapter = OrderProductAdapter(mutableListOf(),
            listener = this,
            onProductClick = { product ->
                findNavController().navigate(
                    R.id.action_orderFragment_to_detailFragment,
                    Bundle().apply { putLong("productId", product.id) }
                )
            },
            onProductDelete = { product ->
                deleteCartItem(product)
            }
        )
        binding.rvOrderProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrderProducts.adapter = adapter
    }

    private fun deleteCartItem(product: Product) {

        val cartItem = adapter.getItemByProductId(product.id) ?: return

        RetrofitClient.instance(requireContext())
            .deleteCartItem(cartItem.id)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        adapter.removeItem(cartItem)

                        Toast.makeText(context, "O‘chirildi", Toast.LENGTH_SHORT).show()

                        updateSelectCount()
                        calculatePrices()

                        if (adapter.itemCount == 0) {
                            showEmpty()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("CART", "delete error", t)
                }
            })
    }

    private fun setupListeners() {
        binding.cbSelectAll.setOnCheckedChangeListener { _, checked ->
            adapter.selectAll(checked)
            updateSelectCount()
            calculatePrices()
        }

        binding.tvDeleteSelected.setOnClickListener {
            deleteSelected()
        }

        binding.btnOrder.setOnClickListener {
            // TODO checkout
        }

    }

    private fun setupScrollBehavior() {
        binding.rvOrderProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 8 && isBottomBarVisible) {
                    hideBottomBar()
                    isBottomBarVisible = false
                } else if (dy < -8 && !isBottomBarVisible) {
                    showBottomBar()
                    isBottomBarVisible = true
                }
            }
        })
    }

    // ================= LOAD CART =================
    private fun loadCart() {

        showLoading()

        RetrofitClient.instance(requireContext())
            .getMyCart()
            .enqueue(object : Callback<List<CartItem>> {

                override fun onResponse(
                    call: Call<List<CartItem>>,
                    response: Response<List<CartItem>>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val items = response.body()!!

                        if (items.isEmpty()) {
                            showEmpty()
                        } else {
                            showCart(items)
                        }

                    } else {
                        showEmpty()
                    }
                }

                override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                    Log.e("CART", "load error", t)
                    showEmpty()
                }
            })
    }

    private fun showCart(items: List<CartItem>) {

        binding.rvShimmer.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.rvOrderProducts.visibility = View.VISIBLE
        binding.bottomBar.visibility = View.VISIBLE

        binding.cbSelectAll.visibility = View.VISIBLE
        binding.tvDeleteSelected.visibility = View.VISIBLE
        binding.tvSellerTitle.visibility = View.VISIBLE

        adapter.submitData(items)
        updateSelectCount()
        calculatePrices()
    }

    private fun showEmpty() {

        binding.rvShimmer.visibility = View.GONE
        binding.rvOrderProducts.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.bottomBar.visibility = View.GONE

        binding.cbSelectAll.visibility = View.GONE
        binding.tvDeleteSelected.visibility = View.GONE
        binding.tvSellerTitle.visibility = View.GONE

        binding.tvTotalPrice.text = "$0.00"
        binding.btnOrder.text = "Buyurtma berish"
    }

    private fun showLoading() {

        binding.rvShimmer.visibility = View.VISIBLE
        binding.rvOrderProducts.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.bottomBar.visibility = View.GONE

        binding.tvTotalPrice.text = "$0.00"
        binding.btnOrder.text = "Buyurtma berish"
    }

    // ================= PRICE =================
    private fun calculatePrices() {

        val selectedItems = adapter.getSelectedItems()

        var totalPrice = 0.0
        var discountTotal = 0.0

        selectedItems.forEach { item ->

            val price = item.product.price
            val discountPrice = item.product.discountPrice

            totalPrice += price * item.quantity

            if (
                discountPrice != null &&
                discountPrice > 0 &&
                discountPrice < price
            ) {
                discountTotal += (price - discountPrice) * item.quantity
            }
        }

        val deliveryFee = 0.0
        val finalPrice = totalPrice - discountTotal + deliveryFee

        fun money(v: Double) = "$" + String.format("%.2f", v)

        binding.tvTotalPrice.text = money(finalPrice)
        binding.btnOrder.text = "Buyurtma • ${money(finalPrice)}"
    }

    // ================= SELECTION =================
    private fun updateSelectCount() {
        val selected = adapter.getSelectedItems().size
        val total = adapter.itemCount
        binding.cbSelectAll.text = "Barchasini tanlash ($selected/$total)"
        binding.cbSelectAll.isChecked = total > 0 && selected == total
    }

    // ================= QUANTITY =================
    override fun onQuantityChanged(item: CartItem) {

        calculatePrices()

        updateRunnable?.let { handler.removeCallbacks(it) }

        updateRunnable = Runnable {
            RetrofitClient.instance(requireContext())
                .updateCartQuantity(item.id, item.quantity)
                .enqueue(object : Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.d("CART", "quantity updated")
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("CART", "quantity error", t)
                    }
                })
        }

        handler.postDelayed(updateRunnable!!, 500)
    }

    // ================= DELETE =================
    private fun deleteSelected() {
        adapter.getSelectedItems().toList().forEach { item ->
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

                        if (adapter.itemCount == 0) {
                            showEmpty()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                })
        }
    }

    override fun onSelectionChanged() {
        updateSelectCount()
        calculatePrices()
    }

    // ================= BOTTOM BAR =================
    private fun hideBottomBar() {
        binding.bottomBar.animate()
            .translationY(binding.bottomBar.height.toFloat())
            .setDuration(180)
            .start()
    }

    private fun showBottomBar() {
        binding.bottomBar.animate()
            .translationY(0f)
            .setDuration(180)
            .start()
    }
}
