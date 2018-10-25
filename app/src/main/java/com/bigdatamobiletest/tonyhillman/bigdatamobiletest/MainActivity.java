package com.bigdatamobiletest.tonyhillman.bigdatamobiletest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.net.URL;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.getActivity;

/**
 * Contacts a servlet, specifying parameters required from met office data. The servlet
 * returns corresponding data.
 */
public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };


    /**
     * Object for background operations, required for networking in Android.
     */
    public netOp50 dataInitOp = null;

    /**
     * One spinner and two number pickers, required for selection of weather
     * attribute, plus max and min range-values.
     */
    public Spinner spinnerForName = null;
    public android.widget.NumberPicker numberPickerForMinValue = null;
    public android.widget.NumberPicker numberPickerForMaxValue = null;

    /**
     * Selected weather attribute (wind speed, temperature, etc).
     */
    public String selectedWeatherAttributeName = "";

    /**
     * The url parameters are assembled in the send message routine, so that whatever
     * the currently selected values are get used. However, this default value is
     * also provided.
     */
    public String urlParameters = "name=G&min=16&max=20";

    /**
     * Area for display of results of execution.
     */
    public TextView outputRegion = null;

    /**
     * Basic set-up. Since a network connection is involved, most of the required work
     * occurs in an asynchronous thread. The sendMessage method spawns this
     * thread. There is no post-op processing required.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.parseColor("#000000"));

        verifyStoragePermissions(this);

        numberPickerForMinValue = (NumberPicker) findViewById(R.id.numberPickerForMinValue);
        numberPickerForMinValue.setMinValue(2);
        numberPickerForMinValue.setMaxValue(20);

        numberPickerForMaxValue = (NumberPicker) findViewById(R.id.numberPickerForMaxValue);
        numberPickerForMaxValue.setMinValue(2);
        numberPickerForMaxValue.setMaxValue(20);

        addItemsOnSpinnerForName();

        TextView outputRegion = (TextView) findViewById(R.id.outputRegion);
        outputRegion.setText("");
    }

    /**
     * Contacts the servlet, passing the required params, and printing
     * returned data to the console.
     */
    public class netOp50 extends AsyncTask<String, String, String> {

        /**
         * Performs the background task.
         *
         * @param params A string, plus other params as required. No input required in this case,
         *               so only an empty string is passed.
         * @return A document. No output required in this case, so only a null Document is
         * returned.
         */
        @Override
        protected String doInBackground(String... params) {
            System.out.println("In doInBackground now.....");
            System.out.println("urlParameters are: " + urlParameters);
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("http://192.168.0.4:8081/CouchbaseAccess/CouchbaseAccess");
                //URL url = new URL("http://192.168.4.45:8081/CouchbaseAccess/CouchbaseAccess");
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException ex) {
                System.out.println("Could not open connection.");
            }

            try {
                urlConnection.setRequestMethod("POST");
            } catch (IOException ex) {
                System.out.println("Could not set request method to POST.");
            }

            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            DataOutputStream wr = null;

            try {
                wr = new DataOutputStream(urlConnection.getOutputStream());
            } catch (IOException ex) {
                System.out.println("No output stream.");
            }

            //System.out.println("\nSending 'POST' request to URL: "
            //        + "http://192.168.0.10:8081/CouchbaseAccess/CouchbaseAccess");

            System.out.println("\nSending 'POST' request to URL: "
                    + "http://192.168.0.4:8081/CouchbaseAccess/CouchbaseAccess");

            //TEST ONLY.
            //

            urlParameters = "question=will_I_need_an_umbrella_tomorrow";
            try {
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
            } catch (IOException ex) {
                System.out.println("Something went wrong during writing, flushing, or closing.");
            }

            // Get the response code. 200 means that things worked okay in terms
            // of a connection being established. 440 means not found. 500 means
            // server error (make sure that Couchbase Server is running and accessible).
            int responseCode = 0;

            try {
                responseCode = urlConnection.getResponseCode();
            } catch (IOException ex) {
                System.out.println("No response code was obtained.");
            }

            System.out.println("Response Code Obtained, with Value: " + responseCode);

            // Create and instantiate input stream for returned data.
            InputStream in = null;

            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException ex) {
                System.out.println("Could not instantiate input stream for specified connection.");
            }

            System.out.println("Input stream for connection instantiated.");

            // Read returned data from instantiated input stream.
            String returnString = readStream(in);

            System.out.println("Disconnecting.");

            urlConnection.disconnect();

            return returnString;
        }

        /**
         * Operation performed after the background operation has concluded. Grabs the
         * string returned with the results of the operation, and displays these onscreen.
         *
         * @param theResult
         */
        protected void onPostExecute(String theResult) {
            System.out.println("In onPostExecute now.");
            String myString = theResult;
            System.out.println("The result is: " + theResult);
            TextView outputRegion = (TextView) findViewById(R.id.outputRegion);
            outputRegion.setText(myString);
        }
    }

    /**
     * Reads data returned from an instantiated input stream.
     *
     * @param is An input stream.
     * @return A string.
     */
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();

            // Read all the data from the input stream.
            //
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }

            System.out.println("Returned string is: " + bo.toString());

            return bo.toString();

        } catch (IOException e) {
            System.out.println("Could not read data from instantiated input stream.");

            return "";
        }
    }

    /**
     * Supports the button used to send the request. This method is called by clicking
     * on the button specified in activity_main.xml. The method-name is specified as
     * the onClick attribute of the button.
     *
     * @param view A view.
     */
    public void sendMessage(View view) {

        System.out.println("Sending...");

        // FIX: The values gotten from the number picker are not the displayed values.
        // Therefore, if the displayed values for an attribute ascend in 10s, the gotten
        // values must be multiplied by ten before being added to urlParameters.
        if (numberPickerForMinValue.getValue() > numberPickerForMaxValue.getValue()) {
            Toast.makeText(MainActivity.this,
                    "Error: Min value must be less than Max.",
                    Toast.LENGTH_SHORT).show();
        } else {
            urlParameters = "name=" + selectedWeatherAttributeName
                    + "&min=" + numberPickerForMinValue.getValue()
                    + "&max=" + numberPickerForMaxValue.getValue();

            System.out.println("Preparing to execute with the following parameter string: "
                    + urlParameters);

            // Create an instance of the asynchronous method class, and execute the
            // asynchronous method.
            dataInitOp = new netOp50();
            dataInitOp.execute("");
        }
    }

    public void voiceTests(View view) {

        // Set up the intent for returning to the book cover activity.
        //
        Intent intent = new Intent(MainActivity.this,  VoiceActivity.class);

        // Start the book cover activity.
        //
        startActivity(intent);

    }

    public void addItemsOnSpinnerForName() {

        spinnerForName = (Spinner) findViewById(R.id.spinnerForName);
        String spinnerString = String.valueOf(spinnerForName.getSelectedItem());

        List<String> list = new ArrayList<String>();
        list.add("G: Wind Gust (mph)");
        list.add("Pp: Precipitation Probability (%)");
        list.add("T: Temperature (C)");
        list.add("S: Wind Speed (mph)");
        list.add("H: Screen Relative Humidity (%)");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerForName.setAdapter(dataAdapter);
        spinnerForName.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public class CustomOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            // Number-scale for precipitation.
            final String[] numbersForPp = new String[11];
            for(int i=0; i<numbersForPp.length; i++) {
                numbersForPp[i] = Integer.toString((i)*10);
            }

            // Number-scale for temperature.
            final String[] numbersForT = new String[11];
            for(int i=0; i<numbersForT.length; i++) {
                numbersForT[i] = Integer.toString((i)*10);
            }

            switch (parent.getSelectedItemPosition()) {
                case 0:
                    System.out.println("Spinner value is 0. G: Wind Gust (mph).");

                    selectedWeatherAttributeName = "G";

                    numberPickerForMinValue.setDisplayedValues(null);
                    numberPickerForMinValue.setMinValue(0);
                    numberPickerForMinValue.setMaxValue(40);

                    numberPickerForMaxValue.setDisplayedValues(null);
                    numberPickerForMaxValue.setMinValue(0);
                    numberPickerForMaxValue.setMaxValue(40);

                    break;

                case 1:
                    System.out.println("Spinner value is 1. Pp: Precipitation Probability (%).");

                    selectedWeatherAttributeName = "Pp";


                    numberPickerForMinValue.setMinValue(0);
                    numberPickerForMinValue.setMaxValue(numbersForPp.length-1);
                    numberPickerForMinValue.setDisplayedValues(numbersForPp);

                    //numberPickerForMinValue.setMaxValue(100);


                    numberPickerForMaxValue.setMinValue(0);
                    numberPickerForMaxValue.setMaxValue(numbersForPp.length-1);
                    numberPickerForMaxValue.setDisplayedValues(numbersForPp);

                    //numberPickerForMaxValue.setMaxValue(100);
                    break;

                case 2:
                    System.out.println("Spinner value is 2. T: Temperature (C).");

                    selectedWeatherAttributeName = "T";

                    numberPickerForMinValue.setMinValue(0);
                    numberPickerForMinValue.setMaxValue(numbersForT.length-1);
                    numberPickerForMinValue.setDisplayedValues(numbersForT);

                    for (int i = 0; i < numbersForT.length; i++)
                    {
                        System.out.println(numbersForT[i]);
                    }

                    //numberPickerForMinValue.setMaxValue(30);

                    numberPickerForMaxValue.setMinValue(0);
                    numberPickerForMaxValue.setMaxValue(numbersForT.length-1);
                    numberPickerForMaxValue.setDisplayedValues(numbersForT);

                    //numberPickerForMaxValue.setMaxValue(30);

                    break;

                case 3:
                    System.out.println("Spinner value is 3. S: Wind Speed (mph).");

                    selectedWeatherAttributeName = "S";

                    numberPickerForMinValue.setDisplayedValues(null);
                    numberPickerForMinValue.setMinValue(0);
                    numberPickerForMinValue.setMaxValue(100);

                    numberPickerForMaxValue.setDisplayedValues(null);
                    numberPickerForMaxValue.setMinValue(0);
                    numberPickerForMaxValue.setMaxValue(100);
                    break;

                case 4:
                    System.out.println("Spinner value is 4. H: Screen Relative Humidity (%).");

                    selectedWeatherAttributeName = "H";

                    numberPickerForMinValue.setDisplayedValues(null);
                    numberPickerForMinValue.setMinValue(0);
                    numberPickerForMinValue.setMaxValue(100);

                    numberPickerForMaxValue.setDisplayedValues(null);
                    numberPickerForMaxValue.setMinValue(0);
                    numberPickerForMaxValue.setMaxValue(100);
                    break;

                default:
                    System.out.println("Spinner default case reached. Using H: Screen Relative Humidity (%).");

                    selectedWeatherAttributeName = "H";

                    numberPickerForMinValue.setDisplayedValues(null);
                    numberPickerForMinValue.setMinValue(0);
                    numberPickerForMinValue.setMaxValue(100);

                    numberPickerForMaxValue.setDisplayedValues(null);
                    numberPickerForMaxValue.setMinValue(0);
                    numberPickerForMaxValue.setMaxValue(100);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE

            );
        }

        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);

        if (permission2 != PackageManager.PERMISSION_GRANTED) {

            System.out.println("Permission to record audio was not granted");

            //ActivityCompat.requestPermissions(getActivity()
            //        activity,
             //       P
                    //PERMISSIONS_STORAGE,
                    //REQUEST_EXTERNAL_STORAGE
            //);



        //if (ContextCompat.checkSelfPermission(Activity activi
        //        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

        //    ActivityCompat.requestPermissions(getActivity(),
        //            new String[]{Manifest.permission.RECORD_AUDIO},
        //            REQUEST_MICROPHONE);

        }
    }
}
