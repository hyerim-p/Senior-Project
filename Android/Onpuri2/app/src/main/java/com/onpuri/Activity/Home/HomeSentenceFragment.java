package com.onpuri.Activity.Home;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onpuri.Activity.MainActivity;
import com.onpuri.Adapter.SenListenListAdapter;
import com.onpuri.Adapter.SenTransListAdapter;
import com.onpuri.DividerItemDecoration;
import com.onpuri.Listener.HomeItemClickListener;
import com.onpuri.R;
import com.onpuri.Activity.Note.workerNote;
import com.onpuri.Activity.Note.workerNoteItemAdd;
import com.onpuri.Thread.workerTrans;
import com.onpuri.Thread.workerListen;

import java.util.ArrayList;
import java.util.Locale;


import static com.onpuri.R.drawable.divider_dark;


/**
 * Created by kutemsys on 2016-05-11.
 */
public class HomeSentenceFragment extends Fragment implements View.OnClickListener, TextToSpeech.OnInitListener, MediaRecorder.OnInfoListener {

    private static final String TAG = "HomeSentenceFragment";
    private workerTrans worker_sentence_trans;
    private workerListen worker_sentence_listen;

    private static View view;

    ArrayList<String> list_trans;
    ArrayList<String> list_trans_userid;
    ArrayList<String> list_trans_day;
    ArrayList<String> list_trans_reco;
    ArrayList<String> list_trans_num;

    ArrayList<String> list_listen;
    ArrayList<String> list_listen_userid;
    ArrayList<String> list_listen_day;
    ArrayList<String> list_listen_reco;
    ArrayList<String> list_listen_num;

    TextView item;
    String sentence = "";
    String sentence_num = "";
    String id = "";
    TextToSpeech tts;

    MediaRecorder mRecorder = null;

    private RecyclerView TransRecyclerView;
    private SenTransListAdapter TransAdapter;
    protected RecyclerView.LayoutManager TransLayoutManager;
    private RecyclerView ListenRecyclerView;
    private SenListenListAdapter ListenAdapter;
    protected RecyclerView.LayoutManager ListenLayoutManager;

    private ArrayList<String> listNote;
    private workerNote mworker_note;
    private workerNoteItemAdd mworker_item_add;

