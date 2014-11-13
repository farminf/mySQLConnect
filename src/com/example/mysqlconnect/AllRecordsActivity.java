package com.example.mysqlconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AllRecordsActivity extends ListActivity {

	// Progress Dialog
    private ProgressDialog pDialog;
 
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
 
    ArrayList<HashMap<String, String>> recordsList;
 
    // url to get all records list
    private static String url_all_records = "http://192.168.2.1/android/read_all_records.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_RECORDS = "places";
    private static final String TAG_PID = "pid";
    private static final String TAG_DESC = "description";
 
    // records JSONArray
    JSONArray records = null;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_records);
		// Hashmap for ListView
        recordsList = new ArrayList<HashMap<String, String>>();
 
        // Loading records in Background Thread
        new LoadAllRecords().execute();
 
        // Get listview
        ListView lv = getListView();
 
        // on seleting single record
        // launching Edit record Screen
        lv.setOnItemClickListener(new OnItemClickListener() {
        	 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.view_pid)).getText().toString();
                
                // Starting new intent
                Intent in = new Intent(getApplicationContext(), EditRecordActivity.class);
                       
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);
 
                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
                        
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted record
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
        
    }
   
    //Background Async Task to Load all record by making HTTP Request
   class LoadAllRecords extends AsyncTask<String, String, String> {

       /**
        * Before starting background thread Show Progress Dialog
        * */
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           pDialog = new ProgressDialog(AllRecordsActivity.this);
           pDialog.setMessage("Loading records. Please wait...");
           pDialog.setIndeterminate(false);
           pDialog.setCancelable(false);
           pDialog.show();
       }
       
       //getting all recorda
       protected String doInBackground(String... args) {
           // Building Parameters
           List<NameValuePair> params = new ArrayList<NameValuePair>();
           // getting JSON string from URL
           JSONObject json = jParser.makeHttpRequest(url_all_records, "GET", params);

           // Check your log cat for JSON reponse
           Log.d("All records: ", json.toString());

           try {
               // Checking for SUCCESS TAG
               int success = json.getInt(TAG_SUCCESS);

               if (success == 1) {
                   // records found
                   // Getting Array of records
                   records = json.getJSONArray(TAG_RECORDS);
                   
                   // looping through All records
                   for (int i = 0; i < records.length(); i++) {
                       JSONObject c = records.getJSONObject(i);

                       // Storing each json item in variable
                       String id = c.getString(TAG_PID);
                       String description = c.getString(TAG_DESC);

                       // creating new HashMap
                       HashMap<String, String> map = new HashMap<String, String>();

                       // adding each child node to HashMap key => value
                       map.put(TAG_PID, id);
                       map.put(TAG_DESC, description);

                       // adding HashList to ArrayList
                       recordsList.add(map);
                   }
               } else {
                   // no records found
                   // Launch Add New record Activity
                   Intent i = new Intent(getApplicationContext(),
                           NewRecordActivity.class);
                   // Closing all previous activities
                   i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(i);
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
           // dismiss the dialog after getting all records
           pDialog.dismiss();
           // updating UI from Background Thread
           runOnUiThread(new Runnable() {
               public void run() {
                   /**
                    * Updating parsed JSON data into ListView
                    * */
                   ListAdapter adapter = new SimpleAdapter(
                           AllRecordsActivity.this, recordsList,
                           R.layout.list_item, new String[] { TAG_PID,
                                   TAG_DESC},
                           new int[] { R.id.view_pid, R.id.view_description });
                   // updating listview
                   setListAdapter(adapter);
               }
           });

       }

   }
 
    
}
