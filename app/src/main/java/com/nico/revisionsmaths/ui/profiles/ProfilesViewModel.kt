package com.nico.revisionsmaths.ui.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nico.revisionsmaths.RevisionsApp
import com.nico.revisionsmaths.data.db.Profile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = (application as RevisionsApp).container.database.profileDao()

    val profiles: StateFlow<List<Profile>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createProfile(name: String, age: Int) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { dao.insert(Profile(name = trimmed, age = age)) }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch { dao.delete(profile) }
    }
}
