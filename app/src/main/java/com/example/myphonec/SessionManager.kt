package com.example.myphonec

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO = stringPreferencesKey("user_photo")
    }

    val userSession: Flow<AuthState> = context.dataStore.data.map { preferences ->
        AuthState(
            isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
            isGuest = preferences[IS_GUEST] ?: false,
            uid = preferences[USER_UID],
            userName = preferences[USER_NAME],
            userEmail = preferences[USER_EMAIL],
            photoUrl = preferences[USER_PHOTO]
        )
    }

    suspend fun saveSession(uid: String, name: String, email: String, photoUrl: String?) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[IS_GUEST] = false
            preferences[USER_UID] = uid
            preferences[USER_NAME] = name
            preferences[USER_EMAIL] = email
            if (photoUrl != null) {
                preferences[USER_PHOTO] = photoUrl
            } else {
                preferences.remove(USER_PHOTO)
            }
        }
    }

    suspend fun saveGuestSession() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences[IS_GUEST] = true
            preferences[USER_NAME] = "Guest User"
            preferences.remove(USER_UID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_PHOTO)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
