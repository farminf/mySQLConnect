package com.example.mysqlconnect;

import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditRecordActivity extends Activity {

	EditText txtLat;
    EditText txtLon;
    EditText txtDesc;
    EditText txtCreatedAt;
    Button btnSave;
    Button btnDelete;
 
    String pid;
 
    // Progress Dialog
    private ProgressDialog pDialog;
 
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
 
    // single record url
    private static final String url_record_detials = "http://192.168.2.1/android/read_record.php";
 
    // url to update record
    private static final String url_update_record= "http://192.168.2.1/android/update_record.php";
 
    // url to delete record
    private static final String url_delete_record = "http://192.168.2.1/android/delete_record.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_RECORD = "places";
    private static final String TAG_PID = "pid";
    private static final String TAG_LON = "lon";
    private static final String TAG_LAT = "lat";
    private static final String TAG_DESCRIPTION = "description";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_record);
        
        // I had an AndroidBlockGuard error, solution was to put these lines
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //--------------
        
        // save button
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);
 
        // getting record details from intent
        Intent i = getIntent();
 
        // getting record id (pid) from intent
        pid = i.getStringExtra(TAG_PID);
 
        // Getting complete record details in background thread
        new GetRecordDetails().execute();
        
        // save button click event
        btnSave.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // starting background task to update record
                new SaveRecordDetails().execute();
            }
        });
        
     // Delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // deleting record in background thread
                new DeleteRecord().execute();
            }
        });
        
    }
    
    /**
     * Background Async Task to Get complete record details
     * */
    class GetRecordDetails extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditRecordActivity.this);
            pDialog.setMessage("Loading Record details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        /**
         * Getting record details in background thread
         * */
        protected String doInBackground(String... params) {
        	 
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("pid", pid));
 
                        // getting record details by making HTTP request
                        // Note that record details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_record_detials, "GET", params);
 
                        // check your log for json response
                        Log.d("Single record Details", json.toString());
 
                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received record details
                            JSONArray recordObj = json.getJSONArray(TAG_RECORD); // JSON Array
 
                            // get first record object from JSON Array
                            JSONObject record = recordObj.getJSONObject(0);
 
                            // record with this pid found
                            // Edit Text
                            txtLat = (EditText) findViewById(R.id.inputEditLat);
                            txtLon = (EditText) findViewById(R.id.inputEditLon);
                            txtDesc = (EditText) findViewById(R.id.inputEditDesc);
 
                            // display record data in EditText
                            txtLat.setText(record.getString(TAG_LAT));
                            txtLon.setText(record.getString(TAG_LON));
                            txtDesc.setText(record.getString(TAG_DESCRIPTION));
 
                        }else{
                           Log.e("","There is not a record with this pid");
                        	// record with pid not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
 
            return null;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }
    }
    class SaveRecordDetails extends AsyncTask<String, String, String> {
    	 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditRecordActivity.this);
            pDialog.setMessage("Saving Record ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        /**
         * Saving record
         * */
        protected String doInBackground(String... args) {
 
            // getting updated data from EditTexts
            String Lat = txtLat.getText().toString();
            String Lon = txtLon.getText().toString();
            String description = txtDesc.getText().toString();
 
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_PID, pid));
            params.add(new BasicNameValuePair(TAG_LAT, Lat));
            params.add(new BasicNameValuePair(TAG_LON, Lon));
            params.add(new BasicNameValuePair(TAG_DESCRIPTION, description));
 
            // sending modified data through http request
            // Notice that update record url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_update_record,
                    "POST", params);
 
            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about record update
                    setResult(100, i);
                    finish();
                } else {
                    // failed to update record
                	Log.e("","Failed to update the record");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once record uupdated
            pDialog.dismiss();
        }
        
    }    // Background Async Task to Delete Record
                  
      class DeleteRecord extends AsyncTask<String, String, String> {
     
            /**
             * Before starting background thread Show Progress Dialog
             * */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(EditRecordActivity.this);
                pDialog.setMessage("Deleting Record...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }
     
           
            protected String doInBackground(String... args) {
     
                // Check for success tag
                int success;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("pid", pid));
     
                    // getting record details by making HTTP request
                    JSONObject json = jsonParser.makeHttpRequest(url_delete_record, "POST", params);
                            
     
                    // check your log for json response
                    Log.d("Delete record", json.toString());
                    
                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        // record successfully deleted
                        // notify previous activity by sending code 100
                        Intent i = getIntent();
                        // send result code 100 to notify about record deletion
                        setResult(100, i);
                        finish();
                    }
                    else{
                    	//failed to delete
                    	Log.e("","failed to delete the record");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
     
                return null;
            }
     
            /**
             * After completing background task Dismiss the progress dialog
             * **/
            protected void onPostExecute(String file_url) {
                // dismiss the dialog once record deleted
                pDialog.dismiss();
     
            }
     
        }
        
   
 
}
