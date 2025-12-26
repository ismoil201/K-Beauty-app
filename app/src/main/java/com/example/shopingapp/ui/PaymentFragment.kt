package com.example.shopingapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentPaymentBinding
import com.example.shopingapp.model.OrderResponse
import com.example.shopingapp.model.PaymentType
import com.example.shopingapp.network.OrderCreateRequest
import com.example.shopingapp.network.RetrofitClient
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private var selectedPayment: PaymentType = PaymentType.CASH_ON_DELIVERY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ default payment
        selectPayment(PaymentType.CASH_ON_DELIVERY)

        binding.payOnDelivery.setOnClickListener {
            selectPayment(PaymentType.CASH_ON_DELIVERY)
        }

        binding.payOnlineCard.setOnClickListener {
            selectPayment(PaymentType.ONLINE_CARD)
            toast("Hozircha test rejimida")
        }

        binding.payInstallment.setOnClickListener {
            toast("Nasiyaga to‘lov tez orada ishga tushadi")
        }

        binding.payUzumCard.setOnClickListener {
            toast("Uzum Card tez orada qo‘shiladi")
        }

        binding.btnPay.setOnClickListener {
            when (selectedPayment) {
                PaymentType.CASH_ON_DELIVERY -> {
                    createOrder()
                }

                PaymentType.ONLINE_CARD -> {
                    toast("Test rejimi: buyurtma saqlandi")
                    createOrder()
                }

                else -> Unit
            }
        }
    }

    // ================= CREATE ORDER =================

    private fun createOrder() {
        val body = OrderCreateRequest(
            address = "Toshkent, Chilonzor",
            phone = "998901234567"
        )

        RetrofitClient.instance(requireContext())
            .createOrder(body)
            .enqueue(object : Callback<OrderResponse> {

                override fun onResponse(
                    call: Call<OrderResponse>,
                    response: Response<OrderResponse>
                ) {
                    if (response.isSuccessful) {
                        toast("Buyurtma yaratildi ✅")

                        // TODO: OrderSuccess yoki OrderDetail screen
                    } else {
                        toast("Xatolik: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    toast("Server bilan bog‘lanib bo‘lmadi")
                }
            })
    }

    // ================= PAYMENT UI =================

    private fun selectPayment(type: PaymentType) {
        selectedPayment = type
        clearStrokes()

        when (type) {
            PaymentType.CASH_ON_DELIVERY -> {
                select(binding.payOnDelivery)
                binding.btnPay.text = "Buyurtma berish"
            }

            PaymentType.ONLINE_CARD -> {
                select(binding.payOnlineCard)
                binding.btnPay.text = "Kartadan to‘lash (TEST)"
            }

            else -> Unit
        }
    }

    private fun clearStrokes() {
        listOf(
            binding.payOnDelivery,
            binding.payOnlineCard,
            binding.payInstallment,
            binding.payUzumCard
        ).forEach {
            it.strokeWidth = 0
        }
    }

    private fun select(card: MaterialCardView) {
        card.strokeWidth =
            resources.getDimensionPixelSize(R.dimen.stroke_selected)
        card.strokeColor =
            ContextCompat.getColor(requireContext(), R.color.black)
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
