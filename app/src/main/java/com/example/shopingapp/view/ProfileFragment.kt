package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopingapp.R
import com.example.shopingapp.adapter.OrderProfileAdapter
import com.example.shopingapp.databinding.FragmentProfileBinding
import com.example.shopingapp.model.OrderResponse
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var orderAdapter: OrderProfileAdapter
    private val orderList = mutableListOf<OrderResponse>()


    private lateinit var  binding : FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionManager = SessionManager(requireContext())

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = sessionManager.getUserId()

        updateUserUI()

        setupOrderRv()
        loadMyOrders()

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        binding.rowAnnouncement.root.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_announcementFragment)
        }

        binding.rowPrivacy.root.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_privacyFragment)
        }

        binding.rowTerms.root.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_termsOfServiceFragment)
        }

        binding.rowOpenSource.root.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_openSourceFragment)
        }

        binding.rowBonus.root.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_cuoponFragment)
        }
    }

    private fun updateUserUI() {
        if (sessionManager.isLoggedIn()) {

            // PROMO YO‘Q
            binding.layoutPromo.visibility = View.GONE

            // USER INFO BOR
            binding.layoutUserInfo.root.visibility = View.VISIBLE
            binding.layoutUserInfo.tvUserName.text = sessionManager.getName()
            binding.layoutUserInfo.tvUserEmail.text = sessionManager.getEmail()

        } else {
            // PROMO BOR
            binding.layoutPromo.visibility = View.VISIBLE
            binding.layoutUserInfo.root.visibility = View.GONE
        }
    }


    private fun setupOrderRv() {
        orderAdapter = OrderProfileAdapter(orderList,
            object : OrderProfileAdapter.OrderActionListener {
                override fun onOrderClick(order: OrderResponse) {
                    val bundle = bundleOf("orderId" to order.id)
                    findNavController().navigate(
                        R.id.action_profileFragment_to_orderHistoryFragment,
                        bundle
                    )

                }
            })

        binding.rvOrderProducts.apply {
            adapter = orderAdapter
        }
    }


    private fun loadMyOrders() {
        if (!sessionManager.isLoggedIn()) return

        RetrofitClient.instance(requireContext()).getMyOrders()
            .enqueue(object : Callback<List<OrderResponse>> {

                override fun onResponse(
                    call: Call<List<OrderResponse>>,
                    response: Response<List<OrderResponse>>
                ) {
                    if (response.isSuccessful) {
                        orderList.clear()
                        orderList.addAll(response.body()!!.map { it })
                        orderAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

}