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
import androidx.lifecycle.map
import com.shashankmunda.musica.constants.Constants.ACTION_REMOVED_FROM_PLAYLIST
import com.shashankmunda.musica.constants.Constants.ACTION_SONG_DELETED
import com.shashankmunda.musica.constants.Constants.ALBUM
import com.shashankmunda.musica.constants.Constants.ARTIST
import com.shashankmunda.musica.constants.Constants.CATEGORY_SONG_DATA
import com.shashankmunda.musica.extensions.argumentOrEmpty
import com.shashankmunda.musica.models.CategorySongData
import com.shashankmunda.musica.models.Genre
import com.shashankmunda.musica.models.MediaID
import com.shashankmunda.musica.models.Playlist
import com.shashankmunda.musica.playback.TimberMusicService.Companion.MEDIA_CALLER
import com.shashankmunda.musica.playback.TimberMusicService.Companion.MEDIA_ID_ARG
import com.shashankmunda.musica.playback.TimberMusicService.Companion.MEDIA_TYPE_ARG
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALBUM
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALL_ALBUMS
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALL_ARTISTS
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALL_FOLDERS
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALL_GENRES
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALL_PLAYLISTS
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ALL_SONGS
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_ARTIST
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_GENRE
import com.shashankmunda.musica.playback.TimberMusicService.Companion.TYPE_PLAYLIST
import com.shashankmunda.musica.ui.fragments.FolderFragment
import com.shashankmunda.musica.ui.fragments.GenreFragment
import com.shashankmunda.musica.ui.fragments.PlaylistFragment
import com.shashankmunda.musica.ui.fragments.album.AlbumDetailFragment
import com.shashankmunda.musica.ui.fragments.album.AlbumsFragment
import com.shashankmunda.musica.ui.fragments.artist.ArtistDetailFragment
import com.shashankmunda.musica.ui.fragments.artist.ArtistFragment
import com.shashankmunda.musica.ui.fragments.songs.CategorySongsFragment
import com.shashankmunda.musica.ui.fragments.songs.SongsFragment
import com.shashankmunda.musica.ui.viewmodels.MediaItemFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

open class MediaItemFragment : BaseNowPlayingFragment() {

    protected lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private lateinit var mediaType: String
    private var mediaId: String? = null
    private var caller: String? = null

    companion object {
        fun newInstance(mediaId: MediaID): MediaItemFragment {
            val args = Bundle().apply {
                putString(MEDIA_TYPE_ARG, mediaId.type)
                putString(MEDIA_ID_ARG, mediaId.mediaId)
                putString(MEDIA_CALLER, mediaId.caller)
            }
            return when (mediaId.type?.toInt()) {
                TYPE_ALL_SONGS -> SongsFragment().apply { arguments = args }
                TYPE_ALL_ALBUMS -> AlbumsFragment().apply { arguments = args }
                TYPE_ALL_PLAYLISTS -> PlaylistFragment().apply { arguments = args }
                TYPE_ALL_ARTISTS -> ArtistFragment().apply { arguments = args }
                TYPE_ALL_FOLDERS -> FolderFragment().apply { arguments = args }
                TYPE_ALL_GENRES -> GenreFragment().apply { arguments = args }
                TYPE_ALBUM -> AlbumDetailFragment().apply {
                    arguments = args.apply { putParcelable(ALBUM, mediaId.mediaItem) }
                }
                TYPE_ARTIST -> ArtistDetailFragment().apply {
                    arguments = args.apply { putParcelable(ARTIST, mediaId.mediaItem) }
                }
                TYPE_PLAYLIST -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Playlist).apply {
                            val data = CategorySongData(name, songCount, TYPE_PLAYLIST, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                TYPE_GENRE -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Genre).apply {
                            val data = CategorySongData(name, songCount, TYPE_GENRE, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                else -> SongsFragment().apply {
                    arguments = args
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaType = argumentOrEmpty(MEDIA_TYPE_ARG)
        mediaId = argumentOrEmpty(MEDIA_ID_ARG)
        caller = argumentOrEmpty(MEDIA_CALLER)

        val mediaId = MediaID(mediaType, mediaId, caller)
        mediaItemFragmentViewModel = getViewModel { parametersOf(mediaId) }

        mainViewModel.customAction
                .map { it.getContentIfNotHandled() }
                .observe(viewLifecycleOwner) {
                    when (it) {
                        ACTION_SONG_DELETED -> mediaItemFragmentViewModel.reloadMediaItems()
                        ACTION_REMOVED_FROM_PLAYLIST -> mediaItemFragmentViewModel.reloadMediaItems()
                    }
                }
    }
}
