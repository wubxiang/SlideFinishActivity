package com.example.slidefinish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.slidefinish.databinding.ItemPage1Binding
import com.example.slidefinish.databinding.ItemPage2Binding

class PageFragment2:Fragment() {
    private val mBinding by lazy { ItemPage2Binding.inflate(layoutInflater) }

    private var mFragmentList = mutableListOf<Fragment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mFragmentList.add(PageFragment3())
        mFragmentList.add(PageFragment4())
        mBinding.viewpager.adapter = ViewpagerAdapter(childFragmentManager, mFragmentList)
        return mBinding.root
    }
}