package com.example.shopingapp.view

import SessionManager
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentRegisterBinding
import com.example.shopingapp.model.FirebaseLoginRequest
import com.example.shopingapp.model.LoginResponse
import com.example.shopingapp.model.RegisterRequest
import com.example.shopingapp.network.RetrofitClient
import com.example.shopingapp.network.SimpleResponse
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var sessionManager: SessionManager

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    // Google
    private lateinit var googleClient: GoogleSignInClient

    // Phone
    private lateinit var verificationId: String

    // =========================
    // GOOGLE RESULT LAUNCHER
    // =========================
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    toast("Google register failed")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 🔥 MUHIM
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Keyboard / Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                ime.bottom.coerceAtLeast(systemBars.bottom)
            )
            insets
        }

        // =========================
        // CLICK LISTENERS
        // =========================

        binding.btnRegister.setOnClickListener { registerEmail() }

        binding.btnGoogle.setOnClickListener {
            googleLauncher.launch(googleClient.signInIntent)
        }

        binding.btnPhone.setOnClickListener {
            startPhoneAuth("+998901234567") // 🔧 test raqam
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_registerFragment_to_loginFragment
            )
        }
    }

    // =========================
    // EMAIL REGISTER
    // =========================
    private fun registerEmail() {

        val fullName = binding.tilUserName.editText?.text.toString().trim()
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString()
        val confirm = binding.tilConfirmPassword.editText?.text.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Fill all fields")
            return
        }

        if (password != confirm) {
            toast("Passwords do not match")
            return
        }

        RetrofitClient.instance(requireContext())
            .register(RegisterRequest(email, password, fullName))
            .enqueue(object : Callback<SimpleResponse> {

                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful) {
                        toast("Register success")
                        findNavController().navigate(
                            R.id.action_registerFragment_to_loginFragment
                        )
                    } else {
                        toast("Register failed")
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    toast(t.message ?: "Error")
                }
            })
    }

    // =========================
    // GOOGLE REGISTER
    // =========================
    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                it.user?.getIdToken(true)
                    ?.addOnSuccessListener { result ->
                        sendTokenToBackend(result.token!!)
                    }
            }
            .addOnFailureListener {
                toast("Firebase auth failed")
            }
    }

    // =========================
    // PHONE REGISTER
    // =========================
    private fun startPhoneAuth(phone: String) {

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    firebaseAuth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            it.user?.getIdToken(true)
                                ?.addOnSuccessListener { res ->
                                    sendTokenToBackend(res.token!!)
                                }
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    toast(e.message ?: "SMS failed")
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = id
                    toast("SMS sent")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // =========================
    // BACKEND FIREBASE REGISTER
    // =========================
    private fun sendTokenToBackend(idToken: String) {

        RetrofitClient.instance(requireContext())
            .firebaseLogin(FirebaseLoginRequest(idToken))
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val user = response.body()!!

                        sessionManager.saveLogin(
                            token = user.token,
                            userId = user.id,
                            name = user.fullName,
                            email = user.email
                        )

                        // 🔥 Register bo‘ldi → ichkariga kiradi
                        findNavController().navigate(R.id.profileFragment)

                    } else {
                        toast("Backend register failed")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    toast(t.message ?: "Error")
                }
            })
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
