/*
 * TerminalActivity.java
 * 
 * First, tries to create a Session and check if it was successful.
 * 
 * If connection was successful, a new activity open which lets you run terminal commands
 * 
 * else, the connection is closed.
 * 
 * Author : Amar Budhiraja
 * 
 * Copyrights : None
 * 
 */


package com.syslog.androidssh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class TerminalActivity extends Activity {
	
	private String username;
	private String domain;
	private String password;
	private boolean connected;
	private Channel channel;
	private Session session;
	String result;
	
	public TerminalActivity() {
		this.connected = false;
		this.result = "";
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terminal);
		Intent intent = getIntent();
		this.username = intent.getStringExtra("username");
		this.domain = intent.getStringExtra("domain");
		this.password = intent.getStringExtra("password");
		ConnectToServer connectToServer = new ConnectToServer();
		connectToServer.execute();		
	}

	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.terminal, menu);
		return true;
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public void getResult(View Button){
		if(!isNetworkAvailable())
			Toast.makeText(TerminalActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
		
		else if(!session.isConnected())
			Toast.makeText(TerminalActivity.this,"Session Expired",Toast.LENGTH_LONG).show();
		
		GetResult getResult = new GetResult();
		getResult.execute();
		
	}
	
	
	public class ConnectToServer extends AsyncTask<Void, Void, Void>{
		 private ProgressDialog dialog;
		 
		 public ConnectToServer() {
			 this.dialog = new ProgressDialog(TerminalActivity.this);
			 this.dialog.setCanceledOnTouchOutside(false);
		}
		
		protected Void doInBackground(Void... params) {			
			setUpSSHConnection();			
			return null;
		}
		
		private Object setUpSSHConnection() {
			JSch shell = new JSch();			
			try {
				session = shell.getSession(username, domain, 22);				  
		        session.setUserInfo(new SSHUserInfo(password));  
		        session.connect();
		        connected = session.isConnected();
		        channel = session.openChannel("shell");  
		        
			} catch (JSchException e) {
				
				Log.d("Connected","Failed");
				connected = false;
				e.printStackTrace();
			}  
			
			return session;
		}
		
		
		protected void onPreExecute(){
			
			this.dialog.setMessage("Connecting to Server");
	        this.dialog.show();
		}
		
		
		protected void onPostExecute(Void result) {
			if (dialog.isShowing())
				this.dialog.dismiss();	
			final String toastText;
			if (connected){
				toastText = "Connected Successfully";
				
				runOnUiThread(new Runnable() {
	                 public void run() {

	                     Toast.makeText(TerminalActivity.this,toastText,Toast.LENGTH_LONG).show();
	                }
	            });
				
			}
			else{
				toastText = "Connection to "+ domain + "with username: "+username+"failed";		
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(TerminalActivity.this,toastText,Toast.LENGTH_LONG).show();
					}
				});
				TerminalActivity.this.finish();
			}
		}		
	}
	
	
	public class GetResult extends AsyncTask<Void, Void, Void>{
		 private ProgressDialog dialog;
		 
		 public GetResult() {
			 this.dialog = new ProgressDialog(TerminalActivity.this);
			 this.dialog.setCanceledOnTouchOutside(false);
		}
		
		protected Void doInBackground(Void... params) {			
			getResultFromServer();			
			return null;
		}
		
		@SuppressWarnings("deprecation")
		private String getResultFromServer() {
	        try {
				channel.connect();
			} catch (JSchException e) {				
				e.printStackTrace();
			}        
	          
	        EditText shellCommand = (EditText) findViewById(R.id.shell_command);
	        String command = shellCommand.getText().toString();
	    
	        try {
	        	DataInputStream dataIn = new DataInputStream(channel.getInputStream());  
		        DataOutputStream dataOut = new DataOutputStream(channel.getOutputStream());
				dataOut.writeBytes(command+"\r\n");
				dataOut.flush();

				final String result = dataIn.readLine() + dataIn.readLine() + dataIn.readLine() + dataIn.readLine();				
				runOnUiThread(new Runnable() {				
					
					public void run() {
						TextView resultView = (TextView) findViewById(R.id.resultView);
						resultView.setText(result);						
					}
				});
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}  
	          
	        return null;
		}
		
		protected void onPreExecute(){
			
			this.dialog.setMessage("Getting Result From Server");
	        this.dialog.show();
		}
		
		protected void onPostExecute(Void result) {
			if (dialog.isShowing())
				this.dialog.dismiss();	
			Toast.makeText(TerminalActivity.this,"Command Ran.",Toast.LENGTH_LONG).show();
		}		
	}
	
	static class SSHUserInfo implements UserInfo {  
        private String password;  
  
        SSHUserInfo(String password) {  
            this.password = password;  
        }  
  
        public String getPassphrase() {  
            return null;  
        }  
  
        public String getPassword() {  
            return password;  
        }  
  
        public boolean promptPassword(String arg0) {  
            return true;  
        }  
  
        public boolean promptPassphrase(String arg0) {  
            return true;  
        }  
  
        public boolean promptYesNo(String arg0) {  
            return true;  
        }  
  
        public void showMessage(String arg0) {  
            System.out.println(arg0);  
        }  
    }  
}
