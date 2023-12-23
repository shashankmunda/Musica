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
package com.shashankmunda.musica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shashankmunda.musica.R
import com.shashankmunda.musica.constants.Constants.ARTIST
import com.shashankmunda.musica.constants.Constants.SONG
import com.shashankmunda.musica.databinding.FragmentLyricsBinding
import com.shashankmunda.musica.extensions.argument
import com.shashankmunda.musica.extensions.disposeOnDetach
import com.shashankmunda.musica.extensions.inflateWithBinding
import com.shashankmunda.musica.extensions.ioToMain
import com.shashankmunda.musica.extensions.subscribeForOutcome
import com.shashankmunda.musica.network.Outcome
import com.shashankmunda.musica.network.api.LyricsRestService
import com.shashankmunda.musica.ui.fragments.base.BaseNowPlayingFragment
import com.shashankmunda.musica.util.AutoClearedValue
import org.koin.android.ext.android.inject

class LyricsFragment : BaseNowPlayingFragment() {
    companion object {
        fun newInstance(artist: String, title: String): LyricsFragment {
            return LyricsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARTIST, artist)
                    putString(SONG, title)
                }
            }
        }
    }

    private lateinit var artistName: String
    lateinit var songTitle: String
    var binding by AutoClearedValue<FragmentLyricsBinding>(this)

    private val lyricsService by inject<LyricsRestService>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_lyrics, container)
        artistName = argument(ARTIST)
        songTitle = argument(SONG)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.songTitle = songTitle

        // TODO make the lyrics handler/repo injectable
        lyricsService.getLyrics(artistName, songTitle)
                .ioToMain()
                .subscribeForOutcome { outcome ->
                    when (outcome) {
                        is Outcome.Success -> binding.lyrics = outcome.data
                        else -> {}
                    }
                }
                .disposeOnDetach(view)
    }
}
