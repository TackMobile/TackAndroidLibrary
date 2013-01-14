package com.tack.android.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class BitmapHelper {

	private static final int THUMB_HEIGHT = 80;
	
	private static final int MAX_SAMPLE_SIZE = 4;
	
	public static Bitmap bitmapToThumbnail(Bitmap bitmap) {
		final int bmpWidth = bitmap.getWidth();
		final int bmpHeight = bitmap.getHeight();
		
		final int ratio = THUMB_HEIGHT / bmpHeight;
		final int newWidth = ratio * bmpWidth;
		
		return Bitmap.createBitmap(newWidth, THUMB_HEIGHT, Bitmap.Config.ARGB_8888);
	}
	
	public static Bitmap scaleBytesToBitmap(byte[] imageData, int maxSize) {
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new BufferedInputStream(new ByteArrayInputStream(imageData)), null,o);

        //Find the correct scale value. It should be the power of 2.
        int scale=1;
        while(o.outWidth/scale/2>=maxSize && o.outHeight/scale/2>=maxSize)
            scale*=2;

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize=scale;
        return BitmapFactory.decodeStream(new BufferedInputStream(new ByteArrayInputStream(imageData)), null, o2);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		// don't scale if we don't have a size yet
		if (reqWidth == 0 || reqHeight == 0) return inSampleSize;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return Math.min(MAX_SAMPLE_SIZE, inSampleSize);
	}
	
	private BitmapHelper(){}
}
