package com.example.mysqlconnect;


import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewRecordActivity extends Activity {
	
	//Progress Dialog
	private ProgressDialog pDialog;
	 
    JSONParser jsonParser = new JSONParser();
    EditText inputLatitude;
    EditText inputLongitude;
    EditText inputDescription;
    
    //create new record URL
    private static String url_create_record = "http://192.168.2.1/android/create_record.php";
    
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    int success;
 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_record);
 
        // Edit Text
        inputLatitude = (EditText) findViewById(R.id.lat_input);
        inputLongitude = (EditText) findViewById(R.id.lon_input);
        inputDescription = (EditText) findViewById(R.id.description_input);
        
     // Create button
        Button btnAddRecord = (Button) findViewById(R.id.add_record);
 
        // button click event
        btnAddRecord.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new CreateNewRecord().execute();
            }
        });
    }
    class CreateNewRecord extends AsyncTask<String, String, String> {
    	 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewRecordActivity.this);
            pDialog.setMessage("Creating record..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
//Creating Record
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			String strLat = inputLatitude.getText().toString();
            String strLon = inputLongitude.getText().toString();
            String description = inputDescription.getText().toString();
            
			//double Latitude = Double.parseDouble(strLat);
            //double Longitude = Double.parseDouble(strLon);
            
     
            // Building Parameters
            List<NameValuePair> args = new ArrayList<NameValuePair>();
            args.add(new BasicNameValuePair("lat", strLat));
            args.add(new BasicNameValuePair("lon", strLon));
            args.add(new BasicNameValuePair("description", description));
           

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_record,
                    "POST", args);
            // check log cat for response
            Log.d("Create Response", json.toString());
 
            // check for success tag
            try {
            	success = json.getInt(TAG_SUCCESS);
                
                    // closing this screen
                    finish();
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
		}
 
		protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (success == 1) {
            	Log.v("","success");
                // successfully created product
                Intent i = new Intent(getApplicationContext(), AllRecordsActivity.class);
                startActivity(i);
            } else {
                // failed to create product
            	Log.v("","failed");
            }

        }
 
    }

}
