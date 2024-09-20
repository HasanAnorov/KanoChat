package com.ierusalem.androchat.core.directory_router

import java.io.File

interface FilesDirectoryService {
    fun getPublicFilesDirectory(): File
    fun getPrivateFilesDirectory(): File?
}