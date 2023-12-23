/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.shashankmunda.musica.ui.fragments.base

import android.os.Bundle
import com.shashankmunda.musica.R
import com.shashankmunda.musica.extensions.observe
import com.shashankmunda.musica.extensions.safeActivity
import com.shashankmunda.musica.ui.activities.MainActivity
import com.shashankmunda.musica.ui.fragments.NowPlayingFragment
import com.shashankmunda.musica.ui.viewmodels.MainViewModel
import com.shashankmunda.musica.ui.viewmodels.NowPlayingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class BaseNowPlayingFragment : CoroutineFragment() {

    protected val mainViewModel by sharedViewModel<MainViewModel>()
    protected val nowPlayingViewModel by sharedViewModel<NowPlayingViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nowPlayingViewModel.currentData.observe(viewLifecycleOwner) { showHideBottomSheet() }
    }

    override fun onPause() {
        showHideBottomSheet()
        super.onPause()
    }

    private fun showHideBottomSheet() {
        val activity = safeActivity as MainActivity
        nowPlayingViewModel.currentData.value?.let {
            if (!it.title.isNullOrEmpty()) {
                if (activity.supportFragmentManager.findFragmentById(R.id.container) is NowPlayingFragment) {
                    activity.hideBottomSheet()
                } else {
                    activity.showBottomSheet()
                }
            } else {
                activity.hideBottomSheet()
            }
        }
    }
}
