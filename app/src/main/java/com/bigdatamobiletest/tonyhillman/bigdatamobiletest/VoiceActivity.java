package com.bigdatamobiletest.tonyhillman.bigdatamobiletest;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.Voice;
import android.view.View.OnClickListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Typeface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.util.Calendar.MONTH;


public class VoiceActivity extends AppCompatActivity implements OnClickListener, OnInitListener {


    /**
     * String sent to server.
     */
    public String urlParameters = "";

    /**
     * A network operation, performed in the background, for server-access.
     */
    public voiceBackgroundTask vgTask = null;

    /**
     * A speech recognizer, used to capture the spoken query of a user.
     *
     */
    private SpeechRecognizer sr;

    /**
     * One of three image frames used to establish an animation that shows the user when
     * to voice a query.
     */
    private ImageView imgFrame;

    /**
     * An animation drawable, the animation derived from which indicates to the user that a query
     * should be voiced.
     *
     */
    private AnimationDrawable frameAnimation = new AnimationDrawable();

    /**
     * An object used for translating text (returned by the Google server in response to a query)
     * to audible speech.
     *
     */
    private TextToSpeech tts;

    /**
     * A graphical button that the user must press in order to start the listener that
     * will capture a voiced query.
     *
     */
    private Button speakButton = null;

    /**
     * A string used for writing to the LogCat.
     */
    private final static String TAG0 = "onCreate: ";

    /**
     * A string used for writing to the LogCat.
     */
    private final static String TAG1 = "returnDocumentObject: ";

    /**
     * A string used for writing to the LogCat.
     */
    private static final String TAG2 = "onPostExecute";

    /**
     * A string used for writing to the LogCat.
     */
    private static final String TAG3 = "onClick";

    /**
     * A string used for writing to the LogCat.
     */
    private static final String TAG4 = "onInit";

    /**
     * A string used for writing to the LogCat.
     */
    private static final String TAG5 = "RecognitionListener";

    /**
     * A string used for writing to the LogCat.
     */
    private static final String TAG6 = "onResults";

    /**
     * A string used for writing to the LogCat.
     */
    private static final String TAG7 = "returnTomenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        imgFrame = (ImageView) findViewById(R.id.imgFrame);


        System.out.println("Started voice activity.");

        speakButton = (Button) findViewById(R.id.btn_speak);
        speakButton.setTypeface(speakButton.getTypeface(), Typeface.ITALIC);
        speakButton.setText("Press here to ask");
        speakButton.setTextColor(Color.parseColor("#000000"));

        // Initiate the process whereby listening can be made to begin, when the
        // button is pressed by the user.
        //
        speakButton.setOnClickListener(this);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        tts = new TextToSpeech(this, this);




        //Voice voiceobj = new Voice("it-it-x-kda#male_2-local",
        //        Locale.getDefault(), 1, 1, false, null);

