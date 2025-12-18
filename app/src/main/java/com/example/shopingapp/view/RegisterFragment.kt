package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentRegisterBinding
import com.example.shopingapp.network.RegisterRequest
import com.example.shopingapp.network.RetrofitClient
import com.example.shopingapp.network.SimpleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private lateinit var  binding: FragmentRegisterBinding
    private lateinit var sessionManager: SessionManager



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->

            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // keyboard ochilganda pastdan padding beriladi
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                imeInsets.bottom.coerceAtLeast(systemBars.bottom)
            )

            insets
        }

        binding.btnRegister.setOnClickListener {
            register()
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun register() {

        val userName = binding.tilUserName.editText?.text.toString().trim()
        val email = binding.tilEmail.editText?.text.toString().trim()

        val password = binding.tilPassword.editText?.text.toString()
        val confirm = binding.tilConfirmPassword.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireActivity(), "Fill all fields", Toast.LENGTH_SHORT).show()

            return
        }

        if (password != confirm) {
            Toast.makeText(requireActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show()

            return
        }

        RetrofitClient.instance(requireContext()).register(
            RegisterRequest(
                email = email,
                password = password,
                fullName = userName
            )
        ).enqueue(object : Callback<SimpleResponse> {

            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {
                if (response.isSuccessful) {


                    Toast.makeText(requireActivity(), "Register success", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                        R.id.action_registerFragment_to_loginFragment
                    )

                } else {
                    Toast.makeText(requireActivity(), "Register failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                Toast.makeText(requireActivity(), t.message ?: "Error", Toast.LENGTH_SHORT).show()


            }
        })
    }









}