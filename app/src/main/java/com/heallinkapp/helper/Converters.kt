package com.heallinkapp.helper

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromList(list: List<Float>?): String? {
        return list?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toList(data: String?): List<Float>? {
        return data?.let { Gson().fromJson(it, object : TypeToken<List<Double>>() {}.type) }
    }
}
