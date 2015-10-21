package momentum.circularprogress;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import junit.framework.Assert;

import java.util.Random;

import momentum.modules.circularprogressview.CircularProgressView;

/**
 * Created by mlostek on 04.05.2015.
 *
 * Demo activity to demonstrate the CircularProgressView
 */
public class DemoActivity extends Activity
{
    // Random value
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_demo);
    }

    /**
     * Start the progress with interpolated mode for random value
     */
    public void onStartRandomClicked(final View view)
    {
        this.startInterpolatedAnim(this.random.nextFloat());
    }

    /**
     * Start the progress with interpolated mode with target value 0
     */
    public void onStartRandomClicked0(final View view)
    {
        this.startInterpolatedAnim(0f);
    }

    /**
     * Start the progress with interpolated mode with target value 1
     */
    public void onStartRandomClicked1(final View view)
    {
        this.startInterpolatedAnim(1f);
    }

    /**
     * Start interpolated animation
     */
    private void startInterpolatedAnim(float targetValue)
    {
        final CircularProgressView progressView = (CircularProgressView)this.findViewById(R.id.circularprogress);
        Assert.assertNotNull(progressView);
        progressView.setProgress(targetValue, 3000);
    }
}
