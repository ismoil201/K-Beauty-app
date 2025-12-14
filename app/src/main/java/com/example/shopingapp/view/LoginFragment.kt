package com.example.shopingapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentLoginBinding
import com.example.shopingapp.databinding.FragmentRegisterBinding

class LoginFragment : Fragment() {
    private lateinit var  binding: FragmentLoginBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

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


    }


}