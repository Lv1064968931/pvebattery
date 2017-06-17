package com.asdc.mybattery;

import java.io.IOException;

import cn.waps.AdView;
import cn.waps.AppConnect;
import cn.waps.MiniAdView;
import cn.waps.UpdatePointsNotifier;
import cn.waps.extend.QuitPopAd;
import cn.waps.extend.SlideWall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PrepareAsyncActivity extends Activity implements UpdatePointsNotifier, OnClickListener {
	
    private MediaPlayer mediaPlayer;
	private ImageButton ppIB;
	private SeekBar audioSB;
	private ProgressDialog dialog;
	private String path;
	
	private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (mediaPlayer != null)
				mediaPlayer.seekTo(audioSB.getProgress());	// ���ò������Ľ���Ϊ�������ĵ�ǰ����
		}
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		}
	};
	
	private OnCompletionListener onCompletionListener = new OnCompletionListener() {
		
		
		public void onCompletion(MediaPlayer mp){
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			audioSB.setProgress(0);
			ppIB.setImageResource(android.R.drawable.ic_media_play);
			sp.edit().putBoolean("isPlaying", false).commit();
			finish();
		}
	};
	private OnPreparedListener onPreparedListener = new OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			dialog.dismiss();		// ȡ���Ի���
			mediaPlayer.start();
			sp.edit().putBoolean("isPlaying", true).commit();// ��ʼ����(���߳���ִ��)
	    	handleSeekBar();		// ���������
	    	ppIB.setImageResource(mediaPlayer.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
		}
	};
	private boolean isPlaying;
	private SharedPreferences sp;
	private Button feedbackButton;
	private Button gameOffersButton;
	private Button OffersButton;
	private LinearLayout slidingDrawerView;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio);
        
		sp = this.getSharedPreferences("config", Context.MODE_PRIVATE);
		isPlaying = sp.getBoolean("isPlaying", false);
        
        ppIB = (ImageButton) findViewById(R.id.ppIB);
        audioSB = (SeekBar) findViewById(R.id.audioSB);
        
        path = getIntent().getStringExtra("path");
        
        feedbackButton = (Button) findViewById(R.id.feedback);
		gameOffersButton = (Button) findViewById(R.id.gameOffers);
		OffersButton = (Button) findViewById(R.id.Offers);
		ppIB.setOnClickListener(this);
		OffersButton.setOnClickListener(this);
		gameOffersButton.setOnClickListener(this);
		feedbackButton.setOnClickListener(this);
		
		slidingDrawerView = SlideWall.getInstance().getView(this);
		// ���������÷�ʽ
		LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
		new AdView(this, container).DisplayAd();

		// ��������÷�ʽ
		LinearLayout miniLayout = (LinearLayout) findViewById(R.id.miniAdLinearLayout);
		new MiniAdView(this, miniLayout).DisplayAd(10);// 10��ˢ��һ��
		
    }
	
	protected void onDestroy(){
		super.onDestroy();
		AppConnect.getInstance(this).finalize();
		sp.edit().putBoolean("isPlaying", false).commit();
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}
    
	
	
	
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
    	AppConnect.getInstance(this).getPoints(this);
    	 mediaPlayer = new MediaPlayer();							// ����ý�岥����
     	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);	// ������Ƶ������
     	try {
 			mediaPlayer.setDataSource("/mnt/sdcard/Battery/bj.mp3");
 			mediaPlayer.setOnCompletionListener(onCompletionListener);	// ���ò�����ɼ�����
 			mediaPlayer.setOnPreparedListener(onPreparedListener);		// ���׼����ɼ�����
 			
 			mediaPlayer.prepareAsync();	
 			showDialog();		
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	// ������ƵԴ
         
		super.onResume();
	}
    /**
     * 
     */
	public void onClick(View view) {
    		if (view instanceof Button) {
    			int id = ((Button) view).getId();

    			switch (id) {
    			case R.id.ppIB:
    				if (mediaPlayer == null) {
    			    	mediaPlayer = new MediaPlayer();							// ����ý�岥����
    			    	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);	// ������Ƶ������
    			    	try {
    						mediaPlayer.setDataSource(path);
    					} catch (IllegalArgumentException e) {
    						e.printStackTrace();
    					} catch (IllegalStateException e) {
    						e.printStackTrace();
    					} catch (IOException e) {
    						e.printStackTrace();
    					}		    	
    			    	mediaPlayer.setOnCompletionListener(onCompletionListener);	// ���ò�����ɼ�����
    			    	mediaPlayer.setOnPreparedListener(onPreparedListener);		// ���׼����ɼ�����
    			    	mediaPlayer.prepareAsync();		// �첽����
    			    	showDialog();					// ��ʾ�Ի���
    		    	} else if (mediaPlayer.isPlaying()) {
    		    		mediaPlayer.pause();	// ��ͣ
    		    	} else {
    		    		mediaPlayer.start();	// ��������
    		    	}
    		    	ppIB.setImageResource(mediaPlayer.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    			case R.id.gameOffersButton:
    				//��ʾ�Ƽ��б���Ϸ��
    				AppConnect.getInstance(this).showGameOffers(this);
    				break;
    			case R.id.OffersButton:
    				//��ʾ�Ƽ��б��ۺϣ�
    				AppConnect.getInstance(this).showOffers(this);
    				break;
    			case R.id.moreAppsButton:
    				//�û�����
    				AppConnect.getInstance(this).showMore(this);
    				break;
    			}
    		}
    }

	private void handleSeekBar() {
		audioSB.setOnSeekBarChangeListener(onSeekBarChangeListener);
		audioSB.setMax(mediaPlayer.getDuration());		// ���ý��������������Ϊ�������ļ���ʱ��
		new Thread(){
			public void run() {
				while (mediaPlayer != null) {
					if (mediaPlayer.isPlaying() && !audioSB.isPressed())
						audioSB.setProgress(mediaPlayer.getCurrentPosition());		// ���ý�������ǰ����Ϊ��������ǰ����
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void showDialog() {
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);	// ���ý�������ʽ
		dialog.setMessage("���ڻ���, ���Ժ�...");
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(SlideWall.getInstance().slideWallDrawer != null
					&& SlideWall.getInstance().slideWallDrawer.isOpened()){
				
				// �������ʽӦ��ǽչʾ�У���رճ���
				SlideWall.getInstance().closeSlidingDrawer();
			}else{
				// �����������
				QuitPopAd.getInstance().show(this);
			}
			
		}
		return true;
	}
	
}