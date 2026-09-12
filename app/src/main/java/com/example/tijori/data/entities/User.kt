package com.example.tijori.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "Users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val firstName: String,
    val lastName: String?,
    val profilePictureURI: String?,
    val currentUser: Boolean = false
)