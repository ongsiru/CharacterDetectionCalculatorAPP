package com.example.calculator;

import static java.lang.String.*;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MainActivity extends AppCompatActivity implements JNIListener{
    TessBaseAPI tessBaseAPI;

    Button button;
    ImageView imageView;
    CameraSurfaceView surfaceView;
    TextView textView;

    TextView tv;
    String str = "";

    JNIDriver mDriver;
    //ReceiveThread mSegThread;
    boolean mThreadRun = true;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("OpenCLDriver");
        System.loadLibrary("JNISegmentDriver");
        System.loadLibrary("JNILEDDriver");
    }

    private native static int openDriver2(String path);
    private native static void closeDriver2();
    private native static void writeDriver2(byte[] data, int length);

    private native static int openDriver3(String path);
    private native static void closeDriver3();
    private native static void writeDriver3(byte[] data, int length);

    int data_int, i;
    boolean mTreadRun, mStart;
    SegmentThread mSegThread;
    byte[] data = {0,0,0,0,0,0,0,0};

    String calcResult2="";

    private class SegmentThread extends Thread{
        @Override
        public void run(){
            super.run();
            while(mTreadRun){
                byte[] n = {0,0,0,0,0,0,0};

                if(!mStart){writeDriver2(n, n.length);}
                else{
                    for(i=0;i<100;i++){
                        n[0] = (byte) (data_int % 1000000 / 100000);
                        n[1] = (byte) (data_int % 1000000 / 10000);
                        n[2] = (byte) (data_int % 1000000 / 1000);
                        n[3] = (byte) (data_int % 1000000 / 100);
                        n[4] = (byte) (data_int % 1000000 / 10);
                        n[5] = (byte) (data_int % 10);
                        writeDriver2(n, n.length);
                    }
                }
            }
        }
    }


    //blur GPU
    public native Bitmap GaussianBlurGPU(Bitmap bitmap);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDriver = new JNIDriver();
        mDriver.setListener(this);

        tv = (TextView)findViewById(R.id.textView1);

        if(mDriver.open("/dev/sm9s5422_interrupt")<0){
            Toast.makeText(MainActivity.this, "Driver Open Failed", Toast.LENGTH_SHORT).show();
        }


        imageView = findViewById(R.id.imageView);
        surfaceView = findViewById(R.id.surfaceView);
        textView = findViewById(R.id.textView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });

        tessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
        if(checkLanguageFile(dir+"/tessdata"))
            tessBaseAPI.init(dir, "eng");
    }

    @Override
    protected void onPause(){
        //TODO Auto-generated method stub
        mDriver.close();
        closeDriver2();
        closeDriver3();
        mTreadRun=false;
        mSegThread=null;
        super.onPause();
    }

    public Handler handler = new Handler((Looper.getMainLooper())){
        public void handleMessage(Message msg){
            switch (msg.arg1){
                case 1: {
                    tv.setText("결과출력 - Segment");
                    String str2 = calcResult2;
                    try{
                        data_int = Integer.parseInt(str2);
                        mStart = true;
                    }
                    catch (NumberFormatException E){
                        Toast.makeText(MainActivity.this, "Input Error", Toast.LENGTH_SHORT).show();
                    }
                }

                    break;
                case 2:
                    tv.setText("이진화 - LED");
                    int num = Integer.parseInt(calcResult2);

                    for (int i=0;i<8;i++) {
                        data[i] = (byte) (num % 2);
                        num = num/2;
                    }

                    writeDriver3(data,data.length);
                    break;
                case 3:
                    tv.setText("Left");
                    break;
                case 4:
                    tv.setText("Right");
                    break;
                case 5:
                    tv.setText("Center:설명 / Up:결과출력 / Down:이진화");
                    break;
            }
        }
    };

    @Override
    protected void onResume(){
        //TODO Auto-generated method stub
        if(openDriver2("/dev/sm9s5422_segment")<0){
            Toast.makeText(MainActivity.this, "Driver Open Failed", Toast.LENGTH_SHORT).show();
        }
        if(openDriver3("/dev/sm9s5422_led")<0){
            Toast.makeText(MainActivity.this, "Driver Open Failed",Toast.LENGTH_SHORT).show();
        }
        mTreadRun = true;
        mSegThread = new SegmentThread();
        mSegThread.start();
        super.onResume();
    }

    @Override
    public void onReceive(int val){
        //TODO Auto-generated method stub
        Message text = Message.obtain();
        text.arg1 = val;
        handler.sendMessage(text);

    }

    boolean checkLanguageFile(String dir)
    {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/eng.traineddata";
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
        }
        return true;
    }

    private void createFiles(String dir)
    {
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
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void capture()
    {
        surfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmap = GetRotatedBitmap(bitmap, 180);

                button.setEnabled(false);
                button.setText("텍스트 인식중...");

                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap buf_bitmap=BitmapFactory.decodeFile("/data/local/tmp/text.jpg", options);
                buf_bitmap = GaussianBlurGPU(buf_bitmap);
                imageView.setImageBitmap(buf_bitmap);

                options.inSampleSize = 8;
                new AsyncTess().execute(bitmap);
                camera.startPreview();

            }
        });
    }


    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {
        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            tessBaseAPI.setImage(mRelativeParams[0]);
            return tessBaseAPI.getUTF8Text();
        }

        protected void onPostExecute(String result) {
            textView.setText(result);

            calc calc = new calc();

            String calcResult= "";
            for (int i = 0; i < result.length(); i++) {
                char ch = result.charAt(i);
                if ((40 <= ch && ch <= 43)||45==ch||(47 <= ch && ch <= 57)) {
                    calcResult += ch;
                }
            }

            calcResult2 = format("%d", calc.run(calcResult));

            Toast.makeText(MainActivity.this, ""+result+"="+calcResult2, Toast.LENGTH_LONG).show();

            button.setEnabled(true);
            button.setText("텍스트 인식");
        }
    }


}