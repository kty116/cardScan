package com.kyoungae.cardioprac;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TessBaseAPI mTessBaseAPI;
    private TextView mTextView;
    private ImageView mImageView;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textView);
        findViewById(R.id.selectImageButton).setOnClickListener(this);
        mImageView = findViewById(R.id.imageView);


        mTessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
        if (checkLanguageFile(dir + "/tessdata"))
            mTessBaseAPI.init(dir, "eng");
    }

    boolean checkLanguageFile(String dir) {
        File file = new File(dir);
        if (!file.exists() && file.mkdirs())
            createFiles(dir);
        else if (file.exists()) {
            String filePath = dir + "/eng.traineddata";
            File langDataFile = new File(filePath);
            if (!langDataFile.exists())
                createFiles(dir);
        }
        return true;
    }

    private void createFiles(String dir) {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("eng.traineddata");

            String destFile = dir + "/eng.traineddata";

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPhoto() {
        permissionCheck(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                false, new PermissionCheckResponseImpl() {
                    @Override
                    public void granted() {
//                        mCropRatioX = 5;
//                        mCropRatioY = 5;

                        PhotoPicker.builder()
                                .setPhotoCount(1)
                                .setShowCamera(true)
                                .setShowGif(false)
                                .setPreviewEnabled(true)
                                .start(MainActivity.this, PhotoPicker.REQUEST_CODE);
                    }

                    @Override
                    public void denied() {
                    }
                });
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
//                String uri = photos.get(0);
//                String s = Commonlib.getPathFromUri(getContext(), uri);
                File file = new File(photos.get(0));
//                resultImage(file);

                if (file.exists()) { // 파일이 존재한다면
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath()); // 비트맵 생성
                    mImageView.setImageBitmap(myBitmap);
//                    imageText(myBitmap);
                    imageTextgoogle(myBitmap);
                    Toast.makeText(this, "파일 존재", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "파일이 존재 하지 않음", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void imageText(final Bitmap bitmap) {

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mTessBaseAPI.setImage(bitmap);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText(mTessBaseAPI.getUTF8Text());
                        Log.d("ddddddd", "run: " + mTessBaseAPI.getUTF8Text());
                    }
                });
            }
        }).start();

    }

    public void imageTextgoogle(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        final StringBuilder stringBuilder = new StringBuilder();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...
                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    for (FirebaseVisionText.Line line : block.getLines()) {
//                                                Log.d(TAG, "onSuccess: "+line.getText());
                                        stringBuilder.append(line.getText()+"\n");
                                    }

//                                    Toast.makeText(MainActivity.this, block.getText(), Toast.LENGTH_SHORT).show();
//                                    Log.d(TAG, "onSuccess: " + block.getText());
//                                            for (FirebaseVisionText.Line line : block.getLines()){
//                                                Log.d(TAG, "onSuccess: "+line.getText());
//                                                for (FirebaseVisionText.Element element : line.getElements()){
//                                                    Log.d(TAG, "onSuccess: "+element.getText().toString());
//                                                }
//                                            }
                                }

                                mTextView.setText(stringBuilder);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.d(TAG, "onFailure: " + e);
                                    }
                                });

    }


    /**
     * 퍼미션 체크 메소드
     *
     * @param permissionCheckList     //     * @param descriptionMessage      "핸드폰번호와 인증번호를 자동으로 가져오려면 이 권한이 필요합니다."
     *                                //     * @param deniedMessage           "해당 권한을 거부하면 이 서비스를 이용할 수 없습니다.\n- 권한 승인 변경 방법\n[설정] > [애플리케이션] > [담너머] \n> [권한] > 모두 허용"
     * @param permissionCheckResponse
     */

    public static void permissionCheck(final Context context,
                                       final String[] permissionCheckList, boolean isDeniedMessage,
                                       final PermissionCheckResponseImpl permissionCheckResponse) {
        boolean isPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크

                int[] permissionChecks = new int[permissionCheckList.length];

                for (int i = 0; i < permissionChecks.length; i++) {
                    permissionChecks[i] = ContextCompat.checkSelfPermission(context, permissionCheckList[i]);
                    if (permissionChecks[i] == PackageManager.PERMISSION_DENIED) {
                        isPermission = false;
                    }
                }

                if (!isPermission) {

                    PermissionListener permissionlistener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            permissionCheckResponse.granted();
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            permissionCheckResponse.denied();
                        }
                    };

                    if (isDeniedMessage) {

                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
//                            .setRationaleMessage(descriptionMessage)
                                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                                .setPermissions(permissionCheckList)
                                .check();
                    } else {
                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
//                            .setRationaleMessage(descriptionMessage)
                                .setPermissions(permissionCheckList)
                                .check();
                    }
                } else {
                    permissionCheckResponse.granted();
                }
            }

        } else {
            permissionCheckResponse.granted();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectImageButton:

                getPhoto();

                break;
        }
    }

    public interface PermissionCheckResponseImpl {
        void granted();

        void denied();
    }

