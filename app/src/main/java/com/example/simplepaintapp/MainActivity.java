package com.example.simplepaintapp;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private CanvasExporter canvasExporter;
    private CanvasView canvasView;
    private ScaleGestureDetector scaleGestureDetector;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set the view of the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // hide the navigation elements, i.e., status and navigation bar
        hideUINavigation();
        // create new CanvasExporter and CanvasView objects
        canvasExporter = new CanvasExporter();
        canvasView = findViewById(R.id.canvasView);
        // create a new ScaleHandler object to handle scaling
        ScaleHandler scaleHandler = createScaleHandler();
        scaleGestureDetector = new ScaleGestureDetector(MainActivity.this, scaleHandler);
        // get the size of the display and initialise the CanvasView using the values
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        canvasView.initialise(displayMetrics.widthPixels, displayMetrics.heightPixels);
        canvasView.setOnTouchListener(new View.OnTouchListener()
        {
            /**
             * Touch handler for the application. Differentiates between scaling gestures and
             * touches which are meant to draw on the application canvas.
             * @param v - the View object being touched.
             * @param event - the event being performed through the touch.
             * @return boolean - whether or not the event is consumed.
             */
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // pass the touch event to the scale gesture detector
                scaleGestureDetector.onTouchEvent(event);
                // differentiate between pressing down and up
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        // hide UI elements if the user is pressing down
                        handleUIElements(View.INVISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        // display UI elements if the user is lifting up
                        handleUIElements(View.VISIBLE);
                        break;
                }
                // ensure that only one finger is being used and that a scale gesture is not in progress
                if (event.getPointerCount() == 1 && !scaleGestureDetector.isInProgress())
                {
                    if (canvasView.getPreviousStrokeWidth() == canvasView.getStrokeWidth())
                    {
                        // provided the scale gesture wasn't completed just before, handle the touches as attempts
                        // to draw on the canvas
                        canvasView.handleTouches(event.getX(), event.getY(), event.getAction());
                    } else
                    {
                        // ignore/remove any touches which were completed just after a scale gesture
                        // as these are typically erroneous
                        canvasView.undo();
                    }
                    // update the stroke width of the pen
                    canvasView.setPreviousStrokeWidth(canvasView.getStrokeWidth());
                }
                return true;
            }
        });
        // set the click listeners for the UI buttons
        ImageButton clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);

        ImageButton undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(this);

        ImageButton redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(this);

        ImageButton styleButton = findViewById(R.id.styleButton);
        styleButton.setOnClickListener(this);

        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        ImageButton shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(this);
    }

    /**
     * Hides the UI navigation elements, such as the navigation and status bars.
     */
    private void hideUINavigation()
    {
        // retrieve the View and define the flags
        final View view = getWindow().getDecorView();
        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        // set the flags
        view.setSystemUiVisibility(flags);
        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {
            /**
             * Handles changes in the system UI visibility.
             * @param visibility - the visibility of the View.
             */
            @Override
            public void onSystemUiVisibilityChange(int visibility)
            {
                // if the application is no longer full-screen, make it so
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    view.setSystemUiVisibility(flags);
            }
        });
    }

    /**
     * Creates and returns a custom scale handler, as well as handles the callback methods.
     * @return ScaleHandler - the custom scale handling object.
     */
    private ScaleHandler createScaleHandler()
    {
        // retrieve the UI pen icon which is used to represent changes in pen size
        ImageView penIcon = findViewById(R.id.penSizeIcon);
        // create a new ScaleHandler object and attach a scale changed listener
        ScaleHandler scaleHandler = new ScaleHandler();
        scaleHandler.setOnScaleChangedListener(new ScaleHandler.ScaleChangedListener()
        {
            /**
             * Returns the scale icon with the correct colour as a scalable GradientDrawable.
             * @return GradientDrawable - the pen icon in a scalable format.
             */
            @Override
            public GradientDrawable onScaleIconRequired()
            {
                // retrieve the pen icon as a GradientDrawable object
                GradientDrawable gradientDrawable = (GradientDrawable) penIcon.getBackground();
                gradientDrawable.setColor(canvasView.getColour());

                return gradientDrawable;
            }

            /**
             * Returns the application context to be used by the ScaleHandler.
             * @return Context - the application context.
             */
            @Override
            public Context getContext()
            {
                return MainActivity.this;
            }

            /**
             * Returns the Resources object for the application context.
             * @return Resources - the resources object.
             */
            @Override
            public Resources getContextResources()
            {
                return getResources();
            }

            /**
             * Handles the start of a scale gesture.
             */
            @Override
            public void onScaleStarted()
            {
                // display the pen icon
                penIcon.setVisibility(View.VISIBLE);
            }

            /**
             * Handles the changes which are completed during a scale gesture.
             * @param scaleFactor - the scale factor of the scale gesture.
             */
            @Override
            public void onScaleChanged(float scaleFactor)
            {
                // if the user has scaled to the minimum or maximum size, adjust the size to be one less to ensure
                // that the previous stroke width is different from the current to prevent erroneous drawing
                if (scaleFactor == ScaleHandler.MIN_WIDTH || scaleFactor == ScaleHandler.MAX_WIDTH)
                    canvasView.setPreviousStrokeWidth(Math.round(scaleFactor) - 1);
                canvasView.setStrokeWidth(Math.round(scaleFactor));
                // get the width/height values of the pen icon as a LayoutParams object
                ViewGroup.LayoutParams params = penIcon.getLayoutParams();
                params.width = (int) scaleFactor;
                params.height = (int) scaleFactor;
                // set the scale factor as the size of the pen and pen icon
                penIcon.setLayoutParams(params);
            }

            /**
             * Handles the end of a scale gesture.
             */
            @Override
            public void onScaleEnded()
            {
                // hide the pen icon
                penIcon.setVisibility(View.GONE);
            }
        });
        return scaleHandler;
    }

    /**
     * Hides or displays the UI buttons within the application depending on the input argument.
     * @param showType - whether to display or hide the view elements.
     */
    private void handleUIElements (int showType)
    {
        // get the view elements as a ViewGroup
        ViewGroup viewGroup = findViewById(R.id.container);
        // loop through each element
        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            View view = viewGroup.getChildAt(i);
            // if the view is not the canvas or the pen size icon, hide/show it
            if (view.getId() != R.id.canvasView && view.getId() != R.id.penSizeIcon)
                view.setVisibility(showType);
        }
    }

    /**
     * Click handler for the application which performs actions depending on the view which was clicked.
     * @param v - the view which was clicked
     */
    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        if (viewID == R.id.clearButton)
        {
            // clear the canvas
            canvasView.clear();
        } else if (viewID == R.id.undoButton)
        {
            // undo the most recent drawing action
            canvasView.undo();
        } else if (viewID == R.id.redoButton)
        {
            // redraw the most recently undone action
            canvasView.redo();
        } else if (viewID == R.id.styleButton)
        {
            // generate a new ColourPickerDialog to allow the user to change colour
            ColourPickerDialog dialog = new ColourPickerDialog(MainActivity.this, canvasView.getColour());
            dialog.setOnDialogOptionSelectedListener(new ColourPickerDialog.ColourPickerOptionSelectedListener()
            {
                /**
                 * Callback method which sets the current pen colour.
                 * @param colour - the selected colour.
                 */
                @Override
                public void onColourPickerOptionSelected(int colour)
                {
                    // set the colour of the pen to the chosen colour via a callback
                    canvasView.setColour(colour);
                }
            });
            dialog.show();
        } else if (viewID == R.id.saveButton)
        {
            // set the export type to save and then check for permission
            canvasExporter.setExportType(CanvasExporter.FLAG_SAVE);
            checkForPermissions();
        } else if (viewID == R.id.shareButton)
        {
            // set the export type to share and then check for permission
            canvasExporter.setExportType(CanvasExporter.FLAG_SHARE);
            checkForPermissions();
        }
    }

    /**
     * Requests storage permission from the user
     */
    private void requestStoragePermission ()
    {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // request the permission to write to storage
        ActivityCompat.requestPermissions(this, new String[]{permission},
                CanvasExporter.PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Checks whether permissions are accepted. If not, permissions are asked for, otherwise the buttons
     * and any saving functionality are disabled.
     */
    private void checkForPermissions()
    {
        int permission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_DENIED)
        {
            // if the user has not granted permission
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (shouldShowRationale)
            {
                // display a rationale for saving (why the permission is needed)
                StorageRationaleDialog dialog = new StorageRationaleDialog(MainActivity.this);
                dialog.setOnStorageRationaleOptionSelectedListener(new StorageRationaleDialog.StorageRationaleOptionSelectedListener()
                {
                    /**
                     * Callback method which handles whether a user accepted the permission or not.
                     * @param allow - whether the permission is accepted or not.
                     */
                    @Override
                    public void onStorageRationaleOptionSelected(boolean allow)
                    {
                        // if the user accepts the storage permission in the dialog, request it officially
                        if (allow)
                            requestStoragePermission();
                    }
                });
                dialog.show();
            } else
            {
                // request the permission to write to storage
                requestStoragePermission();
            }
        } else
        {
            // save/share the image as permission already granted
            exportImage();
        }
    }

    /**
     * Callback method which handles how a user responded to a permission request.
     * @param requestCode - the code for the requested permission.
     * @param permissions - the permissions.
     * @param grantResults - the results for each permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if the permission is for writing to storage
        if (requestCode == CanvasExporter.PERMISSION_WRITE_EXTERNAL_STORAGE)
        {
            // if the results are not empty and have been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // save/share the drawing as an image
                exportImage();
            } else
            {
                // disable the buttons and save/share functionality
                findViewById(R.id.saveButton).setEnabled(false);
                findViewById(R.id.saveButton).setAlpha(0.5f);
                findViewById(R.id.shareButton).setEnabled(false);
                findViewById(R.id.shareButton).setAlpha(0.5f);
            }
        }
    }

    /**
     * Exports the canvas drawing as an image.
     */
    private void exportImage ()
    {
        if (canvasExporter.getExportType() == CanvasExporter.FLAG_SAVE)
        {
            // if the user is wanting to save, attempt and return its filename
            String fileName = canvasExporter.saveImage(canvasView.getBitmap());

            if (fileName != null)
            {
                // refresh the gallery to show the new image if it exists
                MediaScannerConnection.scanFile(
                        MainActivity.this, new String[]{fileName}, null, null);
                Toast.makeText(MainActivity.this, "The image was saved successfully.", Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(MainActivity.this, "There was an error saving the image.", Toast.LENGTH_SHORT).show();
            }
        } else if (canvasExporter.getExportType() == CanvasExporter.FLAG_SHARE)
        {
            // handle the sharing
            shareImage();
        }
    }

    /**
     * Shares the canvas drawing as an image.
     */
    private void shareImage()
    {
        // create a new intent to a sharing activity.
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // get the image as a file
        File image = canvasExporter.getImage(canvasView.getBitmap());

        if (image != null)
        {
            // retrieve the uri of the created file
            Uri uri = FileProvider.getUriForFile(
                    MainActivity.this,
                    MainActivity.this.getApplicationContext().getPackageName() +
                            ".provider", canvasExporter.getImage(canvasView.getBitmap()));
            // pass the uri to the intent, to allow for sharing
            intent.putExtra(Intent.EXTRA_STREAM, uri).setType("image/png");
            // start the intent
            startActivity(Intent.createChooser(intent, "Share image via"));
        } else
        {
            Toast.makeText(MainActivity.this, "There was an error sharing the image.", Toast.LENGTH_SHORT).show();
        }
    }
}