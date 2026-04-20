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
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO = stringPreferencesKey("user_photo")
    }

    val userSession: Flow<AuthState> = context.dataStore.data.map { preferences ->
        AuthState(
            isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
            uid = preferences[USER_UID],
            userName = preferences[USER_NAME],
            userEmail = preferences[USER_EMAIL],
            photoUrl = preferences[USER_PHOTO]
        )
    }

    suspend fun saveSession(uid: String, name: String, email: String, photoUrl: String?) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_UID] = uid
            preferences[USER_NAME] = name
            preferences[USER_EMAIL] = email
            photoUrl?.let { preferences[USER_PHOTO] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
