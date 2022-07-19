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
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.min

/**
 * 支持左右滑动，且可不拦截左滑
 * 兼容viewpager嵌套滑动
 * 需要设置activity主题透明（8.0系统设置主题透明后，不能设置屏幕方向，否则会崩溃）
 * 使用SlideExitUtils中的方法convertActivityToTranslucent()来使activity透明
 */
class SlideViewGroupNew @JvmOverloads constructor(
    val mContext: Context,
    attrs: AttributeSet? = null
) : FrameLayout(mContext, attrs) {

    var mDecorView: View = (mContext as Activity).window.decorView
    var screenWidth = 0f

    private var mIsBeingDragged = false
    private var mIsUnableToDrag = false

    // 手指移动阈值
    private var mTouchSlop = 0
    private var mDefaultGutterSize = 0

    // 默认边缘拖拽阈值
    private val DEFAULT_GUTTER_SIZE = 16 // dp

    // 判断是否边缘拖拽阈值
    private var mGutterSize = 0

    /**
     * Position of the last motion event.
     */
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f

    // 是否右滑
    private var mIsRightSlide = false

    // 是否拦截左滑
    private var mInterceptLeftSlide = false

    private var mListener: ISlideListener? = null

    init {
        screenWidth = ScreenUtil.getScreenWidth(mContext).toFloat()

        val configuration = ViewConfiguration.get(mContext)
        mTouchSlop = configuration.scaledPagingTouchSlop

        val density = mContext.resources.displayMetrics.density
        mDefaultGutterSize = (DEFAULT_GUTTER_SIZE * density).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val maxGutterSize = measuredWidth / 10
        mGutterSize = min(maxGutterSize, mDefaultGutterSize)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.e("SlideViewGroup", "onInterceptTouchEvent")

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */
        val action: Int = event.action and MotionEvent.ACTION_MASK

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            resetTouch()
            return false
        }

        // Nothing more to do here if we have decided whether or not we
        // are dragging.
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) {
                return true
            }
            if (mIsUnableToDrag) {
                return false
            }
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                Log.e("SlideViewGroup", "onInterceptTouchEvent: DOWN")
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mInitialMotionX = event.x
                mInitialMotionY = event.y
                mLastMotionX = mInitialMotionX
                mLastMotionY = mInitialMotionY

                mIsUnableToDrag = false
                mIsBeingDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                Log.e("SlideViewGroup", "onInterceptTouchEvent: MOVE")
                // 如果没有嵌套滑动的view，则不会走这里（当前的view消费了down事件）
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */
                val x: Float = event.x
                val y: Float = event.y
                val dx: Float = x - mLastMotionX
                val xDiff = abs(dx)
                val yDiff: Float = abs(y - mInitialMotionY)

                if (dx != 0f && !isGutterDrag(mLastMotionX, dx)
                    && canScroll(this, false, dx.toInt(), x.toInt(), y.toInt())
                ) {
                    // 嵌套滑动的view还可以滑动，则不拦截事件
                    // Nested view has scrollable area under this point. Let it be handled there.
                    mLastMotionX = x
                    mLastMotionY = y
                    mIsUnableToDrag = true
                    return false
                }

                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                    if (dx >= 0 || (dx < 0 && mInterceptLeftSlide)) {
                        // 右滑或者支持左滑事件拦截时左滑
                        // 只有滑动距离大于滑动阈值后才算滑动
                        mIsBeingDragged = true
                        mLastMotionX =
                            if (dx > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                        mLastMotionY = y
                        requestParentDisallowInterceptTouchEvent(true)
                    } else {
                        //左滑不支持
                        mIsUnableToDrag = true
                    }
                } else if (yDiff > mTouchSlop) {
                    // 如果先垂直滑动再横向滑动，则不算水平滑动
                    // The finger has moved enough in the vertical
                    // direction to be counted as a drag...  abort
                    // any attempt to drag horizontally, to work correctly
                    // with children that have scrolling containers.
                    mIsUnableToDrag = true
                }
            }
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mIsBeingDragged
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e("SlideViewGroup", "onTouchEvent")

        if (event.action == MotionEvent.ACTION_DOWN && event.edgeFlags != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false
        }

        if (mIsUnableToDrag) {
            return false
        }

        val action: Int = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                Log.e("SlideViewGroup", "onTouchEvent: MOVE")
                if (!mIsBeingDragged) {
                    val x: Float = event.x
                    val y: Float = event.y
                    val dx: Float = x - mLastMotionX
                    val xDiff = abs(x - mLastMotionX)
                    val yDiff = abs(y - mLastMotionY)

                    if (xDiff > mTouchSlop && xDiff > yDiff) {
                        if (dx >= 0 || (dx < 0 && mInterceptLeftSlide)) {
                            mIsBeingDragged = true
                            mLastMotionX =
                                if (x - mInitialMotionX > 0) mInitialMotionX + mTouchSlop
                                else mInitialMotionX - mTouchSlop
                            mLastMotionY = y

                            // Disallow Parent Intercept, just in case
                            parent?.requestDisallowInterceptTouchEvent(true)
                        } else {
                            //左滑不支持
                            mIsUnableToDrag = true
                        }
                    } else if (yDiff > mTouchSlop) {
                        // 如果先垂直滑动再横向滑动，则不算水平滑动
                        mIsUnableToDrag = true
                    }
                }
                // Not else! Note that mIsBeingDragged can be set above.
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    val x: Float = event.x
                    val y: Float = event.y
                    mLastMotionX = x
                    mLastMotionY = y

                    val distanceX = abs(x - mInitialMotionX)
                    val distanceY = abs(y - mInitialMotionY)
                    if (mIsRightSlide) {
                        // 右滑
                        mDecorView.x = x - mInitialMotionX - mTouchSlop
                    } else {
                        if (x - mInitialMotionX > mTouchSlop && distanceY < distanceX) {
                            mIsRightSlide = true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 当手指还没有抬起时，如果有新app启动，ACTION_UP不会触发，ACTION_CANCEL会出发
                Log.e("SlideViewGroup", "onTouchEvent: UP")
                if (mIsBeingDragged) {
                    val endX = event.x
                    val endY = event.y
                    val xDiff = endX - mInitialMotionX - mTouchSlop
                    val distanceX = abs(endX - mInitialMotionX - mTouchSlop)
                    val distanceY = abs(endY - mInitialMotionY)

                    if (mIsRightSlide) {
                        if (distanceX > ScreenUtil.getScreenWidth(mContext) / 3 && xDiff > 0) {
                            moveOn(distanceX)
                        } else {
                            backOrigin(xDiff)
                        }
                    } else if (endX - mInitialMotionX < 0 && distanceY < distanceX && mInterceptLeftSlide) {
                        // 左滑
//                        Toast.makeText(context, "左滑", Toast.LENGTH_SHORT).show()
                        mListener?.onSlideLeft()
                    }

                    mIsRightSlide = false
                }
                resetTouch()
            }
        }
        return true
    }

    private fun isGutterDrag(x: Float, dx: Float): Boolean {
        // 是否屏幕边缘拖拽
        return x < mGutterSize && dx > 0 || x > width - mGutterSize && dx < 0
    }

    private fun resetTouch(): Boolean {
        endDrag()
        return false
    }

    private fun endDrag() {
        mIsBeingDragged = false
        mIsUnableToDrag = false
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     * or just its children (false).
     * @param dx Delta scrolled in pixels
     * @param x X coordinate of the active touch point
     * @param y Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     *
     * 桥套滑动的view是否还可以滑动
     */
    protected fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val group = v
            val scrollX = v.getScrollX()
            val scrollY = v.getScrollY()
            val count = group.childCount
            // Count backwards - let topmost views consume scroll distance first.
            for (i in count - 1 downTo 0) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                val child = group.getChildAt(i)
                if (x + scrollX >= child.left && x + scrollX < child.right && y + scrollY >= child.top && y + scrollY < child.bottom && canScroll(
                        child, true, dx, x + scrollX - child.left,
                        y + scrollY - child.top
                    )
                ) {
                    return true
                }
            }
        }
        return checkV && v.canScrollHorizontally(-dx)
    }

    private fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }


    /**
     * 返回原点
     * @param distanceX 横向滑动距离
     */
    private fun backOrigin(distanceX: Float) {
        ObjectAnimator.ofFloat(mDecorView, "X", distanceX, 0f).setDuration(300).start()
    }

    /**
     * 划出屏幕
     * @param distanceX 横向滑动距离
     */
    private fun moveOn(distanceX: Float) {
        val valueAnimator = ValueAnimator.ofFloat(distanceX, screenWidth)
        valueAnimator.duration = 300
        valueAnimator.start()
        valueAnimator.addUpdateListener { animation ->
            mDecorView.x = animation.animatedValue as Float
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                mListener?.onSlideRight()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun setListener(listener: ISlideListener) {
        mListener = listener
    }

    interface ISlideListener {
        fun onSlideLeft()
        fun onSlideRight()
    }
}