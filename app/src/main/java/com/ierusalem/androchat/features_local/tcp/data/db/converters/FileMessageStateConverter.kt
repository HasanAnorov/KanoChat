package com.ierusalem.androchat.features_local.tcp.data.db.converters

import androidx.room.TypeConverter
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState

class FileMessageStateConverter {

    @TypeConverter
    fun fromFileState(value: FileMessageState): String {
        return when (value) {
            is FileMessageState.Loading -> "loading:${value.percentage}"
            FileMessageState.Success -> "success"
            FileMessageState.Failure -> "failure"
        }
    }

    @TypeConverter
    fun toFileState(value: String): FileMessageState {
        return when {
            value.startsWith("loading") -> {
                val percentage = value.split(":")[1].toIntOrNull() ?: 0
                FileMessageState.Loading(percentage)
            }

            value == "success" -> FileMessageState.Success
            value == "failure" -> FileMessageState.Failure
            else -> FileMessageState.Failure  // Default case
        }
    }

}
