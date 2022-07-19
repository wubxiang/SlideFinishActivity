package com.example.slidefinish

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

class SlideViewGroup @JvmOverloads constructor(val mContext: Context, attrs: AttributeSet? = null) :
    FrameLayout(mContext, attrs) {
    var decorView: View = (mContext as Activity).window.decorView
    var screenWidth = 0f

    private var mTouchSlop = 0

    init {
        screenWidth = ScreenUtil.getScreenWidth(mContext).toFloat()

        val configuration = ViewConfiguration.get(mContext)
        mTouchSlop = configuration.scaledPagingTouchSlop
    }


    var startX = 0f
    var startY: Float = 0f
    var endX: Float = 0f
    var endY: Float = 0f
    var distanceX: Float = 0f
    var distanceY: Float = 0f

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.e("SlideViewGroup", "dispatchTouchEvent")
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.e("SlideViewGroup", "onInterceptTouchEvent")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y

                Log.e("SlideViewGroup", "onInterceptTouchEvent: DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.e("SlideViewGroup", "onInterceptTouchEvent: MOVE")
                val deltaX = abs(event.x - startX)
                val deltaY = abs(event.y - startY)
                // 左右滑动拦截
                return deltaX > deltaY
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e("SlideViewGroup", "onTouchEvent")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                endY = event.y
                distanceX = endX - startX
                distanceY = abs(endY - startY)
                //1.判断手势右滑  2.横向滑动的距离要大于竖向滑动的距离
                if (endX - startX > 0 && distanceY < distanceX) {
                    decorView.x = distanceX
                }
            }
            MotionEvent.ACTION_UP -> {
                endX = event.x
                distanceX = endX - startX
                endY = event.y
                distanceY = abs(endY - startY)
                //1.判断手势右滑  2.横向滑动的距离要大于竖向滑动的距离 3.横向滑动距离大于屏幕三分之一才能finish
                if (endX - startX > 0 && distanceY < distanceX && distanceX > ScreenUtil.getScreenWidth(
                        mContext
                    ) / 3
                ) {
                    moveOn(distanceX)
                } else if (endX - startX > 0 && distanceY < distanceX) {
                    backOrigin(distanceX)
                } else {
                    decorView.x = 0f
                }
            }
        }
        return true
    }

    /**
     * 返回原点
     * @param distanceX 横向滑动距离
     */
    private fun backOrigin(distanceX: Float) {
        ObjectAnimator.ofFloat(decorView, "X", distanceX, 0f).setDuration(300).start()
    }

    /**
     * 划出屏幕
     * @param distanceX 横向滑动距离
     */
    private fun moveOn(distanceX: Float) {
        val valueAnimator = ValueAnimator.ofFloat(distanceX, screenWidth)
        valueAnimator.duration = 300
        valueAnimator.start()
        valueAnimator.addUpdateListener { animation -> decorView.x = animation.animatedValue as Float }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                (mContext as Activity).finish()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}