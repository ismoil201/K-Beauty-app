package com.example.shopingapp.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
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
    }

    private fun loadProductById(id: Int) {
        RetrofitClient.instance.getProductById(id).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val p = response.body()!!

                    binding.tvTitle.text = p.name
                    binding.tvDetails.text = p.description
                    binding.tvNewPrice.text = "${p.price}$"

                    Glide.with(requireContext())
                        .load(p.imageUrl)
                        .into(binding.viewPagerImages)
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d("DETAIL", "Error: ${t.message}")
            }
        })
    }

    private fun loadSimilarProducts() {

        binding.rvSimilar.layoutManager =
            GridLayoutManager(requireContext(), 2,
                GridLayoutManager.VERTICAL, false)

        binding.rvSimilar.adapter = adapter

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
