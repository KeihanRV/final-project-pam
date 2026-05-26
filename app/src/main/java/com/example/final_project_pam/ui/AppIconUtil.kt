package com.example.final_project_pam.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Cache untuk menyimpan icon agar tidak perlu me-load ulang dari sistem berkali-kali.
// Ini sangat krusial untuk mencegah Force Close / OOM saat scroll cepat.
private val iconCache = LruCache<String, ImageBitmap>(50) // Simpan 50 icon terakhir di memori

/**
 * Mengonversi Drawable ke ImageBitmap dengan ukuran kecil (100x100) agar hemat RAM.
 */
fun Drawable.toImageBitmapOptimized(): ImageBitmap {
    val bitmap = toBitmap(
        width = 100,
        height = 100,
        config = Bitmap.Config.ARGB_8888
    )
    return bitmap.asImageBitmap()
}

@Composable
fun rememberAppIcon(context: Context, packageName: String): ImageBitmap? {
    // Cek apakah icon sudah ada di cache
    val cachedIcon = iconCache.get(packageName)
    if (cachedIcon != null) return cachedIcon

    return produceState<ImageBitmap?>(initialValue = null, packageName) {
        // Pindahkan proses pengambilan icon ke Background Thread (IO)
        // agar scrolling tidak patah-patah atau force close (ANR)
        value = withContext(Dispatchers.IO) {
            try {
                val icon = context.packageManager.getApplicationIcon(packageName)
                val optimizedIcon = icon.toImageBitmapOptimized()
                
                // Simpan ke cache untuk penggunaan berikutnya
                iconCache.put(packageName, optimizedIcon)
                
                optimizedIcon
            } catch (e: Exception) {
                null
            }
        }
    }.value
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
