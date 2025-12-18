import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavoriteViewModel : ViewModel() {

    private val _favorites =
        MutableLiveData<Map<Long, Boolean>>(emptyMap())

    val favorites: LiveData<Map<Long, Boolean>>
        get() = _favorites

    fun setFavorite(productId: Long, isFavorite: Boolean) {
        val current = _favorites.value ?: emptyMap()
        val updated = current.toMutableMap()
        updated[productId] = isFavorite
        _favorites.value = updated
    }

    fun isFavorite(productId: Long): Boolean {
        return _favorites.value?.get(productId) ?: false
    }
}
