package com.example.shopingapp.ui.register

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shopingapp.data.auth.AuthRepository
import com.example.shopingapp.data.auth.AuthResult
import com.example.shopingapp.data.network.BackendRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class RegisterViewModel(
    private val authRepo: AuthRepository,
    private val backendRepo: BackendRepository
) : ViewModel() {

    val authState = MutableLiveData<AuthResult>()
    val timerText = MutableLiveData("01:00")
    val resendEnabled = MutableLiveData(false)

    private var verificationId: String? = null
    private var timer: CountDownTimer? = null

    // =========================
    // PHONE - SEND CODE
    // =========================
    fun sendCode(phone: String, activity: Activity) {
        authState.value = AuthResult.Loading
        authRepo.sendSms(phone, activity, callbacks)
    }

    // =========================
    // PHONE - VERIFY CODE
    // =========================
    fun verifyCode(code: String) {
        val id = verificationId ?: run {
            authState.value = AuthResult.Error("Verification ID missing")
            return
        }

        authState.value = AuthResult.Loading

        val credential =
            PhoneAuthProvider.getCredential(id, code)

        authRepo.signInWithCredential(
            credential,
            onSuccess = { firebaseToken ->
                Log.d("REGISTER_VM", "Phone Firebase success")
                sendToBackend(firebaseToken)
            },
            onError = {
                authState.value = AuthResult.Error(it)
            }
        )
    }

    // =========================
    // GOOGLE LOGIN
    // =========================
    fun loginWithGoogle(idToken: String) {
        authState.value = AuthResult.Loading
        Log.d("REGISTER_VM", "Google idToken received")

        val credential =
            GoogleAuthProvider.getCredential(idToken, null)

        authRepo.signInWithCredential(
            credential,
            onSuccess = { firebaseToken ->
                Log.d("REGISTER_VM", "Google Firebase success")
                sendToBackend(firebaseToken)
            },
            onError = {
                authState.value = AuthResult.Error(it)
            }
        )
    }

    // =========================
    // BACKEND
    // =========================
    private fun sendToBackend(token: String) {
        Log.d("REGISTER_VM", "Sending token to backend")

        backendRepo.firebaseLogin(
            token,
            onSuccess = { user ->
                authState.value = AuthResult.Verified(user)
            },
            onError = {
                authState.value = AuthResult.Error(it)
            }
        )

    }

    // =========================
    // PHONE CALLBACKS
    // =========================
    private val callbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                authRepo.signInWithCredential(
                    credential,
                    onSuccess = {
                        Log.d("REGISTER_VM", "Auto verification success")
                        sendToBackend(it)
                    },
                    onError = {
                        authState.value = AuthResult.Error(it)
                    }
                )
            }

            override fun onVerificationFailed(e: FirebaseException) {
                authState.value = AuthResult.Error(
                    e.message ?: "SMS verification failed"
                )
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                authState.value = AuthResult.CodeSent
                startTimer()
            }
        }

    // =========================
    // TIMER
    // =========================
    private fun startTimer() {
        resendEnabled.value = false

        timer?.cancel()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(ms: Long) {
                val s = ms / 1000
                timerText.value =
                    String.format("%02d:%02d", s / 60, s % 60)
            }

            override fun onFinish() {
                timerText.value = "00:00"
                resendEnabled.value = true
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
