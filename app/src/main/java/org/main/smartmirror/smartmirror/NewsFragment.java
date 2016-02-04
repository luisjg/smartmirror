package org.main.smartmirror.smartmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.EnumSet;

public class NewsFragment extends Fragment implements CacheManager.CacheListener{

    // the guardian api
    public static String mDefaultGuardURL = "http://content.guardianapis.com/search?show-fields=" +
            "all&order-by=newest&q=world&api-key=";
    public static String mDefNewsSection = "world";
    //public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&q=";
    public static String mPreURL = "http://content.guardianapis.com/search?show-fields=all&order-by=newest&q=";
    public static String mPostURL = "&api-key=";
    public static String mGuardURL = mPreURL + mDefNewsSection + mPostURL;
    public static String mNewsSection;

    // time in minutes before news data is considered old and is discarded
    private final int DATA_UPDATE_FREQUENCY = 1;

    // I've updated NewsFragment to show the DataManager class. Create items as required.
    public static final String NEWS_CACHE = "news cache";
    public static final String SPORTS_CACHE = "sports cache";
    public static final String TECH_CACHE = "tech cache";
    public static final String BUSINESS_CACHE = "business cache";
    public static final String SCIENCE_CACHE = "science cache";
    public static final String MEDIA_CACHE = "media cache";
    public static final String TRAVEL_CACHE = "travel cache";
    private CacheManager mCacheManager = null;

    private TextView mTxtHeadline1;
    private TextView mTxtHeadline2;
    private TextView mTxtHeadline3;
    private TextView mTxtHeadline4;
    private TextView mTxtHeadline5;
    private TextView mTxtHeadline6;
    private TextView mTxtHeadline7;
    private TextView mTxtHeadline8;
    private TextView txtNewsDesk;

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    private ImageView img4;
    private ImageView img5;
    private ImageView img6;
    private ImageView img7;
    private ImageView img8;

    public static String mArticleFullBody = "";
    public static int numItems = 10;
    public static String article[] = new String[numItems];
    public static String hl[] = new String[numItems];
    public static String snippets[] = new String[numItems];
    public static String thumbs[] = new String[numItems];
    public static String thumbnail = "";
    public static String body = "";
    public static String trailText = "";
    public static String webTitle = "";
    public static String mHeadline = "";

    ScrollView mScrollView;


    Handler mHandler = new Handler();

    public NewsFragment() {}

    static String mNewURL;
    static String mGuardAPIKey;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        mCacheManager = CacheManager.getInstance();
        //
        // Initialize Items
        // mNewsSection = "world";

        mTxtHeadline1 = (TextView)view.findViewById(R.id.headline);
        mTxtHeadline2 = (TextView)view.findViewById(R.id.headline2);
        mTxtHeadline3 = (TextView)view.findViewById(R.id.headline3);
        mTxtHeadline4 = (TextView)view.findViewById(R.id.headline4);
        mTxtHeadline5 = (TextView)view.findViewById(R.id.headline5);
        mTxtHeadline6 = (TextView)view.findViewById(R.id.headline6);
        mTxtHeadline7 = (TextView)view.findViewById(R.id.headline7);
        mTxtHeadline8 = (TextView)view.findViewById(R.id.headline8);

        txtNewsDesk = (TextView)view.findViewById(R.id.txtNewsDesk);

        img1 = (ImageView)view.findViewById(R.id.img1);
        img2 = (ImageView)view.findViewById(R.id.img2);
        img3 = (ImageView)view.findViewById(R.id.img3);
        img4 = (ImageView)view.findViewById(R.id.img4);
        img5 = (ImageView)view.findViewById(R.id.img5);
        img6 = (ImageView)view.findViewById(R.id.img6);
        img7 = (ImageView)view.findViewById(R.id.img7);
        img8 = (ImageView)view.findViewById(R.id.img8);

        mScrollView = (ScrollView)view.findViewById(R.id.scrollView2);

        clearLayout();

        mNewsSection = getArguments().getString("arrI");
        //mGuardURL = mGuardURL + mGuardAPIKey;
        mGuardAPIKey = getString(R.string.guardian_api_key); // the guardian api key

        //updateNews(mGuardURL);

        txtNewsDesk.setText(mNewsSection.toUpperCase());

