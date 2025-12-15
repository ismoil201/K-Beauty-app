import android.content.Context
class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(
        token: String,
        userId: Int,
        name: String?,
        email: String?
    ) {
        prefs.edit()
            .putString("token", token ?: "")
            .putInt("user_id", userId)
            .putString("name", name ?: "")
            .putString("email", email ?: "")
            .putBoolean("is_logged_in", true)
            .apply()
    }


    fun isLoggedIn(): Boolean =
        prefs.getBoolean("is_logged_in", false)

    fun getUserId(): Int =
        prefs.getInt("user_id", -1)

    fun getName(): String? = prefs.getString("name", null)
    fun getEmail(): String? = prefs.getString("email", null)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
