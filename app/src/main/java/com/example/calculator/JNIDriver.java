package com.example.calculator;

import android.util.Log;

public class JNIDriver implements JNIListener {
    private boolean mConnectFlag;

    private TranseThread mTranseThread;
    private JNIListener mMainActivity;

    static {
        System.loadLibrary("JNIDriver");
    }

    private native static int openDriver(String path);

    private native static void closeDriver();

    private native char readDriver();

    private native int getInterrupt();

    public JNIDriver() {
        mConnectFlag = false;
    }

    @Override
    public void onReceive(int val) {

        Log.e("test", "4");
        if (mMainActivity != null) {
            mMainActivity.onReceive(val);
            Log.e("test", "2");
        }
    }

    public void setListener(MainActivity a){mMainActivity=a;}
    public int open(String driver){
        if(mConnectFlag) return -1;

        if((openDriver(driver)>0)){
            mConnectFlag = true;
            mConnectFlag = true;
            mTranseThread = new TranseThread();
            mTranseThread.start();
            return 1;
        } else {
            return -1;
        }
    }

    public void close(){
    if(!mConnectFlag) return;
    mConnectFlag = false;
    closeDriver();
    }

    protected void finalize() throws Throwable{
    close();
    super.finalize();
    }

    public char read(){return readDriver();}
    private class TranseThread extends Thread{

        @Override
        public void run(){
            super.run();
            try{
                while (mConnectFlag){
                    try {Log.e("test", "1");
                        onReceive(getInterrupt());
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        e.printStackTrace();
            }
        }
    }catch(Exception e){

    }
}
}
}