        // set onClickListener
        mTxtHeadline1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(0);
            }
        });
        mTxtHeadline2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(1);
            }
        });
        mTxtHeadline3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(2);
            }
        });
        mTxtHeadline4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(3);
            }
        });
        mTxtHeadline5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(4);
            }
        });
        mTxtHeadline6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(5);
            }
        });
        mTxtHeadline7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(6);
            }
        });
        mTxtHeadline8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNewsBodyFragment(7);
            }
        });

        return view;
    }

    public void toNewsBodyFragment(int x){
        mArticleFullBody = article[x];
        mHeadline = hl[x];
        Fragment fragment = new NewsBodyFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, fragment)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit();
    }

    public void startNewsUpdate(){
        mGuardURL = mPreURL + mNewsSection + mPostURL + mGuardAPIKey;
        updateNews(mGuardURL);
    }

    private void clearLayout() {
        mTxtHeadline1.setText("");
        mTxtHeadline2.setText("");
        mTxtHeadline3.setText("");
        mTxtHeadline4.setText("");
        mTxtHeadline5.setText("");
        mTxtHeadline6.setText("");
        mTxtHeadline7.setText("");
        mTxtHeadline8.setText("");
        txtNewsDesk.setText("");

        img1.setImageResource(android.R.color.transparent);
        img2.setImageResource(android.R.color.transparent);
        img3.setImageResource(android.R.color.transparent);
        img4.setImageResource(android.R.color.transparent);
        img5.setImageResource(android.R.color.transparent);
        img6.setImageResource(android.R.color.transparent);
        img7.setImageResource(android.R.color.transparent);
        img8.setImageResource(android.R.color.transparent);
    }


    // ----------------------- Local Broadcast Receiver -----------------------

    // Create a handler for received Intents. This will be called whenever an Intent
    // with an action named "inputAction" is broadcast.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtNewsDesk.setText("");
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            switch (message) {
                case Constants.ONE:
                case Constants.FIRST:
                    toNewsBodyFragment(0);
                    break;
                case Constants.TWO:
                case Constants.SECOND:
                    toNewsBodyFragment(1);
                    break;
                case Constants.THREE:
                case Constants.THIRD:
                    toNewsBodyFragment(2);
                    break;
                case Constants.FOUR:
                case Constants.FOURTH:
                    toNewsBodyFragment(3);
                    break;
                case Constants.FIVE:
                case Constants.FIFTH:
                    toNewsBodyFragment(4);
                    break;
                case Constants.SIX:
                case Constants.SIXTH:
                    toNewsBodyFragment(5);
                    break;
                case Constants.SEVEN:
                case Constants.SEVENTH:
                    toNewsBodyFragment(6);
                    break;
                case Constants.EIGHT:
                case Constants.EIGHTH:
                    toNewsBodyFragment(7);
                    break;
                /*default:
                    if(message.contains(Constants.HELP) || message.contains(Constants.HIDE)) {
                        txtNewsDesk.setText(mNewsSection.toUpperCase());
                    }
                    Log.d("News", "Got message:\"" + message + "\"");
                    break;*/
            }
            if(message.contains(Constants.SCROLLDOWN))
                mScrollView.scrollBy(0, -((int)0.3*((int)getResources().getDisplayMetrics().density * mScrollView.getHeight())-mScrollView.getHeight()));
            else if(!message.contains(Constants.SCROLLDOWN) && message.contains(Constants.SCROLLUP))
                mScrollView.scrollBy(0, (int)0.3*((int)getResources().getDisplayMetrics().density * mScrollView.getHeight())-mScrollView.getHeight());
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Check for any cached news data.
        // If a cache exists, render it to the view.
        // Update the cache if it has expired.

        // TODO: 2/3/2016 add switch case so only current news section is checked/updated
        // TODO: 2/3/2016 don't update when null/not prev visited

        // -----CASE NEWS-----
        if (!mCacheManager.containsKey(NEWS_CACHE)) {
            Log.i(Constants.TAG,"News Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering News");
            renderNews((JSONObject) mCacheManager.get(NEWS_CACHE));
        }
        else if (mCacheManager.isExpired(NEWS_CACHE)) {
            Log.i(Constants.TAG, "NewsCache expired. Refreshing...");
            startNewsUpdate();
        }

        // -----CASE SPORTS-----
        if (!mCacheManager.containsKey(SPORTS_CACHE)) {
            Log.i(Constants.TAG,"Sports Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering Sports");
            renderNews((JSONObject) mCacheManager.get(SPORTS_CACHE));
        }
        else if (mCacheManager.isExpired(SPORTS_CACHE)) {
            Log.i(Constants.TAG, "SportsCache expired. Refreshing...");
            startNewsUpdate();
        }

        // -----CASE TECHNOLOGY-----
        if (!mCacheManager.containsKey(TECH_CACHE)) {
            Log.i(Constants.TAG,"Tech Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering Technology");
            renderNews((JSONObject) mCacheManager.get(TECH_CACHE));
        }
        else if (mCacheManager.isExpired(TECH_CACHE)) {
            Log.i(Constants.TAG, "TechCache expired. Refreshing...");
            startNewsUpdate();
        }

        // -----CASE BUSINESS-----
        if (!mCacheManager.containsKey(BUSINESS_CACHE)) {
            Log.i(Constants.TAG,"Business Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering Business");
            renderNews((JSONObject) mCacheManager.get(BUSINESS_CACHE));
        }
        else if (mCacheManager.isExpired(BUSINESS_CACHE)) {
            Log.i(Constants.TAG, "BusinessCache expired. Refreshing...");
            startNewsUpdate();
        }

        // -----CASE MEDIA-----
        if (!mCacheManager.containsKey(MEDIA_CACHE)) {
            Log.i(Constants.TAG,"Media Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering Media");
            renderNews((JSONObject) mCacheManager.get(MEDIA_CACHE));
        }
        else if (mCacheManager.isExpired(MEDIA_CACHE)) {
            Log.i(Constants.TAG, "MediaCache expired. Refreshing...");
            startNewsUpdate();
        }

        // -----CASE TRAVEL-----
        if (!mCacheManager.containsKey(TRAVEL_CACHE)) {
            Log.i(Constants.TAG,"Travel Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering Travel");
            renderNews((JSONObject) mCacheManager.get(TRAVEL_CACHE));
        }
        else if (mCacheManager.isExpired(TRAVEL_CACHE)) {
            Log.i(Constants.TAG, "TravelCache expired. Refreshing...");
            startNewsUpdate();
        }

        // -----CASE SCIENCE-----
        if (!mCacheManager.containsKey(SCIENCE_CACHE)) {
            Log.i(Constants.TAG,"Science Cache does not exist, updating");
            startNewsUpdate();
            Log.i(Constants.TAG, "Rendering Science");
            renderNews((JSONObject) mCacheManager.get(SCIENCE_CACHE));
        }
        else if (mCacheManager.isExpired(SCIENCE_CACHE)) {
            Log.i(Constants.TAG, "ScienceCache expired. Refreshing...");
            startNewsUpdate();
        }
    }

    /** When this fragment becomes visible, start listening to broadcasts sent from MainActivity.
     *  We're interested in the 'inputAction' intent, which carries any inputs send to
     *  MainActivity from voice recognition, the remote control, etc.
     */
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("inputAction"));

        if (mNewsSection == "world" || mNewsSection == "news") {
            mCacheManager.registerCacheListener(NEWS_CACHE, this);
        } else if (mNewsSection == "sports") {
            mCacheManager.registerCacheListener(SPORTS_CACHE, this);
        } else if (mNewsSection == "technology") {
            mCacheManager.registerCacheListener(TECH_CACHE, this);
        } else if (mNewsSection == "business") {
            mCacheManager.registerCacheListener(BUSINESS_CACHE, this);
        } else if (mNewsSection == "media") {
            mCacheManager.registerCacheListener(MEDIA_CACHE, this);
        } else if (mNewsSection == "travel") {
            mCacheManager.registerCacheListener(TRAVEL_CACHE, this);
        } else if (mNewsSection == "science") {
            mCacheManager.registerCacheListener(SCIENCE_CACHE, this);
        }
    }

    // when this goes out of view, halt listening
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);

        if (mNewsSection == "world" || mNewsSection == "news") {
            mCacheManager.registerCacheListener(NEWS_CACHE, this);
        } else if (mNewsSection == "sports") {
            mCacheManager.registerCacheListener(SPORTS_CACHE, this);
        } else if (mNewsSection == "technology") {
            mCacheManager.registerCacheListener(TECH_CACHE, this);
        } else if (mNewsSection == "business") {
            mCacheManager.registerCacheListener(BUSINESS_CACHE, this);
        } else if (mNewsSection == "media") {
            mCacheManager.registerCacheListener(MEDIA_CACHE, this);
        } else if (mNewsSection == "travel") {
            mCacheManager.registerCacheListener(TRAVEL_CACHE, this);
        } else if (mNewsSection == "science") {
            mCacheManager.registerCacheListener(SCIENCE_CACHE, this);
        }
    }

    // Get news headlines from api and display
    private void updateNews(final String query){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(query);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.news_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable(){
                        public void run(){
                            updateNewsCache(json);
                            //Log.i("NEWS ", json.toString());
                            renderNews(json);
                        }
                    });
                }
            }
        }.start();
    }
    private void updateNewsCache(JSONObject data){
        if (mNewsSection == "world" || mNewsSection == "news") {
            mCacheManager.addCache(NEWS_CACHE, data, DATA_UPDATE_FREQUENCY);
        } else if (mNewsSection == "sports") {
            mCacheManager.addCache(SPORTS_CACHE, data, DATA_UPDATE_FREQUENCY);
        } else if (mNewsSection == "technology") {
            mCacheManager.addCache(TECH_CACHE, data, DATA_UPDATE_FREQUENCY);
        } else if (mNewsSection == "business") {
            mCacheManager.addCache(BUSINESS_CACHE, data, DATA_UPDATE_FREQUENCY);
        } else if (mNewsSection == "media") {
            mCacheManager.addCache(MEDIA_CACHE, data, DATA_UPDATE_FREQUENCY);
        } else if (mNewsSection == "travel") {
            mCacheManager.addCache(TRAVEL_CACHE, data, DATA_UPDATE_FREQUENCY);
        } else if (mNewsSection == "science") {
            mCacheManager.addCache(SCIENCE_CACHE, data, DATA_UPDATE_FREQUENCY);
        }
    }

    private void renderNews(JSONObject json){
        try {
            //Log.i("NEWS JSON", json.toString());
            JSONObject response = null;
            JSONObject results = null;
            JSONObject fields = null;

            int i = 0;

            while (i < numItems) {
                response = json.getJSONObject("response");
                results = response.getJSONArray("results").getJSONObject(i);
                webTitle = results.getString("webTitle");
                hl[i] = webTitle;
                fields = results.getJSONObject("fields");
                body = fields.getString("body");
                article[i] = body;
                trailText = fields.getString("trailText");
                snippets[i] = trailText;
                try {
                    thumbnail = fields.getString("thumbnail");
                    thumbs[i] = thumbnail;
                }
                catch (Exception e) {
                    thumbs[i] = "@drawable/guardian";
                }
                i++;
            }


            String txt0 = "<b>" + hl[0] + "</b> " + "<br>" + snippets[0] + "<br>";
            mTxtHeadline1.setText(Html.fromHtml(txt0));
            Picasso.with(getContext()).load(thumbs[0]).fit().centerInside().into(img1);

            String txt1 = "<b>" + hl[1] + "</b> " + "<br>" + snippets[1] + "<br>";
            mTxtHeadline2.setText(Html.fromHtml(txt1));
            Picasso.with(getContext()).load(thumbs[1]).fit().centerInside().into(img2);

            String txt2 = "<b>" + hl[2] + "</b> " + "<br>" + snippets[2] + "<br>";
            mTxtHeadline3.setText(Html.fromHtml(txt2));
            Picasso.with(getContext()).load(thumbs[2]).fit().centerInside().into(img3);

            String txt3 = "<b>" + hl[3] + "</b> " + "<br>" + snippets[3] + "<br>";
            mTxtHeadline4.setText(Html.fromHtml(txt3));
            Picasso.with(getContext()).load(thumbs[3]).fit().centerInside().into(img4);

            String txt4 = "<b>" + hl[4] + "</b> " + "<br>" + snippets[4] + "<br>";
            mTxtHeadline5.setText(Html.fromHtml(txt4));
            Picasso.with(getContext()).load(thumbs[4]).fit().centerInside().into(img5);

            String txt5 = "<b>" + hl[5] + "</b> " + "<br>" + snippets[5] + "<br>";
            mTxtHeadline6.setText(Html.fromHtml(txt5));
            Picasso.with(getContext()).load(thumbs[5]).fit().centerInside().into(img6);

            String txt6 = "<b>" + hl[6] + "</b> " + "<br>" + snippets[6] + "<br>";
            mTxtHeadline7.setText(Html.fromHtml(txt6));
            Picasso.with(getContext()).load(thumbs[6]).fit().centerInside().into(img7);

            String txt7 = "<b>" + hl[7] + "</b> " + "<br>" + snippets[7] + "<br>";
            mTxtHeadline8.setText(Html.fromHtml(txt7));
            Picasso.with(getContext()).load(thumbs[7]).fit().centerInside().into(img8);

        }catch(Exception e){
            Log.e("NEWS ERROR", e.toString());
        }
    }

    /** Callback from CacheManager */
    @Override
    public void onCacheExpired(String cacheName) {

        if (mNewsSection == "world" || mNewsSection == "news") {
            if (cacheName.equals(NEWS_CACHE)) startNewsUpdate();
        } else if (mNewsSection == "sports") {
            if (cacheName.equals(SPORTS_CACHE)) startNewsUpdate();
        } else if (mNewsSection == "technology") {
            if (cacheName.equals(TECH_CACHE)) startNewsUpdate();
        } else if (mNewsSection == "business") {
            if (cacheName.equals(BUSINESS_CACHE)) startNewsUpdate();
        } else if (mNewsSection == "media") {
            if (cacheName.equals(MEDIA_CACHE)) startNewsUpdate();;
        } else if (mNewsSection == "travel") {
            if (cacheName.equals(TRAVEL_CACHE)) startNewsUpdate();
        } else if (mNewsSection == "science") {
            if (cacheName.equals(SCIENCE_CACHE)) startNewsUpdate();
        }
    }

    /** Callback from CacheManager */
    @Override
    public void onCacheChanged(String cacheName) {
        // In this case we do nothing, as calling startWeatherUpdate() will refresh the views.
    }

}
