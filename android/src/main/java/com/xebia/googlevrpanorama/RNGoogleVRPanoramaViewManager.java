package com.xebia.googlevrpanorama;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ReactMethod;

import java.util.Map;

import javax.annotation.Nullable;

import android.util.Log;




public class RNGoogleVRPanoramaViewManager extends SimpleViewManager<RNGoogleVRPanoramaView> {
    private static final String REACT_CLASS = "RNGoogleVRPanorama";

    private ReactApplicationContext _context;

    public RNGoogleVRPanoramaViewManager(ReactApplicationContext context) {
        super();
        _context = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RNGoogleVRPanoramaView createViewInstance(ThemedReactContext context) {
        return new RNGoogleVRPanoramaView(context, this, context.getCurrentActivity());
    }

    @Override
    protected void onAfterUpdateTransaction(RNGoogleVRPanoramaView view) {
        super.onAfterUpdateTransaction(view);
        view.onAfterUpdateTransaction();
    }

    public @Nullable Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
            .put("onImageLoaded", MapBuilder.of("registrationName", "onImageLoaded"))
            .put("onImageLoadingFailed", MapBuilder.of("registrationName", "onImageLoadingFailed"))
            .build();
    }

    public ReactApplicationContext getContext() {
        return _context;
    }

    @ReactProp(name = "imageUrl")
    public void setImageUrl(RNGoogleVRPanoramaView view, String imageUrl) {
        view.setImageUrl(imageUrl);
    }

    @ReactProp(name = "image")
    public void setImage(RNGoogleVRPanoramaView view, Bitmap image) {
        view.setImage(image);
    }
    
    @ReactProp(name = "showStereo")
    public void setStereo(RNGoogleVRPanoramaView view, boolean showStereo) {
        view.setStereo(showStereo);
    }

    @ReactProp(name = "showInfo")
    public void setInfo(RNGoogleVRPanoramaView view, boolean showInfo) {
        view.setInfo(showInfo);
    }
    
    @ReactProp(name = "showFullScreen")
    public void setFullScreen(RNGoogleVRPanoramaView view, boolean showFullScreen) {
        view.setFullScreen(showFullScreen);
    }

    @ReactProp(name = "dimensions")
    public void setDimensions(RNGoogleVRPanoramaView view, ReadableMap dimensions) {
        int width = dimensions.getInt("width");
        int height = dimensions.getInt("height");
        view.setDimensions(width, height);
    }

    @ReactProp(name = "inputType")
    public void setInputType(RNGoogleVRPanoramaView view, int type) {
        view.setInputType(RNGoogleVRPanoramaNativeModule.inputTypes[type]);
    }
    
    @ReactMethod
    public void clear() {
        Log.d("RNGoogleVRPanorama", "clear called");
    }
}
