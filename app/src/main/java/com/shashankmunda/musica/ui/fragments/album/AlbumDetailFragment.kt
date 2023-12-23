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
package com.shashankmunda.musica.ui.fragments.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashankmunda.musica.R
import com.shashankmunda.musica.constants.Constants.ALBUM
import com.shashankmunda.musica.databinding.FragmentAlbumDetailBinding
import com.shashankmunda.musica.extensions.addOnItemClick
import com.shashankmunda.musica.extensions.argument
import com.shashankmunda.musica.extensions.filter
import com.shashankmunda.musica.extensions.getExtraBundle
import com.shashankmunda.musica.extensions.inflateWithBinding
import com.shashankmunda.musica.extensions.observe
import com.shashankmunda.musica.extensions.safeActivity
import com.shashankmunda.musica.extensions.toSongIds
import com.shashankmunda.musica.models.Album
import com.shashankmunda.musica.models.Song
import com.shashankmunda.musica.ui.adapters.SongsAdapter
import com.shashankmunda.musica.ui.fragments.base.MediaItemFragment
import com.shashankmunda.musica.util.AutoClearedValue

class AlbumDetailFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    lateinit var album: Album
    var binding by AutoClearedValue<FragmentAlbumDetailBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        album = arguments?.getParcelable(ALBUM)!!
        binding = inflater.inflateWithBinding(R.layout.fragment_album_detail, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.album = album

        songsAdapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                val extras = getExtraBundle(songsAdapter.songs.toSongIds(), album.title)
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
