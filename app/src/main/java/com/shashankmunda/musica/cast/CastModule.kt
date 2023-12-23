package com.shashankmunda.musica.cast

import com.shashankmunda.musica.util.MusicUtils
import org.koin.dsl.module.module

val castModule = module {
    single<CastHelper> {
        CastHelper()
    }
    single<MusicUtils> {
        MusicUtils()
    }
}