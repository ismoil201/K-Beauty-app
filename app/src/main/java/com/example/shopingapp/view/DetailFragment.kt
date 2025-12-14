package com.example.shopingapp.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.shopingapp.GridSpacingItemDecoration
import com.example.shopingapp.R
import com.example.shopingapp.adapter.ProductAdapter
import com.example.shopingapp.adapter.onClickItem
import com.example.shopingapp.databinding.FragmentDetailBinding
import com.example.shopingapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding
    private var productId: Int = -1
    private var isBottomBarVisible = true


    // SIMILAR PRODUCTS ADAPTER (with click)
    private val adapter = ProductAdapter(onClickItem = object : onClickItem {
        override fun onClick(id: Int?) {
            id?.let {
                val bundle = Bundle()
                bundle.putInt("productId", it)
                findNavController().navigate(R.id.detailFragment, bundle)
            }
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getInt("productId") ?: -1

        loadProductById(productId)
        loadSimilarProducts()

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { v, insets ->
            val bottomInset =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = bottomInset
            v.layoutParams = params

            insets
        }
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->

            // pastga scroll
            if (scrollY > oldScrollY && isBottomBarVisible) {
                hideBottomBar()
                isBottomBarVisible = false
            }

            // tepaga scroll
            else if (scrollY < oldScrollY && !isBottomBarVisible) {
                showBottomBar()
                isBottomBarVisible = true
            }
        }
        binding.btnAddToCart.setOnClickListener {
            // TODO: cart logic
            Toast.makeText(requireContext(), "장바구니에 담았습니다", Toast.LENGTH_SHORT).show()
        }

        binding.btnBuy.setOnClickListener {
            // TODO: checkout
            Toast.makeText(requireContext(), "구매 진행", Toast.LENGTH_SHORT).show()
        }

        binding.btnLike.setOnClickListener {
            it.isSelected = !it.isSelected
            binding.btnLike.setImageResource(
                if (it.isSelected) R.drawable.heart_clicked_svg
                else R.drawable.heart_svg
            )
        }



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


    private fun loadProductById(id: Int) {
        RetrofitClient.instance.getProductById(id).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val p = response.body()!!

                    binding.tvName.text = p.name
                    binding.tvDescription.text = p.description
                    binding.tvPrice.text = "${p.price}$"

                    Glide.with(requireContext())
                        .load(p.imageUrl)
                        .into(binding.ivProduct)
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d("DETAIL", "Error: ${t.message}")
            }
        })
    }

    private fun loadSimilarProducts() {

        binding.rvSimilar.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSimilar.adapter = adapter
        binding.rvSimilar.setPadding(
            resources.getDimensionPixelSize(R.dimen.grid_side_padding),
            0,
            resources.getDimensionPixelSize(R.dimen.grid_side_padding),
            0
        )

        binding.rvSimilar.clipToPadding = false

        binding.rvSimilar.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 3,
                spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
                includeEdge = false
            )
        )
        RetrofitClient.instance.getAllProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    adapter.submitData(response.body()!!)
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.d("DETAIL", "Error: ${t.message}")
            }
        })
    }
}
