package com.iot.bcrec.soundcapture;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SoundService extends Service {

    /**public static final String TAG="Sound";
    int bufferSize;
    private short[] audioBuffer;
    public int bufferReadSize;
    private Thread readingThread = null;
    private boolean isRecording = false;
    Intent i = new Intent("SoundData");
    AudioRecord audioInput;
    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        //Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "+ channelConfig);
                        bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        //Log.e(C.TAG, rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
    }
*/
    public SoundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**@Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"Service created");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        audioInput = findAudioRecord();
        audioInput.startRecording();
        Log.i(TAG,"Recording started");
        Log.i(TAG,bufferSize+" ");
        audioBuffer = new short[bufferSize/2];
        isRecording = true;

        Runnable s= new Runnable() {
            @Override
            public void run() {

                    int x=0;
                    while(isRecording) {
                        synchronized (this) {
                            try {
                                wait(1000);
                                if(x>=320)
                                {
                                    x=0;
                                }
                                bufferReadSize = audioInput.read(audioBuffer, 0, bufferSize / 2);
                                Log.i(TAG, audioBuffer[x]+"" );
                                i.putExtra("Data", String.valueOf(audioBuffer[x]));
                                sendBroadcast(i);
                                x++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    audioInput.stop();
                    audioInput.release();
                    audioInput = null;
            }
        };
        readingThread = new Thread(s);
        readingThread.start();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        isRecording= false;
        super.onDestroy();
    }*/
}
