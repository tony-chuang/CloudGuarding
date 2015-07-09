package com.example.cloudguarding_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class OpenLogoActivity extends Activity {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.open_logo);
		
		Handler h =new Handler();
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent(OpenLogoActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
				
			}
		};
		h.postDelayed(r, 2000);
	}

}
