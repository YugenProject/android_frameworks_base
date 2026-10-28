/*
 * Copyright (C) 2026 The Yugen Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.quickactions.popups.shared

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Colour mode used by the Quick Actions popup surfaces.
 *
 * The value lives in [Settings.Secure] so that a future Settings page can change it. When the
 * setting is absent the popup falls back to the regular Material surface colour.
 */
object DynamicIslandFeatureSettings {
    const val POPUP_COLOR_MODE_DEFAULT = 0

    const val POPUP_COLOR_MODE_BLUR = 1

    const val POPUP_COLOR_MODE_SOLID_BLACK = 2

    private const val POPUP_COLOR_MODE_SETTING = "dynamic_island_popup_color_mode"

    private val popupColorModeUri: Uri
        get() = Settings.Secure.getUriFor(POPUP_COLOR_MODE_SETTING)

    fun ContentResolver.readDynamicIslandPopupColorMode(): Int =
        Settings.Secure.getInt(
            this,
            POPUP_COLOR_MODE_SETTING,
            POPUP_COLOR_MODE_DEFAULT,
        )

    fun observeDynamicIslandPopupColorMode(context: Context): Flow<Int> = callbackFlow {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(resolver.readDynamicIslandPopupColorMode())
            }
        }
        resolver.registerContentObserver(popupColorModeUri, false, observer)
        trySend(resolver.readDynamicIslandPopupColorMode())
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.conflate()
}
