package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var  binding : FragmentSplashBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionManager = SessionManager(requireContext())
        binding = FragmentSplashBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkUserIsLogged()


    }

    private fun checkUserIsLogged() {

        val navController = findNavController()

        Handler(Looper.getMainLooper()).postDelayed({

            if (sessionManager.isLoggedIn()) {
                navController.navigate(
                    R.id.action_splashFragment_to_mainFragment,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true) // 🔥 MUHIM
                        .build()
                )
            } else {
                navController.navigate(
                    R.id.action_splashFragment_to_onboardingFragment,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true)
                        .build()
                )
            }

        }, 1000)
    }


}