    private int num;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_home_sen, container, false);
        } catch (InflateException e) {}

        list_trans = new ArrayList<String>();
        list_trans_userid = new ArrayList<String>();
        list_trans_day = new ArrayList<String>();
        list_trans_reco = new ArrayList<String>();
        list_trans_num = new ArrayList<String>();

        list_listen = new ArrayList<String>();
        list_listen_userid = new ArrayList<String>();
        list_listen_day = new ArrayList<String>();
        list_listen_reco = new ArrayList<String>();
        list_listen_num = new ArrayList<String>();

        item = (TextView) view.findViewById(R.id.tv_sentence);
        if (getArguments() != null) { //클릭한 문장 출력
            sentence = getArguments().getString("sen");
            sentence_num = getArguments().getString("sen_num");
            id = getArguments().getString("id");

            item.setText(sentence);
        }

        translation();
        listen();
        noteLoad();

        ImageButton tts_sen = (ImageButton) view.findViewById(R.id.tts);
        tts_sen.setOnClickListener(this);
        ImageButton del_sen = (ImageButton) view.findViewById(R.id.del_sen);
        del_sen.setOnClickListener(this);
        ImageButton add_note = (ImageButton) view.findViewById(R.id.add_note);
        add_note.setOnClickListener(this);
        ImageButton add_trans = (ImageButton) view.findViewById(R.id.add_trans);
        add_trans.setOnClickListener(this);
        ImageButton add_listen = (ImageButton) view.findViewById(R.id.add_listen);
        add_listen.setOnClickListener(this);

        Button trans_more = (Button) view.findViewById(R.id.trans_more);
        trans_more.setOnClickListener(this);
        Button listen_more = (Button) view.findViewById(R.id.listen_more);
        listen_more.setOnClickListener(this);

        TransRecyclerView = (RecyclerView) view.findViewById(R.id.trans_list);
        TransLayoutManager = new LinearLayoutManager(getActivity());
        TransRecyclerView.setLayoutManager(TransLayoutManager);
        TransAdapter = new SenTransListAdapter(list_trans, list_trans_reco, TransRecyclerView);
        TransRecyclerView.setAdapter(TransAdapter);// Set CustomAdapter as the adapter for RecyclerView.
        TransRecyclerView.addOnItemTouchListener(
                new HomeItemClickListener(getActivity().getApplicationContext(), TransRecyclerView ,new HomeItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(position < worker_sentence_trans.getCount()) {
                            final TransDetailFragment tdf = new TransDetailFragment();
                            FragmentManager fm = getActivity().getSupportFragmentManager();

                            Bundle args = new Bundle();
                            args.putString("sen", sentence);
                            args.putString("sen_trans", list_trans.get(position));
                            args.putString("userid", list_trans_userid.get(position));
                            args.putString("day", list_trans_day.get(position));
                            args.putString("reco", list_trans_reco.get(position));
                            args.putString("num", list_trans_num.get(position));
                            tdf.setArguments(args);

                            fm.beginTransaction()
                                    .replace(R.id.root_home, tdf)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                    public void onLongItemClick(View view, int position) {}
                })
        );

        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), divider_dark);
        TransRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));

        ListenRecyclerView = (RecyclerView) view.findViewById(R.id.listen_list);
        ListenLayoutManager = new LinearLayoutManager(getActivity());
        ListenRecyclerView.setLayoutManager(ListenLayoutManager);
        ListenAdapter = new SenListenListAdapter(list_listen, list_listen_reco, ListenRecyclerView);
        ListenRecyclerView.setAdapter(ListenAdapter);// Set CustomAdapter as the adapter for RecyclerView.
        ListenRecyclerView.addOnItemTouchListener(
                new HomeItemClickListener(getActivity().getApplicationContext(), ListenRecyclerView ,new HomeItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(tts != null) {
                            tts.stop();
                        }
                        ((MainActivity)getActivity()).mPlayer.PlayFile(list_listen_num.get(position));
                    }
                    public void onLongItemClick(View view, int position) {}
                })
        );
        ListenRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));

        tts = new TextToSpeech(getActivity(), this);

        String userid = ((MainActivity)getActivity()).user.getuserId();
        if (!id.equals(userid)) {
            del_sen.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        final FragmentManager fm = getActivity().getSupportFragmentManager();
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        final Bundle args = new Bundle();
        args.putString("sen", sentence);
        args.putString("sen_num", sentence_num);

        switch (v.getId()) {
            case R.id.del_sen:
                AlertDialog.Builder sentenceDel = new AlertDialog.Builder(getActivity());
                sentenceDel.setTitle("문장을 삭제하시겠습니까?")
                        .setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if(keyCode == KeyEvent.KEYCODE_BACK){
                                    dialog.dismiss();
                                    return true;
                                }
                                return false;
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                fm.popBackStack();
                                ft.commit();
                                Toast.makeText(getActivity(), "삭제되었습니다(구현예정)", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dlg, int sumthin) {
                                Toast.makeText(getActivity(), "취소되었습니다", Toast.LENGTH_SHORT).show();
                            }

                        }).show();
                break;
            case R.id.add_note:
                final String[] items = listNote.toArray(new String[listNote.size()]);
                AlertDialog.Builder noteSelectDialog = new AlertDialog.Builder(getActivity());

                noteSelectDialog.setTitle("노트를 선택해 주세요")
                        .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int index) {
                                num = index;
                                if( index == listNote.size() -1  ){
                                    addNote();
                                }else {
                                    selectNote(items[index]);
                                }
                                dialog.cancel();
                            }
                        })
                        .setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if(keyCode == KeyEvent.KEYCODE_BACK){
                                    dialog.dismiss();
                                    return true;
                                }
                                return false;
                            }
                        });

                AlertDialog alert_dialog = noteSelectDialog.create();
                alert_dialog.show();

                // set defult select value
                alert_dialog.getListView().setItemChecked(num, true);
                break;
            case R.id.add_trans:
                final TransAddFragment atf = new TransAddFragment();
                atf.setArguments(args);
                ft.replace(R.id.root_home, atf);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.trans_more:
                final TransMoreFragment tmf = new TransMoreFragment();
                tmf.setArguments(args);
                ft.replace(R.id.root_home, tmf);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.add_listen:
                final ListenAddFragment alf = new ListenAddFragment();
                alf.setArguments(args);
                ft.replace(R.id.root_home, alf);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.listen_more:
                final ListenMoreFragment lmf = new ListenMoreFragment();
                lmf.setArguments(args);
                ft.replace(R.id.root_home, lmf);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.tts :
                if(((MainActivity)getActivity()).mPlayer.mpfile != null) {
                    ((MainActivity)getActivity()).mPlayer.mpfile.pause();
                }
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
    }


    @Override
    public void onInit(int status) {
        tts.setLanguage(Locale.US);
    }

    private void translation() {
        if(worker_sentence_trans != null && worker_sentence_trans.isAlive()){
            worker_sentence_trans.interrupt();
        }
        worker_sentence_trans = new workerTrans(true, sentence_num);
        worker_sentence_trans.start();
        try {
            worker_sentence_trans.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list_trans.clear();
        list_trans_userid.clear();
        list_trans_day.clear();
        list_trans_reco.clear();
        list_trans_num.clear();

        for (int i = 0; i < worker_sentence_trans.getCount(); i++) {
            list_trans.add(worker_sentence_trans.getTrans().get(i).toString());
            list_trans_userid.add(worker_sentence_trans.getUserid().get(i).toString());
            list_trans_day.add(worker_sentence_trans.getDay().get(i).toString());
            list_trans_reco.add(worker_sentence_trans.getReco().get(i).toString());
            list_trans_num.add(worker_sentence_trans.getTransnum().get(i).toString());
        }
    }

    private void listen() {
        if(worker_sentence_listen != null && worker_sentence_listen.isAlive()){
            worker_sentence_listen.interrupt();
        }
        worker_sentence_listen = new workerListen(true, sentence_num);
        worker_sentence_listen.start();

        try {
            worker_sentence_listen.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list_listen.clear();
        list_listen_userid.clear();
        list_listen_day.clear();
        list_listen_reco.clear();
        list_listen_num.clear();

        for (int i = 0; i < worker_sentence_listen.getCount(); i++) {
            list_listen.add(worker_sentence_listen.getUserid().get(i).toString()+"님   "+worker_sentence_listen.getDay().get(i).toString());
            list_listen_userid.add(worker_sentence_listen.getUserid().get(i).toString());
            list_listen_day.add(worker_sentence_listen.getDay().get(i).toString());
            list_listen_reco.add(worker_sentence_listen.getReco().get(i).toString());
            list_listen_num.add(worker_sentence_listen.getListennum().get(i).toString());
        }
    }

    private void StopFile() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch( what ) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED :
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED :
                StopFile();
                break;
        }
    }

    private void noteLoad(){
        listNote = new ArrayList<String>();

        if (mworker_note != null && mworker_note.isAlive()) {  //이미 동작하고 있을 경우 중지
            mworker_note.interrupt();
        }
        mworker_note = new workerNote(true);
        mworker_note.start();
        try {
            mworker_note.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //문장 모음 리스트
        int i = 0;
        if(mworker_note.getNoteSen() != null){
            while( i < mworker_note.getNoteSen().size()){
                listNote.add( mworker_note.getNoteSen().get(i).toString() );
                Log.d(TAG, mworker_note.getNoteSen().get(i).toString());
                i++;
            }
        }

        listNote.add("새로운 문장 모음 등록하기");

    }

    private void addNote() {
        Toast.makeText(getActivity(), "기능 구현 예정입니다.", Toast.LENGTH_SHORT).show();
    }

    private void selectNote(String item) {
        String nameData = new String ("1+" + item + "+" );
        if (mworker_item_add != null && mworker_item_add.isAlive()) {  //이미 동작하고 있을 경우 중지
            mworker_item_add.interrupt();
        }
        mworker_item_add = new workerNoteItemAdd(true, nameData, Integer.parseInt(sentence_num));
        mworker_item_add.start();
        try {
            mworker_item_add.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(mworker_item_add.getResult() == 1) {
            Toast.makeText(getActivity(), item + "에 추가되었습니다.", Toast.LENGTH_LONG).show();
        }else if( mworker_item_add.getResult() == 2){
            Toast.makeText(getActivity(), item + "에 이미 있습니다.", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity(), "추가에 실패하였습니다.", Toast.LENGTH_LONG).show();
        }
    }

}