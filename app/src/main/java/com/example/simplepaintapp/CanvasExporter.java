package com.example.simplepaintapp;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Class which handles the exporting of CanvasView drawings through saving/sharing.
 */
public class CanvasExporter
{
    private static final String DIRECTORY_PATH = "/Pictures/Paint";
    private static final String SAVE_FILE_NAME = "/drawing_";
    private static final String SHARE_FILE_NAME = "/shared_";
    private static final String FILE_EXTENSION = ".png";

    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    public static final int FLAG_SAVE = 1;
    public static final int FLAG_SHARE = 2;

    private final File subDirectory;

    private int exportType;

    /**
     * Constructor which finds the sub-directory to be exported to.
     */
    public CanvasExporter()
    {
        // get the output storage directory and find the sub-directory.
        File storageDirectory = Environment.getExternalStorageDirectory();
        subDirectory = new File(storageDirectory.toString() + DIRECTORY_PATH);
    }

    /**
     * Sets the export type to be used within the activity.
     * @param exportType - the export type.
     */
    public void setExportType (int exportType)
    {
        this.exportType = exportType;
    }

    /**
     * Returns the export type which is used within the activity.
     * @return exportType - the export type.
     */
    public int getExportType ()
    {
        return exportType;
    }

    /**
     * Creates the sub-directory if it does not already exist.
     * @return boolean - whether the sub-directory exists.
     */
    private boolean createDirectory ()
    {
        // if the sub-directory does not exist, create it
        if (!subDirectory.exists())
            return subDirectory.mkdir();
        // return true if it already exists
        return true;
    }

    /**
     * Returns the number of files which already exist within a given directory.
     * @param directory - the directory to check for files.
     * @return int - the number of existing files.
     */
    public int getExistingFileCount(File directory)
    {
        int count = 0;
        // get the existing images as an array
        File[] existingImages = directory.listFiles();
        // if there is at least one image
        if (existingImages != null)
        {
            // loop through the existing images
            for (File file : existingImages)
            {
                // extract the file name and increment the counter if it is a valid file type
                String name = file.getName();
                if (name.endsWith(".jpg") || name.endsWith(".png"))
                    count++;
            }
        }
        return count;
    }

    /**
     * Outputs an input bitmap to a given output FileOutputStream.
     * @param image - the output image file.
     * @param bitmap - the bitmap representation of a drawing.
     */
    private void outputToFileStream (File image, Bitmap bitmap)
    {
        FileOutputStream fileOutputStream;
        try
        {
            // compress the bitmap and link it to the output stream
            fileOutputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            // flush and close the output stream.
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e)
        {
            // throw an error message
            Log.w("ERROR", "" + e.getMessage());
        }
    }

    /**
     * Saves the image to a file and returns the path to the saved file.
     * @param bitmap - the bitmap to be saved as an image.
     * @return String - the path to the saved image.
     */
    public String saveImage(Bitmap bitmap)
    {
        boolean created = createDirectory();
        // if the sub-directory exists or was created successfully
        if (subDirectory.exists() || created)
        {
            // create a new file for the bitmap
            int fileCount = getExistingFileCount(subDirectory);
            File image = new File(subDirectory, SAVE_FILE_NAME + ++fileCount + FILE_EXTENSION);
            outputToFileStream(image, bitmap);
            // return the path to the saved image.
            return image.getAbsolutePath();
        }
        return null;
    }

    /**
     * Returns the image representation of an input bitmap.
     * @param bitmap - the bitmap to be saved as an image.
     * @return File - the file to which the bitmap was saved.
     */
    public File getImage(Bitmap bitmap)
    {
        boolean created = createDirectory();
        // if the sub-directory exists or was created successfully
        if (subDirectory.exists() || created)
        {
            // create a new file for the bitmap to allow it to be shared
            File image = new File(subDirectory, SHARE_FILE_NAME + Math.random() + FILE_EXTENSION);
            outputToFileStream(image, bitmap);
            // return the image file
            return image;
        }
        return null;
    }
}