package com.shageldi.intercom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

public class AudioCall {

	private static final String LOG_TAG = "AudioCall";
	private static int SAMPLE_RATE = 4000; // Hertz
	private static final int SAMPLE_INTERVAL = 20; // Milliseconds
	private static final int SAMPLE_SIZE = 2; // Bytes
	private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
	private InetAddress address; // Address to call
	private int port = 50000; // Port the packets are addressed to
	private boolean mic = false; // Enable mic?
	private boolean speakers = false; // Enable speakers?
	private static final int SESSION_ID=2;
	AudioTrack track=null;

	String recordaudiofrom,acousticEchoCanceler,playas,quality,volume,progressbar="";

	Context context;
	boolean isTrackStoped=false;
	
	public AudioCall(InetAddress address,Context context) {
		
		this.address = address;
		this.context=context;
	}
	
	public void startCall() {
//		AudioManager manager=(AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//		manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//		manager.setSpeakerphoneOn(false);
//		manager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 0);

		SharedPreferences share1 = context.getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
		recordaudiofrom = share1.getString("recordaudiofrom", "");

		SharedPreferences share2 = context.getSharedPreferences("acousticEchoCanceler", Activity.MODE_PRIVATE);
		acousticEchoCanceler = share2.getString("acousticEchoCanceler", "");

		SharedPreferences share3 = context.getSharedPreferences("playas", Activity.MODE_PRIVATE);
		playas = share3.getString("playas", "");

		SharedPreferences share4 = context.getSharedPreferences("quality", Activity.MODE_PRIVATE);
		quality = share4.getString("quality", "");

		SharedPreferences share5 = context.getSharedPreferences("volume", Activity.MODE_PRIVATE);
		volume = share5.getString("volume", "");

		SharedPreferences share16 = context.getSharedPreferences("progressbar", Activity.MODE_PRIVATE);
		progressbar = share16.getString("progressbar", "");

//		if(Integer.valueOf(quality)>=0 && Integer.valueOf(quality)<=20){
//			SAMPLE_RATE=4000;
//		}
//
//		if(Integer.valueOf(quality)>20 && Integer.valueOf(quality)<=40){
//			SAMPLE_RATE=8000;
//		}
//
//		if(Integer.valueOf(quality)>40 && Integer.valueOf(quality)<=80){
//			SAMPLE_RATE=16000;
//		}
//
//		if(Integer.valueOf(quality)>80 && Integer.valueOf(quality)<=100){
//			SAMPLE_RATE=32000;
//		}
		if(track!=null){
			//System.out.println("Track is null");
			track.stop();
			track.flush();
			track.release();

            track=null;

		}


		startMic();
		startSpeakers();

	}
	
	public void endCall() {
		
		Log.i(LOG_TAG, "Ending call!");
		muteMic();
		muteSpeakers();


	}
	
	public void muteMic() {
		mic = false;
	}
	
	public void muteSpeakers() {
		speakers = false;
	}
	
