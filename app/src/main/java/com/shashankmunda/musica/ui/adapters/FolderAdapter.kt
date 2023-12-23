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
package com.shashankmunda.musica.ui.adapters

import android.app.Activity
import android.os.AsyncTask
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.rxkprefs.Pref
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shashankmunda.musica.R
import com.shashankmunda.musica.extensions.attachLifecycle
import com.shashankmunda.musica.extensions.inflate
import com.shashankmunda.musica.extensions.toSongIds
import com.shashankmunda.musica.models.Song
import com.shashankmunda.musica.repository.FoldersRepository
import com.shashankmunda.musica.repository.SongsRepository
import com.shashankmunda.musica.util.Utils.getAlbumArtUri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

private const val GO_UP = ".."

class FolderAdapter(
    context: Activity,
    private val songsRepository: SongsRepository,
    private val foldersRepository: FoldersRepository,
    private val lastFolderPref: Pref<String>
) : RecyclerView.Adapter<FolderAdapter.ItemHolder>() {

    private val icons = arrayOf(
            getDrawable(context, R.drawable.ic_folder_open_black_24dp)!!,
            getDrawable(context, R.drawable.ic_folder_parent_dark)!!,
            getDrawable(context, R.drawable.ic_file_music_dark)!!,
            getDrawable(context, R.drawable.ic_timer_wait)!!
    )

    private val songsList = mutableListOf<Song>()
    private val root = lastFolderPref.get()

    private var files = emptyList<File>()
    private var rootFolder: File? = null
    private var isBusy = false

    private lateinit var callback: (song: Song, queueIds: LongArray, title: String) -> Unit

    fun init(callback: (song: Song, queueIds: LongArray, title: String) -> Unit) {
        this.callback = callback
        updateDataSetAsync(File(root))
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = viewGroup.inflate<View>(R.layout.item_folder_list)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = files[i]
        val song = songsList[i]
        itemHolder.title.text = localItem.name

        if (localItem.isDirectory) {
            val icon = if (GO_UP == localItem.name) {
                icons[1]
            } else {
                icons[0]
            }
            itemHolder.albumArt.setImageDrawable(icon)
        } else {
            Glide.with(itemHolder.title)
                    .load(getAlbumArtUri(song.albumId))
                    .apply(RequestOptions().error(R.drawable.ic_music_note))
                    .into(itemHolder.albumArt)
        }
    }

    override fun getItemCount() = files.size

    fun updateDataSetAsync(newRoot: File): Boolean {
        if (isBusy) {
            return false
        } else if (GO_UP == newRoot.name) {
            goUpAsync()
            return false
        }
        rootFolder = newRoot
        Observable.fromCallable {
            isBusy = true
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext {
                val files = foldersRepository.getMediaFiles(rootFolder!!, true)
                getSongsForFiles(files)
            }.subscribe {
                this@FolderAdapter.files = files
                notifyDataSetChanged()
                isBusy = false
                lastFolderPref.set(rootFolder?.path ?: "")
            }
        return true
    }

    private fun goUpAsync(): Boolean {
        if (isBusy) {
            return false
        }
        val parent = rootFolder?.parentFile
        return if (parent != null && parent.canRead()) {
            updateDataSetAsync(parent)
        } else {
            false
        }
    }

    private fun getSongsForFiles(files: List<File>) {
        songsList.clear()
        val newSongs = files.map {
            songsRepository.getSongFromPath(it.absolutePath)
        }
        songsList.addAll(newSongs)
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val title: TextView = view.findViewById(R.id.folder_title)
        val albumArt: ImageView = view.findViewById(R.id.album_art)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (isBusy) return
            val f = files[adapterPosition]

            if (f.isDirectory && updateDataSetAsync(f)) {
                albumArt.setImageDrawable(icons[3])
            } else if (f.isFile) {
                val song = songsRepository.getSongFromPath(files[adapterPosition].absolutePath)
                val listWithoutFirstItem = songsList.subList(1, songsList.size).toSongIds()
                callback(song, listWithoutFirstItem, rootFolder?.name ?: "Folder")
            }
        }
    }
}
