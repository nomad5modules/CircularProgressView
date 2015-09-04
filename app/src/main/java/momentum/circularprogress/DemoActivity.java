package momentum.circularprogress;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import junit.framework.Assert;

import momentum.circularprogressview.CircularProgressView;

/**
 * Created by mlostek on 04.05.2015.
 *
 * Demo activity to demonstrate the CircularProgressView
 */
public class DemoActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_demo);
	}

	/**
	 * Start the progress
	 */
	public void onStartClicked(final View view)
	{
		view.setEnabled(false);
		final CircularProgressView progressView = (CircularProgressView)this.findViewById(R.id.circularprogress);
		Assert.assertNotNull(progressView);
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
		animator.setDuration(5000);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				progressView.setProgress(animation.getAnimatedFraction());
			}
		});
		animator.addListener(new Animator.AnimatorListener()
		{
			@Override
			public void onAnimationStart(Animator animation){}
			@Override
			public void onAnimationEnd(Animator animation){	view.setEnabled(true); }
			@Override
			public void onAnimationCancel(Animator animation){}
			@Override
			public void onAnimationRepeat(Animator animation){}
		});
		animator.start();
	}
}
