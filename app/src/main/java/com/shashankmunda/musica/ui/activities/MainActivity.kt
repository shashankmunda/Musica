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
package com.shashankmunda.musica.ui.activities

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore.EXTRA_MEDIA_TITLE
import android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.lifecycle.map
import androidx.mediarouter.app.MediaRouteButton
import com.afollestad.rxkprefs.Pref
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.shashankmunda.musica.PREF_APP_THEME
import com.shashankmunda.musica.R
import com.shashankmunda.musica.constants.AppThemes
import com.shashankmunda.musica.databinding.MainActivityBinding
import com.shashankmunda.musica.extensions.addFragment
import com.shashankmunda.musica.extensions.attachLifecycle
import com.shashankmunda.musica.extensions.filter
import com.shashankmunda.musica.extensions.hide
import com.shashankmunda.musica.extensions.replaceFragment
import com.shashankmunda.musica.extensions.show
import com.shashankmunda.musica.models.MediaID
import com.shashankmunda.musica.repository.SongsRepository
import com.shashankmunda.musica.ui.activities.base.PermissionsActivity
import com.shashankmunda.musica.ui.dialogs.DeleteSongDialog
import com.shashankmunda.musica.ui.fragments.BottomControlsFragment
import com.shashankmunda.musica.ui.fragments.MainFragment
import com.shashankmunda.musica.ui.fragments.base.MediaItemFragment
import com.shashankmunda.musica.ui.viewmodels.MainViewModel
import com.shashankmunda.musica.ui.widgets.BottomSheetListener
import io.reactivex.functions.Consumer
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : PermissionsActivity(), DeleteSongDialog.OnSongDeleted {

    private val viewModel by viewModel<MainViewModel>()
    private val songsRepository by inject<SongsRepository>()
    private val appThemePref by inject<Pref<AppThemes>>(name = PREF_APP_THEME)

    private lateinit var binding: MainActivityBinding
    private var bottomSheetListener: BottomSheetListener? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appThemePref.get().themeRes)
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                bottomSheetBehavior?.let {
                    if (it.state == STATE_EXPANDED) {
                        collapseBottomSheet()
                    }
                    else{
                        finish()
                    }
                }
            }
        })
        if (!permissionsManager.hasStoragePermission()) {
            permissionsManager.requestStoragePermission().subscribe(Consumer {
                setupUI()
            }).attachLifecycle(this)
            return
        }

        setupUI()
    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener
    }

    fun collapseBottomSheet() {
        bottomSheetBehavior?.state = STATE_COLLAPSED
    }

    fun hideBottomSheet() {
        bottomSheetBehavior?.state = STATE_HIDDEN
    }

    fun showBottomSheet() {
        if (bottomSheetBehavior?.state == STATE_HIDDEN) {
            bottomSheetBehavior?.state = STATE_COLLAPSED
        }
    }

    fun setupCastButton(mediaRouteButton: MediaRouteButton) {
        viewModel.setupCastButton(mediaRouteButton)
    }

    override fun onResume() {
        viewModel.setupCastSession()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseCastSession()
    }

    override fun onSongDeleted(songId: Long) {
        viewModel.onSongDeleted(songId)
    }

    private fun setupUI() {
        viewModel.rootMediaId.observe(this) {
            replaceFragment(fragment = MainFragment())
            Handler(Looper.getMainLooper()).postDelayed({
                replaceFragment(
                        R.id.bottomControlsContainer,
                        BottomControlsFragment()
                )
            }, 150)

            //handle playback intents, (search intent or ACTION_VIEW intent)
            handlePlaybackIntent(intent)
        }

        viewModel.navigateToMediaItem
                .map { it.getContentIfNotHandled() }
                .filter { it != null }
                .observe(this) { navigateToMediaItem(it!!) }

        binding.let {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }
        val parentThatHasBottomSheetBehavior = binding.bottomSheetParent as FrameLayout? ?: return

        bottomSheetBehavior = BottomSheetBehavior.from(parentThatHasBottomSheetBehavior)
        bottomSheetBehavior?.isHideable = true
        bottomSheetBehavior?.setBottomSheetCallback(BottomSheetCallback())

        binding.dimOverlay.setOnClickListener { collapseBottomSheet() }
    }

    private fun navigateToMediaItem(mediaId: MediaID) {
        if (getBrowseFragment(mediaId) == null) {
            val fragment = MediaItemFragment.newInstance(mediaId)
            addFragment(
                    fragment = fragment,
                    tag = mediaId.type,
                    addToBackStack = !isRootId(mediaId)
            )
        }
    }

    private fun handlePlaybackIntent(intent: Intent?) {
        if (intent == null || intent.action == null) return

        when (intent.action!!) {
            INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH -> {
                val songTitle = intent.extras?.getString(EXTRA_MEDIA_TITLE, null)
                viewModel.transportControls().playFromSearch(songTitle, null)
            }
            ACTION_VIEW -> {
                val path = getIntent().data?.path ?: return
                val song = songsRepository.getSongFromPath(path)
                viewModel.mediaItemClicked(song, null)
            }
        }
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == STATE_DRAGGING || newState == STATE_EXPANDED) {
                binding.dimOverlay.show()
            } else if (newState == STATE_COLLAPSED) {
                binding.dimOverlay.hide()
            }
            bottomSheetListener?.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                binding.dimOverlay.alpha = slideOffset
            } else if (slideOffset == 0f) {
                binding.dimOverlay.hide()
            }
            bottomSheetListener?.onSlide(bottomSheet, slideOffset)
        }
    }

    private fun isRootId(mediaId: MediaID) = mediaId.type == viewModel.rootMediaId.value?.type

    private fun getBrowseFragment(mediaId: MediaID): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId.type) as MediaItemFragment?
    }
}
