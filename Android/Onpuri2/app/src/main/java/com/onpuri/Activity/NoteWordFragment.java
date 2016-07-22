package com.onpuri.Activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.onpuri.Adapter.NoteWordItemAdapter;
import com.onpuri.Data.WordData;
import com.onpuri.DividerItemDecoration;
import com.onpuri.R;

import java.util.ArrayList;

import static com.onpuri.R.drawable.divider_light;

/**
 * Created by HYERIM on 2016-07-11.
 */
public class NoteWordFragment  extends Fragment implements View.OnClickListener {
    private static final String TAG = "NoteWordFragment";
    private static View view;

    ArrayList<WordData> itemWord;

    private RecyclerView mRecyclerWordItem;
    private RecyclerView.Adapter mWordAdapter;
    private TextView tvItemName;
    private Button btn_listen, btn_test, btn_edit;

    protected RecyclerView.LayoutManager mLayoutManager;
    private Context context;

    String itemName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_note_word, container, false);
        } catch (InflateException e) {
            //map is already there, just return view as it is
        }

        tvItemName = (TextView)view.findViewById(R.id.note_word_name);
        if (getArguments() != null) {   //클릭한 단어이름 저장
            itemName = getArguments().getString("wordItemName");
            tvItemName.setText(itemName);
        }

        initData();
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), divider_light);

        //Set Word Adapter for Word RecyclerView (NoteTab)
        mRecyclerWordItem = (RecyclerView) view.findViewById(R.id.recycle_note_word);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mWordAdapter = new NoteWordItemAdapter(itemWord);
        mRecyclerWordItem.setAdapter(mWordAdapter);// Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerWordItem.addItemDecoration(new DividerItemDecoration(dividerDrawable));

        btn_listen = (Button)view.findViewById(R.id.note_word_listen);
        btn_test = (Button)view.findViewById(R.id.note_word_test);
        btn_edit = (Button)view.findViewById(R.id.note_word_edit);

        btn_listen.setOnClickListener(this);
        btn_test.setOnClickListener(this);
        btn_edit.setOnClickListener(this);

        return view;
    }

    private void initData() {
        itemWord = new ArrayList<>();

        for(int i = 0; i < 20; i++) {
            itemWord.add(new WordData("word" + i, "뜻" + i));
            Log.d("TAG", String.valueOf(itemWord.get(i)));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.note_word_listen:
                Toast.makeText(getActivity(),"내일 화면 추가 예정입니다.",Toast.LENGTH_SHORT).show();
                break;
            case R.id.note_word_test:
                Toast.makeText(getActivity(),"내일 화면 추가 예정입니다.",Toast.LENGTH_SHORT).show();
                break;
            case R.id.note_word_edit:
                Toast.makeText(getActivity(),"내일 화면 추가 예정입니다.",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
