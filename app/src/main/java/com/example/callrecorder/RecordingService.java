package com.example.callrecorder;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

@RequiresApi(api = Build.VERSION_CODES.S)
public class RecordingService extends Service {
    private MediaRecorder rec;
    private boolean recordstarted;
    private File file;

    private String path = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    FileOutputStream fos;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);


        try {
                //it work
                file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
                Date date = new Date();
                CharSequence sdf = DateFormat.format("MM-dd-yy-hh-mm-ss", date.getTime()).toString().replaceAll(":",".");

                rec = new MediaRecorder();
                String manufacturer = Build.MANUFACTURER;
                if (manufacturer.toLowerCase().contains("samsung") || manufacturer.toLowerCase().contains("lg")) {
                    rec.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                } else {
                    rec.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                }
                rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                rec.setOutputFile(file.getAbsolutePath() + "/" + sdf + "rec.3gp");
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Image not saved", Toast.LENGTH_SHORT).show();
        }



        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);

        manager.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                //super.onCallStateChanged(state, phoneNumber){

                    if (TelephonyManager.CALL_STATE_IDLE == state && rec == null){
                        rec.stop();
                        rec.reset();
                        rec.release();
                        recordstarted = false;


                        stopSelf();
                    }else if(TelephonyManager.CALL_STATE_OFFHOOK == state){

                        try {
                            rec.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try{
                            rec.start();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        Toast.makeText(RecordingService.this, phoneNumber, Toast.LENGTH_SHORT).show();
                        recordstarted = true;
                    }

                }



             },PhoneStateListener.LISTEN_CALL_STATE);

        return START_STICKY;

    }







}
