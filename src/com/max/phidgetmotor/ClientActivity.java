package com.max.phidgetmotor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.StepperPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.StepperPositionChangeEvent;
import com.phidgets.event.StepperPositionChangeListener;

/**
 * Created by Bone on 13-5-19.
 */
public class ClientActivity extends Activity {

	protected static final int DIRECTION_UP = 0;
	protected static final int DIRECTION_DOWN = 1;
	protected static final int DIRECTION_LEFT = 2;
	protected static final int DIRECTION_RIGHT = 3;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    private ImageButton button;
    private ImageButton buttonDown;
    private ImageButton buttonUp;
    private ImageButton buttonLeft;
    private ImageButton buttonRight;
    private TextView txtIpAddress;
    private String serverIP = "";
    private TextView txtStepper1,txtStepper2;
	Handler handler = new Handler();
    
    private StepperPhidget motorController;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        buttonDown = (ImageButton) findViewById(R.id.btnDown);
        buttonDown.setOnClickListener(new DirectionButton_Listener());
        buttonUp = (ImageButton) findViewById(R.id.btnLeft);
        buttonUp.setOnClickListener(new DirectionButton_Listener());
        buttonLeft = (ImageButton) findViewById(R.id.btnRight);
        buttonLeft.setOnClickListener(new DirectionButton_Listener());
        buttonRight = (ImageButton) findViewById(R.id.btnUp);
        buttonRight.setOnClickListener(new DirectionButton_Listener());
        button = (ImageButton) findViewById(R.id.btnVoice);
        txtIpAddress = (TextView) findViewById(R.id.txtIp);        
        
        Bundle extras = getIntent().getExtras();

        if(extras!=null)
        {
            serverIP = extras.getString("IP");
        }
        txtIpAddress.setText(serverIP);
        // Disable button if no recognition service is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities( new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            button.setEnabled(false);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak To Control!");
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            }
        });
        
        try {
            motorController = new StepperPhidget();
            Log.d("Start!!!","Phidget Start");
            motorController.addAttachListener(new AttachListener() {
                @Override
                public void attached(AttachEvent attachEvent) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(attachEvent.getSource(), true);
                    try {
						Log.d("Device", attachEvent.getSource().getDeviceName());
					} catch (PhidgetException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            motorController.addDetachListener(new DetachListener() {
                @Override
                public void detached(DetachEvent detachEvent) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(detachEvent.getSource(), false);
                    synchronized (handler) {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            motorController.openAny(serverIP, 5001);
            
            motorController.waitForAttachment();
            double minAccel = motorController.getAccelerationMin(1);
            motorController.setAcceleration(1, minAccel * 2);
            double maxVel = motorController.getVelocityMax(1);
            motorController.setVelocityLimit(1, maxVel / 2);
            
            minAccel = motorController.getAccelerationMin(2);
            motorController.setAcceleration(2, minAccel * 2);
            maxVel = motorController.getVelocityMax(2);
            motorController.setVelocityLimit(2, maxVel / 2);
            
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }

    private class DirectionButton_Listener implements View.OnClickListener{
		@Override
        public void onClick(View v) {
			
        	switch(v.getId())
        	{
        	case R.id.btnUp:        		
        		try {
        			if(motorController.getEngaged(1)){
        				motorController.setEngaged(1, false);
        			}
        			else
        			{
        				motorController.setCurrentPosition(1, 0);
	    				motorController.setEngaged(1, true);
	                    motorController.setTargetPosition(1, 300);
        			}
        			
        			if(motorController.getEngaged(2)){
        				motorController.setEngaged(2, false);
        			}
        			else
        			{
    					motorController.setCurrentPosition(2, 0);
	    				motorController.setEngaged(2, true);
	                    motorController.setTargetPosition(2, -300);
        			}
    				
				} catch (PhidgetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		break;
        	case R.id.btnDown:
        		try {
        			if(motorController.getEngaged(1)){
        				motorController.setEngaged(1, false);
        			}
        			else
        			{
        				motorController.setCurrentPosition(1, 0);
	    				motorController.setEngaged(1, true);
	                    motorController.setTargetPosition(1, -300);
        			}
        			
        			if(motorController.getEngaged(2)){
        				motorController.setEngaged(2, false);
        			}
        			else
        			{
    					motorController.setCurrentPosition(2, 0);
	    				motorController.setEngaged(2, true);
	                    motorController.setTargetPosition(2, 300);
        			}

				} catch (PhidgetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		break;
        	case R.id.btnLeft:
        		try {
        			if(motorController.getEngaged(1)){
        				motorController.setEngaged(1, false);
        			}
        			else
        			{
        				motorController.setCurrentPosition(1, 0);
	    				motorController.setEngaged(1, true);
	                    motorController.setTargetPosition(1, 300);
        			}
        			
        			if(motorController.getEngaged(2)){
        				motorController.setEngaged(2, false);
        			}
        			else
        			{
    					motorController.setCurrentPosition(2, 0);
	    				motorController.setEngaged(2, true);
	                    motorController.setTargetPosition(2, 300);
        			}

				} catch (PhidgetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		break;
        	case R.id.btnRight:
        		try {
        			if(motorController.getEngaged(1)){
        				motorController.setEngaged(1, false);
        			}
        			else
        			{
        				motorController.setCurrentPosition(1, 0);
	    				motorController.setEngaged(1, true);
	                    motorController.setTargetPosition(1, -300);
        			}
        			
        			if(motorController.getEngaged(2)){
        				motorController.setEngaged(2, false);
        			}
        			else
        			{
    					motorController.setCurrentPosition(2, 0);
	    				motorController.setEngaged(2, true);
	                    motorController.setTargetPosition(2, -300);
        			}

				} catch (PhidgetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		break;
        	}
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode ==VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            showToastMessage(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    class AttachDetachRunnable implements Runnable {
        Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(Phidget phidget, boolean attach)
        {
            this.phidget = phidget;
            this.attach = attach;
        }
        public void run() {
            TextView attachedTxt = (TextView) findViewById(R.id.attachedText);
            if(attach)
            {
                attachedTxt.setText("Attached");
                try {
                    TextView nameTxt = (TextView) findViewById(R.id.nameText);
                    TextView serialTxt = (TextView) findViewById(R.id.serialText);

                    nameTxt.setText(phidget.getDeviceName());
                    serialTxt.setText(Integer.toString(phidget.getSerialNumber()));

                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
            else
                attachedTxt.setText("Detached");
            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }
}