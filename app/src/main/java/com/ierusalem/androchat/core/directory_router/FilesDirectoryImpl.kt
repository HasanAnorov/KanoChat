package com.ierusalem.androchat.core.directory_router

import android.content.Context
import android.os.Environment
import com.ierusalem.androchat.core.utils.Constants
import java.io.File

class FilesDirectoryImpl(val context: Context) : FilesDirectoryService {

    override fun getPublicFilesDirectory(): File {
        val publicFilesDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/${Constants.FOLDER_NAME_FOR_RESOURCES}"
        )
        if(!publicFilesDirectory.exists()){
            publicFilesDirectory.mkdir()
        }
        return publicFilesDirectory
    }

    override fun getPrivateFilesDirectory(): File{
        val privateFilesDirectory = context.filesDir

        if(!privateFilesDirectory.exists()){
            privateFilesDirectory.mkdir()
        }
        return privateFilesDirectory
    }

}