package com.onpuri.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.onpuri.ActivityList;
import com.onpuri.R;
import com.onpuri.Server.PacketUser;
import com.onpuri.Server.SocketConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity" ;
    private ActivityList actManager = ActivityList.getInstance();

    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    private GoogleApiClient client;

    //Back Preesed
    private final long FINISH_INTERVAL_TIME = 3000;
    private long backPressedTime = 0;

    //Socket
    DataOutputStream dos;
    DataInputStream dis;

    byte[] outData = new byte[261];
    byte[] inData = new byte[261];

    char check_out;
    char isOut = '0';
    private worker_logout mworker_out;

    //User data - SharedPreferences
    SharedPreferences setting;
    SharedPreferences.Editor editor;

    private String userId = "";
    private String name, joinDate, phone, nowPassword;
    private Bundle bundle;

    private TextView mNavId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actManager.addActivity(this);
        setContentView(R.layout.activity_main);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        setting = getSharedPreferences("setting",0);
        editor = setting.edit();
        mworker_out = null;

        //Setup the DrawerLayout and NavigationView
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view) ;

        //View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main_sub, null);
        //mNavigationView.addHeaderView(header);
        View header = mNavigationView.getHeaderView(0);
        mNavId = (TextView)header.findViewById(R.id.tv_nav_id);

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserId");
        Log.d(TAG, "id : " + userId);
        name = intent.getStringExtra("Name");
        joinDate = intent.getStringExtra("JoinDate");
        phone = intent.getStringExtra("Phone");
        nowPassword = intent.getStringExtra("NowPass");

        mNavId.setText(userId + " 님");

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.containerView,new TabViewPager())
                .commit();

        //Setup click events on the Navigation View Items.
        mNavigationView.setNavigationItemSelectedListener(this);

        // Setup Drawer Toggle of the Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        //Navigation Drawer OPEN / CLOSE
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            //Fragment가 스택에 남아있을 경우
            if(mFragmentManager.getBackStackEntryCount() > 0) {
                Log.d(TAG,"pop");
                mFragmentManager.popBackStack();
                mFragmentManager.beginTransaction()
                        .commit();
            }
            else {
                Log.d(TAG, "close");
                //시간안에 2번 눌렀을 때
                if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                    if (setting.getBoolean("autoLogin", false) == false) {
                        editor.clear();
                        editor.commit();
                    }

                    //logout thread
                    if (mworker_out != null && mworker_out.isAlive()) {  //이미 동작하고 있을 경우 중지
                        mworker_out.interrupt();
                    }

                    mworker_out = new worker_logout(true);
                    mworker_out.start();

                    try {
                        mworker_out.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ActivityCompat.finishAffinity(this);
                    System.runFinalizersOnExit(true);
                    System.exit(0);
                }

                //First back preesed.
                else{
                    backPressedTime = tempTime;
                    Toast.makeText(getApplicationContext(), "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public Fragment getBaseFragment() {
        for (Fragment fragment: getSupportFragmentManager().getFragments()) {
            if (fragment.isVisible()) {
                return (fragment);
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        mDrawerLayout.closeDrawers();
        /*
        if (item.getItemId() == R.id.nav_home) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.containerView,new TabViewPager())
                    .commit();
        }*/

        switch (item.getItemId()){
            case R.id.nav_mypage :
                UserMyFragment MyFrament = new UserMyFragment();
                bundle = new Bundle();
                bundle.putString("MyId", userId);
                bundle.putString("MyName", name);
                bundle.putString("MyJoin", joinDate);
                bundle.putString("MyPhone", phone);
                bundle.putString("MyPass", nowPassword);
                MyFrament.setArguments(bundle);

                mFragmentManager.beginTransaction()
                        .add(R.id.containerView, MyFrament)
                        .addToBackStack("fragBack")
                        .commit();
                break;

            case R.id.nav_myact:
                UserMyActFragment ActFrament = new UserMyActFragment();
                bundle = new Bundle();
                bundle.putString("ActId", userId);
                ActFrament.setArguments(bundle);

                mFragmentManager.beginTransaction()
                        .add(R.id.containerView, ActFrament)
                        .addToBackStack("fragBack")
                        .commit();
                break;

            case R.id.nav_logout:
                logout();
                break;

            case R.id.nav_set:
                UserSetFragment SetFrament = new UserSetFragment();
                bundle = new Bundle();
                bundle.putString("SetId", userId);
                SetFrament.setArguments(bundle);

                mFragmentManager.beginTransaction()
                        .add(R.id.containerView, SetFrament)
                        .addToBackStack("fragBack")
                        .commit();
                break;

        }
        return false;
    }

    private void logout() {
        if(mworker_out != null && mworker_out.isAlive()){  //이미 동작하고 있을 경우 중지
            mworker_out.interrupt();
        }

        mworker_out = new worker_logout(true);
        mworker_out.start();

        try {
            mworker_out.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (setting.getBoolean("autoLogin", false)) {
            editor.clear();
            editor.commit();
        }

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }


    class worker_logout extends Thread {
        private boolean isPlay = false;
        private byte isGenerated;

        public worker_logout(boolean isPlay) {
            this.isPlay = isPlay;
        }

        public void setThread() {
            isPlay = !isPlay;
        }

        public void run() {
            super.run();
            while (isPlay) {

                // SocketConnection.start();
                String toServerDataUser;
                System.out.println("1");
                outData[0] = (byte) PacketUser.SOF;
                outData[1] = (byte) PacketUser.USR_OUT;
                outData[2] = (byte) PacketUser.getSEQ();
                outData[3] = (byte) PacketUser.USR_OUT_LEN;
                outData[4] = (byte) isOut;
                outData[5] = (byte) 85;

                try {
                    System.out.println("1");
                    dos = new DataOutputStream(SocketConnection.socket.getOutputStream());
                    dos.write(outData, 0, outData[3] + 5); // packet transmission
                    dos.flush();

                    System.out.println("2");
                    dis = new DataInputStream(SocketConnection.socket.getInputStream());
                    dis.read(inData);
                    System.out.println("Data form server: " + ((char) inData[4]) + (char) inData[1]);
                    int SOF = inData[0];

                    System.out.println(inData[0]);
                    System.out.println(inData[1]);
                    System.out.println(inData[2]);
                    System.out.println(inData[3]);
                    System.out.println((char) inData[4]); //이게 로그아웃 데이터 값 0,1 인데 서버에서 보내는 패킷보고 바꿔줄겡
                    System.out.println(inData[5]);

                    check_out = (char) inData[4];

                    System.out.println("outData : " + (char) outData[4] + "  inData : " + (char) inData[4]);
                    if (check_out == '0' || check_out == '1')
                        isPlay = !isPlay;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MainSub Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.onpuri/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MainSub Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.onpuri/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        //   SocketConnection.close();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        actManager.removeActivity(this);
    }

}
