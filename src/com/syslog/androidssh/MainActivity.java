/*
 * MainActivity.java
 * 
 * Created as a part of Syslog WarmUp Task for GSoC 15'
 * 
 * The activity logs you into a remote SSH Server with the given credentials
 * 
 * Author : Amar Budhiraja (amar.budhiraja1@gmail.com)
 * 
 * Copyrights : None
 */

package com.syslog.androidssh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
public class MainActivity extends Activity {
	
	private String username;
	private String domain;
	private String password;
	
	public MainActivity() {	
		username = "";
		domain = "";
		password = "";
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	
	public void sshLogin(View Button){
		// Method to remote login on an ssh server
		
		if(!isNetworkAvailable())
			Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
		else{
			boolean detailsValidated = formValidation();
			Log.d("SSH2","Form Validated");
			if (detailsValidated == false)
				Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
			else{
	
				Intent intent = new Intent(MainActivity.this,TerminalActivity.class);
				intent.putExtra("username", this.username);
				intent.putExtra("domain", this.domain);
				intent.putExtra("password", this.password);
				Log.d("username",this.username);
				startActivity(intent);			
			}
		}
	}



	private boolean formValidation() {
		// Checks if all the credentials are present
		
		EditText username = (EditText) findViewById(R.id.username);
		EditText domain = (EditText) findViewById(R.id.domain);
		EditText password = (EditText) findViewById(R.id.password);
		this.username = username.getText().toString();
		this.password = password.getText().toString();
		this.domain = domain.getText().toString();
		if(this.username.matches("") || 
				this.password.matches("") || 
					this.domain.matches("") )
			return false;
		else
			return true;
		
	}
	


}
