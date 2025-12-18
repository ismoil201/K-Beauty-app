package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentLoginBinding
import com.example.shopingapp.network.AuthResponse
import com.example.shopingapp.network.LoginRequest
import com.example.shopingapp.model.LoginResponse
import com.example.shopingapp.model.User
import com.example.shopingapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Keyboard handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                imeInsets.bottom.coerceAtLeast(systemBars.bottom)
            )
            insets
        }

        // 🔘 LOGIN CLICK
        binding.btnLogin.setOnClickListener {
            login()
        }

        // 👉 Register ga o‘tish
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_registerFragment_to_loginFragment
            )
        }
    }

    private fun login() {

        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Fill all fields")
            return
        }

        RetrofitClient.instance(requireContext())
            .login(LoginRequest(email, password))
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val user = response.body()!!

                        sessionManager.saveLogin(
                            token = user.token,   // 🔥 MUHIM
                            userId = user.id,
                            name = user.fullName,
                            email = user.email
                        )

                        toast("Login success")

                        findNavController()
                            .navigate(R.id.profileFragment)

                    } else {
                        toast("Invalid email or password")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    toast(t.message ?: "Network error")
                }
            })
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
