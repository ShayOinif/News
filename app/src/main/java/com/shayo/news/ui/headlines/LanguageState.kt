package com.shayo.news.ui.headlines

import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class LanguageState @Inject constructor(
    private val externalScope: CoroutineScope,
) {
    private val _state = MutableStateFlow("us")
    val state: SharedFlow<String> get() = _state

    fun changeState(language: String) {
        externalScope.launch {
            _state.emit(language)
        }
    }
}