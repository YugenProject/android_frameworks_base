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

package com.android.systemui.penguin;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.systemui.statusbar.phone.SystemUIDialog;

/**
 * A replaceable layout for the global actions (power) menu.
 *
 * <p>The default layout is the regular grid drawn by
 * {@code GlobalActionsDialogLite}. Implementations of this interface can take over the whole
 * dialog, and can also decide how the power and restart sub menus are drawn.
 */
public interface PowerMenuLayout {

    /** Whether this layout should be used for the global actions dialog. */
    boolean isActive(@NonNull Context context);

    /** Inflate this layout into {@code dialog}. */
    void inflate(@NonNull SystemUIDialog dialog, @NonNull Host host);

    /** Create the dialog used for the power and restart sub menus. */
    @NonNull
    Dialog createSubMenu(@NonNull Context context, @NonNull ListAdapter adapter,
            boolean blurSupported);

    interface Host {
        int POWER_OFF = 0;
        int RESTART = 1;
        int EMERGENCY = 2;

        @Nullable
        Option findOption(int kind);

        void onContainerTouch(@NonNull MotionEvent event);

        boolean isBlurBackdropAllowed();

        void setBlurSupported(boolean supported);

        void close();

        void setAnimatedContent(@NonNull View content, @Nullable Drawable backdrop);

        void onLayoutInflated(@NonNull Context context, @NonNull ViewGroup container);
    }

    interface Option {
        @NonNull
        Drawable getIcon(@NonNull Context context);

        void trigger();
    }
}
