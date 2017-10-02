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
import android.content.res.AssetManager;

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

    private ImageLoaderTask imageLoaderTask;
    private Options panoOptions = new Options();
    
    private LruCache<String, Bitmap> mMemoryCache;

    private URL imageUrl = null;
    private String url;

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

    public void onAfterUpdateTransaction(Context context) {
        panoWidgetView = new VrPanoramaView(_activity);
        panoWidgetView.setEventListener(new ActivityEventListener());
        panoWidgetView.setStereoModeButtonEnabled(showStereo);
        panoWidgetView.setInfoButtonEnabled(showInfo);
        panoWidgetView.setFullscreenButtonEnabled(showFullScreen);
        this.addView(panoWidgetView);

        if (imageLoaderTask != null) {
            imageLoaderTask.cancel(true);
        }
        imageLoaderTask = new ImageLoaderTask(context);
        imageLoaderTask.execute(Pair.create(imageUrl, panoOptions));
    }

    public void setImageUrl(String value) {
        if (imageUrl != null && imageUrl.toString().equals(value)) { return; }

        url = value;
        isLocalUrl = true;

    }

    public void setDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public void setInputType(int value) {
        if (panoOptions.inputType == value) { return; }
        panoOptions.inputType = value;
    }

    class ImageLoaderTask extends AsyncTask<Pair<URL, Options>, Void, Boolean> {

        private Context mContext;

        public ImageLoaderTask (Context context){
             mContext = context;
        }

        protected Boolean doInBackground(Pair<URL, Options>... fileInformation) {

			/*
			if (RNGoogleVRPanoramaView.bitmap != null) {
				RNGoogleVRPanoramaView.bitmap.recycle();
				RNGoogleVRPanoramaView.bitmap = null;
			}
			*/


            final URL imageUrl = fileInformation[0].first;
            Options panoOptions = fileInformation[0].second;

            InputStream istr = null;
            Log.d(TAG, "Image loader task is called: " + url);


			Bitmap image = getBitmapFromMemCache(url);
			if (image == null) {
				if (!isLocalUrl) {

					try {
						HttpURLConnection connection = (HttpURLConnection) fileInformation[0].first.openConnection();
						connection.connect();
						istr = connection.getInputStream();
						image = decodeSampledBitmap(istr);

					} catch (IOException e) {
						Log.e(TAG, "Could not load file: " + e);
						return false;
					} finally {
						try {
						    if (istr!=null) {
							    istr.close();
							}
						} catch (IOException e) {
							Log.e(TAG, "Could not close input stream: " + e);
						}
					}
				} else {
				    AssetManager assetManager = mContext.getAssets();

				    try {
                      istr = assetManager.open(url);
                      image = BitmapFactory.decodeStream(istr);
                    } catch (IOException e) {
                      Log.e(TAG, "Could not decode default bitmap: " + e);
                      return false;
                    }

				}
			}


			if (image != null) {
				//bitmap = image;
				Log.d(TAG, "Image does exist, so we're adding it to the pano: " + url);
				//Bitmap temp = getBitmapFromMemCache(url);
				//RNGoogleVRPanoramaView.bitmap = temp;
				panoWidgetView.loadImageFromBitmap(image, panoOptions);
				try {
                  istr.close();
                } catch (IOException e) {
                  Log.e(TAG, "Could not close input stream: " + e);
                }

			}
            return true;
        }

        private Bitmap decodeSampledBitmap(InputStream inputStream) throws IOException {
            final byte[] bytes = getBytesFromInputStream(inputStream);
            BitmapFactory.Options options = new BitmapFactory.Options();

            if(imageWidth != 0 && imageHeight != 0) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);
                options.inJustDecodeBounds = false;
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        }

        private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            IOUtils.copy(inputStream, baos);

            return baos.toByteArray();
        }

        private int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
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
