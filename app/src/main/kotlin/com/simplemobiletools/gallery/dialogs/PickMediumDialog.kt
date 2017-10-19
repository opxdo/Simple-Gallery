package com.simplemobiletools.gallery.dialogs

import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.gallery.R
import com.simplemobiletools.gallery.activities.SimpleActivity
import com.simplemobiletools.gallery.adapters.MediaAdapter
import com.simplemobiletools.gallery.asynctasks.GetMediaAsynctask
import com.simplemobiletools.gallery.extensions.config
import com.simplemobiletools.gallery.extensions.getCachedMedia
import com.simplemobiletools.gallery.helpers.VIEW_TYPE_GRID
import com.simplemobiletools.gallery.models.Medium
import kotlinx.android.synthetic.main.dialog_medium_picker.view.*

class PickMediumDialog(val activity: SimpleActivity, val path: String, val callback: (path: String) -> Unit) {
    var dialog: AlertDialog
    var shownMedia = ArrayList<Medium>()
    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_medium_picker, null)
    var isGridViewType = activity.config.viewTypeFiles == VIEW_TYPE_GRID

    init {
        (view.media_grid.layoutManager as GridLayoutManager).apply {
            orientation = if (activity.config.scrollHorizontally && isGridViewType) GridLayoutManager.HORIZONTAL else GridLayoutManager.VERTICAL
            spanCount = if (isGridViewType) activity.config.mediaColumnCnt else 1
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.select_photo)

            val media = activity.getCachedMedia(path).filter { !it.video } as ArrayList
            if (media.isNotEmpty()) {
                gotMedia(media)
            }

            GetMediaAsynctask(activity, path, false, true, false) {
                gotMedia(it)
            }.execute()
        }
    }

    private fun gotMedia(media: ArrayList<Medium>) {
        if (media.hashCode() == shownMedia.hashCode())
            return

        shownMedia = media
        val adapter = MediaAdapter(activity, media, null, true, false) {
            callback(it.path)
            dialog.dismiss()
        }

        val scrollHorizontally = activity.config.scrollHorizontally && isGridViewType
        view.apply {
            media_grid.adapter = adapter

            media_vertical_fastscroller.isHorizontal = false
            media_vertical_fastscroller.beGoneIf(scrollHorizontally)

            media_horizontal_fastscroller.isHorizontal = true
            media_horizontal_fastscroller.beVisibleIf(scrollHorizontally)

            if (scrollHorizontally) {
                media_horizontal_fastscroller.setViews(media_grid)
            } else {
                media_vertical_fastscroller.setViews(media_grid)
            }
        }
    }
}
