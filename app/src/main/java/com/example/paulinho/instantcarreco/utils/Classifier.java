

package com.example.paulinho.instantcarreco.utils;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.example.paulinho.instantcarreco.model.Recognition;

import java.util.List;

/**
 * Created by paulinho on 4/15/2018.
 */

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {
    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */


    List<Recognition> recognizeImage(Bitmap bitmap);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();
}

