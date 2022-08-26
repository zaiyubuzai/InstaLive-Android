package com.example.instalive.model

import com.example.instalive.app.InstaLivePreferences
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.venus.framework.util.isNeitherNullNorEmpty
import timber.log.Timber

object InstaLiveStringTemplate {
    var template: StringTemplate? = null

    fun cacheTemplate(template: String) {
        Timber.d("VenusStringTemplate 1 ${template.isEmpty()}")
        InstaLivePreferences.stringTemplate = template
        loadTemplate()
    }

    fun loadTemplate() {
        Timber.d("VenusStringTemplate 2 ${InstaLivePreferences.stringTemplate.isNullOrEmpty()}")
        val template = InstaLivePreferences.stringTemplate
        if (template.isNeitherNullNorEmpty()) {
            InstaLiveStringTemplate.template = try {
                Gson().fromJson(template, StringTemplate::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class StringTemplate(
    val version: Int,
    val unsupported_msg_type_tips: String
)