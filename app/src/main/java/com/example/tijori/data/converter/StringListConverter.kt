package com.example.tijori.data.converter

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>?): String? = list?.joinToString("|||")

    @TypeConverter
    fun toList(value: String?): List<String>? = value?.split("|||")
}