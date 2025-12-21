package com.example.shopingapp.ui.register

import SessionManager
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.data.auth.AuthRepository
import com.example.shopingapp.data.auth.AuthResult
import com.example.shopingapp.data.network.BackendRepository
import com.example.shopingapp.databinding.FragmentRegisterBinding
import com.example.shopingapp.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private val TAG = "REGISTER_DEBUG"

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var googleClient: GoogleSignInClient

    private var isVerifying = false

    // ===================================================
    // LIFECYCLE
    // ===================================================
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d(TAG, "onCreateView")

        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        setupViewModel()
        setupGoogle()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")

        setupPhoneInput()
        setupButtonState()
        observeViewModel()

        // =======================
        // SEND / VERIFY BUTTON
        // =======================
        binding.btnAction.setOnClickListener {

            val state = viewModel.authState.value
            Log.d(TAG, "Button clicked, state = $state")

            if (state is AuthResult.CodeSent) {
                val code = binding.etCode.text.toString()

                if (code.length == 6 && !isVerifying) {
                    Log.d(TAG, "VERIFY CODE → $code")
                    isVerifying = true
                    viewModel.verifyCode(code)
                }

            } else {
                val phone = getCleanPhone()
                Log.d(TAG, "SEND CODE → $phone")
                viewModel.sendCode(phone, requireActivity())
            }
        }

        // =======================
        // RESEND
        // =======================
        binding.tvResend.setOnClickListener {
            val phone = getCleanPhone()
            Log.d(TAG, "RESEND CODE → $phone")
            viewModel.sendCode(phone, requireActivity())
        }

        // =======================
        // GOOGLE LOGIN
        // =======================
        binding.btnGoogle.setOnClickListener {
            Log.d(TAG, "Google login clicked")
            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }
    }

    // ===================================================
    // VIEWMODEL
    // ===================================================
    private fun setupViewModel() {

        val authRepo = AuthRepository(FirebaseAuth.getInstance())
        val apiService = RetrofitClient.instance(requireContext())
        val backendRepo = BackendRepository(apiService)

        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(authRepo, backendRepo) as T
                }
            }
        )[RegisterViewModel::class.java]
    }

    // ===================================================
    // GOOGLE
    // ===================================================
    private fun setupGoogle() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    // ===================================================
    // OBSERVE
    // ===================================================
    private fun observeViewModel() {

        viewModel.timerText.observe(viewLifecycleOwner) {
            binding.tvTimer.text = it
        }

        viewModel.resendEnabled.observe(viewLifecycleOwner) {
            binding.tvResend.isEnabled = it
            binding.tvResend.alpha = if (it) 1f else 0.4f
        }

        viewModel.authState.observe(viewLifecycleOwner) {

            Log.d(TAG, "AuthState = $it")

            when (it) {
                is AuthResult.Loading -> {
                    binding.btnAction.isEnabled = false
                    binding.btnAction.text = "Iltimos, kuting..."
                }

                is AuthResult.CodeSent -> {
                    isVerifying = false
                    binding.tilConfirmCode.visibility = View.VISIBLE
                    binding.tvTimer.visibility = View.VISIBLE
                    binding.tvResend.visibility = View.VISIBLE
                    binding.btnAction.text = "Tasdiqlash"
                    binding.btnAction.isEnabled = true
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
                    isVerifying = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    binding.btnAction.isEnabled = true
                }
            }
        }
    }

    // ===================================================
    // GLOBAL PHONE INPUT (ONLY + REQUIRED)
    // ===================================================
    private fun setupPhoneInput() {

        // Agar bo‘sh bo‘lsa, faqat +
        if (binding.etPhone.text.isNullOrEmpty()) {
            binding.etPhone.setText("+")
            binding.etPhone.setSelection(1)
        }

        binding.etPhone.addTextChangedListener(object : TextWatcher {

            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                if (s == null || isEditing) return
                isEditing = true

                // faqat + va raqamlar
                val cleaned = s.toString()
                    .replace("[^0-9+]".toRegex(), "")
                    .let {
                        if (!it.startsWith("+")) "+" + it.replace("+", "")
                        else "+" + it.drop(1).replace("+", "")
                    }

                binding.etPhone.setText(cleaned)
                binding.etPhone.setSelection(cleaned.length)

                Log.d(TAG, "PHONE INPUT → $cleaned")

                isEditing = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ===================================================
    // BUTTON STATE (GLOBAL)
    // ===================================================
    private fun setupButtonState() {

        binding.btnAction.isEnabled = false

        binding.etPhone.addTextChangedListener {
            val phone = it.toString()
            val digits = phone.replace("\\D".toRegex(), "")

            // + va kamida 6 ta raqam bo‘lsa
            binding.btnAction.isEnabled =
                phone.startsWith("+") && digits.length >= 6

            Log.d(TAG, "BUTTON CHECK → phone=$phone digits=$digits")
        }
    }

    // ===================================================
    // CLEAN PHONE (E.164)
    // ===================================================
    private fun getCleanPhone(): String {
        val raw = binding.etPhone.text.toString()
        val result = raw.replace("[^0-9+]".toRegex(), "")
        Log.d(TAG, "CLEAN PHONE → $result")
        return result
    }

    // ===================================================
    // GOOGLE RESULT
    // ===================================================
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    viewModel.loginWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Google register failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
}
