package com.qianseit.westore.util.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.qianseit.westore.Run;
import com.qianseit.westore.util.Comm;

public class FileUtils {
	/** 
     * sd卡的根目录 
     */  
//    private static String mSdRootPath = Environment.getExternalStorageDirectory().getPath();  
    /** 
     * 手机的缓存根目录 
     */  
//    private static String mDataRootPath = null;  
    /** 
     * 保存Image的目录名 
     */   
//    private final static String FOLDER_NAME = File.separator + Comm.TAG +"/ImageCache";  
      
      
    public FileUtils(Context context){  
//        mDataRootPath = context.getCacheDir().getPath();  
    }  
      
  
    /** 
     * 获取储存Image的目录 
     * @return 
     */  
    private String getStorageDirectory(){  
//    	return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?  
//                mSdRootPath + FOLDER_NAME : mDataRootPath + FOLDER_NAME;  
    	return android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Comm.TAG +"/ImageCache";  
    }  
      
    /** 
     * 保存Image的方法，有sd卡存储到sd卡，没有就存储到手机目录 
     * @param fileName  
     * @param bitmap    
     * @throws IOException 
     */  
    public void savaBitmap(String fileName, Bitmap bitmap) throws IOException{  
        if(bitmap == null){  
            return;  
        }  
        String path = getStorageDirectory();  
        File folderFile = new File(path);  
        if(!folderFile.exists()){  
            folderFile.mkdir();  
        }  
        File file = new File(path + File.separator + fileName);  
        file.createNewFile();  
        FileOutputStream fos = new FileOutputStream(file);  
        bitmap.compress(CompressFormat.PNG, 100, fos);  
        fos.flush();  
        fos.close();  
    }  
      
