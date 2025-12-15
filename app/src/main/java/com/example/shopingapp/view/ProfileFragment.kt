package com.example.shopingapp.view

import SessionManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

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



        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)

        }
        binding.rowAnnouncement.root.setOnClickListener {
            findNavController().navigate(R.id.announcementFragment)
        }

        binding.rowPrivacy.root.setOnClickListener {
            findNavController().navigate(R.id.privacyFragment)
        }

        binding.rowTerms.root.setOnClickListener {
            findNavController().navigate(R.id.termsOfServiceFragment)
        }

        binding.rowOpenSource.root.setOnClickListener {
            findNavController().navigate(R.id.openSourceFragment)
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


}