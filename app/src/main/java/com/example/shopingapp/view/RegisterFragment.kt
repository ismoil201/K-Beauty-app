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
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString()
        val confirm = binding.tilConfirmPassword.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Fill all fields")
            return
        }

        if (password != confirm) {
            toast("Passwords do not match")
            return
        }

        // 🔥 API CALL
//        RetrofitClient.instance.register(
//            RegisterRequest(email, password, "Ismoil")
//        ).enqueue(object : Callback<AuthResponse> {
//
//            override fun onResponse(
//                call: Call<AuthResponse>,
//                response: Response<AuthResponse>
//            ) {
//                if (response.isSuccessful && response.body() != null) {
//                    val res = response.body()!!
//
//                    // ✅ SAVE SESSION
                    sessionManager.saveLogin(
                        token = "res.token",
                        name = "Ismoil Jurakhonov",
                        email = "ismoiljurakhonov1@gmail.com"
                    )

//                    // ✅ PROFILE ga qaytamiz
//                    findNavController().navigate(
//                        R.id.action_registerFragment_to_profileFragment
//                    )
//                } else {
//                    toast("Register failed")
//                }
//            }
//
//            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
//                toast(t.message ?: "Error")
//            }
//        })
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }







}