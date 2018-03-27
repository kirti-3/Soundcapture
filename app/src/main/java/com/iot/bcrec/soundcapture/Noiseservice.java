package com.iot.bcrec.soundcapture;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Noiseservice extends Service
{
    private DoubleFFT_1D fft = null;

    private float [] THIRD_OCTAVE = {16, 20, 25, 31.5f, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500,
            630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000};
    String [] THIRD_OCTAVE_LABEL = {"16", "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160", "200", "250", "315", "400", "500",
            "630", "800", "1000", "1250", "1600", "2000", "2500", "3150", "4000", "5000", "6300", "8000", "10000", "12500", "16000", "20000"};

    String levelToShow;


    double linearFftAGlobalRunning = 0;
    private long fftCount = 0;
    private double dbFftAGlobalRunning;
    private float[] dbBandRunning = new float[THIRD_OCTAVE.length];
    private float[] linearBandRunning = new float[THIRD_OCTAVE.length];

    private double dbFftAGlobalMax;
    private double dbFftAGlobalMin;
    private double dbATimeDisplay;
    private float dbFftTimeDisplay[] = new float[BLOCK_SIZE_FFT / 2];
    private float dbFftATimeDisplay[] = new float[BLOCK_SIZE_FFT / 2];
    private float[] dbBandTimeDisplay = new float[THIRD_OCTAVE.length];
    private float[] linearBandTimeDisplay = new float[THIRD_OCTAVE.length];


    double dbFftAGlobalMinTemp = 0;
    double dbFftAGlobalMaxTemp = 0;
    int dbFftAGlobalMinFirst = 0;
    int dbFftAGlobalMaxFirst = 0;

    private Date dateLogStart;

    float[] dbBandMax = new float[THIRD_OCTAVE.length];
    float[] dbBandMin = new float[THIRD_OCTAVE.length];
    int kkk = 0;

    private float[] dbAHistoryTimeDisplay = new float[750];
    private float[] dbFftAGlobalRunningHistory = new float[750];


    private int timeLog;
    private String timeLogStringMinSec;
    private double timeDisplay = 0.1;
    private AudioRecord recorder;
    //LoggerForNoise obj;

    private final static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private final static int RECORDER_SAMPLERATE = 44100;
    private final static int BYTES_PER_ELEMENT = 2;
    private final static int BLOCK_SIZE = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)
            / BYTES_PER_ELEMENT;
    private final static int BLOCK_SIZE_FFT = 1764;
    private final static int NUMBER_OF_FFT_PER_SECOND = RECORDER_SAMPLERATE
            / BLOCK_SIZE_FFT;
    private final static double FREQRESOLUTION = ((double) RECORDER_SAMPLERATE)
            / BLOCK_SIZE_FFT;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    public static final String TAG="Sound";
    private double filter = 0;

    private double[] weightedA = new double[BLOCK_SIZE_FFT];
    private double actualFreq;
    private float gain=0.0f;
    int finalCountTimeDisplay = (int) (timeDisplay * NUMBER_OF_FFT_PER_SECOND);
    private int count=0;
    Intent i = new Intent("SoundData");
    Intent noi = new Intent("SoundData1");




    public Noiseservice()
    {
    }

    public void precalculateWeightedA() {
        for (int i = 0; i < BLOCK_SIZE_FFT; i++) {
            actualFreq = FREQRESOLUTION * i;
            double actualFreqSQ = actualFreq * actualFreq;
            double actualFreqFour = actualFreqSQ * actualFreqSQ;
            double actualFreqEight = actualFreqFour * actualFreqFour;

            double t1 = 20.598997 * 20.598997 + actualFreqSQ;
            t1 = t1 * t1;
            double t2 = 107.65265 * 107.65265 + actualFreqSQ;
            double t3 = 737.86223 * 737.86223 + actualFreqSQ;
            double t4 = 12194.217 * 12194.217 + actualFreqSQ;
            t4 = t4 * t4;

            double weightFormula = (3.5041384e16 * actualFreqEight)
                    / (t1 * t2 * t3 * t4);

            weightedA[i] = weightFormula;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        precalculateWeightedA();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BLOCK_SIZE * BYTES_PER_ELEMENT);

        if (recorder.getState() == 1)
            Log.i(TAG," The recorder is ready");
        else
            Log.i(TAG," The recorder is not ready");

        recorder.startRecording();
        isRecording = true;




        fft = new DoubleFFT_1D(BLOCK_SIZE_FFT);

        recordingThread = new Thread(new Runnable() {
            public void run() {


                short rawData[] = new short[BLOCK_SIZE_FFT];

                final float dbFft[] = new float[BLOCK_SIZE_FFT / 2];

                final float dbFftA[] = new float[BLOCK_SIZE_FFT / 2];

                float normalizedRawData;

                double[] audioDataForFFT = new double[BLOCK_SIZE_FFT * 2];

                float amplitudeRef = 0.00002f;



                final float[] dbBand = new float[THIRD_OCTAVE.length];

                final float[] linearBand = new float[THIRD_OCTAVE.length];
                final float[] linearBandCount = new float[THIRD_OCTAVE.length];
                int n = 3;
                int indexTimeDisplay = 1;
                double linearATimeDisplay = 0;



                int indexTimeLog = 0;
                double linearTimeLog = 0;
                double linearATimeLog = 0;
                final float[] linearBandTimeLog = new float[THIRD_OCTAVE.length];

                final float linearFftTimeDisplay[] = new float[BLOCK_SIZE_FFT / 2];
                final float linearFftATimeDisplay[] = new float[BLOCK_SIZE_FFT / 2];

                int initial_delay = 0;


                while (isRecording) {

                    recorder.read(rawData, 0, BLOCK_SIZE_FFT);


                    initial_delay++;

                    if (initial_delay > 20) {

                        for (int i = 0, j = 0; i < BLOCK_SIZE_FFT; i++, j += 2) {

                            normalizedRawData = (float) rawData[i]
                                    / (float) Short.MAX_VALUE;


                            filter = normalizedRawData;


                            double x = (2 * Math.PI * i) / (BLOCK_SIZE_FFT - 1);
                            double winValue = (1 - Math.cos(x)) * 0.5d;


                            audioDataForFFT[j] = filter * winValue;

                            audioDataForFFT[j + 1] = 0.0;
                        }

                        fft.complexForward(audioDataForFFT);

                        double linearFftGlobal = 0;

                        double linearFftAGlobal = 0;

                        int k = 0;

                        for (int ki = 0; ki < THIRD_OCTAVE.length; ki++) {
                            linearBandCount[ki] = 0;
                            linearBand[ki] = 0;
                            dbBand[ki] = 0;
                        }

                        for (int i = 0, j = 0; i < BLOCK_SIZE_FFT / 2; i++, j += 2) {

                            double re = audioDataForFFT[j];
                            double im = audioDataForFFT[j + 1];

                            double mag = Math.sqrt((re * re) + (im * im));

                            double weightFormula = weightedA[i];

                            dbFft[i] = (float) (10 * Math.log10(mag * mag
                                    / amplitudeRef))
                                    + (float) gain;
                            dbFftA[i] = (float) (10 * Math.log10(mag * mag
                                    * weightFormula
                                    / amplitudeRef))
                                    + (float) gain;

                            linearFftGlobal += Math.pow(10, (float) dbFft[i] / 10f);
                            linearFftAGlobal += Math.pow(10, (float) dbFftA[i] / 10f);

                            float linearFft = (float) Math.pow(10, (float) dbFft[i] / 10f);


                            if ((0 <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 17.8f)) {
                                linearBandCount[0] += 1;
                                linearBand[0] += linearFft;
                                dbBand[0] = (float) (10 * Math.log10(linearBand[0]));
                            }
                            if ((17.8f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 22.4f)) {
                                linearBandCount[1] += 1;
                                linearBand[1] += linearFft;
                                dbBand[1] = (float) (10 * Math.log10(linearBand[1]));
                            }
                            if ((22.4f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 28.2f)) {
                                linearBandCount[2] += 1;
                                linearBand[2] += linearFft;
                                dbBand[2] = (float) (10 * Math.log10(linearBand[2]));
                            }
                            if ((28.2f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 35.5f)) {
                                linearBandCount[3] += 1;
                                linearBand[3] += linearFft;
                                dbBand[3] = (float) (10 * Math.log10(linearBand[3]));
                            }
                            if ((35.5f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 44.7f)) {
                                linearBandCount[4] += 1;
                                linearBand[4] += linearFft;
                                dbBand[4] = (float) (10 * Math.log10(linearBand[4]));
                            }
                            if ((44.7f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 56.2f)) {
                                linearBandCount[5] += 1;
                                linearBand[5] += linearFft;
                                dbBand[5] = (float) (10 * Math.log10(linearBand[5]));
                            }
                            if ((56.2f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 70.8f)) {
                                linearBandCount[6] += 1;
                                linearBand[6] += linearFft;
                                dbBand[6] = (float) (10 * Math.log10(linearBand[6]));
                            }
                            if ((70.8f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 89.1f)) {
                                linearBandCount[7] += 1;
                                linearBand[7] += linearFft;
                                dbBand[7] = (float) (10 * Math.log10(linearBand[7]));
                            }
                            if ((89.1f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 112f)) {
                                linearBandCount[8] += 1;
                                linearBand[8] += linearFft;
                                dbBand[8] = (float) (10 * Math.log10(linearBand[8]));
                            }
                            if ((112f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 141f)) {
                                linearBandCount[9] += 1;
                                linearBand[9] += linearFft;
                                dbBand[9] = (float) (10 * Math.log10(linearBand[9]));
                            }
                            if ((141f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 178f)) {
                                linearBandCount[10] += 1;
                                linearBand[10] += linearFft;
                                dbBand[10] = (float) (10 * Math.log10(linearBand[10]));
                            }
                            if ((178f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 224f)) {
                                linearBandCount[11] += 1;
                                linearBand[11] += linearFft;
                                dbBand[11] = (float) (10 * Math.log10(linearBand[11]));
                            }
                            if ((224f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 282f)) {
                                linearBandCount[12] += 1;
                                linearBand[12] += linearFft;
                                dbBand[12] = (float) (10 * Math.log10(linearBand[12]));
                            }
                            if ((282f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 355f)) {
                                linearBandCount[13] += 1;
                                linearBand[13] += linearFft;
                                dbBand[13] = (float) (10 * Math.log10(linearBand[13]));
                            }
                            if ((355f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 447f)) {
                                linearBandCount[14] += 1;
                                linearBand[14] += linearFft;
                                dbBand[14] = (float) (10 * Math.log10(linearBand[14]));
                            }
                            if ((447f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 562f)) {
                                linearBandCount[15] += 1;
                                linearBand[15] += linearFft;
                                dbBand[15] = (float) (10 * Math.log10(linearBand[15]));
                            }
                            if ((562f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 708f)) {
                                linearBandCount[16] += 1;
                                linearBand[16] += linearFft;
                                dbBand[16] = (float) (10 * Math.log10(linearBand[16]));
                            }
                            if ((708f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 891f)) {
                                linearBandCount[17] += 1;
                                linearBand[17] += linearFft;
                                dbBand[17] = (float) (10 * Math.log10(linearBand[17]));
                            }
                            if ((891f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1122f)) {
                                linearBandCount[18] += 1;
                                linearBand[18] += linearFft;
                                dbBand[18] = (float) (10 * Math.log10(linearBand[18]));
                            }
                            if ((1122f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1413f)) {
                                linearBandCount[19] += 1;
                                linearBand[19] += linearFft;
                                dbBand[19] = (float) (10 * Math.log10(linearBand[19]));
                            }
                            if ((1413f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1778f)) {
                                linearBandCount[20] += 1;
                                linearBand[20] += linearFft;
                                dbBand[20] = (float) (10 * Math.log10(linearBand[20]));
                            }
                            if ((1778f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 2239f)) {
                                linearBandCount[21] += 1;
                                linearBand[21] += linearFft;
                                dbBand[21] = (float) (10 * Math.log10(linearBand[21]));
                            }
                            if ((2239f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 2818f)) {
                                linearBandCount[22] += 1;
                                linearBand[22] += linearFft;
                                dbBand[22] = (float) (10 * Math.log10(linearBand[22]));
                            }
                            if ((2818f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 3548f)) {
                                linearBandCount[23] += 1;
                                linearBand[23] += linearFft;
                                dbBand[23] = (float) (10 * Math.log10(linearBand[23]));
                            }
                            if ((3548f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 4467f)) {
                                linearBandCount[24] += 1;
                                linearBand[24] += linearFft;
                                dbBand[24] = (float) (10 * Math.log10(linearBand[24]));
                            }
                            if ((4467f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 5623f)) {
                                linearBandCount[25] += 1;
                                linearBand[25] += linearFft;
                                dbBand[25] = (float) (10 * Math.log10(linearBand[25]));
                            }
                            if ((5623f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 7079f)) {
                                linearBandCount[26] += 1;
                                linearBand[26] += linearFft;
                                dbBand[26] = (float) (10 * Math.log10(linearBand[26]));
                            }
                            if ((7079f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 8913f)) {
                                linearBandCount[27] += 1;
                                linearBand[27] += linearFft;
                                dbBand[27] = (float) (10 * Math.log10(linearBand[27]));
                            }
                            if ((8913f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 11220f)) {
                                linearBandCount[28] += 1;
                                linearBand[28] += linearFft;
                                dbBand[28] = (float) (10 * Math.log10(linearBand[28]));
                            }
                            if ((11220f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 14130f)) {
                                linearBandCount[29] += 1;
                                linearBand[29] += linearFft;
                                dbBand[29] = (float) (10 * Math.log10(linearBand[29]));
                            }
                            if ((14130f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 17780f)) {
                                linearBandCount[30] += 1;
                                linearBand[30] += linearFft;
                                dbBand[30] = (float) (10 * Math.log10(linearBand[30]));
                            }
                            if ((17780f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 22390f)) {
                                linearBandCount[31] += 1;
                                linearBand[31] += linearFft;
                                dbBand[31] = (float) (10 * Math.log10(linearBand[31]));
                            }

                        }


                        final double dbFftAGlobal = 10 * Math.log10(linearFftAGlobal);

                        if (dbFftAGlobal > 0) {
                            if (dbFftAGlobalMinFirst == 0) {
                                dbFftAGlobalMinTemp = dbFftAGlobal;
                                dbFftAGlobalMinFirst = 1;
                            } else {
                                if (dbFftAGlobalMinTemp > dbFftAGlobal) {
                                    dbFftAGlobalMinTemp = dbFftAGlobal;
                                }
                            }
                            if (dbFftAGlobalMaxFirst == 0) {
                                dbFftAGlobalMaxTemp = dbFftAGlobal;
                                dbFftAGlobalMaxFirst = 1;
                            } else {
                                if (dbFftAGlobalMaxTemp < dbFftAGlobal) {
                                    dbFftAGlobalMaxTemp = dbFftAGlobal;
                                }
                            }
                        }
                        dbFftAGlobalMin = dbFftAGlobalMinTemp;
                        dbFftAGlobalMax = dbFftAGlobalMaxTemp;


                        fftCount++;
                        linearFftAGlobalRunning += linearFftAGlobal;
                        dbFftAGlobalRunning = 10 * Math.log10(linearFftAGlobalRunning / fftCount);

                        for (int ki = 0; ki < THIRD_OCTAVE.length; ki++) {
                            linearBandRunning[ki] += linearBand[ki];
                            dbBandRunning[ki] = 10 * (float) Math.log10(linearBandRunning[ki] / fftCount);
                        }

                        for (int kk = 0; kk < dbBand.length; kk++) {
                            if (dbBandMax[kk] < dbBand[kk]) {
                                dbBandMax[kk] = dbBand[kk];
                            }
                            if (kkk >= 10) {
                                if (dbBandMin[kk] == 0f) {
                                    if (dbBand[kk] > 0) {
                                        dbBandMin[kk] = dbBand[kk];
                                    }
                                } else if (dbBandMin[kk] > dbBand[kk]) {
                                    dbBandMin[kk] = dbBand[kk];
                                }
                            }
                        }
                        kkk++;


                        linearATimeDisplay += linearFftAGlobal;
                        for (int i = 0; i < THIRD_OCTAVE.length; i++) {
                            linearBandTimeDisplay[i] += linearBand[i];
                        }

                        for (int i = 0; i < dbFftTimeDisplay.length; i++) {
                            linearFftTimeDisplay[i] +=  Math.pow(10, (float) dbFft[i] / 10f);
                            linearFftATimeDisplay[i] +=  Math.pow(10, (float) dbFftA[i] / 10f);
                        }

                        if (indexTimeDisplay < finalCountTimeDisplay) {
                            indexTimeDisplay++;
                        } else {
                            for (int i = 0; i < THIRD_OCTAVE.length; i++) {
                                dbBandTimeDisplay[i] =  10 *  (float) Math.log10(linearBandTimeDisplay[i] / finalCountTimeDisplay);
                                linearBandTimeDisplay[i] = 0;
                            }

                            // FFT plot
                            for (int i = 0; i < dbFftTimeDisplay.length; i++) {
                                dbFftTimeDisplay[i] =  10 *  (float) Math.log10(linearFftTimeDisplay[i] / finalCountTimeDisplay);
                                dbFftATimeDisplay[i] =  10 *  (float) Math.log10(linearFftATimeDisplay[i] / finalCountTimeDisplay);
                                linearFftTimeDisplay[i] = 0;
                                linearFftATimeDisplay[i] = 0;
                            }

                            dbATimeDisplay = 10 * Math.log10(linearATimeDisplay / finalCountTimeDisplay);
                            indexTimeDisplay = 1;
                            linearATimeDisplay = 0;

                            /**if(count>10){
                                isRecording=false;
                                noi.putExtra("button",true);
                                sendBroadcast(noi);
                                count=0;
                            }*/
                            i.putExtra("Data", String.valueOf(dbATimeDisplay));
                            sendBroadcast(i);
                            //count++;

                                   synchronized (this) {
                                        try {
                                            wait(200);

                                            noi.putExtra("Data1", String.valueOf(dbATimeDisplay));
                                            sendBroadcast(noi);

                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                        }
                    }



                } // while
            }
        }, "AudioRecorder Thread");
        recordingThread.start();

        return Service.START_REDELIVER_INTENT;
    }

    /*public String counting(double data,int cont)
    {

        int limit = Integer.parseInt(MainActivity.time1)*60;
        avgdata =avgdata + Math.pow(10,data/10);
        Log.i("kksTURN",String.valueOf(cont));
        Log.i("kks1234",String.valueOf(avgdata));
        if(cont==limit)
        {
            avgdata =avgdata/limit;
            avgdata =10 * Math.log10(avgdata);
            String out = String.valueOf(avgdata);
            Log.i("kksLOGMean",String.valueOf(avgdata));

            return out;
        }
        return "0";
    }*/



    @Override
    public void onDestroy() {
        isRecording = false;
        try {
            recordingThread.join();
            //fos.close();
        } catch (Exception e) {
            Log.d(TAG,"Error");
        }
        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
