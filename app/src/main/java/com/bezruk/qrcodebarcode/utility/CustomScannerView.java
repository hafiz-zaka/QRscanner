package com.bezruk.qrcodebarcode.utility;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import com.bezruk.qrcodebarcode.zxing.ZXingScannerView;
import com.bezruk.qrcodebarcode.zxing.core.CameraUtils;
import com.bezruk.qrcodebarcode.zxing.core.CameraWrapper;
import com.bezruk.qrcodebarcode.zxing.core.IViewFinder;


/**
 * Created by sntech on 2/11/2018.
 */

public class CustomScannerView extends ZXingScannerView {

    private static final String TAG = "CustomScannerView";
    private byte[] mImageData;
    private Camera mCamera;
    private int pWidth = 0, pHeight = 0;
    private CustomViewFinderView customViewFinderView;
    private boolean callbackFocus = false;


    public CustomScannerView(Context context) {
        super(context);
    }

    @Override
    protected IViewFinder createViewFinderView(Context context) {
        customViewFinderView = new CustomViewFinderView(context);
        return customViewFinderView;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        super.onPreviewFrame(data, camera);
        try {
            mImageData = data;
            mCamera = camera;
        } catch (Exception e) {
            // It is possible that this method is invoked after camera is released.
            e.printStackTrace();
        }
    }

    @Override
    public void setResultHandler(ResultHandler resultHandler) {
        super.setResultHandler(resultHandler);
    }

    public byte[] getImageData() {
        return mImageData;
    }

    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public synchronized Rect getFramingRectInPreview(int previewWidth, int previewHeight) {
        pWidth = previewWidth;
        pHeight = previewHeight;
        Log.d(TAG, "getFramingRectInPreview: previewWidth = " + previewWidth);
        Log.d(TAG, "getFramingRectInPreview: previewHeight = " + previewHeight);
        return super.getFramingRectInPreview(previewWidth, previewHeight);
    }

    public int getPreviewWidth() {
        return pWidth;
    }

    public int getPreviewHeight() {
        return pHeight;
    }

    @Override
    public void setFlash(boolean isFlash) {
        try {
            if (CameraUtils.isFlashSupported(mCamera)) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (isFlash) {
                    if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                        return;
                    }
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else {
                    if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                        return;
                    }
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                mCamera.setParameters(parameters);
            }
        } catch (Exception e) {
            // It is possible that this method is invoked after camera is released.
            e.printStackTrace();
        }
    }

    @Override
    public void setupCameraPreview(CameraWrapper cameraWrapper) {
        try {
            if(cameraWrapper != null && cameraWrapper.mCamera != null){
                Camera.Parameters parameters = cameraWrapper.mCamera.getParameters();
                if (parameters != null) {
                    try {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        cameraWrapper.mCamera.setParameters(parameters);
                        callbackFocus = false;
                    } catch (Exception e) {
                        callbackFocus = true;
                    }
                }
            }
        } catch (Exception e){
            // It is possible that this method is invoked after camera is released.
            e.printStackTrace();
        }
        super.setupCameraPreview(cameraWrapper);
    }
    @Override
    public void setAutoFocus(boolean state) {
        super.setAutoFocus(callbackFocus);
    }
}
