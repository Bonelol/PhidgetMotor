package com.max.phidgetmotor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.ServoPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;

/**
 * Created by Bone on 13-5-30.
 */
public class ServerActivity extends Activity {
    protected static final int DIRECTION_UP = 0;
    protected static final int DIRECTION_DOWN = 1;
    protected static final int DIRECTION_LEFT = 2;
    protected static final int DIRECTION_RIGHT = 3;
    private TextView serverStatus;
    private ListView commandListView;
    ArrayList<String> commandList;
    ArrayAdapter<String> commandAdapter;
    ServoPhidget motorController;
    Handler listViewHandler;
    String line;
    Button btnControl;
    // DEFAULT IP
    public static String SERVERIP = "192.168.137.3";

    // DESIGNATE A PORT
    public static final int SERVERPORT = 8080;

    private Handler handler = new Handler(){};
    private ServerSocket serverSocket;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        serverStatus = (TextView) findViewById(R.id.serverStatus);
        btnControl = (Button) findViewById(R.id.btnControl);
        commandListView = (ListView) findViewById(R.id.commandListView);
        
        commandList = new ArrayList<String>();
        commandAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,commandList);
        commandListView.setAdapter(commandAdapter);
        
        //SERVERIP = getLocalIpAddress();
        try {
            motorController = new ServoPhidget();
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
            
            motorController.openAny("192.168.137.1", 5001);
            motorController.setPosition(3, 10);
            
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
        
        Thread fst = new Thread(new ServerThread());
        fst.start();
        
        btnControl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					if(motorController.getEngaged(3))
					{
						Log.d("Engaged","N.O 3 is engaged");
					}
					if(100 <= motorController.getPosition(3))
					{
						motorController.setPosition(3, 0);
					}
					else motorController.setPosition(3, motorController.getPosition(3) + 10);
					
					Log.d("Set Position","Set position to 30");
				} catch (PhidgetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
    }

    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Listening on IP: " + SERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        Socket client = serverSocket.accept();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                serverStatus.setText("Connected.");
                            }
                        });

                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            while ((line = in.readLine()) != null) {
                                Log.d("ServerActivity", line);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                    	commandList.add(0, line);
                                    	if(commandList.size()>6) commandList.remove(6);
                                        Log.d("New Message","Message: " + line);
                                    }
                                });
                            }
                            break;
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.setText("Error");
                    }
                });
                e.printStackTrace();
            }
        }
    }

    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // MAKE SURE YOU CLOSE THE SOCKET UPON EXITING
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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