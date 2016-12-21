package com.example.networkmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class FileSave {
    public void createExternalStoragePublicPicture(Context mContext,String record) {
        /* Create a path where we will place the network information in the user's
           public directory.  Note that you should be careful about
           what you place here, since the user often manages these files.  For
           pictures and other media owned by the application, consider
           Context.getExternalMediaDir().
        */
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path,"network_info_log.txt");
        try {
            // Make sure the Pictures directory exists.
            path.mkdirs();

            /* Very simple code to copy a picture from the application's
               resource into the external file.  Note that this code does
               no error checking, and assumes the picture is small (does not
               try to copy it in chunks).  Note that if external storage is
               not currently mounted this will silently fail.
             */
           // InputStream is = getResources().openRawResource(R.drawable.balloons);
            OutputStream os = new FileOutputStream(file,true);
            byte[] data = record.getBytes();
            os.write(data);
            os.close();

            /* Tell the media scanner about the new file so that it is
               immediately available to the user.
            */
            MediaScannerConnection.scanFile(mContext,new String[] { file.toString() }, null,
            		new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch (IOException e) {
            /* Unable to create file, likely because external storage is
               not currently mounted.
            */
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

    public void deleteExternalStoragePublicPicture() {
        /* Create a path where we will place our picture in the user's
           public pictures directory and delete the file.  If external
           storage is not currently mounted this will fail.
        */
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        file.delete();
    }

    public boolean hasExternalStoragePublicPicture() {
        /* Create a path where we will place our picture in the user's
           public pictures directory and check if the file exists.  If
           external storage is not currently mounted this will think the
           picture doesn't exist.
        */
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        return file.exists();
    }

}
