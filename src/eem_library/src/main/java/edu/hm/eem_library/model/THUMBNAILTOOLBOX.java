package edu.hm.eem_library.model;

import android.app.Activity;
import android.app.Application;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.loader.content.CursorLoader;

import java.io.File;

public final class THUMBNAILTOOLBOX {
    private THUMBNAILTOOLBOX(){}

    public static Bitmap getThumbnailBitmap(File file, Application activity){
        Uri uri = Uri.fromFile(file);
        String[] proj = { MediaStore.Images.Media.DATA };

        CursorLoader cursorLoader = new CursorLoader(activity, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        cursor.moveToFirst();
        long imageId = cursor.getLong(column_index);
        cursor.close();

        return MediaStore.Images.Thumbnails.getThumbnail(
                activity.getContentResolver(), imageId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                 null );
    }
}
