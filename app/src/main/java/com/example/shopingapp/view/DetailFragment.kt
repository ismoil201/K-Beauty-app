package com.example.shopingapp.view

import FavoriteViewModel
import SessionManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.shopingapp.GridSpacingItemDecoration
import com.example.shopingapp.R
import com.example.shopingapp.adapter.ProductAdapter
import com.example.shopingapp.databinding.FragmentDetailBinding
import com.example.shopingapp.model.CartAddRequest
import com.example.shopingapp.model.Product
import com.example.shopingapp.model.ProductDetail
import com.example.shopingapp.network.FavoriteResponse
import com.example.shopingapp.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var favoriteVM: FavoriteViewModel
    private lateinit var adapter: ProductAdapter

    private var productId: Long = -1L
    private var isBottomBarVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())
        favoriteVM = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

        adapter = ProductAdapter(
            onClickItem = { id ->
                findNavController().navigate(
                    R.id.action_detailFragment_self,
                    Bundle().apply { putLong("productId", id) }
                )
            },
            onLikeClick = { product ->
                toggleFavorite(product.id)
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getLong("productId") ?: -1L
        if (productId == -1L) {
            showToast("Product topilmadi")
            return
        }

        // 🔲 SHIMMER DASTLAB
        showDetailLoading()
        showSimilarLoading()

        binding.btnLike.setOnClickListener { toggleFavorite(productId) }

        favoriteVM.favorites.observe(viewLifecycleOwner) { map ->
            updateLikeIcon(map[productId] ?: false)
            adapter.updateFavorites(map)
        }

        setupBottomBar()
        setupScroll()
        setupButtons()

        loadProduct()
        loadSimilar()
    }

    // ================= 🔲 SHIMMER =================
    private fun showDetailLoading() {
        binding.shimmerDetailItem.root.visibility = View.VISIBLE
        binding.layoutDetailContent.visibility = View.GONE
    }

    private fun showDetailContent() {
        binding.shimmerDetailItem.root.visibility = View.GONE
        binding.layoutDetailContent.visibility = View.VISIBLE
    }

    private fun showSimilarLoading() {
        binding.shimmerSimilar.visibility = View.VISIBLE
        binding.rvSimilar.visibility = View.GONE
    }

    private fun showSimilarContent() {
        binding.shimmerSimilar.visibility = View.GONE
        binding.rvSimilar.visibility = View.VISIBLE
    }

    // ================= 📦 PRODUCT DETAIL =================
    private fun loadProduct() {
        RetrofitClient.instance(requireContext())
            .getProductDetail(productId)
            .enqueue(object : Callback<ProductDetail> {

                override fun onResponse(
                    call: Call<ProductDetail>,
                    response: Response<ProductDetail>
                ) {
                    val p = response.body() ?: return

                    binding.tvBrand.text = p.category
                    binding.tvName.text = p.name
                    binding.tvDescription.text = p.description
                    binding.tvPrice.text = "${p.price}$"

                    Glide.with(requireContext())
                        .load(p.imageUrl)
                        .into(binding.ivProduct)

                    showDetailContent() // 🔥 SHIMMER O‘CHDI
                }

                override fun onFailure(call: Call<ProductDetail>, t: Throwable) {
                    showDetailContent()
                    Log.e("DETAIL", "load error", t)
                }
            })
    }

    // ================= 🔁 SIMILAR =================
    private fun loadSimilar() {
        binding.rvSimilar.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSimilar.adapter = adapter

        RetrofitClient.instance(requireContext())
            .getAllProducts()
            .enqueue(object : Callback<List<Product>> {

                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                    response.body()?.let {
                        adapter.submitData(it)
                        favoriteVM.favorites.value?.let { fav ->
                            adapter.updateFavorites(fav)
                        }
                        showSimilarContent() // 🔥 SHIMMER O‘CHDI
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    showSimilarContent()
                }
            })
    }

    // ================= ❤️ FAVORITE =================
    private fun toggleFavorite(productId: Long) {
        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        RetrofitClient.instance(requireContext())
            .toggleFavorite(productId)
            .enqueue(object : Callback<FavoriteResponse> {

                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                    response.body()?.let {
                        favoriteVM.setFavorite(productId, it.favorite)
                    }
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Log.e("FAVORITE", "toggle error", t)
                }
            })
    }

    // ================= 🛒 BUTTONS =================
    private fun setupButtons() {
        binding.btnBuy.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        binding.btnAddToCart.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                findNavController().navigate(R.id.loginFragment)
                return@setOnClickListener
            }
            addToCart()
        }
    }

    private fun addToCart() {
        RetrofitClient.instance(requireContext())
            .addToCart(CartAddRequest(productId, 1))
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    showToast("🛒 Savatchaga qo‘shildi")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showToast("Internet xatosi")
                }
            })
    }

    // ================= UI =================
    private fun updateLikeIcon(isFavorite: Boolean) {
        binding.btnLike.setImageResource(
            if (isFavorite)
                R.drawable.heart_clicked_svg
            else
                R.drawable.heart_svg
        )
    }

    private fun setupBottomBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { v, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = bottom
            insets
        }
    }

    private fun setupScroll() {
        binding.scrollView.setOnScrollChangeListener { _, _, y, _, oldY ->
            if (y > oldY && isBottomBarVisible) {
                hideBottomBar(); isBottomBarVisible = false
            } else if (y < oldY && !isBottomBarVisible) {
                showBottomBar(); isBottomBarVisible = true
            }
        }
    }

    private fun hideBottomBar() {
        binding.bottomBar.animate().translationY(binding.bottomBar.height.toFloat()).setDuration(200).start()
    }

    private fun showBottomBar() {
        binding.bottomBar.animate().translationY(0f).setDuration(200).start()
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