        //tts.setVoice(voiceobj);



    }

    /**
     * Contacts the servlet, passing the required params, and printing
     * returned data to the console.
     */
    public class voiceBackgroundTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            System.out.println("In doInBackground now.....");

            String userQuestion = params[0];

            System.out.println("params[0] received as: " + userQuestion);

            urlParameters = "question=" + userQuestion;

            System.out.println("urlParameters are: " + userQuestion);

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

            //System.out.println("\nSending 'POST' request to URL: "
            //        + "http://192.168.4.45:8081/CouchbaseAccess/CouchbaseAccess");

            //TEST ONLY.
            //


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
         * @param theResult String returned from server.
         */
        protected void onPostExecute(String theResult) {
            System.out.println("In onPostExecute now.");
            String myString = theResult;
            System.out.println("The result is: " + theResult);

            System.out.print("Got string as: " + myString + '\n');

            // Integer to help us index our attempt to match the string that
            // we've retrieved from the user-input.
            //
            int queryIndex = 0;

            Log.w(TAG6, "Starting matching now:\n");

            myString = myString + ".";


            // Now speak to the user, using the value of "text", whatever it has turned out
            // to be.
            //
            tts.speak(myString, TextToSpeech.QUEUE_FLUSH, null);

            // Reset the appearance of the speak button and the border of the text-display
            // field.
            //
            speakButton.setTextColor(Color.parseColor("#000000"));
            speakButton.setText("Press here, to ask.");
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
     * Runnable used to start the frame animation that prompts the user for vocal
     * input.
     *
     */
    Runnable run = new Runnable()
    {
        @Override
        public void run()
        {
            Log.w(TAG1,"Starting frameAnimation now");
            frameAnimation.start();
        }
    };

    /**
     * Runnable used to stop the frame animation.
     *
     */
    Runnable stopAnimation = new Runnable()
    {
        @Override
        public void run()
        {
            frameAnimation.stop();
        }
    };

    /**
     * A listener class, used to grab audible user-input.
     *
     */
    class listener implements RecognitionListener
    {
        /**
         * Writes to LogCat, confirming readiness to take speech input.
         *
         */
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG5, "onReadyForSpeech");
        }

        /**
         * Writes to LogCat, confirming the beginning of speech.
         *
         */
        public void onBeginningOfSpeech()
        {
            Log.d(TAG5, "onBeginningOfSpeech");
        }

        /**
         * Writes to LogCat, confirming that RMS has changed.
         *
         */
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG5, "onRmsChanged");
        }

        /**
         * Writes to LogCat, confirming that a buffer of information has been
         * received.
         *
         */
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG5, "onBufferReceived");
        }

        /**
         * Writes to LogCat, confirming that speech has ended.
         *
         */
        public void onEndOfSpeech()
        {
            Log.d(TAG5, "onEndofSpeech...");
        }

        /**
         * The onError method resets the Voice Activity UI, in the event of a anomaly.
         *
         */
        public void onError(int error)
        {
            Log.d(TAG5, "error occurred:" + error);


            // Reset the appearance of button, text field, and animation
            // frame...
            //
            speakButton.setTextColor(Color.parseColor("#000000"));
            speakButton.setText("Press here, to ask.");

            // Stop the animation, and show the corresponding still image.
            //
            imgFrame.post(stopAnimation);
            imgFrame.setBackgroundResource(R.drawable.nowave);

        }

        /**
         * Receives a text string, based on
         * a user-voiced query. It strives to match the text string with ones locally
         * maintained, in correspondence with the contents of the supportive xml file. If
         * it finds a match, it produces a spoken answer to the user. If it does not,
         * it produces a spoken message of regret. In each phase, it changes the appearance
         * of certain UI elements, so providing helpful feedback to the user.
         *
         */
        public void onResults(Bundle results)
        {
            System.out.println("onResults started.");

            // Change the appearance of the button, in preparation for giving
            // the answer.
            //
            speakButton.setTextColor(Color.parseColor("#000000"));
            speakButton.setText("Your answer is...");

            // Stop the animation, and restore the still image to the image frame.
            //
            imgFrame.post(stopAnimation);
            imgFrame.setBackgroundResource(R.drawable.nowave);


            // Establish a string object for holding the result-data.
            //
            String str = new String();

            Log.d(TAG6, "onResults " + results);

            // Grab the data that was obtained by the speech recognizer, as a result
            // of the user's voiced query.
            //
            ArrayList<?> data = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            // Add the data in the array list to the string object.
            // NOTE: PROBABLY JUST DELETE THIS. FIX.
            //
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG6, "result " + data.get(i));
                str += data.get(i);
            }


            // Save that data as a string object.
            //
            String text = (String) data.get(0);

            // Make a duplicate, since the value of text will be changed to the answer. NOTE: NAME
            // THESE VARIABLES MORE EXPLICITLY. FIX.
            //
            String repeatText = text;

            text = text.replace(" ", "_");

            System.out.println("The voice-captured string with replacement underscores is now: "
                                        + text);

            /**
            Calendar cal = Calendar.getInstance();
            Date date=cal.getTime();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String formattedDate=dateFormat.format(date);
            System.out.println("Current time of the day using Calendar - 24 hour format: "+ formattedDate);


            Date date2 = new Date();
            String strDateFormat = "hh:mm:ss a";
            DateFormat dateFormat2 = new SimpleDateFormat(strDateFormat);
            String formattedDate2 = dateFormat.format(date);
            System.out.println("Current time of the day using Date - 12 hour format: " + formattedDate);
             **/

            //Date date3 = new Date();

            Calendar cal = Calendar.getInstance();
            Date date=cal.getTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int myYear = calendar.get(Calendar.YEAR);
            int myMonth = calendar.get(MONTH) + 1;
            int myDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int myDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            System.out.println("Current year is: " + myYear);
            System.out.println("Current month is: " + myMonth);
            System.out.println("Current date is: " + myDayOfMonth);
            System.out.println("Current day is: " + myDayOfWeek);

            String fullDate = "";
            String formattedMonth = "";
            String formattedDay = "";

            if (myMonth < 10){
                formattedMonth = "0" + Integer.toString(myMonth);
            }
            else
            {
                formattedMonth = Integer.toString(myMonth);
            }

            if (myDayOfMonth < 10){
                formattedDay = "0" + Integer.toString(myDayOfMonth);
            }
            else
            {
                formattedDay = Integer.toString(myDayOfMonth);
            }

            fullDate = fullDate + Integer.toString(myYear) +
                    "-" + formattedMonth + "-" + formattedDay + "Z";

            System.out.println("Today is: " + fullDate);






            // At this point, send the value of 'text' to the server, by means of the
            // background processs. Move the remaining operations in this method
            // to the onComplete method.
            //
            // Create an instance of the asynchronous method class, and execute the
            // asynchronous method.
            voiceBackgroundTask vbt = new voiceBackgroundTask();
            vbt.execute(text);



        }

        /**
         * Writes to the LogCat, indicating that partial results were found.
         *
         */
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG6, "onPartialResults");
        }

        /**
         * Writes to the LogCat, indicating the type of a received event.
         *
         */
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG6, "onEvent " + eventType);
        }
    }

    /**
     * Starts the listener and associated processes
     * that allow a user to present a voiced query.
     *
     */
    public void onClick(View v)
    {

        // Change the color of the button, and change its displayed text.
        //
        speakButton.setTextColor(Color.parseColor("#000000"));
        speakButton.setText("Ask your question now...");

        System.out.println("Received click.");

        // Set up and start the animation that plays while the listener is active.
        //
        imgFrame.setBackgroundResource(R.drawable.animation_frames);
        frameAnimation = (AnimationDrawable) imgFrame.getBackground();
        imgFrame.post(run);

        if (v.getId() == R.id.btn_speak)
        {
            // Establish an intent for the listener.
            //
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "voice.recognition.test");

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

            // Start listening, specifying the intent.
            sr.startListening(intent);
        }
    }

    /**
     * Initializes the text to speech facility, whereby an answer
     * to the user's query can be made audible.
     *
     */
    @Override
    public void onInit(int arg0)
    {
        System.out.println("onInit started.");

        if (arg0 == TextToSpeech.SUCCESS)
        {
            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.w(TAG4, "Language is not supported");
            }
            else
            {
                // Do nothing.
            }

        }
        else
        {
            Log.w(TAG4, "Initilization Failed");
        }
    }



}
