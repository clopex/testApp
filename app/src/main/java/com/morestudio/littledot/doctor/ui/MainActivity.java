package com.morestudio.littledot.doctor.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.morestudio.littledot.doctor.R;
import com.morestudio.littledot.doctor.api.SipManager;
import com.morestudio.littledot.doctor.api.SipProfile;
import com.morestudio.littledot.doctor.db.DBProvider;
import com.morestudio.littledot.doctor.models.Filter;
import com.morestudio.littledot.doctor.utils.CustomDistribution;
import com.morestudio.littledot.doctor.utils.PreferencesProviderWrapper;
import com.morestudio.littledot.doctor.utils.PreferencesWrapper;
import com.morestudio.littledot.doctor.wizards.WizardIface;
import com.morestudio.littledot.doctor.wizards.WizardUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {

    public static final String SERVER_URL_LOGIN = "http://littledot.flipkod.com/api/app/check_email_and_password";
    public static final String SERVER_URL_PATCH_INFO = "http://littledot.flipkod.com/api/user/info";
    private TextView m_tvEmail;
    private TextView m_tvPwd;
    private Button m_btnLogin;
    private String m_strEmail;
    private String m_strPwd;
    private String wizardId = "";

    SharedPreferences.Editor editor;
    SharedPreferences preferences;

    private JSONObject m_objPatchInfo;
    private WizardIface wizard = null;
    protected SipProfile account = null;
    private final String wId = "BASIC";

    private Activity m_activity;
    PreferencesProviderWrapper prefProviderWrapper;
    private boolean hasTriedOnceActivateAcc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        m_activity = getParent();

        Intent intent = getIntent();
        prefProviderWrapper = new PreferencesProviderWrapper(this);
        long accountId = intent.getLongExtra(SipProfile.FIELD_ID, SipProfile.INVALID_ID);
        account = SipProfile.getProfileFromDbId(this, accountId, DBProvider.ACCOUNT_FULL_PROJECTION);
        WizardUtils.WizardInfo wizardInfo = WizardUtils.getWizardClass(wId);
        //startSipService();
        try {
            wizard = (WizardIface) wizardInfo.classObject.newInstance();
        } catch (IllegalAccessException e) {
            com.morestudio.littledot.doctor.utils.Log.e("plivo", "Can't access wizard class", e);
        } catch (InstantiationException e) {
            com.morestudio.littledot.doctor.utils.Log.e("plivo", "Can't access wizard class", e);
        }

        m_tvEmail = (TextView)findViewById(R.id.tvEmail);
        m_tvPwd = (TextView)findViewById(R.id.tvPwd);

//        m_tvEmail.setText("heavydayfest@gmail.com");
//        m_tvPwd.setText("vicko");

        m_btnLogin = (Button)findViewById(R.id.btnLogin);
        m_btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startSipService();
                m_strEmail = m_tvEmail.getText().toString();
                m_strPwd = m_tvPwd.getText().toString();
                if(m_strEmail.equals("") || m_strPwd.equals("")){
                    Toast.makeText(getApplicationContext(), "please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                new GetResult().execute();
            }
        });
    }
    class GetResult extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String param1="{\n" +
                    "  \"email\": \""+m_strEmail+"\",\n" +
                    "  \"password\": \""+m_strPwd +"\"\n" +
                    "}";
            String strToken = GetResultToken(SERVER_URL_LOGIN, param1, "application/json");
            if(strToken == ""){
                return "TokenFailed";
            }

            String strResult = GetResultInfo(SERVER_URL_PATCH_INFO, strToken);
            try {
                m_objPatchInfo = new JSONObject(strResult);
                String checkEndpoint = m_objPatchInfo.getString("sip_endpoint");
                String checkPass = m_objPatchInfo.getString("sip_password");

                if (checkEndpoint == "null" || checkPass == "null"){
                    return "error";
                }

                /*if(m_objPatchInfo.getString("message")=="Missing token"){
                    return "ErrorGettingInfo";
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
            saveAccount(wId);
            return "success";
        }
        protected void onPostExecute(String feed) {
            if (feed == "error"){
                Toast.makeText(getApplicationContext(), "VoIP not configured for your account. Please contact administrator.", Toast.LENGTH_LONG).show();
            }

            if(feed == "TokenFailed"){
                Toast.makeText(getApplicationContext(), "Could not find user with email "+m_strEmail +"or Password is not correct", Toast.LENGTH_SHORT).show();
                return;
            }
            /*if(feed == "ErrorGettingInfo"){
                Toast.makeText(getApplicationContext(), "Could not get any info of user with your account data.", Toast.LENGTH_SHORT).show();
                return;
            }*/
            if(feed == "success"){
                finish();
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                startActivity(intent);
            }
        }
    }
    private String GetResultToken(String uri, String param, String contentType){
        String result = "";
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 5000);
        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        BasicCookieStore cookieStore = new BasicCookieStore();
        httpClient.setCookieStore(cookieStore);

        HttpPost httpPost = new HttpPost(uri);
        try {
            StringEntity entity = new StringEntity(param, "UTF8");
            entity.setContentType(contentType);
            httpPost.setEntity(entity);

            httpPost.setHeader("Content-Type", contentType);
            httpPost.setHeader("Accept", contentType);
        } catch (UnsupportedEncodingException e) {
            Log.e("TAG", "UnsupportedEncodingException: " + e);
        }
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity != null) {
                InputStream is = httpEntity.getContent();
                result = getStringFromInputStream(is);
                Log.i("TAG", "Result: " + result);
            }
        } catch (ClientProtocolException e) {
            Log.e("TAG", "ClientProtocolException: " + e);
        } catch (IOException e) {
            Log.e("TAG", "IOException: " + e);
        }
        String ret = "";
        try {
            JSONObject obj = new JSONObject(result);
            ret = obj.getString("access_token");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }
    private String GetResultInfo(String uri, String token){
        String result = "";
        String param = getB64Auth(token, "") + token;
        //MDMzNmU5ZDdhNzcxNGYyZGY2MzdmODMwYTg3MWZhYmNkYmE1MzU5MTo=
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Authorization", param);
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while ((s = buffer.readLine()) != null) {
                result += s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("TAG", "result: " + result);
        return result;
    }
    @TargetApi(Build.VERSION_CODES.FROYO)
    private String getB64Auth (String login, String pass) {
        String source=login+":"+pass;
        String ret="Basic "+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        editor = preferences.edit();
        editor.putString("token", ret);
        editor.apply();
        //Log.i("Testni Token: ", preferences.getString("token", ""));
        return ret;
    }
    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    private boolean saveAccount(String wizardId) {
        boolean needRestart = false;

        PreferencesWrapper prefs = new PreferencesWrapper(getApplicationContext());
        account = buildAccount(account);
        account.wizard = wizardId;
        if (account.id == SipProfile.INVALID_ID) {
            // This account does not exists yet
            prefs.startEditing();
            wizard.setDefaultParams(prefs);
            prefs.endEditing();
            applyNewAccountDefault(account);
            Uri uri = getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());
            // After insert, add filters for this wizard
            account.id = ContentUris.parseId(uri);
            List<Filter> filters = wizard.getDefaultFilters(account);
            if (filters != null) {
                for (Filter filter : filters) {
                    // Ensure the correct id if not done by the wizard
                    filter.account = (int) account.id;
                    getContentResolver().insert(SipManager.FILTER_URI, filter.getDbContentValues());
                }
            }
            // Check if we have to restart
            needRestart = wizard.needRestart();

        } else {
            // TODO : should not be done there but if not we should add an
            // option to re-apply default params
            prefs.startEditing();
            wizard.setDefaultParams(prefs);
            prefs.endEditing();
            getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, account.id), account.getDbContentValues(), null, null);
        }

        // Mainly if global preferences were changed, we have to restart sip stack
        if (needRestart) {
            Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
            sendBroadcast(intent);
        }
        return true;
    }
    public SipProfile buildAccount(SipProfile account) {
        com.morestudio.littledot.doctor.utils.Log.d("Basic>>>", "begin of save ....");
        account.display_name = "plivo";
        try {
            account.acc_id = m_objPatchInfo.getString("sip_endpoint");//"<sip:microsip160405135902@phone.plivo.com>";
            String regUri = "sip:" + "phone.plivo.com";
            account.reg_uri = regUri;
            account.proxies = new String[] { regUri } ;
            account.realm = "*";

            String[] strtmp = m_objPatchInfo.getString("sip_endpoint").split(":");
            String[] strUser = strtmp[1].split("@");
            account.username = strUser[0];//"microsip160405135902";

            account.data = m_objPatchInfo.getString("sip_password");//"micro";
            account.scheme = SipProfile.CRED_SCHEME_DIGEST;
            account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
            account.transport = SipProfile.TRANSPORT_UDP;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return account;
    }
    private void applyNewAccountDefault(SipProfile account) {
        if(account.use_rfc5626) {
            if(TextUtils.isEmpty(account.rfc5626_instance_id)) {
                String autoInstanceId = (UUID.randomUUID()).toString();
                account.rfc5626_instance_id = "<urn:uuid:"+autoInstanceId+">";
            }
        }
    }
    @Override
    public void onBackPressed(){
        finish();
        //m_activity.onBackPressed();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //startSipService();
    }
    private void startSipService() {
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(MainActivity.this.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(MainActivity.this, MainActivity.class));
                startService(serviceIntent);
                postStartSipService();
            };
        };
        t.start();

    }

    private void postStartSipService() {
        // If we have never set fast settings
        if (CustomDistribution.showFirstSettingScreen()) {
            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(prefsIntent);
                return;
            }
        } else {
            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
            if (doFirstParams) {
                prefProviderWrapper.resetAllDefaultValues();
            }
        }

        // If we have no account yet, open account panel,
        if (!hasTriedOnceActivateAcc) {

            Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, new String[] {
                    SipProfile.FIELD_ID
            }, null, null, null);
            int accountCount = 0;
            if (c != null) {
                try {
                    accountCount = c.getCount();
                } catch (Exception e) {
                    com.morestudio.littledot.doctor.utils.Log.e("plivo", "Something went wrong while retrieving the account", e);
                } finally {
                    c.close();
                }
            }

            if (accountCount == 0) {
//                Intent accountIntent = null;
//                WizardUtils.WizardInfo distribWizard = CustomDistribution.getCustomDistributionWizard();
//                if (distribWizard != null) {
//                    accountIntent = new Intent(this, BasePrefsWizard.class);
//                    accountIntent.putExtra(SipProfile.FIELD_WIZARD, distribWizard.id);
//                } else {
//                    accountIntent = new Intent(this, AccountsEditList.class);
//                }
//
//                if (accountIntent != null) {
//                    accountIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(accountIntent);
//                    hasTriedOnceActivateAcc = true;
//                    return;
//                }
            }
            hasTriedOnceActivateAcc = true;
        }
    }
}
