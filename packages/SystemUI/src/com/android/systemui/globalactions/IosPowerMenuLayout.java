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

package com.android.systemui.globalactions;

import static com.android.systemui.util.kotlin.JavaAdapterKt.collectFlow;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.systemui.penguin.PowerMenuLayout;
import com.android.systemui.res.R;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.window.domain.interactor.WindowRootViewBlurInteractor;

import java.util.function.Consumer;

import javax.inject.Inject;

/**
 * The full screen power menu with one slide-to-act row per action.
 *
 * <p>It is used when {@link PowerMenuStyle#SETTING_NAME} selects {@link PowerMenuStyle#IOS}.
 */
public class IosPowerMenuLayout implements PowerMenuLayout {

    @Inject
    public IosPowerMenuLayout() {}

    @Override
    public boolean isActive(@NonNull Context context) {
        return PowerMenuStyle.current(context) == PowerMenuStyle.IOS;
    }

    @NonNull
    @Override
    public Dialog createSubMenu(@NonNull Context context, @NonNull ListAdapter adapter,
            boolean blurSupported) {
        return GlobalActionsIosPowerDialog.create(context, adapter, blurSupported);
    }

    @Override
    public void inflate(@NonNull SystemUIDialog dialog, @NonNull Host host) {
        dialog.setContentView(R.layout.global_actions_ios);
        fixNavBarClipping(dialog);

        final Context context = dialog.getContext();
        final Resources res = context.getResources();

        ViewGroup container = dialog.findViewById(R.id.global_actions_container);
        container.setOnTouchListener((v, event) -> {
            host.onContainerTouch(event);
            return v.onTouchEvent(event);
        });
        final View content = dialog.findViewById(R.id.power_menu_ios_content);

        final WindowRootViewBlurInteractor blurInteractor = dialog.getBlurInteractor();
        final Consumer<Boolean> applyBlurSupported = supported -> {
            host.setBlurSupported(supported);
            if (host.isBlurBackdropAllowed() && supported) {
                container.setBackground(null);
                host.setAnimatedContent(content, null);
                return;
            }
            Drawable backdrop = res.getDrawable(R.drawable.power_menu_ios_backdrop,
                    context.getTheme()).mutate();
            container.setBackground(backdrop);
            host.setAnimatedContent(content, backdrop);
        };
        collectFlow(container, blurInteractor.isBlurCurrentlySupported(), applyBlurSupported);
        applyBlurSupported.accept(blurInteractor.isBlurCurrentlySupported().getValue());

        final int track = res.getColor(R.color.power_menu_ios_track, null);
        final int thumb = res.getColor(R.color.power_menu_ios_thumb, null);
        final int foreground = res.getColor(R.color.power_menu_ios_foreground, null);

        SlideToActView powerOffRow = dialog.findViewById(R.id.power_menu_ios_power_off);
        powerOffRow.setColors(track, thumb, foreground);
        powerOffRow.setLabel(context.getText(R.string.power_menu_ios_power_off));
        bindSlideRow(powerOffRow, host.findOption(Host.POWER_OFF), true);

        SlideToActView restartRow = dialog.findViewById(R.id.power_menu_ios_restart);
        restartRow.setColors(track, thumb, foreground);
        restartRow.setLabel(context.getText(R.string.power_menu_ios_restart));
        bindSlideRow(restartRow, host.findOption(Host.RESTART), true);

        SlideToActView emergencyRow = dialog.findViewById(R.id.power_menu_ios_emergency);
        emergencyRow.setColors(
                res.getColor(R.color.power_menu_ios_emergency_track, null),
                res.getColor(R.color.power_menu_ios_emergency_thumb, null),
                res.getColor(R.color.power_menu_ios_emergency_foreground, null));
        emergencyRow.setLabel(context.getText(R.string.power_menu_ios_emergency));
        emergencyRow.setThumbText(context.getText(R.string.power_menu_ios_emergency_thumb_label));
        bindSlideRow(emergencyRow, host.findOption(Host.EMERGENCY), false);

        dialog.findViewById(R.id.power_menu_ios_emergency_hint)
                .setVisibility(emergencyRow.getVisibility());
        dialog.findViewById(R.id.power_menu_ios_restart_gap)
                .setVisibility(
                        restartRow.getVisibility() == View.VISIBLE
                                && emergencyRow.getVisibility() == View.VISIBLE
                                ? View.VISIBLE : View.GONE);

        dialog.findViewById(R.id.power_menu_ios_close).setOnClickListener(v -> host.close());

        host.onLayoutInflated(context, container);
    }

    private static void bindSlideRow(@NonNull SlideToActView row,
            @Nullable Option option, boolean useIcon) {
        if (option == null) {
            row.setVisibility(View.GONE);
            return;
        }
        if (useIcon) {
            row.setThumbIcon(option.getIcon(row.getContext()));
        }
        row.setOnSlideCompleteListener(v -> option.trigger());
    }

    private static void fixNavBarClipping(@NonNull SystemUIDialog dialog) {
        ViewGroup content = dialog.findViewById(android.R.id.content);
        content.setClipChildren(false);
        content.setClipToPadding(false);
        ViewGroup contentParent = (ViewGroup) content.getParent();
        contentParent.setClipChildren(false);
        contentParent.setClipToPadding(false);
    }
}