    /** 
     * 从手机或者sd卡获取Bitmap 
     * @param fileName 
     * @return 
     */  
    public Bitmap getBitmap(String fileName){
    	BitmapFactory.Options bfOptions=new BitmapFactory.Options();
    	 bfOptions.inDither=false;                     //Disable Dithering mode   
         bfOptions.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared   
        bfOptions.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future   
         bfOptions.inTempStorage=new byte[1024];

	
		fileName = getStorageDirectory()+"/" +fileName;
        File file = new File(fileName);
        if(!file.exists())return null;
        FileInputStream fs=null;
        try {
           fs = new FileInputStream(file);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
        Bitmap bmp = null;
        if(fs != null)
           try {
               bmp = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
           } catch (IOException e) {
               e.printStackTrace();
           }finally{ 
               if(fs!=null) {
                   try {
                       fs.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
    return bmp; 
    	
    	
    	
      //  return BitmapFactory.decodeFile(getStorageDirectory() + File.separator + fileName);  
    }  
      
    /** 
     * 判断文件是否存在 
     * @param fileName 
     * @return 
     */  
    public boolean isFileExists(String fileName){  
        return new File(getStorageDirectory() + File.separator + fileName).exists();  
    }  
      
    /** 
     * 获取文件的大小 
     * @param fileName 
     * @return 
     */   
    public long getFileSize(String fileName) {  
        return new File(getStorageDirectory() + File.separator + fileName).length();  
    }  
      
      
    /** 
     * 删除SD卡或者手机的缓存图片和目录 
     */  
    public void deleteFile() {  
        File dirFile = new File(getStorageDirectory());  
        if(!dirFile.exists()){  
            return;  
        }  
        if (dirFile.isDirectory()) {  
            String[] children = dirFile.list();  
            for (int i = 0; i < children.length; i++) {  
                new File(dirFile, children[i]).delete();  
            }  
        }
        dirFile.delete();  
    }  
    
    public static Bitmap getBitMap(String path){
    	BitmapFactory.Options bfOptions=new BitmapFactory.Options();
   	 bfOptions.inDither=false;                     //Disable Dithering mode   
        bfOptions.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared   
       bfOptions.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future   
        bfOptions.inTempStorage=new byte[512];
        
        File file = new File(path);
        if(!file.exists())return null;
        FileInputStream fs=null;
        try {
           fs = new FileInputStream(file);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
        Bitmap mBitmap = null;
        if(fs != null)
           try {
           	  mBitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
           } catch (IOException e) {
               e.printStackTrace();
           }finally{ 
               if(fs!=null) {
                   try {
                       fs.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
        return mBitmap;
    }
	/**
	 * 质量压缩，根据Bitmap质量，压缩
	 * 
	 * @param image
	 *            较大的Bitmap
	 * @param size
	 *            目标图片质量大小(kb)
	 * @return JPEG格式的Bitmap
	 */
	public static Bitmap compressImage(Bitmap image, int size) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 0-100。0意义压缩体积小,100表示质量最佳。一些格式，PNG是无损的，会忽略质量压缩设置，把压缩后的数据存放到baos中
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int quality = 85;
		while (baos.toByteArray().length / 1024 > size) { // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
			// 重置baos
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, quality, baos);// 这里压缩quality%，把压缩后的数据存放到baos中
			quality -= 10;// 每次压缩10%
		}
		Log.i("aaaaa", baos.toByteArray().length / 1024+"");
		
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		try {
			isBm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
    
    public static Bitmap getSmallBitmap(String filePath) {  
        
        final BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        BitmapFactory.decodeFile(filePath, options);  
  
        // Calculate inSampleSize  
        options.inSampleSize = calculateInSampleSize(options, 480, 800);  
  
        // Decode bitmap with inSampleSize set  
        options.inJustDecodeBounds = false;  
          
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);  
        if(bm == null){  
            return  null;  
        }  
        int degree = readPictureDegree(filePath);  
        bm = rotateBitmap(bm,degree) ;  
        ByteArrayOutputStream baos = null ;  
        try{  
            baos = new ByteArrayOutputStream();  
            bm.compress(Bitmap.CompressFormat.JPEG, 30, baos);  
              
        }finally{  
            try {  
                if(baos != null)  
                    baos.close() ;  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return bm ;  
  
    }  
    public static Bitmap getSmallBitmap(String filePath,int width,int height) {  
    	final BitmapFactory.Options options = new BitmapFactory.Options();  
    	options.inJustDecodeBounds = true;  
    	BitmapFactory.decodeFile(filePath, options);  
    	
    	// Calculate inSampleSize  
    	options.inSampleSize = calculateInSampleSize(options, width, height);  
    	
    	// Decode bitmap with inSampleSize set  
    	options.inJustDecodeBounds = false;  
    	
    	Bitmap bm = BitmapFactory.decodeFile(filePath, options);  
    	if(bm == null){  
    		return  null;  
    	}  
    	int degree = readPictureDegree(filePath);  
    	bm = rotateBitmap(bm,degree) ; 
    	ByteArrayOutputStream baos = null ;   
    	try{  
    		baos = new ByteArrayOutputStream();  
    		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
    		
    	}finally{  
    		try {  
    			if(baos != null)  
    				baos.close() ;  
    		} catch (IOException e) {  
    			e.printStackTrace();  
    		}  
    	}  
    	return bm ;  
    }  
    private static int calculateInSampleSize(BitmapFactory.Options options,  
            int reqWidth, int reqHeight) {  
        // Raw height and width of image  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
  
        if (height > reqHeight || width > reqWidth) {  
  
            // Calculate ratios of height and width to requested height and  
            // width  
            final int heightRatio = Math.round((float) height  
                    / (float) reqHeight);  
            final int widthRatio = Math.round((float) width / (float) reqWidth);  
  
            // Choose the smallest ratio as inSampleSize value, this will  
            // guarantee  
            // a final image with both dimensions larger than or equal to the  
            // requested height and width.  
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;  
        }  
  
        return inSampleSize;  
    } 
    private static int readPictureDegree(String path) {    
        int degree  = 0;    
        try {    
                ExifInterface exifInterface = new ExifInterface(path);    
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);    
                switch (orientation) {    
                case ExifInterface.ORIENTATION_ROTATE_90:    
                        degree = 90;    
                        break;    
                case ExifInterface.ORIENTATION_ROTATE_180:    
                        degree = 180;    
                        break;    
                case ExifInterface.ORIENTATION_ROTATE_270:    
                        degree = 270;    
                        break;    
                }    
        } catch (IOException e) {    
                e.printStackTrace();    
        }    
        return degree;    
    }  
    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate){  
        if(bitmap == null)  
            return null ;  
          
        int w = bitmap.getWidth();  
        int h = bitmap.getHeight();  
  
        // Setting post rotate to 90  
        Matrix mtx = new Matrix();  
        mtx.postRotate(rotate);  
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);  
    } 
}
