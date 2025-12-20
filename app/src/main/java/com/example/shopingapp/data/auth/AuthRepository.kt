package com.example.shopingapp.data.auth

import android.app.Activity
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val auth: FirebaseAuth
) {

    // 🔥 GOOGLE + PHONE UCHUN UNIVERSAL
    fun signInWithCredential(
        credential: AuthCredential,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                it.user?.getIdToken(true)
                    ?.addOnSuccessListener { token ->
                        onSuccess(token.token!!)
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Firebase auth failed")
            }
    }

    // =========================
    // PHONE SMS
    // =========================
    fun sendSms(
        phone: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
        )
    }
}
