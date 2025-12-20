import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(
        token: String,
        userId: Long,
        name: String?,
        email: String?
    ) {
        prefs.edit()
            .putString("token", token)
            .putLong("user_id", userId)   // 👈 Long qilib saqlayapsan
            .putString("name", name)
            .putString("email", email)
            .putBoolean("is_logged_in", true)
            .apply()
    }


    fun getToken(): String? =
        prefs.getString("token", null)

    fun isLoggedIn(): Boolean =
        prefs.getBoolean("is_logged_in", false)

    fun getUserId(): Long =
        prefs.getLong("user_id", -1)

    fun getName(): String? = prefs.getString("name", null)
    fun getEmail(): String? = prefs.getString("email", null)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
