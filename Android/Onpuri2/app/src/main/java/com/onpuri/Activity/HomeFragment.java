package com.onpuri.Activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.onpuri.Adapter.RecycleviewAdapter;
import com.onpuri.DividerItemDecoration;
import com.onpuri.Listener.EndlessRecyclerOnScrollListener;
import com.onpuri.Listener.HomeItemClickListener;
import com.onpuri.R;
import com.onpuri.Server.PacketUser;
import com.onpuri.Server.SocketConnection;
import com.onpuri.Thread.workerSentenceList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.onpuri.R.drawable.divider_dark;

/**
 * Created by kutemsys on 2016-05-03.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static View view;

    private workerSentenceList mworker_sentence;

    ArrayList<String> listSentence = new ArrayList<String>();
    ArrayList<String> listSentenceNum = new ArrayList<String>();
    ArrayList<String> listTransNum = new ArrayList<String>();
    ArrayList<String> listListenNum = new ArrayList<String>();
    PacketUser userSentence;

    private RecyclerView mRecyclerView;
    private RecycleviewAdapter mAdapter;

    protected RecyclerView.LayoutManager mLayoutManager;
    protected Handler handler;

    // on scroll
    private static int current_page = 1;
    private int ival = 0;
    private int loadLimit = 10;
    private int sentence_num;
    private Boolean sentenceEnd = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_home, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        sentence_num = 0;
        userSentence = new PacketUser();

        listSentence = new ArrayList<String>();
        listSentenceNum = new ArrayList<String>();
        listTransNum = new ArrayList<String>();
        listListenNum = new ArrayList<String>();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if(!sentenceEnd) {
            loadData(current_page);
        }
        handler = new Handler();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_sentence);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RecycleviewAdapter(listSentence, listTransNum, listListenNum, mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);// Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener((LinearLayoutManager) mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMoreData(current_page);
                if(sentenceEnd)
                    Toast.makeText(getActivity(),"불러올 문장이 더이상 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    //    mAdapter.notifyDataSetChanged();
        mRecyclerView.addOnItemTouchListener(
                new HomeItemClickListener(getActivity().getApplicationContext(), mRecyclerView ,new HomeItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        HomeSentenceFragment hsf = new HomeSentenceFragment();
                        FragmentManager fm = getActivity().getSupportFragmentManager();

                        Bundle args = new Bundle();
                        args.putString("sen", listSentence.get(position));
                        args.putString("sen_num", listSentenceNum.get(position));
                        hsf.setArguments(args);

                        fm.beginTransaction()
                                .replace(R.id.root_home, hsf)
                                .addToBackStack(null)
                                .commit();
                   //     fm.executePendingTransactions();
                    }
                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), divider_dark);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    // By default, we add 10 objects for first time.
    private void loadData(int current_page) {
        if (mworker_sentence != null && mworker_sentence.isAlive()) {  //이미 동작하고 있을 경우 중지
            mworker_sentence.interrupt();
        }
        mworker_sentence = new workerSentenceList(true, userSentence, sentence_num);
        mworker_sentence.start();
        try {
            mworker_sentence.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sentenceEnd = mworker_sentence.getSentenceEnd();
        userSentence = mworker_sentence.getUserSentence();

        for (int i = 0; i < loadLimit; i++) {
            listSentence.add(userSentence.arrSentence.get(i));
            listSentenceNum.add(userSentence.arrSentenceNum.get(i));
            listTransNum.add(userSentence.arrSentenceTransNum.get(i));
            listListenNum.add(userSentence.arrSentenceListenNum.get(i));
            ival++;
        }
    }

    // adding 10 object creating dymically to arraylist and updating recyclerview when ever we reached last item
    private void loadMoreData(int current_page) {
        if (mworker_sentence != null && mworker_sentence.isAlive()) {  //이미 동작하고 있을 경우 중지
            mworker_sentence.interrupt();
        }
        mworker_sentence = new workerSentenceList(true, userSentence, sentence_num);
        mworker_sentence.start();
        try {
            mworker_sentence.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sentenceEnd = mworker_sentence.getSentenceEnd();
        userSentence = mworker_sentence.getUserSentence();

        if(!sentenceEnd) {
            loadLimit = ival + 10;

            for (int i = ival; i < loadLimit; i++) {
                listSentence.add(userSentence.arrSentence.get(i));
                listSentenceNum.add(userSentence.arrSentenceNum.get(i));
                listTransNum.add(userSentence.arrSentenceTransNum.get(i));
                listListenNum.add(userSentence.arrSentenceListenNum.get(i));
                ival++;
            }
            mAdapter.notifyDataSetChanged();
        }

        else{
            if (mworker_sentence != null && mworker_sentence.isAlive()) {  //이미 동작하고 있을 경우 중지
                mworker_sentence.interrupt();
            }
            Toast.makeText(getActivity(), "불러올 문장이 더이상 없습니다.", Toast.LENGTH_SHORT).show();

        }
    }

}
