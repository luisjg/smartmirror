package org.main.smartmirror.smartmirror;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

import io.fabric.sdk.android.Fabric;

/**
 * Activity that handles the Account Credentials and Work address
 * TODO handle all the permissions for MainActivity Here.
 */
public class AccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private long mUserID;
    private KeyStore mKeyStore;
    private Preferences mPreference;
    private TwitterLoginButton mTwitterLoginButton;
    private TwitterSession mSession;

    public String mScreenName;
    public String mAuthToken;
    public String mAuthSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mKeyStore = KeyStore.getInstance(Constants.KEY_STORE);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig));
        setContentView(R.layout.account_activity);
        mPreference = Preferences.getInstance(this);
        findGoogleAccounts();
        setUpTwitterButton();
        if (mPreference.getFirstTimeRun()) {
            // generate the keys
        } else {
            // we don't care if the values are empty
            // each fragment should handle this
            startMain();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!parent.getItemAtPosition(position).toString().equals("None")) {
            mPreference.setUserAccountName(parent.getItemAtPosition(position).toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * We listen here for the result from Twitter
     * and pass it on to TwitterLoginBUtton
     *
     * @param requestCode the request
     * @param resultCode  the result
     * @param data        data that we received
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void createNewKeys() {
        try {
            // Create new key if needed
            if (!mKeyStore.containsAlias(Constants.TAG)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(Constants.TAG)
                        .setSubject(new X500Principal("CN=Sample Name, O=" + Constants.TAG))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", Constants.KEY_STORE);
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(Constants.TAG, Log.getStackTraceString(e));
        }
        refreshKeys();
    }

    private void refreshKeys() {
        ArrayList keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = mKeyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the functionality for the twitter button.
     */
    private void setUpTwitterButton() {
        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                mPreference.setTwitterLoggedIn(true);
                String output = "Status: " +
                        "Your login was successful " +
                        result.data.getUserName() +
                        "\nAuth Token Received: " +
                        result.data.getAuthToken().token;

                mSession = result.data;
                mUserID = mSession.getUserId();
                mScreenName = mSession.getUserName();
                String msg = "@" + mSession.getUserName() + " logged in! (#" + mUserID + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                Long id = mSession.getId();
                Log.i("ID: ", id.toString());
                mAuthToken = result.data.getAuthToken().token;
                mAuthSecret = result.data.getAuthToken().secret;
                TwitterASyncTask.TWITTER_ACCESS_TOKEN = mAuthToken;
                TwitterASyncTask.TWITTER_ACCESS_SECRET = mAuthSecret;
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with TwitterArrayList failure", exception);
            }
        });
    }

    /**
     * Finds the google accounts that are tied to the device
     */
    private void findGoogleAccounts() {
        Spinner googleAccountsPicker = (Spinner) findViewById(R.id.google_account_picker);

        ArrayList<String> accountsList = new ArrayList<>();
        //Getting all registered Google Accounts on device
        try {
            Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
            for (Account account : accounts) {
                accountsList.add(account.name);
            }
        } catch (Exception e) {
            Log.i(Constants.TAG, "Exception:" + e);
        }
        // add a none option for privacy reasons
        accountsList.add("None");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        googleAccountsPicker.setAdapter(adapter);
        googleAccountsPicker.setOnItemSelectedListener(this);
    }

    /**
     * Handles the button press
     *
     * @param view current view
     */
    public void saveUserInputs(View view) {
        EditText facebookUsername = (EditText) findViewById(R.id.facebook_username);
        EditText facebookPassword = (EditText) findViewById(R.id.facebook_password);
        if (facebookPassword.getText().toString().equals("") && facebookUsername.getText().toString().equals("")) {
            encryptString(facebookUsername + "::" + facebookPassword);
        }
        EditText workAddress = (EditText) findViewById(R.id.work_location);
        // since by default the work lat and long is set to -1 we are OK
        // to not have an else case here
        if (!(workAddress.getText().toString().equals(""))) {
            String strAddress = workAddress.getText().toString().replace(' ', '+');
            convertAddressToLatLong(strAddress);
        }
        startMain();
    }

    /**
     * Encripts a string to the android KeyStore. This might not be the best
     * way of doing it since make two copies of the plain text string. Further
     * investigation needs to be made here to make a better implementation.
     *
     * @param plainText the string to encrypt
     */
    private void encryptString(String plainText) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(Constants.TAG, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            // Encrypt the text
            Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            input.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, input);
            cipherOutputStream.write(plainText.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            // save to preferences.
            mPreference.setFacebookUsername(Base64.encodeToString(vals, Base64.DEFAULT));
        } catch (Exception e) {
            Log.e(Constants.TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * Converts the given address to latitude and longitude
     *
     * @param addressInput the given address
     */
    private void convertAddressToLatLong(String addressInput) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(addressInput, 5);
            if (addressList != null) {
                // interested in only the first result
                mPreference.setWorkLatitude(addressList.get(0).getLatitude());
                mPreference.setWorkLongitude(addressList.get(0).getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts Main Activity
     */
    private void startMain() {
        mPreference.setFirstTimeRun(false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
