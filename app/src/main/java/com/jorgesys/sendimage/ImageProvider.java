package com.jorgesys.sendimage;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

public class ImageProvider extends ContentProvider {
    private static final String TAG = "ImageProvider";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        URI fileURI = URI.create("file://" + uri.getPath());
        File file = new File(fileURI);

        ParcelFileDescriptor parcel = null;
        try {
            parcel = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException :: " + e.getMessage());
        }
        return parcel;
    }

}
