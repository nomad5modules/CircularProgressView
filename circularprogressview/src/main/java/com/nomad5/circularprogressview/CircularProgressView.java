package com.nomad5.circularprogressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mlostek on 10.05.2015.
 *
 * A progress view that shows the progress as a filling arc shape.
 * It renders the text in the inverse color for the 'done' area of the arc.
 *
 * It supports a fade-in and fade-out time. (By default set to 0)
 * It supports interpolation between values (Place a #VAL# in the progress text and call setProgress right)
 */
public class CircularProgressView extends View
{
    // This is the size of the arc shape
    private int borderOffset = 		    250;
    // The color of the circular progress
    private int progressColor = 	    Color.WHITE;
    // The size of the text
    private int progressTextSize = 	    32;
    // The font of the text
    private String progressFont =	    null;
    // The main text to be rendered
    private String progressText = 	    "";
    // The progress values
    private float progress = 		    0f;
    // Check if we have to add the progress float
    private boolean progressAddValue =  false;
    // The fade-in and fade-out times
    private int fadeTimeMs =		    0;
    // The dimensions of this view
    int width = 					    0;
    int height = 					    0;

    // the paintProgress and the path
    final Paint paint = 			    new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    final PorterDuffXfermode cls = 	    new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    RectF progressBounds = 			    new RectF();
    Rect textBounds = 				    new Rect();

    // the bitmaps to draw on
    Bitmap bmpForeground = 			    null;
    Canvas canvasForeground = 	    	null;
    Bitmap bmpBackground = 			    null;
    Canvas canvasBackground = 		    null;

    // the interpolate animator
    ValueAnimator valueAnimator =       null;

    //region//////////////////////////////////////////////////////////////// CONSTRUCTION
    /****************************************************************************************************************************
     * Java Code Constructor
     */
    public CircularProgressView(Context context)
    {
        super(context);
    }

    /****************************************************************************************************************************
     * XML Constructor
     */
    public CircularProgressView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.parseAttributes(context, attrs);
    }

    /****************************************************************************************************************************
     * XML Constructor with style
     */
    public CircularProgressView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.parseAttributes(context, attrs);
    }

    /****************************************************************************************************************************
     * Parse the attributes
     */
    private void parseAttributes(Context context, AttributeSet attrs)
    {
        // prevent exception in interface builders
        if(this.isInEditMode())
        {
            return;
        }
        // parse attributes first
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView);
        if(array != null)
        {
            // get attributes
            this.borderOffset = array.getInt(R.styleable.CircularProgressView_borderOffset, this.borderOffset);
            this.progressColor = array.getInt(R.styleable.CircularProgressView_progressColor, this.progressColor);
            this.progressTextSize = array.getInt(R.styleable.CircularProgressView_progressTextSize, this.progressTextSize);
            this.progressFont = array.getString(R.styleable.CircularProgressView_progressTextFont);
            this.parseText(array.getString(R.styleable.CircularProgressView_progressText));
            this.fadeTimeMs = array.getInt(R.styleable.CircularProgressView_fadeTimeMs, this.fadeTimeMs);
            this.setAlpha(this.fadeTimeMs > 0f ? 0f : 1f);
            // cleanup
            array.recycle();
        }
    }

    /****************************************************************************************************************************
     * Check the given text string for #VAL#
     */
    private void parseText(String text)
    {
        // reset
        this.progressText = "";
        this.progressAddValue = false;
        if(text != null)
        {
            if(text.contains("#VAL#"))
            {
                text = text.replace("#VAL#", "%2.0f");
                this.progressAddValue = true;
            }
            this.progressText = text;
        }
        this.paint.getTextBounds(this.progressText, 0, this.progressText.length(), this.textBounds);
    }
    //endregion////////////////////////////////////////////////////////////////////////////

    //region//////////////////////////////////////////////////////////////// ANIMATOR LISTENER
    /****************************************************************************************************************************
     */
    ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener()
    {
        @Override
        public void onAnimationUpdate(ValueAnimator animation)
        {
            CircularProgressView.this.updateAndRender((float)animation.getAnimatedValue());
        }
    };
    //endregion////////////////////////////////////////////////////////////////////////////

    //region//////////////////////////////////////////////////////////////// FADING
    /****************************************************************************************************************************
     * Start fade in
     */
    private void fadeIn()
    {
        this.animate().alpha(1f).setDuration(this.fadeTimeMs).start();
    }

    /****************************************************************************************************************************
     * Start fade out
     */
    private void fadeOut()
    {
        this.animate().alpha(0f).setDuration(this.fadeTimeMs).start();
    }
    //endregion////////////////////////////////////////////////////////////////////////////

    /****************************************************************************************************************************
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // save dimensions
        this.width = width;
        this.height = height;
        // bitmaps and canvas
        this.bmpBackground = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        this.canvasBackground = new Canvas(this.bmpBackground);
        this.bmpForeground = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        this.canvasForeground = new Canvas(this.bmpForeground);
        // create render objects
        this.progressBounds = new RectF(-this.borderOffset,
                                               -this.borderOffset,
                                               this.width + this.borderOffset,
                                               this.height + this.borderOffset);
        // the painter
        if(this.progressFont != null)
        {
            this.paint.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(), this.progressFont));
        }
        this.paint.setTextSize(this.progressTextSize);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setTextAlign(Paint.Align.CENTER);
        // get text bounds
        this.paint.getTextBounds(this.progressText, 0, this.progressText.length(), this.textBounds);
    }

    /****************************************************************************************************************************
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        // update the text bounds
        String txt = this.progressText;
        if(this.progressAddValue)
        {
            txt = String.format(this.progressText, this.progress * 100f);
            this.paint.getTextBounds(txt, 0, txt.length(), this.textBounds);

        }
        float progressAngle = this.progress * 360f;
        progressAngle = Math.min(progressAngle, 360f);
        progressAngle = Math.max(0f, progressAngle);
        float x = (this.width / 2f);
        float y = (this.height / 2f) - this.textBounds.exactCenterY();
        // background
        this.paint.setXfermode(null);
        this.paint.setColor(Color.BLACK);
        this.canvasBackground.drawColor(this.progressColor);
        this.paint.setXfermode(this.cls);
        this.canvasBackground.drawText(txt, x, y, this.paint);
        this.canvasBackground.drawArc(this.progressBounds, -90f + progressAngle, 360f - progressAngle, true, this.paint);
        // foreground
        this.paint.setXfermode(null);
        this.paint.setColor(this.progressColor);
        this.bmpForeground.eraseColor(Color.argb(0, 0, 0, 0));
        this.canvasForeground.drawColor(Color.TRANSPARENT);
        this.canvasForeground.drawText(txt, x, y, this.paint);
        this.paint.setXfermode(this.cls);
        this.canvasForeground.drawArc(this.progressBounds, -90f, progressAngle, true, this.paint);
        this.paint.setXfermode(null);
        // compose
        canvas.drawBitmap(this.bmpBackground, 0, 0, this.paint);
        canvas.drawBitmap(this.bmpForeground, 0, 0, this.paint);
    }

    /****************************************************************************************************************************
     * Set the title of this view programmatically
     * Updates the text bounds needed for rendering
     */
    @SuppressWarnings("unused")
    public void setText(String text)
    {
        this.parseText(text);
    }

    /****************************************************************************************************************************
     * Progress setter
     * (floats from 0 to 1)
     *
     * Checks whether you are calling from UI thread or not. Just use it
     */
    public synchronized void setProgress(float targetProgress, int interpolateTimeMs)
    {
        // check if we shall interpolate
        if(interpolateTimeMs > 0)
        {
            float currentValue = 0f;
            if(this.valueAnimator != null)
            {
                currentValue = (float) this.valueAnimator.getAnimatedValue();
                this.valueAnimator.removeAllUpdateListeners();
            }
            this.valueAnimator = ValueAnimator.ofFloat(currentValue, targetProgress);
            this.valueAnimator.addUpdateListener(this.updateListener);
            this.valueAnimator.setDuration(interpolateTimeMs);
            this.valueAnimator.start();
        }
        else
        {
            this.updateAndRender(targetProgress);
        }
    }

    /****************************************************************************************************************************
     * Set the progress value and render it
     */
    private void updateAndRender(float targetProgress)
    {
        // check fading ...
        if(this.fadeTimeMs > 0)
        {
            if     (targetProgress > 0f && this.progress == 0f) this.fadeIn();
            else if(targetProgress < 1f && this.progress == 1f) this.fadeIn();
            else if(targetProgress == 0f && this.progress > 0f) this.fadeOut();
            else if(targetProgress == 1f && this.progress < 1f) this.fadeOut();
        }
        // ... update ...
        this.progress = targetProgress;
        // ... and render
        if(Looper.myLooper() == Looper.getMainLooper())
        {
            this.invalidate();
        }
        else
        {
            this.postInvalidate();
        }
    }
}