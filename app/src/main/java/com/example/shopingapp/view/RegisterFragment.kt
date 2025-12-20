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
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var googleClient: GoogleSignInClient
    private var verificationId: String? = null
    private var isCodeSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
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

        binding.btnRegister.setOnClickListener {
            if (!isCodeSent) {
                sendSmsCode()
            } else {
                verifySmsCode()
            }
        }

        binding.btnGoogle.setOnClickListener {
            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_registerFragment_to_loginFragment
            )
        }
    }

    // =========================
    // SEND SMS
    // =========================
    private fun sendSmsCode() {

        val phone = binding.tilPhoneNumber.editText?.text.toString().trim()

        if (phone.isEmpty()) {
            toast("Enter phone number")
            return
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(phoneCallbacks)
                .build()
        )
    }

    private val phoneCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                toast(e.message ?: "SMS failed")
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                isCodeSent = true

                binding.tilConfirCode.visibility = View.VISIBLE
                binding.btnRegister.text = "Verify & Register"

                toast("SMS sent")
            }
        }

    // =========================
    // VERIFY SMS CODE
    // =========================
    private fun verifySmsCode() {

        val code = binding.tilConfirCode.editText?.text.toString().trim()

        if (code.isEmpty() || verificationId == null) {
            toast("Enter SMS code")
            return
        }

        val credential =
            PhoneAuthProvider.getCredential(verificationId!!, code)

        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                it.user?.getIdToken(true)
                    ?.addOnSuccessListener { result ->
                        sendTokenToBackend(result.token!!)
                    }
            }
            .addOnFailureListener {
                toast("Verification failed")
            }
    }

    // =========================
    // BACKEND REGISTER
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

                        findNavController().navigate(
                            R.id.action_loginFragment_to_homeFragment
                        )
                    } else {
                        toast("Backend register failed")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    toast(t.message ?: "Error")
                }
            })
    }

    // =========================
    // GOOGLE REGISTER
    // =========================
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential =
                        GoogleAuthProvider.getCredential(account.idToken, null)

                    firebaseAuth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            it.user?.getIdToken(true)
                                ?.addOnSuccessListener { token ->
                                    sendTokenToBackend(token.token!!)
                                }
                        }
                } catch (e: Exception) {
                    toast("Google register failed")
                }
            }
        }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