//    private void resultImage(File imageFile) {
//
////        File imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "이미지 파일 경로.png"); // 파일 불러오기
//
//        if (imageFile.exists()) { // 파일이 존재한다면
//
//            Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath()); // 비트맵 생성
////            int height = myBitmap.getHeight();
////            int width = myBitmap.getWidth();
////
////            Bitmap resized = null;
////            while (height > 118) {
////                resized = Bitmap.createScaledBitmap(myBitmap, (width * 118) / height, 118, true);
////            }
//
//            Bitmap image1 = null;
//            OpenCVLoader.initDebug(); // 이 코드를 선언해주지않으면 컴파일 에러 발생
//
//            Mat img1 = new Mat();
//            Utils.bitmapToMat(myBitmap, img1);
//
//            testContour(img1);
//
////            Mat imageGray1 = new Mat();
////            Mat imageCny1 = new Mat();
////
////            int thresh = 100;
////
////
////            Imgproc.cvtColor(img1, imageGray1, Imgproc.COLOR_BGR2GRAY); // GrayScale
////
//////            Imgproc.Canny(imageGray1, imageCny1, thresh, thresh * 2, 3); // Canny Edge 검출
////
////
//////            Imgproc.threshold(imageGray1, imageCny1, 125, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU); //Binary
////
////
////            List<MatOfPoint> contours = new ArrayList<>();
//////            List<MatOfPoint2f> contours2 = new ArrayList<>();
////            Scalar color = new Scalar(0, 255, 0);
//////            Mat dest = Mat.zeros(imageCny1.size(), CvType.CV_8UC3);
//////
////            Mat hierarchy = new Mat();
////            Mat rectangleMat = new Mat();
////
////            int[][] listarr = new int[8][4];
////
////            Imgproc.findContours(imageGray1, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point());
////
////            int count = 0;
////
////            for(int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
////
////                if(count>7) break;
////
////                MatOfPoint matOfPoint = contours.get(idx);
////
////                Rect rect = Imgproc.boundingRect(matOfPoint);
////
////                // 해상도별로 조절하기.
////
////                if(rect.x < myBitmap.getWidth()/20 || rect.x > myBitmap.getWidth()-(myBitmap.getWidth()/10) ||
////
////                        rect.y < myBitmap.getHeight()/10 || rect.y > myBitmap.getHeight()-(myBitmap.getHeight()/10) ||
////
////                        rect.width < myBitmap.getWidth()/50 || rect.width > myBitmap.getWidth()/8 ||
////
////                        rect.height <= myBitmap.getHeight()/10 ) continue;
////
////
////
////                Log.d("RECT : ", "x : " + rect.x + ", y : " + rect.y + ", w :" + rect.width + ", h : " + rect.height);
////
////                rectangle(img1, rect.tl(), rect.br(), new Scalar(255, 0, 0, 255), 2);
////
////                listarr[count][0] = rect.x;
////
////                listarr[count][1] = rect.y;
////
////                listarr[count][2] = rect.width;
////
////                listarr[count][3] = rect.height;
////
////                count++;
////
////            }
////
//////            for (int i = 0; i < contours.size(); i++) {
//////                MatOfPoint cnt = contours.get(i);
//////                double area = Imgproc.contourArea(cnt);
//////                Rect rect = Imgproc.boundingRect(cnt);
//////
//////                double rectArea = rect.width * rect.height;  //rect 영역 사이즈
//////                double aspectRatio = (rect.width) / rect.height;  //rect의 비율
//////
//////                if (aspectRatio >= 0.5 && aspectRatio <= 1 && rectArea >= 200 && rectArea <= 1400) {
//////
//////                    Imgproc.rectangle(imageCny1, rect, new Scalar(255, 0, 0), 2);
////////                    Rect rect1 = Imgproc.boundingRect(cnt);
//////
//////                }
//////            }
//////
//////            Log.d(TAG, "resultImage: " + contours.size());
////
////
////            image1 = Bitmap.createBitmap(img1.cols(), img1.rows(), Bitmap.Config.ARGB_8888); // 비트맵 생성
////
////            Utils.matToBitmap(img1, image1); // Mat을 비트맵으로 변환
////
////
////            ImageView imageView = (ImageView) findViewById(R.id.imageView);
////
////            imageView.setImageBitmap(image1); // 이미지 뷰에 비트맵 출력
////
////            imageText(image1);
//        }
//    }
//
//    public void imageLine(Mat imageCny1) {
//        List<MatOfPoint> contours = new ArrayList<>();
//
//        Mat hierarchy = new Mat();
//
//        Imgproc.findContours(imageCny1, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//
//        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
//
//            MatOfPoint matOfPoint = contours.get(idx);
//
//            Rect rect = Imgproc.boundingRect(matOfPoint);
//
//            if (rect.width < 30 || rect.height < 30 || rect.width <= rect.height || rect.x < 20 || rect.y < 20
//
//                    || rect.width <= rect.height * 3 || rect.width >= rect.height * 6)
//                continue; // 사각형 크기에 따라 출력 여부 결정
//
//
//// ROI 출력
//
////            Bitmap image1 = Bitmap.createBitmap(hierarchy.cols(), hierarchy.rows(), Bitmap.Config.ARGB_8888); // 비트맵 생성
////
////            Utils.matToBitmap(imageCny1, image1); // Mat을 비트맵으로 변환
//
////            Bitmap roi = Bitmap.createBitmap(image1, (int) rect.tl().x, (int) rect.tl().y, rect.width, rect.height);
////
////            ImageView imageView1 = (ImageView) findViewById(R.id.imageView);
////
////            imageView1.setImageBitmap(roi);
//
//        }
//
//
////        Bitmap image1 = Bitmap.createBitmap(img1.cols(), img1.rows(), Bitmap.Config.ARGB_8888);
////
////        Utils.matToBitmap(img1, image1); // Mat to Bitmap
////
////
////        imageView = (ImageView) findViewById(R.id.image_result);
////
////        imageView.setImageBitmap(image1);
//
//    }
//
////    private void findImageline(){
////
////    }
//
//    private void testContour(Mat imageMat) {
//        Mat rgb = new Mat();  //rgb color matrix
//        rgb = imageMat.clone();
//        Mat grayImage = new Mat();  //grey color matrix
//        Imgproc.cvtColor(rgb, grayImage, Imgproc.COLOR_RGB2GRAY);
//
//        Mat gradThresh = new Mat();  //matrix for threshold
//        Mat hierarchy = new Mat();    //matrix for contour hierachy
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        //Imgproc.threshold(grayImage,gradThresh, 127,255,0);  global threshold
//        Imgproc.adaptiveThreshold(grayImage, gradThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 3, 12);  //block size 3
//        Imgproc.findContours(gradThresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//        if (contours.size() > 0) {
//            for (int idx = 0; idx < contours.size(); idx++) {
//                Rect rect = Imgproc.boundingRect(contours.get(idx));
//                if (rect.height > 10 && rect.width > 40 && !(rect.width >= 512 - 5 && rect.height >= 512 - 5)) {
//                    rectangle(imageMat, new Point(rect.br().x - rect.width, rect.br().y - rect.height)
//                            , rect.br()
//                            , new Scalar(0, 255, 0), 5);
//                }
//
//            }
//            Imgcodecs.imwrite("/tmp/dev/doc_original.jpg", rgb);
//            Imgcodecs.imwrite("/tmp/dev/doc_gray.jpg", grayImage);
//            Imgcodecs.imwrite("/tmp/dev/doc_thresh.jpg", gradThresh);
//            Imgcodecs.imwrite("/tmp/dev/doc_contour.jpg", imageMat);
//        }
//
//        Bitmap image1 = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
//
//        Utils.matToBitmap(imageMat, image1); // Mat to Bitmap
//
//
//        mImageView = (ImageView) findViewById(R.id.imageView);
//
//        mImageView.setImageBitmap(image1);
//    }
}
