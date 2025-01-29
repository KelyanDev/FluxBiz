package com.kelyandev.fluxbiz.Bizzes.Circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.kelyandev.fluxbiz.R;

public class SmallProgressCircleView extends View {
    private Paint circlePaint;
    private Paint progressPaint;
    private Paint textPaint;

    private int maxChars = 280;
    private int currentChars = 0;
    private int circleStrokeWidth = 5;

    private RectF oval;
    private int primaryColor, disabledColor;

    /**
     * Constructor for the progress circle
     * @param context The context of the progress circle
     * @param attrs The attributes of the progress circle
     */
    public SmallProgressCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initialize the different parts of the progress circle
     */
    private void init(Context context) {
        primaryColor = ContextCompat.getColor(context, R.color.my_light_primary);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true);
        disabledColor = typedValue.data;

        // Configure the paint for the background circle
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(disabledColor);
        circlePaint.setStrokeWidth(circleStrokeWidth);
        circlePaint.setAntiAlias(true);

        // Configure the paint for the progress circle
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(circleStrokeWidth);
        progressPaint.setAntiAlias(true);

        // Configure the paint for the text
        textPaint = new Paint();
        textPaint.setColor(Color.LTGRAY);
        textPaint.setTextSize(25);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        oval = new RectF();
    }

    /**
     * Function used to draw the circle
     * @param canvas The canva
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - circleStrokeWidth;

        oval.set(
                width / 2f - radius,
                height / 2f - radius,
                width / 2f + radius,
                height / 2f + radius
        );

        canvas.drawCircle(width / 2f, height / 2f, radius, circlePaint);

        float sweepAngle = (360f * currentChars) / maxChars;

        int remainingChars = maxChars - currentChars;
        if (remainingChars > 20) {
            progressPaint.setColor(primaryColor);
        } else if (remainingChars > 0) {
            progressPaint.setColor(Color.YELLOW);
            textPaint.setColor(Color.YELLOW);
        } else {
            progressPaint.setColor(Color.RED);
            textPaint.setColor(Color.RED);
        }

        canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);

        if (remainingChars <= 20) {
            String text = String.valueOf(remainingChars);
            float x = width / 2f;
            float y = height / 2f - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(text, x, y, textPaint);
        }
    }

    /**
     * Sets the current chars to a new value. Use this when writing in an edit text field
     * @param currentChars The new current characters count
     */
    public void setCurrentChars(int currentChars) {
        this.currentChars = currentChars;
        invalidate();
    }

    /**
     * Sets the max chars for the edit text. The remaining characters will be calculated using this attribute
     * @param maxChars The new max characters limit
     */
    public void setMaxChars(int maxChars) {
        this.maxChars = maxChars;
    }
}
