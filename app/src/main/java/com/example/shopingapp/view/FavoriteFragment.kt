package com.example.shopingapp.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopingapp.R
import com.example.shopingapp.adapter.ProductAdapter
import com.example.shopingapp.adapter.ShimmerAdapter
import com.example.shopingapp.databinding.FragmentFavoriteBinding
import com.example.shopingapp.model.Product
import com.example.shopingapp.network.FavoriteResponse
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var adapter: ProductAdapter

    private lateinit var shimmerAdapter: ShimmerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        loadFavorites()
        setupShimmer()


        binding.btnGoShopping.setOnClickListener {
            findNavController().navigate(
                R.id.homeFragment,
            )
        }

    }

    private fun setupShimmer() {
        shimmerAdapter = ShimmerAdapter()

        binding.rvShimmer.layoutManager =

            GridLayoutManager(requireContext(), 3)

        binding.rvShimmer.adapter = shimmerAdapter
    }

    private fun setupRecycler() {

        binding.rvProducts.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)
        // ↑ marketplace style (2 column)

        adapter = ProductAdapter(
            onClickItem = { productId ->

                val bundle = Bundle().apply {
                    putLong("productId", productId)
                }

                findNavController().navigate(
                    R.id.action_favoriteFragment_to_detailFragment,
                    bundle
                )
            },
            onLikeClick = { product ->
                toggleFavorite(product)
            }
        )

        binding.rvProducts.adapter = adapter
    }

    private fun loadFavorites() {
        showLoading()

        RetrofitClient.instance(requireContext()).getMyFavorites()
            .enqueue(object : Callback<List<Product>> {
                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                    if (response.isSuccessful) {
                        val favorites = response.body() ?: emptyList()

                        if (favorites.isEmpty()) {
                            showEmpty()
                        } else {
                            adapter.submitData(favorites)
                            binding.tvItemsCount.text =
                                "${favorites.size} ta mahsulot"
                            showList()
                        }
                    } else {
                        showEmpty()
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Log.e("FAVORITE", "load error", t)
                    showEmpty()
                }
            })
    }

    private fun toggleFavorite(product: Product) {
        RetrofitClient.instance(requireContext()).toggleFavorite(product.id)
            .enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                    if (response.isSuccessful) {
                        val isFav = response.body()?.favorite ?: false

                        if (!isFav) {
                            // ❌ favorite dan o‘chdi → listdan ham o‘chiramiz
                            loadFavorites()
                        }
                    }
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Log.e("FAVORITE", "toggle error", t)
                }
            })
    }

    // ===== UI STATES =====

    private fun showLoading() {
        binding.rvShimmer.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
    }


    private fun showList() {
        binding.rvShimmer.visibility = View.GONE
        binding.rvProducts.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.rvShimmer.visibility = View.GONE
        binding.rvProducts.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }

}
