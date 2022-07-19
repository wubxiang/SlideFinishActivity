package com.example.slidefinish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.slidefinish.databinding.ItemPage1Binding
import com.example.slidefinish.databinding.ItemPage4Binding

class PageFragment4:Fragment() {
    private val mBinding by lazy { ItemPage4Binding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return mBinding.root
    }
}