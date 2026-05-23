package com.example.final_project_pam.ui

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.content.Context
import android.os.Build
import androidx.core.graphics.drawable.toBitmap

fun Drawable.toImageBitmap(): ImageBitmap {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        toBitmap()
    } else {
        if (this is BitmapDrawable) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            val bmp = Bitmap.createBitmap(intrinsicWidth.takeIf { it > 0 } ?: 48,
                intrinsicHeight.takeIf { it > 0 } ?: 48, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bmp
        }
    }
    return bitmap.asImageBitmap()
}

@Composable
fun rememberAppIcon(context: Context, packageName: String): ImageBitmap? {
    return remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName).toImageBitmap()
        } catch (e: Exception) {
            null
        }
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
