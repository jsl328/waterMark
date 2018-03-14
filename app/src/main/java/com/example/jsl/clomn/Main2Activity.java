package com.example.jsl.clomn;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 */
public class Main2Activity extends Activity {

    private static final int PHOTO_WITH_CAMERA = 0;
    private static final int PHOTO_WITH_DATA = 1;
    private String imgName = "";
    private ImageView iv_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
    }

    /**
     * 拍照
     *
     * @param v
     */
    public void takePhoto(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 调用系统相机


        Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);


        // 直接使用，没有缩小
        startActivityForResult(intent, PHOTO_WITH_CAMERA); // 用户点击了从相机获取
    }


    /**
     * 从相册中选取照片
     *
     * @param v
     */
    public void pickPhoto(View v) {
        Intent intent = new Intent();
        intent.setType("image/*"); // 开启Pictures画面Type设定为image
        intent.setAction(Intent.ACTION_GET_CONTENT); // 使用Intent.ACTION_GET_CONTENT这个Action
        startActivityForResult(intent, PHOTO_WITH_DATA); // 取得相片后返回到本画面
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) { // 返回成功
            switch (requestCode) {
                case PHOTO_WITH_CAMERA: {// 拍照获取图片
                    String status = Environment.getExternalStorageState();
                    if (status.equals(Environment.MEDIA_MOUNTED)) { // 是否有SD卡


                        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/image.jpg");


                        imgName = createPhotoFileName();
                        // 保存图片
                        savePhotoToSDCard("/sdcard/photos", imgName, bitmap);
                        if (bitmap != null) {
                            // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                            //Bitmap smallBitmap = zoomBitmap(bitmap, bitmap.getWidth() / 5,
//                                    bitmap.getHeight() / 5);
                            //iv_photo.setImageBitmap(smallBitmap);
                        }
                        Toast.makeText(this, "照片已经保存", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "没有SD卡", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                case PHOTO_WITH_DATA: {// 从图库中选择图片
                    ContentResolver resolver = getContentResolver();
                    // 照片的原始资源地址
                    Uri originalUri = data.getData();
                    try {
                        // 使用ContentProvider通过URI获取原始图片
                        Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);

                        if (photo != null) {
                            // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                            int w = photo.getWidth();
                            int h = photo.getHeight();
                            //添加水印后的位图文件
                            Bitmap smallBitmap =ImageUtilMark(photo,photo);
                            //填充添加后的图片
                            iv_photo.setImageBitmap(smallBitmap);
                            //实际没有用到
                            imgName = createPhotoFileName();
                            Log.d("message",retrunUrlpath(photo,resolver,originalUri));
                            //保存添加水印后的图片到相册原来的位置
                            savePhotoToClum(retrunUrlpath(photo,resolver,originalUri),imgName,smallBitmap);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * @param sourBitmap
     * @param waterBitmap
     * @return
     */
    private Bitmap ImageUtilMark(Bitmap sourBitmap,Bitmap waterBitmap){

        Bitmap watermarkBitmap = ImageUtil.createWaterMaskCenter(sourBitmap, waterBitmap);
        watermarkBitmap = ImageUtil.createWaterMaskLeftBottom(this, watermarkBitmap, waterBitmap, 0, 0);
        watermarkBitmap = ImageUtil.createWaterMaskRightBottom(this, watermarkBitmap, waterBitmap, 0, 0);
        watermarkBitmap = ImageUtil.createWaterMaskLeftTop(this, watermarkBitmap, waterBitmap, 0, 0);
        watermarkBitmap = ImageUtil.createWaterMaskRightTop(this, watermarkBitmap, waterBitmap, 0, 0);

        /*
        * Bitmap textBitmap = ImageUtil.drawTextToLeftTop(this, watermarkBitmap, "左上角", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToRightBottom(this, textBitmap, "右下角", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToRightTop(this, textBitmap, "右上角", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToLeftBottom(this, textBitmap, "左下角", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToCenter(this, textBitmap, "中间", 16, Color.RED);
        * */
        Bitmap textBitmap = ImageUtil.drawTextToLeftTop(this, watermarkBitmap, "", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToRightBottom(this, textBitmap, "@蒋蒋蒋", 20, Color.WHITE, 0, 0);
        textBitmap = ImageUtil.drawTextToRightTop(this, textBitmap, "", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToLeftBottom(this, textBitmap, "", 16, Color.RED, 0, 0);
        textBitmap = ImageUtil.drawTextToCenter(this, textBitmap, "天辰互创", 30, Color.parseColor("#c5576370"));

        return textBitmap;
    }


    /**
     * @param bitmap
     * @param resolver
     * @param uri
     * @return
     */
    private  String retrunUrlpath(Bitmap bitmap ,ContentResolver resolver, Uri uri){

        //这里开始的第二部分，获取图片的路径：
        String[] proj = {MediaStore.Images.Media.DATA};
        String pathStr="";
        Cursor cursor;
        if (Build.VERSION.SDK_INT < 11) {
            cursor = managedQuery(uri, proj, null, null, null);
        } else {
            CursorLoader cursorLoader = new CursorLoader(this, uri, null, null, null, null);
            cursor = cursorLoader.loadInBackground();
            //按我个人理解 这个是获得用户选择的图片的索引值
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //将光标移至开头 ，这个很重要，不小心很容易引起越界
            cursor.moveToFirst();
            pathStr=cursor.getString(column_index);
        }
        return pathStr;
    }

    /** 创建图片不同的文件名
     * @return
     * 新建后的文件名字
     * */
    private String createPhotoFileName() {
        String fileName = "";
        Date date = new Date(System.currentTimeMillis()); // 系统当前时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        fileName = dateFormat.format(date) + ".jpg";
        return fileName;
    }

    /**
     * @param path  源文件路径
     * @param photoName 保存文件名字
     * @param photoBitmap   原来图片
     */
    private void savePhotoToClum(String path, String photoName, Bitmap photoBitmap)
    {
        FileOutputStream fileOutputStream = null;
        File dir = new File(path);
        //找不到图片的文件
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photoFile = new File(path);
        try {
            fileOutputStream = new FileOutputStream(photoFile);
            if (photoBitmap != null) {
                if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (FileNotFoundException e) {
            photoFile.delete();
            e.printStackTrace();
        } catch (IOException e) {
            photoFile.delete();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存照片到SDCard
     *
     * @param path
     *            需要保存的路径
     * @param photoName
     *            保存的相片名字
     * @param photoBitmap
     *            照片的Bitmap对象
     */
    private void savePhotoToSDCard(String path, String photoName, Bitmap photoBitmap) {
        FileOutputStream fileOutputStream = null;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File photoFile = new File(path, photoName);
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    /**
//     * @param bitmap 原始图片
//     * @param width  缩小后的宽
//     * @param height 缩小后的高
//     * @return
//     */
//    private Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        Matrix matrix = new Matrix();
//        float scaleWidth = ((float) width / w);
//        float scaleHeight = ((float) height / h);
//        matrix.postScale(scaleWidth, scaleHeight);
//        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
//        return newbmp;
//    }
//
//    /**
//     * @param src
//     * @param w
//     * @param h
//     * @return
//     */
//    public static Bitmap scaleWithWH(Bitmap src, double w, double h) {
//        if (w == 0 || h == 0 || src == null) {
//            return src;
//        } else {
//            // 记录src的宽高
//            int width = src.getWidth();
//            int height = src.getHeight();
//            // 创建一个matrix容器
//            Matrix matrix = new Matrix();
//            // 计算缩放比例
//            float scaleWidth = (float) (w / width);
//            float scaleHeight = (float) (h / height);
//            // 开始缩放
//            matrix.postScale(scaleWidth, scaleHeight);
//            // 创建缩放后的图片
//            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
//        }
//    }
//
//    public Bitmap drawTextToBitmap(Context gContext,
//                                   int gResId,
//                                   String gText) {
//        Resources resources = gContext.getResources();
//        float scale = resources.getDisplayMetrics().density;
//        Bitmap bitmap =
//                BitmapFactory.decodeResource(resources, gResId);
//
//        bitmap = scaleWithWH(bitmap, 300*scale, 300*scale);
//
//        android.graphics.Bitmap.Config bitmapConfig =
//                bitmap.getConfig();
//
//
//
//        // set default bitmap config if none
//        if(bitmapConfig == null) {
//            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
//        }
//        // resource bitmaps are imutable,
//        // so we need to convert it to mutable one
//        bitmap = bitmap.copy(bitmapConfig, true);
//
//        Canvas canvas = new Canvas(bitmap);
//        // new antialised Paint
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        // text color - #3D3D3D
//        paint.setColor(Color.RED);
//        paint.setTextSize((int) (18 * scale));
//        paint.setDither(true); //获取跟清晰的图像采样
//        paint.setFilterBitmap(true);//过滤一些
//        Rect bounds = new Rect();
//        paint.getTextBounds(gText, 0, gText.length(), bounds);
//        int x = 30;
//        int y = 30;
//        canvas.drawText(gText, x * scale, y * scale, paint);
//        return bitmap;
//    }
//
//    private Bitmap createWatermark(Bitmap bitmap, String mark) {
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bmp);
//        Paint p = new Paint();
//        // 水印颜色
//        p.setColor(Color.parseColor("#c5576370"));
//        // 水印字体大小
//        p.setTextSize(150);
//        //抗锯齿
//        p.setAntiAlias(true);
//        //绘制图像
//        canvas.drawBitmap(bitmap, 0, 0, p);
//        //绘制文字
//        canvas.drawText(mark, 0, h / 2, p);
//        canvas.save(Canvas.ALL_SAVE_FLAG);
//        canvas.restore();
//        return bmp;
//    }
//
//    public static Bitmap getMarkTextBitmap(Context gContext, String gText, int width, int height, boolean is4Showing){
////  Bitmap bitmap = ACache.get(gContext).getAsBitmap(gText);
////  if (is4Showing && bitmap != null){
////   return bitmap;
////  }
//
//        float textSize;
//        float inter;
//        if (is4Showing){
//            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, gContext.getResources().getDisplayMetrics());
//            inter = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, gContext.getResources().getDisplayMetrics());
//        } else {
//            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 54, gContext.getResources().getDisplayMetrics());
//            inter = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, gContext.getResources().getDisplayMetrics());
//        }
//
//        int sideLength;
//        if (width > height) {
//            sideLength = (int) Math.sqrt(2*(width * width));
//        } else {
//            sideLength = (int) Math.sqrt(2*(height * height));
//        }
//
//
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        Rect rect = new Rect();
//        paint.setTextSize(textSize);
//        //获取文字长度和宽度
//        paint.getTextBounds(gText, 0, gText.length(), rect);
//
//        int strwid = rect.width();
//        int strhei = rect.height();
//
//        Bitmap markBitmap = null;
//        try {
//            markBitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_4444);
//            Canvas canvas = new Canvas(markBitmap);
//            //创建透明画布
//            canvas.drawColor(Color.TRANSPARENT);
//
//            paint.setColor(Color.BLACK);
//            paint.setAlpha((int) (0.1*255f));
//            // 获取跟清晰的图像采样
//            paint.setDither(true);
//            paint.setFilterBitmap(true);
//
//            //先平移，再旋转才不会有空白，使整个图片充满
//            if (width > height) {
//                canvas.translate(width - sideLength - inter, sideLength - width + inter);
//            } else {
//                canvas.translate(height - sideLength - inter, sideLength - height + inter);
//            }
//
//            //将该文字图片逆时针方向倾斜45度
//            canvas.rotate(-45);
//
//            for (int i =0; i <= sideLength; ){
//                int count = 0;
//                for (int j =0; j <= sideLength; count++){
//                    if (count % 2 == 0){
//                        canvas.drawText(gText, i, j, paint);
//                    } else {
//                        //偶数行进行错开
//                        canvas.drawText(gText, i + strwid/2, j, paint);
//                    }
//                    j = (int) (j + inter + strhei);
//                }
//                i = (int) (i + strwid + inter);
//            }
//            canvas.save(Canvas.ALL_SAVE_FLAG);
////  ACache.get(gContext).put(gText, markBitmap);
//        } catch (OutOfMemoryError e) {
////            Util.LOGD(TAG, e);
//            if(markBitmap != null && !markBitmap.isRecycled()){
//                markBitmap.recycle();
//                markBitmap = null;
//            }
//        }
//
//        return markBitmap;
//    }
//
//    /**
//     * 获得文字水印的图片
//     * @param width
//     * @param height
//     * @return
//     */
//    public static Drawable getMarkTextBitmapDrawable(Context gContext, String gText, int width, int height, boolean is4Showing){
//        Bitmap bitmap = getMarkTextBitmap(gContext, gText, width, height, is4Showing);
//        if (bitmap != null){
//            BitmapDrawable drawable = new BitmapDrawable(gContext.getResources(), bitmap);
//            drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//            drawable.setDither(true);
////   Drawable drawableFinal = drawable.getConstantState().newDrawable();
////   if(!bitmap.isRecycled()){
////    bitmap.recycle();
////    bitmap = null;
////   }
////
//            return drawable;
//        }
//        return null;
//    }

}
