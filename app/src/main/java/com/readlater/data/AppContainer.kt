package com.readlater.data

import android.content.Context

interface AppContainer {
    val eventRepository: EventRepository
    val authRepository: AuthRepository
    val themeRepository: ThemeRepository
    val calendarRepository: CalendarRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val calendarRepository: CalendarRepository by lazy { CalendarRepository(context) }

    override val authRepository: AuthRepository by lazy { AuthRepository(context) }

    override val themeRepository: ThemeRepository by lazy { ThemeRepository(context) }

    override val eventRepository: EventRepository by lazy {
        EventRepository(context, calendarRepository)
    }
}
