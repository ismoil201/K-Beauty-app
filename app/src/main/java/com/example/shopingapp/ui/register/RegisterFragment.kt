package com.example.shopingapp.ui.register

import SessionManager
import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.data.auth.AuthRepository
import com.example.shopingapp.data.auth.AuthResult
import com.example.shopingapp.data.network.BackendRepository
import com.example.shopingapp.databinding.FragmentRegisterBinding
import com.example.shopingapp.model.FirebaseLoginRequest
import com.example.shopingapp.model.LoginResponse
import com.example.shopingapp.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var googleClient : GoogleSignInClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        setupViewModel()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 🔥 MUHIM
            .requestEmail()
            .build()

          googleClient = GoogleSignIn.getClient(requireActivity(), gso)
        return binding.root
    }

    private fun setupViewModel() {

        val authRepo = AuthRepository(FirebaseAuth.getInstance())

        val apiService =
            RetrofitClient
                .instance(requireContext())

        val backendRepo = BackendRepository(apiService)

        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(c: Class<T>): T {
                    return RegisterViewModel(authRepo, backendRepo) as T
                }
            }
        )[RegisterViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnRegister.setOnClickListener {

            val currentState = viewModel.authState.value

            if (currentState is AuthResult.CodeSent) {
                val code = binding.tilConfirCode.editText?.text.toString()
                viewModel.verifyCode(code)
            } else {
                val phone = binding.tilPhoneNumber.editText?.text.toString()
                viewModel.sendCode(phone, requireActivity())
            }
        }


        binding.tvResend.setOnClickListener {
            val phone = binding.tilPhoneNumber.editText?.text.toString()
            viewModel.sendCode(phone, requireActivity())
        }

        observeViewModel()

        binding.btnGoogle.setOnClickListener {
            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

    }

    private fun observeViewModel() {

        viewModel.timerText.observe(viewLifecycleOwner) {
            binding.tvTimer.text = it
        }

        viewModel.resendEnabled.observe(viewLifecycleOwner) {
            binding.tvResend.isEnabled = it
            binding.tvResend.alpha = if (it) 1f else 0.4f
        }

        viewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthResult.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Please wait..."
                }
                is AuthResult.CodeSent -> {
                    binding.tilConfirCode.visibility = View.VISIBLE
                    binding.tvTimer.visibility = View.VISIBLE
                    binding.tvResend.visibility = View.VISIBLE
                    binding.btnRegister.text = "Verify"
                    binding.btnRegister.isEnabled = true
                }

                is AuthResult.Verified -> {

                    val user = it.user

                    sessionManager.saveLogin(
                        token = user.token,
                        userId = user.id,
                        name = user.fullName,
                        email = user.email
                    )

                    findNavController().navigate(R.id.homeFragment)
                }

                is AuthResult.Error -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)

                    // 🔥 MUHIM JOY
                    viewModel.loginWithGoogle(account.idToken!!)

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Google register failed", Toast.LENGTH_SHORT).show()
                }
            }
        }


}
