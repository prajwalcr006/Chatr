package com.prajwalcr.domain.usecase.message

import android.net.Uri
import com.prajwalcr.domain.repository.caching.SupabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadImageUseCase(
    private val supabaseRepository: SupabaseRepository
) {
    suspend operator fun invoke(uri: Uri) = withContext(Dispatchers.IO) {
        supabaseRepository.uploadImage(uri)
    }
}