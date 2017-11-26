package com.nomad5.circularprogress;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.nomad5.circularprogressview.CircularProgressView;

import junit.framework.Assert;

import java.util.Random;

/**
 * Created by mlostek on 04.05.2015.
 * Demo activity to demonstrate the CircularProgressView
 */
public class DemoActivity extends Activity
{
    Random random = new Random();

    /****************************************************************************************************************************
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_demo);
    }

    /****************************************************************************************************************************
     * Start the progress with interpolated mode for random value
     */
    public void onStartRandomClicked(final View view)
    {
        this.startInterpolatedAnim(this.random.nextFloat());
    }

    /****************************************************************************************************************************
     * Start the progress with interpolated mode with target value 0
     */
    public void onStart0Clicked(final View view)
    {
        this.startInterpolatedAnim(0f);
    }

    /****************************************************************************************************************************
     * Start the progress with interpolated mode with target value 1
     */
    public void onStart1Clicked(final View view)
    {
        this.startInterpolatedAnim(1f);
    }

    /****************************************************************************************************************************
     * Start interpolated animation
     */
    private void startInterpolatedAnim(float targetValue)
    {
        final CircularProgressView progressView = this.findViewById(R.id.circularprogress);
        Assert.assertNotNull(progressView);
        progressView.setProgress(targetValue, 3000);
    }
}
