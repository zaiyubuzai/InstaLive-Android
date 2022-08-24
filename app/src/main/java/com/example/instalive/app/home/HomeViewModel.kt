package com.example.instalive.app.home

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import com.venus.dm.model.UserData
import kotlinx.coroutines.launch

class HomeViewModel: BaseViewModel() {
    fun getUserDetail(userId: String?, username: String?, result: (UserData) -> Unit){
        viewModelScope.launch {
            DataRepository.getUserDetail(userId, username, result, this@HomeViewModel)
        }
    }
}