/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private byte[] imagebyte;
    private byte[] filebyte;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    public void writeS(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeS(out);
    }
    
    public void writeM(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeM(out);
    }
    
    public void writeT(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeT(out);
    }
    
    public void writeP(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeP(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[256];
            int bytes;
            
            //byte picCache[] = new byte[1024*16];

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    
                    String[] synchroData =new String(buffer).split("---");
                    //缓冲区相关
                      
//                    int len = 0;  
//                    int temp=0;          //所有读取的内容都使用temp接收  
//                    while((temp=mmInStream.read())!=-1){    //当没有读取完时，继续读取  
//                        picCache[len]=(byte)temp;  
//                        len++;  
//                    } 
                    
                    if((buffer[0]=='s')&&(buffer[1]=='o')&&(buffer[2]=='n')&&(buffer[3]=='d')){
                    	byte[] tempBuffer=subBytes(buffer,4,buffer.length-4);
                    	
                    	
                    	if(filebyte!=null)
                    		filebyte=arraycat(filebyte,tempBuffer);
        				else
        					filebyte=tempBuffer.clone();
                    }
                    else if((buffer[0]=='s')&&(buffer[1]=='e')&&(buffer[2]=='n')&&(buffer[3]=='d')){
                    	byte[] tempBuffer=subBytes(buffer,4,buffer.length-4);
                    	filebyte=arraycat(filebyte,tempBuffer);
                    	mHandler.obtainMessage(BluetoothChat.SOUND_READ,filebyte.length, -1,filebyte)
                        .sendToTarget();
                    	filebyte=null;
                    }
                    else if((buffer[0]=='f')&&(buffer[1]=='i')&&(buffer[2]=='l')&&(buffer[3]=='e')){
                    	byte[] tempBuffer=subBytes(buffer,4,buffer.length-4);
                    	
                    	
                    	if(filebyte!=null)
                    		filebyte=arraycat(filebyte,tempBuffer);
        				else
        					filebyte=tempBuffer.clone();
                    }
                    else if((buffer[0]=='f')&&(buffer[1]=='e')&&(buffer[2]=='n')&&(buffer[3]=='d')){
                    	byte[] tempBuffer=subBytes(buffer,4,buffer.length-4);
                    	filebyte=arraycat(filebyte,tempBuffer);
                    	mHandler.obtainMessage(BluetoothChat.FILE_READ,filebyte.length, -1,filebyte)
                        .sendToTarget();
                    	filebyte=null;
                    }
                    else if(synchroData[0].equals("msg")){
                    	mHandler.obtainMessage(BluetoothChat.MESSAGE_READ,synchroData[1].getBytes().length, -1,synchroData[1].getBytes())
                        .sendToTarget();
                    }
                    else if(synchroData[0].equals("tit")){
                    	mHandler.obtainMessage(BluetoothChat.TITLE_READ,synchroData[1].getBytes().length, -1,synchroData[1].getBytes())
                        .sendToTarget();
                    }
                    else
                    {
                    	// Send the obtained bytes to the UI Activity
                        //mHandler.obtainMessage(BluetoothChat.ICO_READ,synchroData[1].getBytes().length, -1,synchroData[1].getBytes())
                                //.sendToTarget();
                    	//byte[] temp=Base64.decode(synchroData[1],Base64.DEFAULT);
                        //mHandler.obtainMessage(BluetoothChat.ICO_READ,temp.length, -1,temp)
                        //.sendToTarget();
                    	if(imagebyte!=null)
        					imagebyte=arraycat(imagebyte,buffer);
        				else
        					imagebyte=buffer.clone();
                    	Bitmap bm3 = BitmapFactory.decodeByteArray(imagebyte,0,imagebyte.length);
                    	if(bm3!=null){
        					//im3.setImageBitmap(bm3);
                    		mHandler.obtainMessage(BluetoothChat.ICO_READ  , imagebyte.length  , -1,imagebyte)
                            .sendToTarget();
        				}
                    	
                        
                    }
                    
                    buffer = new byte[256];

                    
                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, len , -1, picCache)
//                            .sendToTarget();

                } catch (IOException e) { 
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
            	
            	byte[] filehead=new byte[4];
    		    filehead[0]='f';
    		    filehead[1]='i';
    		    filehead[2]='l';
    		    filehead[3]='e';
    		    byte[] fileend=new byte[4];
    		    fileend[0]='f';
    		    fileend[1]='e';
    		    fileend[2]='n';
    		    fileend[3]='d';
    		    int n=buffer.length;
    		    
            	int b=256-4;
            	int i=0;
            	
            	while(n>0){
            		//byte[] b1=arraycat(filehead,buffer);
            		//byte[] b2=arraycat(fileend,buffer);
            		byte[] temp;
            		mmOutStream.flush();
            		if(n>b){
            			temp=subBytes(buffer,i,b);
            			mmOutStream.write(arraycat(filehead,temp),0,b+4);
            		}
            			
            		else{
            			temp=subBytes(buffer,i,n);
            			mmOutStream.write(arraycat(fileend,temp),0,n+4);
            		}
            			
            		mmOutStream.flush();
            		n-=b;
            		i+=b;
            	}
                //mmOutStream.write(buffer);
                //缓冲区相关
                //mmOutStream.flush();
               // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        
        public void writeS(byte[] buffer) {
            try {
            	
            	byte[] filehead=new byte[4];
    		    filehead[0]='s';
    		    filehead[1]='o';
    		    filehead[2]='n';
    		    filehead[3]='d';
    		    byte[] fileend=new byte[4];
    		    fileend[0]='s';
    		    fileend[1]='e';
    		    fileend[2]='n';
    		    fileend[3]='d';
    		    int n=buffer.length;
    		    
            	int b=256-4;
            	int i=0;
            	
            	while(n>0){
            		//byte[] b1=arraycat(filehead,buffer);
            		//byte[] b2=arraycat(fileend,buffer);
            		byte[] temp;
            		mmOutStream.flush();
            		if(n>b){
            			temp=subBytes(buffer,i,b);
            			mmOutStream.write(arraycat(filehead,temp),0,b+4);
            		}
            			
            		else{
            			temp=subBytes(buffer,i,n);
            			mmOutStream.write(arraycat(fileend,temp),0,n+4);
            		}
            			
            		mmOutStream.flush();
            		n-=b;
            		i+=b;
            	}
                //mmOutStream.write(buffer);
                //缓冲区相关
                //mmOutStream.flush();
               // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        
        public void writeT(byte[] buffer) {
            try {
                mmOutStream.write(("tit---"+new String(buffer)+"---").getBytes());
                mmOutStream.flush();
               // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        
        
        
        public void writeM(byte[] buffer) {
            try {
                mmOutStream.write(("msg---"+new String(buffer)).getBytes());
                mmOutStream.flush();
               // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void writeP(byte[] buffer) {
            try {
                //mmOutStream.write(Base64.decode(("msg---"+Base64.encodeToString(buffer,Base64.DEFAULT)),Base64.DEFAULT));
            	
            	//mmOutStream.write(("msg---"+Base64.encodeToString(buffer,Base64.DEFAULT)).getBytes());
            	
            	int n=buffer.length;
            	int b=256;
            	int i=0;
            	while(n>0){
            		mmOutStream.flush();
            		if(n>b)
            			mmOutStream.write(buffer,i,b);
            		else
            			mmOutStream.write(buffer,i,n);
            		mmOutStream.flush();
            		n-=b;
            		i+=b;
            	}
            	//mmOutStream.write(buffer);
            	
            	
               // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                        //.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        
        byte[] arraycat(byte[] buf1,byte[] buf2)
    	{
        	byte[] bufret=null;
        	int len1=0;
        	int len2=0;
        	if(buf1!=null)
        		len1=buf1.length;
        	if(buf2!=null)
        		len2=buf2.length;
        	if(len1+len2>0)
        		bufret=new byte[len1+len2];
        	if(len1>0)
        		System.arraycopy(buf1,0,bufret,0,len1);
        	if(len2>0)
        		System.arraycopy(buf2,0,bufret,len1,len2);
    	return bufret;
    	} 

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        
        public byte[] subBytes(byte[] src, int begin, int count) {
        	byte[] bs = new byte[count];
        	for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        	return bs;
    	}
    }
}
