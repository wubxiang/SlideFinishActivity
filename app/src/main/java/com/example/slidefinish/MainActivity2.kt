package com.example.slidefinish

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.slidefinish.databinding.ActivityMain2Binding


class MainActivity2 : AppCompatActivity() {
    lateinit var decorView: View
    var screenWidth = 0f

    private var mFragmentList = mutableListOf<Fragment>()

    val mBinding:ActivityMain2Binding by lazy { ActivityMain2Binding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 禁用横屏，不设置会跟随上个页面横竖屏状态
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(mBinding.root)
        mBinding.root.post {
            // 反射使activity透明，否则右滑有问题，且不能设置透明主题，否则8.0手机设置屏幕方向会崩溃
            // 如果不需要设置屏幕方向，可以直接使用透明主题，不需要以下方法
            Utils.convertActivityToTranslucent(this)
        }

        decorView = window.decorView
        screenWidth = ScreenUtil.getScreenWidth(this).toFloat()

        mFragmentList.add(PageFragment1())
        mFragmentList.add(PageFragment2())
        mBinding.viewpager.adapter = ViewpagerAdapter(supportFragmentManager, mFragmentList)

        mBinding.tvChild.setOnClickListener {
            Toast.makeText(this, "点击了子view", Toast.LENGTH_SHORT).show()
        }

        mBinding.slideLayout.setListener(object :SlideViewGroupNew.ISlideListener{
            override fun onSlideLeft() {
                TODO("Not yet implemented")
            }

            override fun onSlideRight() {
                finish()
            }
        })
    }

    var startX = 0f
    var startY:Float = 0f
    var endX:Float = 0f
    var endY:Float = 0f
    var distanceX:Float = 0f
    var distanceY:Float = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                startX = event.x
//                startY = event.y
//            }
//            MotionEvent.ACTION_MOVE -> {
//                endX = event.x
//                endY = event.y
//                distanceX = endX - startX
//                distanceY = Math.abs(endY - startY)
//                //1.判断手势右滑  2.横向滑动的距离要大于竖向滑动的距离
//                if (endX - startX > 0 && distanceY < distanceX) {
//                    decorView.setX(distanceX)
//                }
//            }
//            MotionEvent.ACTION_UP -> {
//                endX = event.x
//                distanceX = endX - startX
//                endY = event.y
//                distanceY = Math.abs(endY - startY)
//                //1.判断手势右滑  2.横向滑动的距离要大于竖向滑动的距离 3.横向滑动距离大于屏幕三分之一才能finish
//                if (endX - startX > 0 && distanceY < distanceX && distanceX > ScreenUtil.getScreenWidth(this) / 3) {
//                    moveOn(distanceX)
//                } else if (endX - startX > 0 && distanceY < distanceX) {
//                    backOrigin(distanceX)
//                } else {
//                    decorView.setX(0f)
//                }
//            }
//        }
        return super.onTouchEvent(event)
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
        val valueAnimator = ValueAnimator.ofFloat(distanceX, ScreenUtil.getScreenWidth(this).toFloat())
        valueAnimator.duration = 300
        valueAnimator.start()
        valueAnimator.addUpdateListener { animation -> decorView.setX(animation.animatedValue as Float) }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                finish()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }



//    var mDownX: Float = 0f
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        event?.apply {
//            when (action) {
//                MotionEvent.ACTION_DOWN -> {
//                    mDownX = x
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val moveDistanceX = x - mDownX
//                    if (moveDistanceX > 0) {
//                        window.decorView.x = moveDistanceX
//                    }
//                }
//                MotionEvent.ACTION_UP -> {
//                    val moveDistanceX = x - mDownX
//                    if (moveDistanceX > ScreenUtil.getScreenWidth(this@MainActivity2) / 2) {
//                        finish()
//                    } else {
//                        window.decorView.x = 0f
//                    }
//                }
//            }
//        }
//
//        return super.onTouchEvent(event)
//    }
}