package app.openair

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson

/**
 * allows retrieval of a serialized object from shared preferences
 * useful for storing enums or other "simple" objects
 */
inline fun <reified T> SharedPreferences.getSerialized(path: String, default: T): T {
    val json = this.getString(path, null) ?: return default
    return Gson().fromJson(json, T::class.java)
}

/**
 * allows saving of a serialized object to shared preferences
 * useful for storing enums or other "simple" objects
 */
fun SharedPreferences.putSerialized(path: String, value: Any){
    val json = Gson().toJson(value)
    this.let {
        it.edit()?.putString(path, json)?.apply()
    }
}

/**
 * enables retrieving a theme color directly from context
 */
fun Context.themeColor(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

/**
 * enables retrieving a string resource directly from context
 */
fun Context.getStringResourceByName(stringName: String): String? {
    val resId = resources.getIdentifier(stringName, "string", packageName)
    return getString(resId)
}

/**
 * allows watching live data for the first change only
 */
fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
    observeForever(object: Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}