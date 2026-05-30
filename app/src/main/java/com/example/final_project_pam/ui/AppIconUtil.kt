package com.example.final_project_pam.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Cache untuk menyimpan icon agar tidak perlu me-load ulang dari sistem berkali-kali.
// Ini sangat krusial untuk mencegah Force Close / OOM saat scroll cepat.
val iconCache = LruCache<String, ImageBitmap>(200) // Simpan 200 icon terakhir di memori

/**
 * Mengonversi Drawable ke ImageBitmap dengan ukuran kecil (100x100) agar hemat RAM.
 */
fun Drawable.toImageBitmapOptimized(): ImageBitmap {
    val bitmap = toBitmap(
        width = 80,
        height = 80,
        config = Bitmap.Config.ARGB_8888
    )
    return bitmap.asImageBitmap()
}

@Composable
fun rememberAppIcon(context: Context, packageName: String): ImageBitmap? {
    val cached = iconCache.get(packageName)
    if (cached != null) return cached

    var icon by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        icon = withContext(Dispatchers.Default) {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                val optimized = drawable.toImageBitmapOptimized()
                iconCache.put(packageName, optimized)
                optimized
            } catch (_: Exception) { null }
        }
    }

    return icon
}

/** Pre-load icons for all given packages into cache in batch (chunked per frame) */
suspend fun preloadAppIcons(context: Context, packages: List<String>) = withContext(Dispatchers.Default) {
    for (pkg in packages) {
        if (iconCache.get(pkg) != null) continue
        try {
            val icon = context.packageManager.getApplicationIcon(pkg)
            val optimizedIcon = icon.toImageBitmapOptimized()
            iconCache.put(pkg, optimizedIcon)
        } catch (_: Exception) { }
    }
}

fun Context.getAppLabel(packageName: String): String {
    return try {
        val pm = packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        appInfo.loadLabel(pm).toString()
    } catch (e: Exception) {
        packageName.substringAfterLast('.')
    }
}
