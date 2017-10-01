package com.xebia.googlevrpanorama;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.util.Log;
import android.util.Pair;
import android.util.LruCache;
import android.widget.RelativeLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import java.lang.Runtime;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

public class RNGoogleVRPanoramaView extends RelativeLayout {
    private static final String TAG = RNGoogleVRPanoramaView.class.getSimpleName();
	
    // public static Bitmap bitmap = null;
	
    // public Bitmap bitmap = null;
    
    private android.os.Handler _handler;
    private RNGoogleVRPanoramaViewManager _manager;
    private Activity _activity;

    private VrPanoramaView panoWidgetView;

    private Options panoOptions = new Options();
    
    private LruCache<String, Bitmap> mMemoryCache;

    private URL imageUrl = null;
    private String url;

    private String image = null;

    private int imageWidth;
    private int imageHeight;
    
    private boolean showFullScreen = false;
    private boolean showStereo = false;
    private boolean showInfo = false;
    
    
    private boolean isLocalUrl = false;
	// Get max available VM memory, exceeding this amount will throw an
	// OutOfMemory exception. Stored in kilobytes as LruCache takes an
	// int in its constructor.
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory / 8;

    @UiThread
    public RNGoogleVRPanoramaView(Context context, RNGoogleVRPanoramaViewManager manager, Activity activity) {
        super(context);
        _handler = new android.os.Handler();
        _manager = manager;
        _activity = activity;
        
        

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};
    }

	public void setStereo(boolean showStereo) {
		this.showStereo = showStereo;
	}
	
	public void setInfo(boolean showInfo) {
		this.showInfo = showInfo;
	}
	
	public void setFullScreen(boolean showFullScreen) {
		this.showFullScreen = showFullScreen;
	}

	public void setImage(String value) {
        if (image != null) { return; }
        Bitmap bitmap = BitmapFactory.decodeFile(image);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        byte[] bitmapdata = blob.toByteArray();

        Bitmap finalimage = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

        panoWidgetView.loadImageFromBitmap(finalimage, panoOptions);
    }
	
	public void clear() {
		/*
		Log.d("Surya", "Clearing bitmap");
		this.bitmap.recycle();
		*/
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

    public void onAfterUpdateTransaction() {
        panoWidgetView = new VrPanoramaView(_activity);
        panoWidgetView.setEventListener(new ActivityEventListener());
        panoWidgetView.setStereoModeButtonEnabled(showStereo);
        panoWidgetView.setInfoButtonEnabled(showInfo);
        panoWidgetView.setFullscreenButtonEnabled(showFullScreen);
        this.addView(panoWidgetView);
    }

    public void setImageUrl(String value) {
        if (imageUrl != null && imageUrl.toString().equals(value)) { return; }

        try {
            
            url = value;
            String tmepUrl = value.replaceAll("^\\s*file://", "");
            
            if (url.length() > tmepUrl.length()) {
				isLocalUrl = true;
				url = tmepUrl;	
			} else {
				imageUrl = new URL(value);
			}
			
        } catch(MalformedURLException e) {}
    }

    public void setDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public void setInputType(int value) {
        if (panoOptions.inputType == value) { return; }
        panoOptions.inputType = value;
    }

    private class ActivityEventListener extends VrPanoramaEventListener {
        @Override
        public void onLoadSuccess() {
            emitEvent("onImageLoaded", null);
        }

        @Override
        public void onLoadError(String errorMessage) {
            Log.e(TAG, "Error loading pano: " + errorMessage);

            emitEvent("onImageLoadingFailed", null);
        }
    }

    void emitEvent(String name, @Nullable WritableMap event) {
        if (event == null) {
            event = Arguments.createMap();
        }
        ((ReactContext)getContext())
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(getId(), name, event);
    }
}
