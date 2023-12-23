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
import android.os.Bundle
import android.view.MenuItem
import com.afollestad.rxkprefs.Pref
import com.shashankmunda.musica.PREF_APP_THEME
import com.shashankmunda.musica.R
import com.shashankmunda.musica.constants.AppThemes
import com.shashankmunda.musica.databinding.ActivitySettingsBinding
import com.shashankmunda.musica.extensions.attachLifecycle
import com.shashankmunda.musica.extensions.ioToMain
import com.shashankmunda.musica.extensions.replaceFragment
import com.shashankmunda.musica.ui.activities.base.PermissionsActivity
import com.shashankmunda.musica.ui.fragments.SettingsFragment
import org.koin.android.ext.android.inject
import kotlin.properties.Delegates.notNull

class SettingsActivity : PermissionsActivity() {
    private var themeRes by notNull<Int>()
    private val appThemePref by inject<Pref<AppThemes>>(name = PREF_APP_THEME)
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        themeRes = appThemePref.get().themeRes
        setTheme(themeRes)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.apply { setTitle(R.string.settings) })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        replaceFragment(R.id.container, SettingsFragment(), null, false)

        // This code is very unideal - it would be better if every activity could observe this preference's change
        // and recreate() themselves. However it looks like that lifecycle messes up some media connection stuff in
        // this app currently - this solution works for now:
        appThemePref.observe()
                .ioToMain()
                .filter { it.themeRes != themeRes }
                .distinctUntilChanged()
                .subscribe {
                    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return@subscribe
                    startActivity(intent.apply {
                        flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    finish()
                }
                .attachLifecycle(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
