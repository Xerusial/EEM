package edu.hm.eem_library.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/** Toolbox as provided by Jerry Zhao @ https://www.dev2qa.com/how-to-get-real-file-path-from-android-uri/
 */
public class URITOOLBOX {
    private URITOOLBOX(){}

    static String pathFromUri(Context context, Uri uri){
        String ret = null;
        String uriAuthority = uri.getAuthority();
        String documentId = DocumentsContract.getDocumentId(uri);
        if(isMediaDoc(uriAuthority))
        {
            String[] idArr = documentId.split(":");
            if(idArr.length == 2)
            {
                // First item is document type.
                String docType = idArr[0];

                // Second item is document real id.
                String realDocId = idArr[1];

                // Get content uri by document type.
                Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                if("video".equals(docType))
                {
                    mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }else if("audio".equals(docType))
                {
                    mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                // Get where clause with real document id.
                String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;

                ret = getImageRealPath(context.getContentResolver(), mediaContentUri, whereClause);
            }

        }else if(isDownloadDoc(uriAuthority))
        {
            // Build download uri.
            Uri downloadUri = Uri.parse("content://downloads/public_downloads");

            // Append download document id at uri end.
            Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));

            ret = getImageRealPath(context.getContentResolver(), downloadUriAppendId, null);

        }else if(isExternalStoreDoc(uriAuthority))
        {
            String[] idArr = documentId.split(":");
            if(idArr.length == 2)
            {
                String type = idArr[0];
                String realDocId = idArr[1];

                if("primary".equalsIgnoreCase(type))
                {
                    ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                }
            }
        }
        return ret;
    }

    /* Check whether this document is provided by MediaProvider. */
    private static boolean isMediaDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.providers.media.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by ExternalStorageProvider. */
    private static boolean isExternalStoreDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.externalstorage.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by DownloadsProvider. */
    private static boolean isDownloadDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.providers.downloads.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    private static String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if(cursor!=null)
        {
            boolean moveToFirst = cursor.moveToFirst();
            if(moveToFirst)
            {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
            cursor.close();
        }

        return ret;
    }
}
