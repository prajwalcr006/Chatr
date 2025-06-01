package com.prajwalcr.domain.repository.caching

import android.net.Uri
import java.net.URI

interface SupabaseRepository {
    suspend fun uploadImage(uri: Uri): String?
}