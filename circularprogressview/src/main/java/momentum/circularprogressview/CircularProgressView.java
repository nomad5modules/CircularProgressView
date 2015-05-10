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
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mlostek on 10.05.2015.
 *
 * A progress view that shows the progress as a filling arc shape.
 * It renders the text in the inverse color.
 *
 * Pay attention if you are calling the progress update from the ui thread or not,
 * two separate methods exist.
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
	// The dimensions of this view
	int width = 0;
	int height = 0;

	// the paintProgress and the path
	final Paint paint = 			new Paint(Paint.ANTI_ALIAS_FLAG);
	final PorterDuffXfermode cls = 	new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

	RectF progressBounds;
	Rect textBounds = new Rect();

	// the bitmaps to draw on
	Bitmap bmpForeground = null;
	Canvas canvasForeground = null;
	Bitmap bmpBackground = null;
	Canvas canvasBackground = null;

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
			this.progressColor = array.getInt(R.styleable.CircularProgressView_progressColor, this.progressColor);
			this.progressTextColor = array.getInt(R.styleable.CircularProgressView_progressTextColor, this.progressTextColor);
			this.progressTextSize = array.getInt(R.styleable.CircularProgressView_progressTextSize, this.progressTextSize);
			this.borderOffset = array.getInt(R.styleable.CircularProgressView_borderOffset, 50);
			this.progressFont = array.getString(R.styleable.CircularProgressView_progressTextFont);
			String text = array.getString(R.styleable.CircularProgressView_progressText);
			if(text != null) 	this.progressText = text;
			// cleanup
			array.recycle();
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
		if(this.progress <= 0f || this.progress >= 1f)
		{
			canvas.drawColor(Color.TRANSPARENT);
			return;
		}
		float progressAngle = this.progress * 360f;
		float x = (this.width / 2f);
		float y = (this.height / 2f) - this.textBounds.exactCenterY();
		// background
		this.paint.setColor(this.progressTextColor);
		this.canvasBackground.drawColor(Color.WHITE);
		this.canvasBackground.drawText(this.progressText, x, y, this.paint);
		this.paint.setXfermode(this.cls);
		this.canvasBackground.drawArc(this.progressBounds, -90f + progressAngle, 360f - progressAngle, true, this.paint);
		this.paint.setXfermode(null);
		// foreground
		this.paint.setColor(Color.WHITE);
		this.canvasForeground.drawColor(Color.TRANSPARENT);
		this.canvasForeground.drawText(this.progressText, x, y, this.paint);
		this.paint.setXfermode(this.cls);
		this.canvasForeground.drawArc(this.progressBounds, -90f, progressAngle, true, this.paint);
		this.paint.setXfermode(null);
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
	 * Public progress setter
	 * (floats from 0 to 1)
	 * Use this method if you are calling from the ui thread
	 */
	public synchronized void setProgress(float progress)
	{
		// TODO run postInvalidate if this is not the ui thread calling
		this.progress = progress;
		this.invalidate();
	}

	/**
	 * Public progress setter
	 * (floats from 0 to 1)
	 * Use this if a non-ui-thread wants to update the progress
	 */
	/*public synchronized void setProgressNonUiThread(float progress)
	{
		this.progress = progress;
		this.postInvalidate();
	}*/
}
