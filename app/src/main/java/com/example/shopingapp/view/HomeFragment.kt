package com.example.shopingapp.view

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
import com.example.shopingapp.databinding.FragmentHomeBinding
import com.example.shopingapp.model.Product
import com.example.shopingapp.network.FavoriteResponse
import com.example.shopingapp.network.RetrofitClient
import com.example.shopingapp.viewmodel.HomeViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

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
        binding.progressBar.visibility = View.VISIBLE

        // ✅ ViewModels
        favoriteVM = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[HomeViewModel::class.java]

        // ✅ RecyclerView
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvProducts.adapter = adapter

        binding.rvProducts.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 3,
                spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
                includeEdge = false
            )
        )

        // ✅ Observers
        favoriteVM.favorites.observe(viewLifecycleOwner) {
            adapter.updateFavorites(it)
        }

        viewModel.products.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE   // 🔥 SHU YO‘Q EDI
            adapter.submitData(it)
        }


        // ✅ Load data
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadProducts()
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
}
