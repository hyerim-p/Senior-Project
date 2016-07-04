package com.onpuri.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.onpuri.R;

/**
 * Created by kutemsys on 2016-04-26.
 */
public class UserProfileTab01 extends FragmentActivity {
    //Button btn_note_word, btn_note_sentecnce;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

       // btn_note_word = (Button) findViewById(R.id.btn_note_text);
        //btn_note_sentecnce = (Button) findViewById(R.id.btn_note_sentence);
    }

    public void selectFrag(View view){
        Fragment fr;

        if(view == findViewById(R.id.btn_sen)){
            fr = new NoteSentenceFragment();
        }else{
            fr = new NoteWordFragment();
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.note_content, fr);
        fragmentTransaction.commit();
    }
/*
    public void onBackPressed(){
        UserProfile parent = ((UserProfile)getParent());
        parent.onBackPressed();
    }
    */
}
