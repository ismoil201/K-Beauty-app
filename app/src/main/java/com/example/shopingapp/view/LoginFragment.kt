package com.example.shopingapp.view

import SessionManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shopingapp.R
import com.example.shopingapp.databinding.FragmentLoginBinding
import com.example.shopingapp.model.FirebaseLoginRequest
import com.example.shopingapp.model.LoginRequest
import com.example.shopingapp.model.LoginResponse
import com.example.shopingapp.network.RetrofitClient
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var sessionManager: SessionManager

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    // Google
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>

    // Phone
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("GOOGLE_FLOW", "onCreate called")

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 🔥 WEB CLIENT ID
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(requireActivity(), gso)

        // 🔥 GOOGLE RESULT LISTENER — MUHIM
        googleLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("GOOGLE_FLOW", "Result code = ${result.resultCode}")

                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        Log.d("GOOGLE_FLOW", "Account selected, idToken=${account.idToken != null}")
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: Exception) {
                        Log.e("GOOGLE_FLOW", "Google result error", e)
                        toast("Google login error")
                    }
                } else {
                    Log.d("GOOGLE_FLOW", "User canceled Google login")
                }
            }
    }

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

        // ================= CLICK LISTENERS =================

        binding.btnLogin.setOnClickListener { loginEmail() }

        binding.btnGoogle.setOnClickListener {
            Log.d("GOOGLE_FLOW", "Google button clicked")

            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

        binding.btnPhone.setOnClickListener {
            startPhoneAuth("+998901234567") // test
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    // ================= EMAIL LOGIN =================
    private fun loginEmail() {

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
                            token = user.token,
                            userId = user.id,
                            name = user.fullName,
                            email = user.email
                        )

                        if (isAdded) {
                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFragment // men qoshdim
                            )
                        }


                    } else {
                        toast("Invalid email or password")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    toast(t.message ?: "Network error")
                }
            })
    }

    // ================= GOOGLE LOGIN =================
    private fun firebaseAuthWithGoogle(idToken: String) {

        Log.d("GOOGLE_FLOW", "firebaseAuthWithGoogle called")

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d("GOOGLE_FLOW", "Firebase signIn success")

                it.user?.getIdToken(true)
                    ?.addOnSuccessListener { result ->
                        Log.d("GOOGLE_FLOW", "Firebase ID token ready")
                        sendTokenToBackend(result.token!!)
                    }
            }
            .addOnFailureListener {
                Log.e("GOOGLE_FLOW", "Firebase auth failed", it)
                toast("Firebase auth failed")
            }
    }

    // ================= PHONE LOGIN =================
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

    // ================= BACKEND FIREBASE LOGIN =================
    private fun sendTokenToBackend(idToken: String) {

        Log.d("GOOGLE_FLOW", "Sending token to backend")

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

                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment) // men qoshdim

                    } else {
                        toast("Backend login failed")
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
