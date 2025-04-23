package com.example.projectmanagement.data.item;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.graphics.drawable.GradientDrawable;

public class BackgroundColorItem {
    @ColorRes
    private int startColorResId;
    @Nullable
    @ColorRes
    private Integer endColorResId;    // null ⇒ solid
    @Nullable
    @DrawableRes
    private Integer iconResId;        // null ⇒ no icon
    @Nullable
    private GradientDrawable.Orientation gradientOrientation; // null ⇒ default

    /**
     * Solid color, no icon, default orientation ignored
     */
    public BackgroundColorItem(@ColorRes int colorResId) {
        this.startColorResId = colorResId;
    }

    /**
     * Solid color + icon
     */
    public BackgroundColorItem(@ColorRes int colorResId,
                               @DrawableRes int iconResId) {
        this.startColorResId = colorResId;
        this.iconResId       = iconResId;
    }

    /**
     * Gradient + optional icon + orientation
     */
    public BackgroundColorItem(@ColorRes int startColorResId,
                               @Nullable @ColorRes Integer endColorResId,
                               @Nullable @DrawableRes Integer iconResId,
                               @Nullable GradientDrawable.Orientation orientation) {
        this.startColorResId    = startColorResId;
        this.endColorResId      = endColorResId;
        this.iconResId          = iconResId;
        this.gradientOrientation = orientation;
    }

    public int getStartColorResId() {
        return startColorResId;
    }

    @Nullable
    public Integer getEndColorResId() {
        return endColorResId;
    }

    @Nullable
    public Integer getIconResId() {
        return iconResId;
    }

    @Nullable
    public GradientDrawable.Orientation getGradientOrientation() {
        return gradientOrientation;
    }

    // Optionally setters if needed
    public void setGradientOrientation(GradientDrawable.Orientation orientation) {
        this.gradientOrientation = orientation;
    }
}