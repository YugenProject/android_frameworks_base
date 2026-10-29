/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.qs.ui.composable

import android.graphics.Typeface
import android.text.TextUtils
import android.text.format.DateFormat
import android.widget.TextClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.compose.animation.Expandable
import com.android.compose.animation.rememberExpandableController
import com.android.compose.animation.scene.ContentScope
import com.android.systemui.animation.Expandable
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.qs.panels.ui.viewmodel.toolbar.ToolbarViewModel
import com.android.systemui.qs.shared.style.QsPanelStyle
import com.android.systemui.qs.shared.style.glassPress
import com.android.systemui.qs.shared.style.glassRim
import com.android.systemui.qs.shared.style.liquidGlassEnabled
import com.android.systemui.qs.shared.style.panelStyle
import com.android.systemui.qs.ui.viewmodel.QuickSettingsContainerViewModel
import com.android.systemui.res.R
import com.android.systemui.shade.ui.composable.QuickSettingsOverlayPrivacyChip
import com.android.systemui.shade.ui.composable.ShadeStatusRow

private val ButtonSize = 44.dp

@Composable
fun controlCentreTopShown(viewModel: QuickSettingsContainerViewModel): Boolean =
    viewModel.panelStyle == QsPanelStyle.OneUi ||
        (viewModel.panelStyle == QsPanelStyle.Penguin && liquidGlassEnabled())

@Composable
fun controlCentreWide(viewModel: QuickSettingsContainerViewModel): Boolean =
    viewModel.panelStyle == QsPanelStyle.Penguin && controlCentreTopShown(viewModel) && wideControlCentre()

@Composable
fun ContentScope.ControlCentreTop(
    viewModel: QuickSettingsContainerViewModel,
    toolbarViewModel: ToolbarViewModel,
) {
    if (viewModel.panelStyle == QsPanelStyle.OneUi) {
        OneUiOverlayHeader(viewModel, toolbarViewModel)
        return
    }
    val editButton =
        rememberViewModel("ControlCentreTop") {
            toolbarViewModel.editModeButtonViewModelFactory.create()
        }
    if (wideControlCentre()) {
        ShadeStatusRow(
            viewModel.shadeHeaderViewModel,
            Modifier.fillMaxWidth()
                .wrapContentWidth()
                .width(wideGridWidth())
                .padding(horizontal = PenguinGridMargin + 4.dp)
                .padding(top = 4.dp, bottom = 12.dp),
        )
        return
    }
    Column(
        Modifier.fillMaxWidth()
            .padding(horizontal = PenguinGridMargin)
            .padding(top = 12.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassButton(Icons.Rounded.Edit) { editButton.onButtonClick() }
            GlassButton(Icons.Rounded.Settings) {
                toolbarViewModel.settingsButtonViewModel?.onClick?.invoke(it)
            }
        }
        Spacer(Modifier.height(20.dp))
        ShadeStatusRow(viewModel.shadeHeaderViewModel, Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun GlassButton(icon: ImageVector, onClick: (Expandable) -> Unit) {
    Box(
        Modifier.size(ButtonSize)
            .glassRim(CircleShape)
            .glassPress()
    ) {
        Expandable(
            controller =
                rememberExpandableController(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = CircleShape,
                ),
            useModifierBasedImplementation = true,
            defaultMinSize = false,
            onClick = onClick,
            modifier = Modifier.size(ButtonSize),
        ) {
            Box(Modifier.size(ButtonSize), contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ContentScope.OneUiOverlayHeader(
    viewModel: QuickSettingsContainerViewModel,
    toolbar: ToolbarViewModel,
) {
    val edit = rememberViewModel("OneUiHeaderEdit") { toolbar.editModeButtonViewModelFactory.create() }
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(top = 10.dp, bottom = 12.dp)) {
        QuickSettingsOverlayPrivacyChip(viewModel.shadeHeaderViewModel)
        ShadeStatusRow(viewModel.shadeHeaderViewModel, Modifier.height(32.dp))
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                OneUiHeaderClock(time = true)
                Spacer(Modifier.width(6.dp))
                OneUiHeaderClock(time = false, modifier = Modifier.weight(1f))
            }
            OneUiHeaderButton(Icons.Rounded.Edit, stringResource(R.string.oneui_edit)) {
                edit.onButtonClick()
            }
            OneUiHeaderButton(Icons.Rounded.PowerSettingsNew, stringResource(R.string.oneui_qs_power)) {
                toolbar.powerButtonViewModel.onClick(it)
            }
            OneUiHeaderButton(Icons.Rounded.Settings, stringResource(R.string.oneui_qs_settings)) {
                toolbar.settingsButtonViewModel?.onClick?.invoke(it)
            }
        }
    }
}

@Composable
private fun OneUiHeaderClock(time: Boolean, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextClock(context).apply {
                val locale = resources.configuration.locales[0]
                format24Hour = if (time) "HH:mm" else DateFormat.getBestDateTimePattern(locale, "EEEdMMM")
                format12Hour = if (time) "hh:mm" else format24Hour
                textSize = if (time) 24f else 16f
                typeface = Typeface.create(Typeface.SANS_SERIF, if (time) 600 else 400, false)
                setTextColor(android.graphics.Color.WHITE)
                includeFontPadding = false
                setSingleLine(true)
                ellipsize = TextUtils.TruncateAt.END
            }
        },
    )
}

@Composable
private fun OneUiHeaderButton(icon: ImageVector, description: String, onClick: (Expandable) -> Unit) {
    Expandable(
        controller = rememberExpandableController(color = Color.Transparent, shape = CircleShape),
        useModifierBasedImplementation = true,
        defaultMinSize = false,
        onClick = onClick,
        modifier = Modifier.size(40.dp),
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}
