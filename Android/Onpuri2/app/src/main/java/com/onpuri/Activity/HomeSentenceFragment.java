package com.onpuri.Activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onpuri.Adapter.SenListenListAdapter;
import com.onpuri.Adapter.SenTransListAdapter;
import com.onpuri.DividerItemDecoration;
import com.onpuri.Listener.HomeItemClickListener;
import com.onpuri.R;
import com.onpuri.Server.PacketUser;
import com.onpuri.Server.SocketConnection;
import com.onpuri.Thread.workerTrans;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.onpuri.R.drawable.divider_dark;


/**
 * Created by kutemsys on 2016-05-11.
 */
public class HomeSentenceFragment extends Fragment implements View.OnClickListener, TextToSpeech.OnInitListener {

    private static final String TAG = "HomeSentenceFragment";
    private workerTrans mworker_sentence_trans;

    private static View view;
    private Toast toast;

    int j=0;

    ArrayList<String> list_trans;
    ArrayList<String> list_trans_userid;
    ArrayList<String> list_trans_day;
    ArrayList<String> list_trans_reco;

    ArrayList<String> list_listen;
    ArrayList<String> list_listen_userid;
    ArrayList<String> list_listen_day;
    ArrayList<String> list_listen_reco;

    TextView item;
    String sentence = "";
    String sentence_num = "";
    TextToSpeech tts;

    private RecyclerView TransRecyclerView;
    private SenTransListAdapter TransAdapter;
    protected RecyclerView.LayoutManager TransLayoutManager;
    private RecyclerView ListenRecyclerView;
    private SenListenListAdapter ListenAdapter;
    protected RecyclerView.LayoutManager ListenLayoutManager;
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
        list_listen = new ArrayList<String>();

        item = (TextView) view.findViewById(R.id.tv_sentence);
        if (getArguments() != null) { //클릭한 문장 출력
            sentence = getArguments().getString("sen");
            sentence_num = getArguments().getString("sen_num");
            item.setText(sentence);
        }

        translation();
        listen();

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
        TransAdapter = new SenTransListAdapter(list_trans, TransRecyclerView);
        TransRecyclerView.setAdapter(TransAdapter);// Set CustomAdapter as the adapter for RecyclerView.
        TransRecyclerView.addOnItemTouchListener(
                new HomeItemClickListener(getActivity().getApplicationContext(), TransRecyclerView ,new HomeItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(position < mworker_sentence_trans.getNum()) {
                            final TransDetailFragment tdf = new TransDetailFragment();
                            FragmentManager fm = getActivity().getSupportFragmentManager();

                            Bundle args = new Bundle();
                            args.putString("sen", sentence);
                            args.putString("sen_trans", list_trans.get(position));
                            args.putString("userid", list_trans_userid.get(position));
                            args.putString("day", list_trans_day.get(position));
                            args.putString("reco", list_trans_reco.get(position));
                            tdf.setArguments(args);

                            fm.beginTransaction()
                                    .replace(R.id.root_home, tdf)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), divider_dark);
        TransRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));

        ListenRecyclerView = (RecyclerView) view.findViewById(R.id.listen_list);
        ListenLayoutManager = new LinearLayoutManager(getActivity());
        ListenRecyclerView.setLayoutManager(ListenLayoutManager);
        ListenAdapter = new SenListenListAdapter(list_listen, ListenRecyclerView);
        ListenRecyclerView.setAdapter(ListenAdapter);// Set CustomAdapter as the adapter for RecyclerView.
        ListenRecyclerView.addOnItemTouchListener(
                new HomeItemClickListener(getActivity().getApplicationContext(), ListenRecyclerView ,new HomeItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        toast = Toast.makeText(getActivity(), position + "번째 클릭", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );
        ListenRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));

        tts = new TextToSpeech(getActivity(), this);

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
                new AlertDialog.Builder(getActivity())
                        .setTitle("문장을 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                fm.popBackStack();
                                ft.commit();
                               toast = Toast.makeText(getActivity(), "삭제되었습니다(구현예정)", Toast.LENGTH_SHORT);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dlg, int sumthin) {
                                toast = Toast.makeText(getActivity(), "취소되었습니다", Toast.LENGTH_SHORT);
                            }

                        }).show();

                break;
            case R.id.add_note:
                final CharSequence[] items = {"노트1", "노트2", "노트3"};
                new AlertDialog.Builder(getActivity())
                        .setTitle("노트를 선택해 주세요(구현 예정)")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int index) {
                                Toast.makeText(getActivity(), items[index] + "선택", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dlg, int sumthin) {
                            }
                        }).show();
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
            tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
    }

    @Override
    public void onInit(int status) {
        tts.setLanguage(Locale.US);
    }

    private void translation() {
        if(mworker_sentence_trans != null && mworker_sentence_trans.isAlive()){  //이미 동작하고 있을 경우 중지
            mworker_sentence_trans.interrupt();
        }
        mworker_sentence_trans = new workerTrans(true, sentence_num);
        mworker_sentence_trans.start();
        try {
            mworker_sentence_trans.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mworker_sentence_trans.getCount(); i++) {
            list_trans.add(mworker_sentence_trans.getTrans().get(j).toString());
            list_trans_userid.add(mworker_sentence_trans.getUserid().get(j).toString());
            list_trans_day.add(mworker_sentence_trans.getDay().get(j).toString());
            list_trans_reco.add(mworker_sentence_trans.getReco().get(j).toString());
            j++;
        }
    }
    private void listen() {
        for (int i = 0; i < 3; i++) {
            list_listen.add("구현중");
        }
    }
}