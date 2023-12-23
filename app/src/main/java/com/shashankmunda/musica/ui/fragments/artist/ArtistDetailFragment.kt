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
package com.shashankmunda.musica.ui.fragments.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.shashankmunda.musica.R
import com.shashankmunda.musica.constants.Constants.ARTIST
import com.shashankmunda.musica.databinding.FragmentArtistDetailBinding
import com.shashankmunda.musica.extensions.addOnItemClick
import com.shashankmunda.musica.extensions.argument
import com.shashankmunda.musica.extensions.filter
import com.shashankmunda.musica.extensions.getExtraBundle
import com.shashankmunda.musica.extensions.inflateWithBinding
import com.shashankmunda.musica.extensions.observe
import com.shashankmunda.musica.extensions.safeActivity
import com.shashankmunda.musica.extensions.toSongIds
import com.shashankmunda.musica.models.Artist
import com.shashankmunda.musica.models.Song
import com.shashankmunda.musica.repository.AlbumRepository
import com.shashankmunda.musica.ui.adapters.AlbumAdapter
import com.shashankmunda.musica.ui.adapters.SongsAdapter
import com.shashankmunda.musica.ui.fragments.base.MediaItemFragment
import com.shashankmunda.musica.util.AutoClearedValue
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class ArtistDetailFragment : MediaItemFragment() {
    lateinit var artist: Artist
    var binding by AutoClearedValue<FragmentArtistDetailBinding>(this)

    private val albumRepository by inject<AlbumRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        artist = argument(ARTIST)
        binding = inflater.inflateWithBinding(R.layout.fragment_artist_detail, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.artist = artist

        val adapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(safeActivity)
        binding.recyclerView.adapter = adapter

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    adapter.updateData(list as List<Song>)
                }

        binding.recyclerView.addOnItemClick { position: Int, _: View ->
            val extras = getExtraBundle(adapter.songs.toSongIds(), artist.name)
            mainViewModel.mediaItemClicked(adapter.songs[position], extras)
        }

        setupArtistAlbums()
    }

    private fun setupArtistAlbums() {
        val albumsAdapter = AlbumAdapter(true)
        binding.rvArtistAlbums.apply {
            layoutManager = LinearLayoutManager(safeActivity, HORIZONTAL, false)
            adapter = albumsAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(albumsAdapter.albums[position], null)
            }
        }

        // TODO should this be in a view model?
        launch {
            val albums = withContext(IO) {
                albumRepository.getAlbumsForArtist(artist.id)
            }
            albumsAdapter.updateData(albums)
        }
    }
}
