/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to Morphe contributions.
 */

package app.morphe.extension.youtube.patches;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.shared.settings.Setting;
import app.morphe.extension.shared.spoof.SpoofAppVersionPatch;
import app.morphe.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class LegacyPlayerControlsPatch {

    public static final class RestoreOldPlayerButtonsAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return VersionCheckPatch.IS_20_31_OR_GREATER && !SpoofAppVersionPatch.isSpoofingToLessThan("20.31.00");
        }
    }

    public static final boolean RESTORE_OLD_PLAYER_BUTTONS =
            Settings.RESTORE_OLD_PLAYER_BUTTONS.get()
                    || !VersionCheckPatch.IS_20_31_OR_GREATER
                    || SpoofAppVersionPatch.isSpoofingToLessThan("20.31.00");

    /**
     * Injection point.
     */
    public static boolean useNullBottomGradient() {
        return RESTORE_OLD_PLAYER_BUTTONS;
    }

    /**
     * Injection point.
     */
    public static void hideBottomGradientScrim(ImageView bottomGradientScrim) {
        if (!RESTORE_OLD_PLAYER_BUTTONS) {
            return;
        }
        if (bottomGradientScrim != null) {
            Utils.runOnMainThread(() -> {
                bottomGradientScrim.setImageAlpha(0);
                bottomGradientScrim.setVisibility(View.GONE);
            });
        }
    }

    /**
     * Injection point.
     */
    public static boolean forcePlayerSeekbar(boolean original) {
        if (!original) {
            Logger.printDebug(() -> "Player seekbar feature flag is off");
        }
        return true;
    }

    /**
     * The value the app has for the new player overlays flag, before it is forced off.
     */
    private static volatile boolean newPlayerOverlaysOriginal;

    /**
     * Injection point.
     */
    public static boolean disableNewPlayerOverlays(boolean original) {
        newPlayerOverlaysOriginal = original;
        return false;
    }

    /**
     * Injection point.
     * Called right after the flag is read, so the value is the one just stored.
     */
    public static boolean getOriginalNewPlayerOverlays(boolean overridden) {
        return newPlayerOverlaysOriginal;
    }

    /**
     * Added to the class of the video metadata panel feature flags (21.30+).
     */
    public interface MetapanelFlagsInterface {
        // Method is added during patching.
        /**
         * @return If the video metadata panel has its own dark or light theme.
         */
        boolean patch_isMetapanelThemed();
    }

    @Nullable
    private static volatile MetapanelFlagsInterface metapanelFlags;

    /**
     * Injection point.
     * Called before the elevated player flag is read.
     */
    public static void setMetapanelFlags(MetapanelFlagsInterface flags) {
        metapanelFlags = flags;
    }

    /**
     * Injection point.
     * <p>
     * The elevated player shows the video metadata over the ambient mode colors of the player.
     * YouTube gives the metadata panel its own dark (or light) theme for it. Without either of them,
     * the light theme text can not be read over the dark colors, so the elevated player is turned off
     * (https://github.com/MorpheApp/morphe-patches/issues/3703).
     * With a forced theme the elevated player is kept, because the metadata is then themed for it
     * and can not be read over the background of the watch page
     * (https://github.com/MorpheApp/morphe-patches/issues/3809).
     */
    public static boolean useElevatedPlayer(boolean original) {
        if (!allowModernPlayerLayoutFlags(original)) {
            return false;
        }

        MetapanelFlagsInterface flags = metapanelFlags;
        if (flags == null) {
            // 21.29 and older, which have no themes for the metadata panel of the elevated player.
            return true;
        }

        final boolean themed = flags.patch_isMetapanelThemed();
        if (!themed) {
            Logger.printDebug(() -> "Elevated player turned off, the metadata panel has no theme");
        }
        return themed;
    }

    /**
     * Injection point.
     */
    public static boolean usePlayerBottomControlsExploderLayout(boolean original) {
        return !RESTORE_OLD_PLAYER_BUTTONS;
    }

    /**
     * Injection point.
     */
    public static boolean allowModernPlayerLayoutFlags(boolean original) {
        if (RESTORE_OLD_PLAYER_BUTTONS) {
            return false; // Flag causes app crash on startup if old player buttons is used.
        }
        return original;
    }

    /**
     * Injection point.
     */
    public static boolean useModernPlayerTopControls(boolean original) {
        if (original) {
            Logger.printDebug(() -> "useModernPlayerTopControls is set on");
        }

        return false;
    }
}
