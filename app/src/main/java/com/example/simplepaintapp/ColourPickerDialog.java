package com.example.simplepaintapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Custom dialog which displays the various colour options to the user.
 */
public class ColourPickerDialog extends Dialog implements View.OnClickListener
{
    private ColourPickerOptionSelectedListener listener;

    private final ArrayList<ColourButton> colourButtons;

    /**
     * Constructor for the class. Handles the creation of the dialog window and setting of listeners.
     * @param context - the context of the application.
     * @param currentColour - the colour being used for drawing.
     */
    public ColourPickerDialog(@NonNull Context context, int currentColour)
    {
        // set the current activity using the context
        super(context);
        setOwnerActivity ((Activity) context);
        // define the window and set the background as transparent to allow for rounded corners
        Window window = super.getWindow ();
        if (window != null)
            window.setBackgroundDrawable (new ColorDrawable(Color.TRANSPARENT));
        // set the resource to be used for the dialog
        super.setContentView (R.layout.activity_colour_picker_dialog);
        super.setCancelable (true);
        // Assign variables to the three rows of colour buttons
        LinearLayout colourColumn1 = findViewById(R.id.columnLayout1);
        LinearLayout colourColumn2 = findViewById(R.id.columnLayout2);
        LinearLayout colourColumn3 = findViewById(R.id.columnLayout3);
        // Create an ArrayList of the colour buttons
        ArrayList<Integer> colours = new ArrayList<>();
        addColours(colours, colourColumn1);
        addColours(colours, colourColumn2);
        addColours(colours, colourColumn3);
        // Extract the various colour IDs to be assigned
        final int[] colourIDs = ColourManager.getColourIDs();
        // Create an ArrayList for the colour buttons
        colourButtons = new ArrayList<>();
        // Loop through the colour buttons extracted from the layout
        for (int i = 0; i < colours.size(); i++)
        {
            // Create a view object for the colour button and set a click listener
            View view = findViewById(colours.get(i));
            view.setOnClickListener(this);
            // Get the colour representation of the colour ID
            int colour = ContextCompat.getColor(context, colourIDs[i]);
            // Adjust the colour of the button to match
            GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
            gradientDrawable.setColor(ContextCompat.getColor(context, colourIDs[i]));
            // Get the resource ID for the tick icon
            int tickResourceID = getTickResourceID(colour);
            // Create a ColourButton object using the View ID, colour ID, and "tick" ID
            ColourButton colourButton = new ColourButton(colours.get(i), colourIDs[i], tickResourceID);
            colourButtons.add(colourButton);
            // Set the "tick" icon if the current colour is selected in CanvasView
            if (colour == currentColour)
                ((ImageButton) view).setImageResource(tickResourceID);
        }
        // Initialise the buttons
        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonSelect = findViewById(R.id.buttonSelect);
        // Set the click listeners for the buttons
        buttonCancel.setOnClickListener(this);
        buttonSelect.setOnClickListener(this);
    }

    /**
     * Sets the listener for the dialog box.
     * @param listener - the listener for options being selected.
     */
    public void setOnDialogOptionSelectedListener (ColourPickerOptionSelectedListener listener)
    {
        this.listener = listener;
    }

    /**
     * Adds the IDs of the various colour buttons to an ArrayList.
     * @param colours - the ArrayList of colours.
     * @param colourColumn - the LinearLayout which houses the colour buttons.
     */
    private void addColours (ArrayList<Integer> colours, LinearLayout colourColumn)
    {
        // loop through the LinearLayout which holds all the colours and add them to the array
        for (int i = 0; i < colourColumn.getChildCount(); i++)
            colours.add(colourColumn.getChildAt(i).getId());
    }

    /**
     * Returns whether the current UI theme is night mode or not.
     * @return boolean - whether night mode or not
     */
    private boolean isNightMode ()
    {
        return (getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Returns the "tick" resource drawable which depends on whether the colour is light or dark.
     * @param colour - the colour to check against.
     * @return int - the resource ID for the "tick" drawable.
     */
    private int getTickResourceID (int colour)
    {
        int tickResourceID;
        if (isNightMode())
        {
            // prefer white "tick" drawables over black
            tickResourceID = R.drawable.ic_done_black_24dp;
            if (ColourManager.isSignificantlyDark(colour))
                tickResourceID = R.drawable.ic_done_white_24dp;
        } else
        {
            // prefer black "tick" drawables over white
            tickResourceID = R.drawable.ic_done_white_24dp;
            if (ColourManager.isSignificantlyLight(colour))
                tickResourceID = R.drawable.ic_done_black_24dp;
        }
        return tickResourceID;
    }

    /**
     * The click handler for the various clickable elements in the dialog.
     * @param v - the dialog view object which was clicked.
     */
    @Override
    public void onClick(View v)
    {
        // get the ID of the view object being clicked
        int viewID = v.getId();
        // prompt the listener depending on the option selected
        if (viewID == R.id.buttonCancel || viewID == R.id.buttonSelect)
        {
            dismiss();
        } else
        {
            // loop through the colour button drawables
            for (ColourButton colourButton : colourButtons)
            {
                // if the clicked view object is the same as the colour button
                if (colourButton.getViewID() == viewID)
                {
                    // get the colour of the button
                    int colour = ContextCompat.getColor(getContext(), colourButton.getColourID());
                    // set the "tick" icon overlay to show the colour is selected
                    ((ImageButton) v).setImageResource(colourButton.getSelectedIconID());
                    listener.onColourPickerOptionSelected(colour);
                } else
                {
                    // get the button as a view and remove any pre-existing "tick" drawable
                    View view = findViewById(colourButton.getViewID());
                    ((ImageButton) view).setImageDrawable(null);
                }
            }
        }
    }

    /**
     * Class which holds the ID of the view, colour, and selected icon for each colour button.
     */
    private static class ColourButton
    {
        private final int viewID;
        private final int colourID;
        private final int selectedIconID;

        /**
         * Constructor which assigns the ID of the view, colour, and selected icon.
         * @param viewID - the ID of the colour button view.
         * @param colourID - the ID of the colour code.
         * @param selectedIconID - the ID of the "tick" icon.
         */
        public ColourButton(int viewID, int colourID, int selectedIconID)
        {
            this.viewID = viewID;
            this.colourID = colourID;
            this.selectedIconID = selectedIconID;
        }

        /**
         * Returns the ID of the colour button view.
         * @return int - the view ID.
         */
        public int getViewID()
        {
            return viewID;
        }

        /**
         * Returns the ID of the colour.
         * @return int - the colour ID.
         */
        public int getColourID()
        {
            return colourID;
        }

        /**
         * Returns the ID of the "tick" icon.
         * @return int - the "tick" drawable ID.
         */
        public int getSelectedIconID ()
        {
            return selectedIconID;
        }
    }

    /**
     * Interface which handles callbacks when dialog options are selected.
     */
    public interface ColourPickerOptionSelectedListener
    {
        void onColourPickerOptionSelected (int colour);
    }
}