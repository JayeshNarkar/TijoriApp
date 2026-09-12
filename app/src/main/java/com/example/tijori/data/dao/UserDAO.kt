package com.example.tijori.data.dao

import androidx.room.*
import com.example.tijori.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM Users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM Users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM Users WHERE currentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM Users WHERE currentUser = 1 LIMIT 1")
    suspend fun getCurrentUserOnce(): User?

    @Query("SELECT * FROM Users ORDER BY firstName ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("UPDATE Users SET firstName = :firstName, lastName = :lastName WHERE id = :userId")
    suspend fun updateName(userId: String, firstName: String, lastName: String?)

    @Query("UPDATE Users SET currentUser = 0")
    suspend fun clearCurrentUser()

    @Transaction
    suspend fun setCurrentUser(userId: String) {
        clearCurrentUser()
        getUserById(userId)?.let { update(it.copy(currentUser = true)) }
    }
}