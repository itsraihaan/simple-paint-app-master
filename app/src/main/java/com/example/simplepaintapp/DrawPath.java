package com.example.simplepaintapp;

import android.graphics.Path;

/**
 * Class which holds the path, colour, and stroke width for a drawn path which is displayed on the canvas.
 */
public class DrawPath
{
    private final int colour;
    private final int width;

    private final Path path;

    /**
     * Constructor for the DrawPath class.
     * @param colour - the colour of the path to be drawn.
     * @param width - the width of the path to be drawn.
     * @param path - the path object to be drawn.
     */
    public DrawPath(int colour, int width, Path path)
    {
        this.colour = colour;
        this.width = width;
        this.path = path;
    }

    /**
     * Returns the colour of the path to be drawn.
     * @return int - the colour of the path.
     */
    public int getColour() {
        return colour;
    }

    /**
     * Returns the width of the path to be drawn.
     * @return int - the width of the path.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the path object to be drawn.
     * @return Path - the path object.
     */
    public Path getPath() {
        return path;
    }
}