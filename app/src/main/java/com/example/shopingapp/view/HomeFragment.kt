package com.example.shopingapp.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopingapp.adapter.ProductAdapter
import com.example.shopingapp.databinding.FragmentHomeBinding
import com.example.shopingapp.model.Product
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val adapter = ProductAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter

        loadProducts()
    }

    private fun loadProducts() {

        // 1) LOADING KO‘RSATAMIZ
        binding.progressBar.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE

        RetrofitClient.instance.getAllProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {

                // 2) LOADINGNI YO‘QOTAMIZ
                binding.progressBar.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE

                if (response.isSuccessful) {
                    adapter.submitData(response.body() ?: emptyList())

                    Toast.makeText(requireContext(), response.body().toString()+"Success", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "onFailure:  ${response.body()}")

                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE

                Log.d("TAG", "onFailure:  ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
