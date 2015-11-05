package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;


public class keyRevokeActivity extends ActionBarActivity {
    LinearLayout layout;
    SharedPreferences sharedpref;
    int mPosition;
    RviService   rviService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_change);
        sharedpref = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<UserCredentials> arrayofusers = new ArrayList<UserCredentials>();
        //layout = (LinearLayout) findViewById(R.id.userlayout);

        UsersAdapter adapter = new UsersAdapter(this, arrayofusers);
        ListView listView = (ListView) findViewById(R.id.sharedKeys);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                alertMessage();
            }
        });
        listView.setAdapter(adapter);
        addUsers(adapter);

    }

    public void alertMessage(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            RviService.revokeKey(selectKey());//share_fragment.getFormData());
                        } catch (Exception e){

                        }
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Revoke Key", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public JSONArray selectKey() {
        JSONArray revokeKeyOuter = new JSONArray();
        JSONArray revokeKey = new JSONArray();

        try {
            /* Old code */
            JSONObject jsonObject = new JSONObject(sharedpref.getString("Certificates", "NOTHING here"));
            JSONArray jsonArray = jsonObject.getJSONArray("certificates");
            JSONObject key = jsonArray.getJSONObject(mPosition);

            JSONObject payload = new JSONObject();
            JSONArray authServices = new JSONArray();

            authServices.put(new JSONObject().put("lock", "false"));
            authServices.put(new JSONObject().put("start", "false"));
            authServices.put(new JSONObject().put("trunk", "false"));
            authServices.put(new JSONObject().put("windows", "false"));
            authServices.put(new JSONObject().put("lights", "false"));
            authServices.put(new JSONObject().put("hazard", "false"));
            authServices.put(new JSONObject().put("horn", "false"));

            payload.put("authorizedServices", authServices);
            payload.put("validTo", "1971-09-09T23:00:00.000Z");
            payload.put("validFrom", "1971-09-09T22:00:00.000Z");
            payload.put("certid", key.getString("certid"));

            revokeKey.put(payload);
            revokeKeyOuter.put(revokeKey);
            Log.d("REVOKE_OLD", revokeKeyOuter.toString());

            /* New code */
            Collection<UserCredentials> remoteCredentialsList = PrefsWrapper.getRemoteCredentialsList();
            assert remoteCredentialsList != null;
            UserCredentials[] remoteCredsArray = remoteCredentialsList.toArray(new UserCredentials[0]);

            UserCredentials remoteCredentials = remoteCredsArray[mPosition];//.get(mPosition);
            UserCredentials revokingCredentials = new UserCredentials();

            revokingCredentials.setCertId(remoteCredentials.getCertId());

            Log.d("REVOKE_NEW", revokingCredentials.toString());

        } catch (Exception e) { e.printStackTrace(); }

        return revokeKey;
    }

    public void addUsers(UsersAdapter adapter){
                try{
                    JSONObject jsonObject=new JSONObject(sharedpref.getString("Certificates","NOTHING here"));
                    JSONArray jsonArray = jsonObject.getJSONArray("certificates");
                    ArrayList<UserCredentials> newUserCredentialses = UserCredentials.fromJson(jsonArray);//, layout, this);
                    adapter.addAll(newUserCredentialses);
                }catch(Exception e){e.printStackTrace();}

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
