package com.example.tijori.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tijori.data.dao.UserDao
import com.example.tijori.data.entities.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UserLoadState {
    data object Loading : UserLoadState
    data class Loaded(val user: User?) : UserLoadState
}

@HiltViewModel
class UserDBViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    val currentUserState: StateFlow<UserLoadState> = userDao.getCurrentUser()
        .map { UserLoadState.Loaded(it) as UserLoadState }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserLoadState.Loading)
    val currentUser = userDao.getCurrentUser()

    val allUsers = userDao.getAllUsers()

    fun addUser(
        email: String,
        displayName: String?,
        profilePictureURI: String?,
    ) {
        viewModelScope.launch {
            val (firstName, lastName) = parseDisplayName(displayName)

            val existing = userDao.getUserByEmail(email)
            if (existing != null) {

                setCurrentUser(existing.id)
                return@launch
            }

            val newUser = User(
                email = email,
                firstName = firstName,
                lastName = lastName,
                profilePictureURI = profilePictureURI
            )
            userDao.insert(newUser)
            setCurrentUser(newUser.id)
        }
    }

    fun setCurrentUser(userId: String) {
        viewModelScope.launch {
            userDao.setCurrentUser(userId)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDao.clearCurrentUser()
        }
    }

    fun updateProfile(firstName: String, lastName: String?) {
        viewModelScope.launch {
            val user = (currentUserState.value as? UserLoadState.Loaded)?.user ?: return@launch
            userDao.updateName(user.id, firstName, lastName)
        }
    }

    private fun parseDisplayName(displayName: String?): Pair<String, String?> {
        val tokens = displayName?.trim()?.split("\\s+".toRegex())?.filter { it.isNotBlank() }
            ?: return "User" to null

        // A token is "noise" if it has no letters at all (pure digits, dashes, dots, etc.)
        fun isNoise(token: String) = token.none { it.isLetter() }

        var start = 0
        var end = tokens.size

        while (start < end && isNoise(tokens[start])) start++
        while (end > start && isNoise(tokens[end - 1])) end--

        val nameTokens = tokens.subList(start, end)

        if (nameTokens.isEmpty()) return "User" to null

        val firstName = nameTokens.first()
        val lastName = if (nameTokens.size > 1) {
            nameTokens.drop(1).joinToString(" ")
        } else {
            null
        }

        return firstName to lastName
    }
}