	public void startMic() {
		// Creates the thread for capturing and transmitting audio
		mic = true;
		Thread thread = new Thread(new Runnable() {
			
			@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void run() {
				// Create an instance of the AudioRecord class
				//Log.i(LOG_TAG, "Send thread started. Thread id: " + Thread.currentThread().getId());
				AudioRecord audioRecorder=null;
				if(recordaudiofrom.equals("1") || recordaudiofrom.isEmpty()){
					audioRecorder = new AudioRecord (MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
							AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT,
							AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)*10);
				} else if(recordaudiofrom.equals("2")){
					audioRecorder = new AudioRecord (MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
							AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
							AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT)*10);
				}



				MediaRecorder mediaRecorder=new MediaRecorder();




					if(AutomaticGainControl.isAvailable())
					{
						AutomaticGainControl agc =AutomaticGainControl.create(audioRecorder.getAudioSessionId());
						//agc.g
						//Log.d("AudioRecord", "AGC is " + (agc.getEnabled()?"enabled":"disabled"));
						agc.setEnabled(true);
						//Log.d("AudioRecord", "AGC is " + (agc.getEnabled()?"enabled":"disabled" +" after trying to enable"));
					}else
					{
						//Log.d("AudioRecord", "AGC is unavailable");
					}
//
//
//
					if(NoiseSuppressor.isAvailable()){
						NoiseSuppressor ns = NoiseSuppressor.create(audioRecorder.getAudioSessionId());
						//Log.d("AudioRecord", "NS is " + (ns.getEnabled()?"enabled":"disabled"));
						ns.setEnabled(true);
						//Log.d("AudioRecord", "NS is " + (ns.getEnabled()?"enabled":"disabled" +" after trying to disable"));
					}else
					{
						//Log.d("AudioRecord", "NS is unavailable");
					}


					if(AcousticEchoCanceler.isAvailable() && recordaudiofrom.equals("2")){

						AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioRecorder.getAudioSessionId());
						//Log.d("AudioRecord", "AEC is " + (aec.getEnabled()?"enabled":"disabled"));
						aec.setEnabled(true);
						//Log.d("AudioRecord", "AEC is " + (aec.getEnabled()?"enabled":"disabled" +" after trying to disable"));

					}else
					{
						//Log.d("AudioRecord", "aec is unavailable");
					}


//				AcousticEchoCanceler acousticEchoCanceler=AcousticEchoCanceler.create(audioRecorder.getAudioSessionId());
//				acousticEchoCanceler.setEnabled(true);



				//Toast.makeText(context, ""+ptt_enable, Toast.LENGTH_SHORT).show();



					int bytes_read = 0;
					int bytes_sent = 0;
					byte[] buf = new byte[BUF_SIZE];
					try {
						// Create a socket and start recording
						DatagramSocket socket = new DatagramSocket();
						//byte[] key=Encryption.generateKey("password");
						while(mic) {
							// Capture audio from the mic and transmit it
							SharedPreferences share6 = context.getSharedPreferences("ptt_enable", Activity.MODE_PRIVATE);
							String ptt_enable = share6.getString("ptt_enable", "");

							SharedPreferences share61= context.getSharedPreferences("voicedetection", Activity.MODE_PRIVATE);
							String voicedetection = share61.getString("voicedetection", "");
							if(voicedetection.equals("0") && ptt_enable.equals("1")) {
								Log.i(LOG_TAG, "Packet destination: " + address.toString());

								audioRecorder.startRecording();
								bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);

								DatagramPacket packet = new DatagramPacket(AESencrp.encrypt(buf, progressbar), bytes_read, address, port);
								socket.send(packet);
								bytes_sent += bytes_read;
								//	System.out.println("Send data: "+buf);
								//System.out.println("Encryption Send data: "+AESencrp.encrypt(buf));
								//	Log.i(LOG_TAG, "Total bytes sent: " + bytes_sent);
								Thread.sleep(SAMPLE_INTERVAL, 0);
							} else if(voicedetection.equals("1")){
								Log.i(LOG_TAG, "Packet destination: " + address.toString());

								audioRecorder.startRecording();
								bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);

								DatagramPacket packet = new DatagramPacket(AESencrp.encrypt(buf, progressbar), bytes_read, address, port);
								socket.send(packet);
								bytes_sent += bytes_read;
								//	System.out.println("Send data: "+buf);
								System.out.println("Encryption Send data: "+AESencrp.encrypt(buf,progressbar));
								//	Log.i(LOG_TAG, "Total bytes sent: " + bytes_sent);
								Thread.sleep(SAMPLE_INTERVAL, 0);
							}
						}
						// Stop recording and release resources
						Log.d("AUDIORECORD","Stopping audio recorder");
						audioRecorder.stop();
						audioRecorder.release();
						socket.disconnect();
						socket.close();
						mic = false;
						return;
					}
					catch(InterruptedException e) {

						Log.e(LOG_TAG, "InterruptedException: " + e.toString());
						mic = false;
					}
					catch(SocketException e) {

						Log.e(LOG_TAG, "SocketExceptionRecord: " + e.toString());
						mic = false;
					}
					catch(UnknownHostException e) {

						Log.e(LOG_TAG, "UnknownHostExceptionRecord: " + e.toString());
						mic = false;
					}
					catch(IOException e) {

						Log.e(LOG_TAG, "IOExceptionRecord: " + e.toString());
						mic = false;
					} catch (Exception e) {
						e.printStackTrace();
					}




			}
		});
		thread.start();
	}
	
	public void startSpeakers() {
		// Creates the thread for receiving and playing back audio
		if(!speakers) {
			
			speakers = true;
			Thread receiveThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					if(isTrackStoped || track==null){
						if(playas.equals("1") || playas.isEmpty()){
							track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT,
									AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
						} else if(playas.equals("2")){
							track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT,
									AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
						}

//						android.media.AudioAttributes attributes;
//
//							attributes = new android.media.AudioAttributes.Builder()
//									.setContentType(android.media.AudioAttributes.CONTENT_TYPE_MOVIE)
//									.setFlags(android.media.AudioAttributes.FLAG_HW_AV_SYNC)
//									.setUsage(android.media.AudioAttributes.USAGE_MEDIA)
//									.build();
//
//						AudioFormat format =
//								new AudioFormat.Builder()
//										.setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
//										.setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
//										.build();
//
//
//						AudioTrack myTrack = new AudioTrack(attributes, format, BUF_SIZE, AudioTrack.MODE_STREAM,SESSION_ID);
						//	track.setStereoVolume(1,1);
						//track.setVolume()
						//	track.setVolume()
						track.play();
//						track.setStereoVolume(1,1);
//						track.setVolume(100);
						isTrackStoped=false;
					}

					try {

						// Define a socket to receive the audio
						DatagramSocket socket = new DatagramSocket(null);
						socket.setReuseAddress(true);
						socket.setBroadcast(true);
						socket.bind(new InetSocketAddress(port));
						byte[] buf = new byte[BUF_SIZE];

						//byte[] key=Encryption.generateKey("password");
						while(speakers) {

							// Play back the audio received from packets
							DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
							socket.receive(packet);
							//Log.i(LOG_TAG, "Packet received: " + packet.getLength());
						//	System.out.println("Received STREAM: "+packet.getData());
							System.out.println("Decrypted Received STREAM: "+AESencrp.decrypt(packet.getData(),progressbar));
							System.out.println("Sender Ip address: "+packet.getAddress().getHostAddress()+" : "+getIpAddressNew().getHostAddress());
                            if(packet.getAddress().getHostAddress().equals(address.getHostAddress())){

								track.write(AESencrp.decrypt(packet.getData(),progressbar), 0, BUF_SIZE);
							}

						}
						// Stop playing back and release resources
						Log.d("AUDIOTRACK","Stopping audio tracker");
						socket.disconnect();
						socket.close();


							track.stop();
							track.flush();
							track.release();
							isTrackStoped=true;
							track=null;


						speakers = false;
						return;
					}
					catch(SocketException e) {
						
						Log.e(LOG_TAG, "SocketExceptionTRack: " + e.toString());
						speakers = false;
					}
					catch(IOException e) {
						
						Log.e(LOG_TAG, "IOExceptionTrack: " + e.toString());
						speakers = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			receiveThread.start();
		}
	}


	public InetAddress getIpAddressNew() {
		InetAddress inetAddress = null;
		InetAddress myAddr = null;

		try {
			for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
					.getNetworkInterfaces(); networkInterface.hasMoreElements();) {

				NetworkInterface singleInterface = networkInterface.nextElement();

				for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
						.hasMoreElements();) {
					inetAddress = IpAddresses.nextElement();

					if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
							.contains("wlan0") ||
							singleInterface.getDisplayName().contains("eth0") ||
							singleInterface.getDisplayName().contains("ap0"))) {

						myAddr = inetAddress;
					}
				}
			}

		} catch (SocketException ex) {
			Log.e("ERROR", ex.toString());
		}
		return myAddr;
	}

	public InetAddress getBroadcast(InetAddress inetAddr) {

		if(inetAddr!=null) {
			NetworkInterface temp;
			InetAddress iAddr = null;
			try {
				temp = NetworkInterface.getByInetAddress(inetAddr);
				List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

				for (InterfaceAddress inetAddress : addresses)

					iAddr = inetAddress.getBroadcast();
				Log.d("MSG", "iAddr=" + iAddr);
				return iAddr;

			} catch (SocketException e) {

				e.printStackTrace();
				Log.d("ERROR", "getBroadcast" + e.getMessage());
			}
		}
		return null;
	}
}
