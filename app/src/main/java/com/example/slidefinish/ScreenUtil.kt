package com.example.slidefinish

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

/**
 * Author: wbx
 * Date: 2020/8/25
 * Description:
 */
object ScreenUtil {
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.x
    }

    @JvmStatic
    fun getScreenWidth2(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getRealSize(point)
        return point.x
    }


    /**
     * 不包括状态栏、不包括底部导航栏的高度
     */
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        Log.e("height", "getsize; ${point.y.toString()}")
        return point.y
    }

    /**
     * 不包括状态栏、不包括底部导航栏的高度
     */
    fun getScreenHeight2(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outSize = Rect()
        windowManager.defaultDisplay.getRectSize(outSize)
        Log.e("height", "getRectSize; ${outSize.bottom.toString()}")
        return outSize.bottom
    }

    /**
     * 不包括状态栏、不包括底部导航栏的高度
     */
    fun getScreenHeight3(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        Log.e("height", "getMetrics; ${outMetrics.heightPixels.toString()}")
        return outMetrics.heightPixels
    }

    /**
     * 包括状态栏、底部导航栏的高度
     */
    fun getScreenHeight4(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getRealSize(point)
        Log.e("height", "getRealSize; ${point.y.toString()}")
        return point.y
    }

    /**
     * 包括状态栏、底部导航栏的高度
     */
    fun getScreenHeight5(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(outMetrics)
        Log.e("height", "getRealMetrics; ${outMetrics.heightPixels.toString()}")
        return outMetrics.heightPixels
    }


    /**
     * 包括状态栏、不包括底部导航栏的高度
     */
    fun getScreenHeight6(context: Context): Int {
        val outSize = Rect()
        (context as Activity).window.decorView.getWindowVisibleDisplayFrame(outSize)
        Log.e("height", "DisplayFrame; ${outSize.bottom}")
        return outSize.bottom
    }


    /**view截图*/
    @JvmStatic
    fun takeScreenShot2(viewGroup: ViewGroup): Bitmap? {
        var bitmap: Bitmap? = null
        bitmap = Bitmap.createBitmap(viewGroup.getWidth(), viewGroup.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        viewGroup.draw(canvas)
        return bitmap
    }

    /**全屏截图*/
    @JvmStatic
    fun takeScreenShot3(activity: Activity): Bitmap? {
        val view: View = activity.getWindow().getDecorView()
        var bitmap: Bitmap? = null
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}