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
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.shashankmunda.musica.R
import com.shashankmunda.musica.models.Song
import com.shashankmunda.musica.constants.Constants.SONGS
import com.shashankmunda.musica.extensions.toast
import com.shashankmunda.musica.repository.SongsRepository
import com.shashankmunda.musica.util.Utils
import org.koin.android.ext.android.inject

class DeleteSongDialog : DialogFragment() {
    interface OnSongDeleted {
        fun onSongDeleted(songId: Long)
    }

    private val songsRepository by inject<SongsRepository>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireActivity()).show {
            title(R.string.delete_song_prompt)
            positiveButton(R.string.delete) {
                val songs = arguments?.getLongArray(SONGS) ?: return@positiveButton
                val deleted = songsRepository.deleteTracks(songs)
                val message = Utils.makeLabel(context, R.plurals.NNNtracksdeleted, deleted)
                activity.toast(message)
                (activity as? OnSongDeleted)?.onSongDeleted(songs.single())
            }
            negativeButton(android.R.string.cancel)
            onDismiss {
                // Make sure the DialogFragment dismisses as well
                this@DeleteSongDialog.dismiss()
            }
        }
    }

    companion object {
        private const val TAG = "DeleteSongDialog"

        fun <T> show(activity: T, song: Song? = null) where T : FragmentActivity, T : OnSongDeleted {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            show(activity, songs)
        }

        fun <T> show(activity: T, songList: LongArray) where T : FragmentActivity, T : OnSongDeleted {
            val dialog = DeleteSongDialog().apply {
                arguments = Bundle().apply { putLongArray(SONGS, songList) }
            }
            dialog.show(activity.supportFragmentManager, TAG)
        }
    }
}
