package org.main.smartmirror.smartmirror;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;


public class FacebookFragment extends Fragment {

    LoginButton btnLoginButton;
    CallbackManager mCBManager;
    WebView webview;




    private String curURL;

    public void init(String url) {

        curURL = url;

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCBManager = CallbackManager.Factory.create();
        View view = inflater.inflate(R.layout.facebook_fragment, container, false);
        init("https://www.facebook.com/");

        webview = (WebView) view.findViewById(R.id.facebook_webview);
        if (curURL != null) {

            webview.getSettings().setJavaScriptEnabled(true);

            webview.setWebViewClient(new webClient());

            webview.loadUrl(curURL);

        }

        Bundle args = getArguments();
        if (args != null) {
            // Use initialisation data
        }

        //TODO put this elsewhere
        /*String fbScroll = this.getArguments().getString("scroll");
        try {
            if(fbScroll.contains(SCROLLDOWN.toLowerCase())) {
                webview.scrollBy(0, -20);
            } else if(fbScroll.contains(SCROLLUP.toLowerCase())) {
                webview.scrollBy(0, +20);
            }
        }catch (Exception e) {
            Log.i("Err "," didn't catch that");
        }*/
        //TODO put this elsewhere


        //btnLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        //btnLoginButton.setReadPermissions("user_friends, user_posts");

        //btnLoginButton.setFragment(this);

        /*btnLoginButton.registerCallback(mCBManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("status: ", "SUCCESS!");
                *//*new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        //"/me/feed",
                        //"/{user_id}/notifications",
                        "/me/feed",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.i("response ", response.toString());
                            }
                        }
                ).executeAsync();*//*

               *//* mAccessToken = AccessToken.getCurrentAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        mAccessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("graph response ", response.toString());
                                try{
                                    JSONObject myJson = new JSONObject(object.toString());
                                    mID = myJson.optString("id,stream");
                                    Log.i("My Page ID ", mID);
                                    //String apiURL = "https://graph.facebook.com/";
                                    //String fbURL = apiURL + mID + "/feed?" + AccessToken.getCurrentAccessToken() + "=" + getString(R.string.facebook_app_id) + "|" + getString(R.string.facebook_app_secret);

                                    //updateFeed(fbURL);
                                }catch (Exception e) {
                                    Log.i("ERROR ", " json parse error");
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id");
                request.setParameters(parameters);
                request.executeAsync();*//*

                //https://graph.facebook.com/page-username/posts?access_token=sometoken
                //use this url
                //https://graph.facebook.com/Facebookpageid/feed?access_token=Facebookappid|Facebookappsecret
                //JSONObject json = jParser.getJSONFromUrl(url)

            }

            @Override
            public void onCancel() {
                Log.i("status: ", "CANCEL!");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("status: ", "ERROR!");
            }
        });*/

        return view;
    }

    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("News", "Got message:\"" + message +"\"");
            switch (message) {
                case MainActivity.mSCROLLUP:
                    Log.i(" is it ", message);
                    webview.scrollBy(0, -1000);
                    break;
                case MainActivity.mSCROLLDOWN:
                    Log.i(" is it ", message);
                    webview.scrollBy(0, +1000);
                    break;
            }
        }
    };

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to MainActivity from
     *  voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private class webClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return false;

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCBManager.onActivityResult(requestCode, resultCode, data);
    }

   /* private void updateFeed(final String query){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(query);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.sports_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable(){
                        public void run(){
                            renderFeed(json);
                        }
                    });
                }
            }
        }.start();

    }


    private void renderFeed(JSONObject json){
        try {
            Log.i(" FB JSON ",json.toString());

        }catch(Exception e){
            Log.e("FB ERROR", e.toString());
        }
    }*/

}
