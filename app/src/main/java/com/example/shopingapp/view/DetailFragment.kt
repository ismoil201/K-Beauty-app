package com.example.shopingapp.view

import FavoriteViewModel
import SessionManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.shopingapp.model.SimpleResponse
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

        productId = requireArguments().getLong("productId")
        favoriteVM = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

        // 🔥 DETAIL LIKE BUTTON (PASTKI ❤️)
        binding.btnLike.setOnClickListener {
            toggleFavorite(productId)
        }

        // 🔥 AVVAL VIEWMODEL HOLATINI KO‘RSAT
        updateLikeIcon(favoriteVM.isFavorite(productId))

        // 🔥 FAVORITE OBSERVER (YAGONA MANBA)
        favoriteVM.favorites.observe(viewLifecycleOwner) { map ->
            updateLikeIcon(map[productId] ?: false)
            adapter.updateFavorites(map) // similar list uchun ham
        }

        loadProduct()
        loadSimilar()

        // 🔥 BOTTOM BAR INSETS
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { v, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = bottomInset
            v.layoutParams = params
            insets
        }

        // 🔥 SCROLL LISTENER
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && isBottomBarVisible) {
                hideBottomBar()
                isBottomBarVisible = false
            } else if (scrollY < oldScrollY && !isBottomBarVisible) {
                showBottomBar()
                isBottomBarVisible = true
            }
        }


        binding.btnBuy.setOnClickListener {
            if (!sessionManager.isLoggedIn()) {
                findNavController().navigate(R.id.loginFragment)
                return@setOnClickListener
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

        val request = CartAddRequest(
            productId = productId,
            quantity = 1
        )

        RetrofitClient.instance(requireContext())
            .addToCart(request)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        // 🔥 BACKEND RESPONSE’NI O‘QIMAYMIZ
                        showToast("🛒 Savatga qo‘shildi")

                    } else {
                        showToast("Xatolik: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showToast("Internet xatosi")
                }
            })
    }


    private fun showToast(text: String) {
        android.widget.Toast
            .makeText(requireContext(), text, android.widget.Toast.LENGTH_SHORT)
            .show()
    }



    // ❤️ TOGGLE FAVORITE (BARCHA JOY UCHUN)
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
                    if (response.isSuccessful && response.body() != null) {
                        favoriteVM.setFavorite(
                            productId,
                            response.body()!!.favorite
                        )
                    }
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {}
            })
    }

    // 📦 PRODUCT DETAIL (LIKE STATEGA TEGMAYDI!)
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
                }

                override fun onFailure(call: Call<ProductDetail>, t: Throwable) {
                    Log.e("DETAIL", t.message ?: "error")
                }
            })
    }

    // 🔁 SIMILAR PRODUCTS
    private fun loadSimilar() {
        binding.rvSimilar.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSimilar.adapter = adapter

        binding.rvSimilar.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                resources.getDimensionPixelSize(R.dimen.grid_spacing),
                false
            )
        )

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
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {}
            })
    }

    // ❤️ ICON UPDATE
    private fun updateLikeIcon(isFavorite: Boolean) {
        binding.btnLike.setImageResource(
            if (isFavorite)
                R.drawable.heart_clicked_svg
            else
                R.drawable.heart_svg
        )
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
