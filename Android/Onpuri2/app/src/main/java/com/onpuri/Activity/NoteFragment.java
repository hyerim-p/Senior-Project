package com.onpuri.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onpuri.R;

/**
 * Created by kutemsys on 2016-04-26.
 */
//내노트
public class NoteFragment extends Fragment {
    //public static UserProfile ProfileGroup;
   // private ArrayList<View> history;
    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_note, container, false);
        } catch (InflateException e) {
    /* map is already there, just return view as it is */
        }
        return view;
    }

    /*
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        history = new ArrayList<>();
        ProfileGroup = this;

        Intent intent =new Intent(UserProfile.this, UserProfileTab01.class );
        View view = getLocalActivityManager().startActivity("UserProfileTab01", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
        replaceView(view);
    }

    //새로운 level의 activity를 추가하는 경우
    public void replaceView(View view) {
        history.add(view);
        setContentView(view);
    }

    //back key가 눌러졌을 경우에 대한 처리
    public void back(){
        if(history.size() > 0) {
            history.remove(history.size() - 1);
            if (history.size() == 0)
                finish();
            else
                setContentView(history.get(history.size() - 1));

        }else{
            finish();
        }
    }

    //back key에 대한 event handler
    public void onBackPressed(){
        ProfileGroup.back();
        return;
    }
*/
}