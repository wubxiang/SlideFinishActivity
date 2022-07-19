package com.example.slidefinish

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs
/**
 * 左右滑动不拦截事件
 */

class SlideViewPager(val mContext: Context, attrs: AttributeSet? = null) :
    ViewPager(mContext, attrs) {
    val TAG = "SlideViewPager"

    var startX = 0f
    var startY = 0f
    var endX = 0f
    var endY = 0f
    var distanceX = 0f
    var distanceY = 0f
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                startX = event.x
//                startY = event.y
//
//                // 保证子View能够接收到Action_move事件
//                parent.requestDisallowInterceptTouchEvent(true)
//            }
//            MotionEvent.ACTION_MOVE -> {
//                endX = event.x
//                endY = event.y
//                distanceX = abs(endX - startX)
//                distanceY = abs(endY - startY)
//                val curPosition = this.currentItem
//                val count = this.adapter!!.count
//                Log.i(TAG, "curPosition:=$curPosition")
//                // 当前页面在最后一页和第0页的时候，由父亲拦截触摸事件
//                if (curPosition == 0) {
//                    if (endX - startX > 0 && distanceY < distanceX) {
//                        // 右滑
//                        parent.requestDisallowInterceptTouchEvent(false)
//                    }else{
//                        parent.requestDisallowInterceptTouchEvent(true)
//                    }
//                } else if (curPosition == count - 1) {
//                    if (endX - startX < 0 && distanceY < distanceX) {
//                        // 右滑
//                        parent.requestDisallowInterceptTouchEvent(false)
//                    }else{
//                        parent.requestDisallowInterceptTouchEvent(true)
//                    }
//                } else { //其他情况，由孩子拦截触摸事件
//                    parent.requestDisallowInterceptTouchEvent(true)
//                }
//            }
//            MotionEvent.ACTION_CANCEL -> {}
//            MotionEvent.ACTION_UP -> {}
//        }
        return super.dispatchTouchEvent(event)
    }
}