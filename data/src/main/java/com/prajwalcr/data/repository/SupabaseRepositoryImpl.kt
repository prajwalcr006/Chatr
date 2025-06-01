package com.prajwalcr.data.repository

import android.content.Context
import android.net.Uri
import com.prajwalcr.data.BuildConfig
import com.prajwalcr.domain.repository.caching.SupabaseRepository
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.datetime.Clock
import timber.log.Timber
import java.net.URI

class SupabaseRepositoryImpl(
    private val appContext: Context
): SupabaseRepository {

    companion object {
        const val BUCKET_NAME = "chatr-images"
    }

    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_API_KEY
    ) {
        install(Storage)
    }

    override suspend fun uploadImage(uri: Uri): String? {
        try {
            val extension = uri.path?.substringAfter(".") ?: "jpg"

            val fileName = "${System.currentTimeMillis()}.$extension"

            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                supabase.storage.from(BUCKET_NAME).upload(fileName, inputStream.readBytes())
            } ?: return null

            return supabase.storage.from(BUCKET_NAME).publicUrl(fileName)
        } catch (ex: Exception) {
            Timber.e("Error uploading image: $ex")
            return null
        }
    }
}