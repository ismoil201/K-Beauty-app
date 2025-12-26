package com.example.shopingapp.ui.home

import FavoriteViewModel
import SessionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopingapp.GridSpacingItemDecoration
import com.example.shopingapp.R
import com.example.shopingapp.adapter.ProductAdapter
import com.example.shopingapp.adapter.ShimmerAdapter
import com.example.shopingapp.databinding.FragmentHomeBinding
import com.example.shopingapp.model.Product
import com.example.shopingapp.network.FavoriteResponse
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var shimmerAdapter: ShimmerAdapter


    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var favoriteVM: FavoriteViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        adapter = ProductAdapter(
            onClickItem = { productId ->
                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                    Bundle().apply { putLong("productId", productId) }
                )
            },
            onLikeClick = { product ->
                toggleFavorite(product)
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvProducts.visibility = View.GONE
        binding.rvShimmer.visibility = View.VISIBLE



        favoriteVM = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[HomeViewModel::class.java]

        // RecyclerView
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter

        binding.rvProducts.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 3,
                spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
                includeEdge = false
            )
        )
        // 🔥 LOAD
        setupShimmer()
        loadFavorites()
        viewModel.loadProducts()

        // 🔥 Favorites observer
        favoriteVM.favorites.observe(viewLifecycleOwner) {
            adapter.updateFavorites(it)
        }

        // 🔥 Products observer
        viewModel.products.observe(viewLifecycleOwner) { products ->
            binding.rvShimmer.visibility = View.GONE

            binding.rvProducts.visibility = View.VISIBLE
            adapter.submitData(products)
        }



    }

    private fun setupShimmer() {
        shimmerAdapter = ShimmerAdapter()

        binding.rvShimmer.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.rvShimmer.adapter = shimmerAdapter
    }


    private fun toggleFavorite(product: Product) {

        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        RetrofitClient
            .instance(requireContext())
            .toggleFavorite(product.id)
            .enqueue(object : Callback<FavoriteResponse> {

                override fun onResponse(
                    call: Call<FavoriteResponse>,
                    response: Response<FavoriteResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        favoriteVM.setFavorite(
                            product.id,
                            response.body()!!.favorite
                        )
                    }
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {}
            })
    }

    private fun loadFavorites() {

        if (!sessionManager.isLoggedIn()) return

        RetrofitClient
            .instance(requireContext())
            .getMyFavorites()
            .enqueue(object : Callback<List<Product>> {

                override fun onResponse(
                    call: Call<List<Product>>,
                    response: Response<List<Product>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        favoriteVM.setFavoritesFromApi(response.body()!!)
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {}
            })
    }

}