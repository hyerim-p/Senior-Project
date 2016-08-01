package com.onpuri.Activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.onpuri.R;


/**
 * Created by kutemsys on 2016-07-16.
 */

public class ListenAddFragment extends Fragment implements View.OnClickListener, MediaRecorder.OnInfoListener {
    private static final String TAG = "ListenAddFragment";

    private static View view;
    private Toast toast;

    TextView item;
    String sentence = "";

    boolean Islisten = false;
    boolean Isplay = false;
    boolean Isstart = false;

    Button btn_listen, btn_play;
    MediaPlayer mPlayer = null;
    MediaRecorder mRecorder = null;
    String mFilePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_listen_add, container, false);
        } catch (InflateException e) {}

        item = (TextView) view.findViewById(R.id.tv_sentence);
        if (getArguments() != null) {
            sentence = getArguments().getString("sen");
            item.setText(sentence);
        }

        btn_listen = (Button) view.findViewById(R.id.listen);
        btn_listen.setOnClickListener(this);
        btn_play = (Button) view.findViewById(R.id.play);
        btn_play.setOnClickListener(this);

        Button btn_new_listen = (Button) view.findViewById(R.id.btn_new_listen);
        btn_new_listen.setOnClickListener(this);
        Button btn_new_trans_back = (Button) view.findViewById(R.id.btn_new_listen_back);
        btn_new_trans_back.setOnClickListener(this);

        mFilePath = GetFilePath();
        return view;
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        switch (v.getId()) {
            case R.id.listen:
                Isstart = true;
                if (!Islisten) {
                    Islisten = true;
                    btn_listen.setText("중지");
                    btn_play.setEnabled(false);
                    btn_play.setTextColor(Color.parseColor("#FEE098"));
                    btnRecord();

                } else {
                    Islisten = false;
                    btn_listen.setText("녹음");
                    btn_play.setEnabled(true);
                    btn_play.setTextColor(Color.parseColor("#000000"));
                    onBtnStop();
                }
                break;

            case R.id.play:
                if(!Isstart) {
                    toast = Toast.makeText(getActivity(), "녹음파일이 없습니다", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                if (!Isplay) {
                    Isplay = true;
                    btn_play.setText("정지");
                    btn_listen.setEnabled(false);
                    btn_listen.setTextColor(Color.parseColor("#FEE098"));
                    onBtnPlay();
                }
                 else {
                    Isplay = false;
                    btn_play.setText("재생");
                    btn_listen.setEnabled(true);
                    btn_listen.setTextColor(Color.parseColor("#000000"));
                }
                break;

            case R.id.btn_new_listen:
                toast = Toast.makeText(getActivity(), "등록", Toast.LENGTH_SHORT);
                toast.show();
                break;

            case R.id.btn_new_listen_back:
                fm.popBackStack();
                ft.commit();
                break;
        }
    }

    private void btnRecord() {
        if( mRecorder != null ) {
            mRecorder.release();
            mRecorder = null;
        }
        mRecorder = new MediaRecorder();

        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mRecorder.setMaxDuration(5 * 1000);
        mRecorder.setMaxFileSize(5 * 1000 * 1000);
        mRecorder.setOnInfoListener(this);

        try {
            mRecorder.prepare();
        } catch(IOException e) {
            Log.d("tag", "Record Prepare error");
        }
        mRecorder.start();
    }
    public void onBtnStop() {
        mRecorder.stop();
        mRecorder.release();
    }

    public void onBtnPlay() {
        if( mPlayer != null ) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(mFilePath);
            mPlayer.prepare();
        } catch(IOException e) {
            Log.d(TAG, "Audio Play error");
            return;
        }
        mPlayer.start();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch( what ) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED :
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED :
                onBtnStop();
                break;
        }
    }
    public static synchronized String GetFilePath() {
        String sdcard = Environment.getExternalStorageState();
        File file = null;

        if ( !sdcard.equals(Environment.MEDIA_MOUNTED)) {
            file = Environment.getRootDirectory();
        }
        else {
            file = Environment.getExternalStorageDirectory();
        }
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat( "MMdd_HHmmss", Locale.KOREA );
        Date currentTime = new Date ( );
        String mTime = mSimpleDateFormat.format ( currentTime );

        String dir = file.getAbsolutePath() + String.format("/Daily E");
        String path = file.getAbsolutePath() + String.format("/Daily E/record %s.mp3",mTime);

        file = new File(dir);
        if ( !file.exists() )
        {
            file.mkdirs();
        }
        return path;
    }

}