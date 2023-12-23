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
package com.shashankmunda.musica.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.shashankmunda.musica.R
import com.shashankmunda.musica.constants.Constants.SONGS
import com.shashankmunda.musica.extensions.toast
import com.shashankmunda.musica.models.Song
import com.shashankmunda.musica.repository.PlaylistRepository
import org.koin.android.ext.android.inject

class CreatePlaylistDialog : DialogFragment() {
    interface PlaylistCreatedCallback {
        fun onPlaylistCreated()
    }

    companion object {
        private const val TAG = "CreatePlaylistDialog"

        fun <T> show(parent: T, song: Song? = null) where T : Fragment, T : PlaylistCreatedCallback {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            show(parent, songs)
        }

        fun <T> show(parent: T, songList: LongArray) where T : Fragment, T : PlaylistCreatedCallback {
            val dialog = CreatePlaylistDialog().apply {
                arguments = Bundle().apply { putLongArray(SONGS, songList) }
                setTargetFragment(parent, 69)
            }
            dialog.show(parent.fragmentManager!!, TAG)
        }
    }

    private val playlistsRepository by inject<PlaylistRepository>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: throw IllegalStateException("Not attached")
        val songs = arguments?.getLongArray(SONGS)

        return MaterialDialog(context).show {
            title(R.string.create_new_playlist)
            positiveButton(R.string.create)
            negativeButton(android.R.string.cancel)

            input(hintRes = R.string.enter_playlist_name, callback = { _, text ->
                val playlistId = playlistsRepository.createPlaylist(text.toString())
                if (playlistId != -1L) {
                    if (songs != null && songs.isNotEmpty()) {
                        val inserted = playlistsRepository.addToPlaylist(playlistId, songs)
                        val message = context.resources.getQuantityString(
                                R.plurals.NNNtrackstoplaylist, inserted, inserted)
                        context.toast(message)
                    } else {
                        context.toast(R.string.playlist_created)
                    }
                    (targetFragment as? PlaylistCreatedCallback)?.onPlaylistCreated()
                } else {
                    context.toast(R.string.unable_create_playlist)
                }
            })

            onDismiss {
                // Make sure the DialogFragment dismisses as well
                this@CreatePlaylistDialog.dismiss()
            }
        }
    }
}
