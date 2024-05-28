package com.example.smarthcms.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.ConnectivityManager
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.util.Timer
import kotlin.concurrent.schedule

class Constants {

    fun load(toDo: () -> Unit) {
        Timer().schedule(3000) {
            toDo.invoke()
        }
    }

    fun createToast(activity: AppCompatActivity, toastText: TextView, msg: View, text: String) {
        msg.visibility = View.VISIBLE
        toastText.text = text
        Constants().load {
            activity.runOnUiThread {
                msg.visibility = View.GONE
            }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected ?: false
    }

    fun networkMsg(root: View) {
        return "Check your internet connection please!".let {
            Snackbar.make(root, it, Snackbar.ANIMATION_MODE_SLIDE)
                .show()
        }
    }

    fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.WHITE

        canvas.drawRoundRect(
            rectF,
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat(),
            paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val sourceRect = Rect(0, 0, bitmap.width, bitmap.height)
        canvas.drawBitmap(bitmap, sourceRect, rect, paint)

        return output
    }
}