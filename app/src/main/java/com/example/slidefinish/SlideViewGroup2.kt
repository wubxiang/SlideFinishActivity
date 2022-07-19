package com.example.slidefinish

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class SlideViewGroup2 @JvmOverloads constructor(
    val mContext: Context,
    attrs: AttributeSet? = null
) :
    FrameLayout(mContext, attrs) {

    private val DEFAULT_GUTTER_SIZE = 16 // dips
    private val INVALID_POINTER = -1

    val SCROLL_STATE_IDLE = 0

    /**
     * Indicates that the pager is currently being dragged by the user.
     */
    val SCROLL_STATE_DRAGGING = 1

    /**
     * Indicates that the pager is in the process of settling to a final position.
     */
    val SCROLL_STATE_SETTLING = 2

    var decorView: View = (mContext as Activity).window.decorView
    var screenWidth = 0f

    private var mIsBeingDragged = false
    private var mIsUnableToDrag = false
    private var mTouchSlop = 0
    private var mDefaultGutterSize = 0
    private var mGutterSize = 0

    /**
     * Position of the last motion event.
     */
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f

    private var mActivePointerId = INVALID_POINTER

    private var mVelocityTracker: VelocityTracker? = null

    private var mScrollState = SCROLL_STATE_IDLE

    private var mIsScrollStarted = false

    private val mFakeDragging = false

    private var mMaximumVelocity = 0


    init {
        screenWidth = ScreenUtil.getScreenWidth(mContext).toFloat()

        val configuration = ViewConfiguration.get(mContext)
        mTouchSlop = configuration.scaledPagingTouchSlop

        val density = mContext.resources.displayMetrics.density
        mDefaultGutterSize = (DEFAULT_GUTTER_SIZE * density).toInt()

        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val maxGutterSize = measuredWidth / 10
        mGutterSize = maxGutterSize.coerceAtMost(mDefaultGutterSize)


    }

    var startX = 0f
    var startY: Float = 0f
    var endX: Float = 0f
    var endY: Float = 0f
    var distanceX: Float = 0f
    var distanceY: Float = 0f

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        Log.e("SlideViewGroup", "dispatchTouchEvent")
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x
                startY = ev.y
            }
        }
        return super.dispatchTouchEvent(ev)
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
            MotionEvent.ACTION_MOVE -> {

                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionY is set to the y value
                * of the down event.
                */
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
//                    break
                } else {
                    val pointerIndex: Int = event.findPointerIndex(activePointerId)
                    val x: Float = event.getX(pointerIndex)
                    val dx: Float = x - mLastMotionX
                    val xDiff = abs(dx)
                    val y: Float = event.getY(pointerIndex)
                    val yDiff: Float = abs(y - mInitialMotionY)

                    if (dx != 0f && !isGutterDrag(mLastMotionX, dx)
                        && canScroll(this, false, dx.toInt(), x.toInt(), y.toInt())
                    ) {
                        // Nested view has scrollable area under this point. Let it be handled there.
                        mLastMotionX = x
                        mLastMotionY = y
                        mIsUnableToDrag = true
                        return false
                    }
                    if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                        mIsBeingDragged = true
                        requestParentDisallowInterceptTouchEvent(true)
                        setScrollState(ViewPager.SCROLL_STATE_DRAGGING)
                        mLastMotionX =
                            if (dx > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                        mLastMotionY = y
                    } else if (yDiff > mTouchSlop) {
                        // The finger has moved enough in the vertical
                        // direction to be counted as a drag...  abort
                        // any attempt to drag horizontally, to work correctly
                        // with children that have scrolling containers.
                        mIsUnableToDrag = true
                    }
                    if (mIsBeingDragged) {
                        // Scroll to follow the motion event
//                        if (performDrag(x)) {
//                            ViewCompat.postInvalidateOnAnimation(this)
//                        }
                    }
                }
            }
            MotionEvent.ACTION_DOWN -> {

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mInitialMotionX = event.x
                mLastMotionX = mInitialMotionX
                mInitialMotionY = event.y
                mLastMotionY = mInitialMotionY
                mActivePointerId = event.getPointerId(0)
                mIsUnableToDrag = false
                mIsScrollStarted = true

                if (mScrollState == SCROLL_STATE_SETTLING) {
                    mIsBeingDragged = true
                    requestParentDisallowInterceptTouchEvent(true)
                    setScrollState(SCROLL_STATE_DRAGGING)
                } else {
//                    completeScroll(false)
                    mIsBeingDragged = false
                }
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mIsBeingDragged
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.e("SlideViewGroup", "onTouchEvent")

        if (mFakeDragging) {
            // A fake drag is in progress already, ignore this real one
            // but still eat the touch events.
            // (It is likely that the user is multi-touching the screen.)
            return true
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)

        val action: Int = event.action

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                mInitialMotionX = event.x
                mLastMotionX = mInitialMotionX
                mInitialMotionY = event.y
                mLastMotionY = mInitialMotionY
                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsBeingDragged) {
                    val pointerIndex: Int = event.findPointerIndex(mActivePointerId)
                    if (pointerIndex == -1) {
                        // A child has consumed some touch events and put us into an inconsistent
                        // state.
                        resetTouch()
//                        break
                    } else {
                        val x: Float = event.getX(pointerIndex)
                        val xDiff = abs(x - mLastMotionX)
                        val y: Float = event.getY(pointerIndex)
                        val yDiff = abs(y - mLastMotionY)

                        if (xDiff > mTouchSlop && xDiff > yDiff) {
                            mIsBeingDragged = true
                            requestParentDisallowInterceptTouchEvent(true)
                            mLastMotionX =
                                if (x - mInitialMotionX > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                            mLastMotionY = y
                            setScrollState(ViewPager.SCROLL_STATE_DRAGGING)

                            // Disallow Parent Intercept, just in case
                            val parent = parent
                            parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
                // Not else! Note that mIsBeingDragged can be set above.
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    val activePointerIndex: Int = event.findPointerIndex(mActivePointerId)
                    val x: Float = event.getX(activePointerIndex)
                    performDrag(x)

                    endX = event.x
                    endY = event.y
                    distanceX = abs(endX - startX)
                    distanceY = abs(endY - startY)
                    //1.判断手势右滑  2.横向滑动的距离要大于竖向滑动的距离
                    if (endX - startX > 0 && distanceY < distanceX) {
                        // 右滑
                        decorView.x = distanceX
                    }
                }
            }
            MotionEvent.ACTION_UP -> if (mIsBeingDragged) {
                val velocityTracker = mVelocityTracker!!
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())

                resetTouch()

                endX = event.x
                distanceX = abs(endX - startX)
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
                } else if (endX - startX < 0 && distanceY < distanceX) {
                    // 左滑
                    Toast.makeText(context, "左滑", Toast.LENGTH_SHORT).show()
                }
            }
            MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged) {
                resetTouch()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index: Int = event.getActionIndex()
                val x: Float = event.getX(index)
                mLastMotionX = x
                mActivePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                mLastMotionX = event.getX(event.findPointerIndex(mActivePointerId))
            }
        }
        return true


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                // Remember where the motion event started
                mLastMotionX = event.x.also { mInitialMotionX = it }
                mLastMotionY = event.y.also { mInitialMotionY = it }
                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                endY = event.y
                distanceX = abs(endX - mLastMotionX)
                distanceY = abs(endY - mLastMotionY)
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

    private fun isGutterDrag(x: Float, dx: Float): Boolean {
        return x < mGutterSize && dx > 0 || x > width - mGutterSize && dx < 0
    }

    private fun resetTouch(): Boolean {
        mActivePointerId = INVALID_POINTER
        endDrag()
        return false
    }

    private fun endDrag() {
        mIsBeingDragged = false
        mIsUnableToDrag = false
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
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

    fun setScrollState(newState: Int) {
        if (mScrollState == newState) {
            return
        }
        mScrollState = newState
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
            if (mVelocityTracker != null) {
                mVelocityTracker!!.clear()
            }
        }
    }

    private fun performDrag(x: Float): Boolean {
        val deltaX = mLastMotionX - x
        mLastMotionX = x
        val oldScrollX = scrollX.toFloat()
        var scrollX = oldScrollX + deltaX

        // Don't lose the rounded component
        mLastMotionX += scrollX - scrollX.toInt()

        return false
    }

    private fun getClientWidth(): Int {
        return measuredWidth - paddingLeft - paddingRight
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
        valueAnimator.addUpdateListener { animation ->
            decorView.x = animation.animatedValue as Float
        }
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