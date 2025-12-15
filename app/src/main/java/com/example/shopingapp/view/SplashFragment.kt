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

    private fun checkUserIsLogged(){

        if(sessionManager.isLoggedIn()){
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)

            },1000)
        }else{
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
            },2000)
        }
    }


}