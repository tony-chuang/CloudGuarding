package com.example.cloudguarding_final;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

@SuppressLint({ "SimpleDateFormat", "HandlerLeak", "DefaultLocale" })
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity {

	String IP = "192.168.2.103";
	int Port = 9000;
	private String getXML;
	static private String sendXML;
	private ToggleButton GuardSwitch;
	private ToggleButton SpeakerSwitch;
	public List<IoTdevice> devices = new ArrayList<IoTdevice>();
	int buttonFlag = 0;
	int gasFlag = 0, suspiciousFlag = 0, suspiciousTimeFlag = 0,
			switchFlag = 0, tvFlag = 0, switchTimeFlag = 0, gasTimeFlag = 0,
			visitorFlag = 0, visitorTimeFlag = 0, speakerCheckFlag = 0,
			switchCheckFlag = 0;
	int second1, second2, second3;
	String pastTemp = null;
	String nowtemp = null;
	ImageView guardLight, visitor, suspicious_person, gas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initEventsListenr();

		GuardSwitch = (ToggleButton) findViewById(R.id.GuardSwitch);
		SpeakerSwitch = (ToggleButton) findViewById(R.id.SpeakerSwitch);
		guardLight = (ImageView) findViewById(R.id.guardlight);
		visitor = (ImageView) findViewById(R.id.visitor);
		suspicious_person = (ImageView) findViewById(R.id.suspicious_person);
		gas = (ImageView) findViewById(R.id.gas);

		SwitchButton();

		Thread receiveXMLDataTask = new Thread(receiveXMLData);
		receiveXMLDataTask.start();
	}

	private void SwitchButton() {

		CompoundButton.OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					switch (buttonView.getId()) {
					case R.id.GuardSwitch:
						sendXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><iotDevices>"
								+ "<device id=\"A1\" name=\"磁簧\" status=\"\" action=\"enable\" alarm=\"\"></device>"
								+ "<device id=\"B1\" name=\"紅外線\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"C1\" name=\"按鈕\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"D1\" name=\"溫度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"E1\" name=\"溼度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"F1\" name=\"一氧化碳\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"G1\" name=\"蜂鳴器\" status=\"\" action=\"\" alarm=\"\"></device></iotDevices>";
						buttonFlag = 1;
						switchCheckFlag = 1;
						break;
					case R.id.SpeakerSwitch:
						sendXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><iotDevices>"
								+ "<device id=\"A1\" name=\"磁簧\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"B1\" name=\"紅外線\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"C1\" name=\"按鈕\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"D1\" name=\"溫度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"E1\" name=\"溼度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"F1\" name=\"一氧化碳\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"G1\" name=\"蜂鳴器\" status=\"\" action=\"enable\" alarm=\"\"></device></iotDevices>";
						buttonFlag = 1;
						speakerCheckFlag = 1;
						break;
					}
				} else {
					switch (buttonView.getId()) {
					case R.id.GuardSwitch:
						sendXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><iotDevices>"
								+ "<device id=\"A1\" name=\"磁簧\" status=\"\" action=\"disable\" alarm=\"\"></device>"
								+ "<device id=\"B1\" name=\"紅外線\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"C1\" name=\"按鈕\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"D1\" name=\"溫度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"E1\" name=\"溼度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"F1\" name=\"一氧化碳\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"G1\" name=\"蜂鳴器\" status=\"\" action=\"\" alarm=\"\"></device></iotDevices>";
						buttonFlag = 1;
						switchCheckFlag = 1;
						break;
					case R.id.SpeakerSwitch:
						sendXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><iotDevices>"
								+ "<device id=\"A1\" name=\"磁簧\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"B1\" name=\"紅外線\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"C1\" name=\"按鈕\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"D1\" name=\"溫度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"E1\" name=\"溼度\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"F1\" name=\"一氧化碳\" status=\"\" action=\"\" alarm=\"\"></device>"
								+ "<device id=\"G1\" name=\"蜂鳴器\" status=\"\" action=\"disable\" alarm=\"\"></device></iotDevices>";
						buttonFlag = 1;
						speakerCheckFlag = 1;
						break;
					}
				}
				// ctrlMsg.controlMessage = sendXML;

			}
		};
		GuardSwitch.setOnCheckedChangeListener(listener);
		SpeakerSwitch.setOnCheckedChangeListener(listener);
	}

	private Runnable receiveXMLData = new Runnable() {

		@Override
		public void run() {

			Socket socket = null;
			while (true) {
				try {
					try {
						socket = new Socket(IP, Port);
						InputStream ServerReader = socket.getInputStream();
						BufferedReader br = new BufferedReader(
								new InputStreamReader(ServerReader, "utf-8"));
						getXML = br.readLine();
						xmlData.sendMessage(xmlData.obtainMessage());
						if (buttonFlag == 1) {
							PrintStream Writer = new PrintStream(
									socket.getOutputStream(), true, "utf-8");
							Writer.println(sendXML);
							buttonFlag = 0;
						}
						socket.close();
						Thread.currentThread();
						Thread.sleep(1000);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} finally {
				}
			}
		}
	};

	private List<IoTdevice> getXMLData() {
		List<IoTdevice> devices = new ArrayList<IoTdevice>();
		try {
			InputStream inStream = new ByteArrayInputStream(getXML.getBytes());
			devices = DomParseDevicesXML.ReadEquipmentXML(inStream);
		} catch (Exception e) {
			Log.e("getXMLData", "ParseXML Error");
		}
		return devices;
	}

	private Handler xmlData = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			devices = getXMLData();
			TextView tvT1 = (TextView) findViewById(R.id.tvT1);
			TextView tvR1 = (TextView) findViewById(R.id.tvR1);

			for (int j = 0; j < devices.size(); j++) {
				IoTdevice device = devices.get(j);

				if (device.getName().equals("溫度") == true) {
					if (Integer.parseInt(device.getStatus()) < 30) {
						tvT1.setText(device.getStatus());
						tvFlag = 0;
						continue;
					}
					if (Integer.parseInt(device.getStatus()) >= 30) {
						tvT1.setText(device.getStatus());
						if (tvFlag == 0) {
							pastTemp = device.getStatus();
							showTRNotification(pastTemp);
							tvFlag = 1;
							continue;
						}
						if (tvFlag == 1) {
							nowtemp = device.getStatus();
							if (Integer.parseInt(nowtemp) > Integer
									.parseInt(pastTemp)) {
								showTRNotification(nowtemp);
								pastTemp = nowtemp;
								continue;
							} else {
								continue;
							}
						}
					}
				}

				if (device.getName().equals("溼度") == true) {
					tvR1.setText(device.getStatus());
					continue;
				}

				if (device.getName().equals("磁簧") == true) {
					boolean check = GuardSwitch.isChecked();
					if (device.getStatus().equals("disable") == true) {
						if (switchCheckFlag != 1) {
							GuardSwitch.setChecked(false);
						}
						if (check == false) {
							switchCheckFlag = 0;
						}
						if (device.getAlarm().equals("false") == true) {
							String uri = "@drawable/" + "warning_end";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							guardLight.setImageDrawable(image);

						}
						if (device.getAlarm().equals("true") == true) {
							String uri = "@drawable/" + "warning_start";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							guardLight.setImageDrawable(image);
						}
						switchFlag = 0;
						continue;
					}
					if (device.getStatus().equals("enable") == true) {
						if (switchCheckFlag != 1) {
							GuardSwitch.setChecked(true);
						}
						if (check == true) {
							switchCheckFlag = 0;
						}
						if (device.getAlarm().equals("false") == true) {
							String uri = "@drawable/" + "warning_end";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							guardLight.setImageDrawable(image);
							switchFlag = 0;
							continue;
						}
						if (device.getAlarm().equals("true") == true) {
							if (switchFlag == 0) {
								String uri = "@drawable/" + "warning_start";
								int imageResource = getResources()
										.getIdentifier(uri, null,
												getPackageName());
								Drawable image = getResources().getDrawable(
										imageResource);
								guardLight.setImageDrawable(image);
								showGuardNotification();
								switchFlag = 1;
								switchTimeFlag = 0;
								continue;
							}
							if (switchFlag == 1) {
								switchTimeFlag++;
								if (switchTimeFlag != 16) {
									continue;
								} else {
									showGuardNotificationRepeat();
									switchTimeFlag = 0;
									continue;
								}
							}
						}
					}
				}

				if (device.getName().equals("蜂鳴器") == true) {
					boolean check = SpeakerSwitch.isChecked();
					if (device.getAlarm().equals("false") == true) {
						if (speakerCheckFlag != 1) {
							SpeakerSwitch.setChecked(false);
						}
						if (check == false) {
							speakerCheckFlag = 0;
						}
						continue;
					}
					if (device.getAlarm().equals("true") == true) {
						if (speakerCheckFlag != 1) {
							SpeakerSwitch.setChecked(true);
						}
						if (check == true) {
							speakerCheckFlag = 0;
						}
						continue;
					}
				}

				if (device.getName().equals("一氧化碳") == true) {
					if (device.getAlarm().equals("false") == true) {
						String uri = "@drawable/" + "warning_end";
						int imageResource = getResources().getIdentifier(uri,
								null, getPackageName());
						Drawable image = getResources().getDrawable(
								imageResource);
						gas.setImageDrawable(image);
						gasFlag = 0;
						continue;
					}
					if (device.getAlarm().equals("true") == true) {
						if (gasFlag == 0) {
							String uri = "@drawable/" + "warning_start";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							gas.setImageDrawable(image);
							showGasNotification();
							gasFlag = 1;
							gasTimeFlag = 0;
							continue;
						}
						if (gasFlag == 1) {
							gasTimeFlag++;
							if (gasTimeFlag != 18) {
								continue;
							} else {
								showGasNotificationRepeat();
								gasTimeFlag = 0;
								continue;
							}
						}
					}
				}

				if (device.getName().equals("按鈕") == true) {
					if (device.getAlarm().equals("false") == true) {
						if (visitorFlag == 0) {
							String uri = "@drawable/" + "warning_end";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							visitor.setImageDrawable(image);
							continue;
						}
						if (visitorFlag == 1) {
							visitorTimeFlag++;
							if (visitorTimeFlag < 10) {
								continue;
							} else {
								String uri = "@drawable/" + "warning_end";
								int imageResource = getResources()
										.getIdentifier(uri, null,
												getPackageName());
								Drawable image = getResources().getDrawable(
										imageResource);
								visitor.setImageDrawable(image);
								visitorFlag = 0;
								visitorTimeFlag = 0;
								continue;
							}
						}
					}
					if (device.getAlarm().equals("true") == true) {
						if (visitorFlag == 0) {
							String uri = "@drawable/" + "warning_start";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							visitor.setImageDrawable(image);
							showbuttonNotification();
							visitorFlag = 1;
							continue;
						}
						if (visitorFlag == 1) {
							visitorTimeFlag++;
							if (visitorTimeFlag < 10) {
								continue;
							} else {
								showbuttonNotification();
								visitorTimeFlag = 0;
								continue;
							}
						}
					}
				}

				if (device.getName().equals("紅外線") == true) {
					if (device.getAlarm().equals("false") == true) {
						if (suspiciousFlag == 0) {
							String uri = "@drawable/" + "warning_end";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							suspicious_person.setImageDrawable(image);
							continue;
						}
						if (suspiciousFlag == 1) {
							suspiciousTimeFlag++;
							if (suspiciousTimeFlag < 10) {
								continue;
							} else {
								String uri = "@drawable/" + "warning_end";
								int imageResource = getResources()
										.getIdentifier(uri, null,
												getPackageName());
								Drawable image = getResources().getDrawable(
										imageResource);
								suspicious_person.setImageDrawable(image);
								suspiciousFlag = 0;
								suspiciousTimeFlag = 0;
								continue;
							}
						}
					}
					if (device.getAlarm().equals("true") == true) {
						if (suspiciousFlag == 0) {
							String uri = "@drawable/" + "warning_start";
							int imageResource = getResources().getIdentifier(
									uri, null, getPackageName());
							Drawable image = getResources().getDrawable(
									imageResource);
							suspicious_person.setImageDrawable(image);
							showRpiNotification();
							suspiciousFlag = 1;
							continue;
						}
						if (suspiciousFlag == 1) {
							suspiciousTimeFlag++;
							if (suspiciousTimeFlag < 10) {
								continue;
							} else {
								showRpiNotification();
								suspiciousTimeFlag = 0;
								continue;
							}
						}
					}
				}
			}
		}
	};

	protected void showGuardNotification() {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		String dateTime = simpleDateFormat.format(calendar.getTime());

		final int notifyID = 1;
		final Uri soundUri = Uri.parse("android.resource://" + getPackageName()
				+ "/" + R.raw.balarm);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSmallIcon(R.drawable.icon)
				.setContentTitle("警戒異常").setContentText("警報時間:" + dateTime)
				.setSound(soundUri)
				.setVibrate(new long[] { 0, 1000, 300, 1000, 300, 1000 })
				.build();
		notificationManager.notify(notifyID, notification);
	}

	protected void showGuardNotificationRepeat() {

		final int notifyID = 2;
		final Uri soundUri = Uri.parse("android.resource://" + getPackageName()
				+ "/" + R.raw.balarm);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSound(soundUri)
				.setVibrate(new long[] { 0, 1000, 300, 1000, 300, 1000 })
				.setLights(Color.RED, 500, 500).build();
		notificationManager.notify(notifyID, notification);
	}

	protected void showGasNotification() {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		String dateTime = simpleDateFormat.format(calendar.getTime());

		final int notifyID = 3;
		final Uri soundUri = Uri.parse("android.resource://" + getPackageName()
				+ "/" + R.raw.gas);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSmallIcon(R.drawable.icon)
				.setContentTitle("氣體異常").setContentText("異常時間:" + dateTime)
				.setSound(soundUri)
				.setVibrate(new long[] { 0, 500, 300, 500, 300, 500 })
				.setLights(Color.YELLOW, 500, 500).build();
		notificationManager.notify(notifyID, notification);
	}

	protected void showGasNotificationRepeat() {
		final int notifyID = 4;
		final Uri soundUri = Uri.parse("android.resource://" + getPackageName()
				+ "/" + R.raw.gas);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSound(soundUri)
				.setVibrate(new long[] { 0, 500, 300, 500, 300, 500 })
				.setLights(Color.YELLOW, 500, 500).build();
		notificationManager.notify(notifyID, notification);
	}

	protected void showbuttonNotification() {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		String dateTime = simpleDateFormat.format(calendar.getTime());

		final int notifyID = 5;
		final Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSmallIcon(R.drawable.icon)
				.setContentTitle("外有訪客").setContentText("門鈴時間:" + dateTime)
				.setSound(soundUri).setVibrate(new long[] { 0, 1000 }).build();
		notificationManager.notify(notifyID, notification);
	}

	protected void showRpiNotification() {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		String dateTime = simpleDateFormat.format(calendar.getTime());

		final int notifyID = 6;
		final Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSmallIcon(R.drawable.icon)
				.setContentTitle("外有可疑人物").setContentText("出現時間:" + dateTime)
				.setSound(soundUri)
				.setVibrate(new long[] { 0, 1000, 300, 1000 }).build();
		notificationManager.notify(notifyID, notification);
	}

	protected void showTRNotification(String t) {

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		String dateTime = simpleDateFormat.format(calendar.getTime());

		final int notifyID = 8;
		final Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification.Builder(
				getApplicationContext()).setSmallIcon(R.drawable.icon)
				.setContentTitle("溫度異常")
				.setContentText("最高溫度:" + t + "℃" + "(" + dateTime + ")")
				.setSound(soundUri).build();
		notificationManager.notify(notifyID, notification);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	void initEventsListenr() {
		Button btnIPcamCtrl = (Button) findViewById(R.id.buttonIPcamControl);
		btnIPcamCtrl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (true) {
					String url = "rtsp://192.168.2.200";
					url += ":";
					url += 554;
					url += "/h264_2";
					Uri uri = Uri.parse(url);
					startActivity(new Intent(Intent.ACTION_VIEW, uri));
				}
			}
		});
	}

}
