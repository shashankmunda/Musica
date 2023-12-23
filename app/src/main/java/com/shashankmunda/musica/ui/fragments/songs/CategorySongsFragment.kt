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
package com.shashankmunda.musica.ui.fragments.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashankmunda.musica.R
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_PLAYLIST
import com.shashankmunda.musica.constants.Constants.CATEGORY_SONG_DATA
import com.shashankmunda.musica.databinding.FragmentCategorySongsBinding
import com.shashankmunda.musica.extensions.addOnItemClick
import com.shashankmunda.musica.extensions.argument
import com.shashankmunda.musica.extensions.filter
import com.shashankmunda.musica.extensions.getExtraBundle
import com.shashankmunda.musica.extensions.inflateWithBinding
import com.shashankmunda.musica.extensions.observe
import com.shashankmunda.musica.extensions.safeActivity
import com.shashankmunda.musica.extensions.toSongIds
import com.shashankmunda.musica.models.CategorySongData
import com.shashankmunda.musica.models.Song
import com.shashankmunda.musica.ui.adapters.SongsAdapter
import com.shashankmunda.musica.ui.fragments.base.MediaItemFragment
import com.shashankmunda.musica.util.AutoClearedValue

class CategorySongsFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    private lateinit var categorySongData: CategorySongData
    var binding by AutoClearedValue<FragmentCategorySongsBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categorySongData = argument(CATEGORY_SONG_DATA)
        binding = inflater.inflateWithBinding(R.layout.fragment_category_songs, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.categorySongData = categorySongData

        songsAdapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
            if (categorySongData.type == TYPE_PLAYLIST) {
                playlistId = categorySongData.id
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                val extras = getExtraBundle(songsAdapter.songs.toSongIds(), categorySongData.title)
                mainViewModel.mediaItemClicked(songsAdapter.songs[position], extras)
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    songsAdapter.updateData(list as List<Song>)
                }
    }
}
