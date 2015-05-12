package momentum.circularprogressview;

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
 */
public class CircularProgressView extends View
{
	// This is the size of the arc shape
	private int borderOffset = 		150;
	// The color of the circular progress
	private int progressColor = 	Color.WHITE;
	// The color of the text in the foreground
	private int progressTextColor = Color.BLACK;
	// The size of the text
	private int progressTextSize = 	32;
	// The font of the text
	private String progressFont =	null;
	// The main text to be rendered
	private String progressText = 	"";
	// The progress value
	private float progress = 		0f;
	// The fade-in and fade-out times
	private int fadeTime = 			0;
	private int fadeAlpha = 		0;
	private Thread fadeThread =		null;
	// The dimensions of this view
	int width = 					0;
	int height = 					0;

	// the paintProgress and the path
	final Paint paint = 			new Paint(Paint.ANTI_ALIAS_FLAG);
	final PorterDuffXfermode cls = 	new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

	RectF progressBounds = 			new RectF();
	Rect textBounds = 				new Rect();

	// the bitmaps to draw on
	Bitmap bmpForeground = 			null;
	Canvas canvasForeground = 		null;
	Bitmap bmpBackground = 			null;
	Canvas canvasBackground = 		null;

	//region//////////////////////////////////////////////////////////////// CONSTRUCTION
	/**
	 * Java Code Constructor
	 */
	public CircularProgressView(Context context)
	{
		super(context);
	}

	/**
	 * XML Constructor
	 */
	public CircularProgressView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.parseAttributes(context, attrs);
	}

	/**
	 * XML Constructor with style
	 */
	public CircularProgressView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.parseAttributes(context, attrs);
	}

	/**
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
			this.progressTextColor = array.getInt(R.styleable.CircularProgressView_progressTextColor, this.progressTextColor);
			this.progressTextSize = array.getInt(R.styleable.CircularProgressView_progressTextSize, this.progressTextSize);
			this.progressFont = array.getString(R.styleable.CircularProgressView_progressTextFont);
			String text = array.getString(R.styleable.CircularProgressView_progressText);
			if(text != null) 	this.progressText = text;
			this.fadeTime = array.getInt(R.styleable.CircularProgressView_fadeTime, this.fadeTime);
			if(this.fadeTime < 0) this.fadeAlpha = 255;
			// cleanup
			array.recycle();
		}
	}
	//endregion////////////////////////////////////////////////////////////////////////////

	//region//////////////////////////////////////////////////////////////// FADE RUNNABLE
	/**
	 * The fade runnable
	 */
	class FadeRunnable implements Runnable
	{
		final boolean fadeIn;
		final int fadeTimeMs;

		FadeRunnable(boolean fadeIn, int fadeTime)
		{
			this.fadeIn = fadeIn;
			this.fadeTimeMs = fadeTime;
		}

		@Override
		public void run()
		{
			long startMs = System.currentTimeMillis();
			long endMs = startMs + this.fadeTimeMs;
			long currentMs = startMs;
			while(currentMs <= endMs)
			{
				int newAlpha = (int)(255f * (currentMs - startMs) / this.fadeTimeMs);
				CircularProgressView.this.fadeAlpha = this.fadeIn ? newAlpha : 255 - newAlpha;
				CircularProgressView.this.postInvalidate();
				try								{	Thread.sleep(5);		}
				catch(InterruptedException e)	{	e.printStackTrace();	}
				currentMs = System.currentTimeMillis();
			}
			CircularProgressView.this.fadeAlpha = this.fadeIn ? 255 : 0;
			CircularProgressView.this.postInvalidate();
		}
	}
	//endregion////////////////////////////////////////////////////////////////////////////

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

	@Override
	protected void onDraw(Canvas canvas)
	{
		float progressAngle = this.progress * 360f;
		progressAngle = Math.min(progressAngle, 360f);
		progressAngle = Math.max(0f, progressAngle);
		float x = (this.width / 2f);
		float y = (this.height / 2f) - this.textBounds.exactCenterY();
		// background
		this.paint.setColor(this.progressTextColor);
		this.canvasBackground.drawColor(this.progressColor);
		this.canvasBackground.drawText(this.progressText, x, y, this.paint);
		this.paint.setXfermode(this.cls);
		this.canvasBackground.drawArc(this.progressBounds, -90f + progressAngle, 360f - progressAngle, true, this.paint);
		this.paint.setXfermode(null);
		// foreground
		this.paint.setColor(this.progressColor);
		this.canvasForeground.drawColor(Color.TRANSPARENT);
		this.canvasForeground.drawText(this.progressText, x, y, this.paint);
		this.paint.setXfermode(this.cls);
		this.canvasForeground.drawArc(this.progressBounds, -90f, progressAngle, true, this.paint);
		this.paint.setXfermode(null);
		this.paint.setAlpha(this.fadeAlpha);
		// compose
		canvas.drawBitmap(this.bmpBackground, 0, 0, this.paint);
		canvas.drawBitmap(this.bmpForeground, 0, 0, this.paint);
	}

	/**
	 * Set the title of this view programmatically
	 * Updates the text bounds needed for rendering
	 */
	public void setText(String text)
	{
		this.progressText = text;
		this.paint.getTextBounds(this.progressText, 0, this.progressText.length(), this.textBounds);
	}

	/**
	 * Progress setter
	 * (floats from 0 to 1)
	 *
	 * Checks whether you are calling from UI thread or not. Just use it
	 */
	public synchronized void setProgress(float progress)
	{
		this.progress = progress;
		// check if we have to start a fade
		if(this.fadeTime >= 0)
		{
			if(progress <= 0f)			this.startFade(true);
			else if(progress >= 1f)		this.startFade(false);
		}
		// check if this is the ui thread and trigger redraw
		if(Looper.myLooper() == Looper.getMainLooper())
		{
			this.invalidate();
		}
		else
		{
			this.postInvalidate();
		}
	}

	/**
	 * Start a fade
	 */
	private void startFade(boolean fadeIn)
	{
		// check existing
		if(this.fadeThread != null)
		{
			// check if fade is already running
			switch(this.fadeThread.getState())
			{
				case TERMINATED:
				case NEW:
					this.fadeThread = null;
					break;
				case RUNNABLE:
				case BLOCKED:
				case TIMED_WAITING:
				case WAITING:
					return;
			}
		}
		// create new
		this.fadeThread = new Thread(new FadeRunnable(fadeIn, this.fadeTime));
		this.fadeThread.start();
	}
}
