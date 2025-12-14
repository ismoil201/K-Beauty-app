package com.example.shopingapp.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopingapp.GridSpacingItemDecoration
import com.example.shopingapp.adapter.ProductAdapter
import com.example.shopingapp.adapter.onClickItem
import com.example.shopingapp.databinding.FragmentHomeBinding
import com.example.shopingapp.viewmodel.HomeViewModel

import com.example.shopingapp.R

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val adapter = ProductAdapter(onClickItem =  object : onClickItem{
        override fun onClick(id: Int?) {


            id?.let {
                val bundle = Bundle()
                bundle.putInt("productId", it)
                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                    bundle
                )
            }
        }

    })

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvProducts.adapter = adapter
        binding.rvProducts.setPadding(
            resources.getDimensionPixelSize(R.dimen.grid_side_padding),
            0,
            resources.getDimensionPixelSize(R.dimen.grid_side_padding),
            0
        )

        binding.rvProducts.clipToPadding = false

        binding.rvProducts.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 3,
                spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
                includeEdge = false
            )
        )

        observeProducts()

        // 🔥 MUHIM: API faqat 1 MARTA chaqiladi
        viewModel.loadProducts()
    }

    private fun observeProducts() {
        viewModel.products.observe(viewLifecycleOwner) { list ->

            binding.progressBar.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE

            adapter.submitData(list.shuffled())
        }
    }
}
