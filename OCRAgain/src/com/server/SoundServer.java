package com.server;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import sun.audio.AudioPlayer;
/**
 * 警报提示音频
 * @author zhoushuyan
 *
 */
public class SoundServer {
	private InputStream inputStream = null;
	//private String file = "E:\\111\\ALARM8.wav";
	public void playSound(String file) {
		    try {
				inputStream = new FileInputStream(new File(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   AudioPlayer.player.start(inputStream);
	}
	
}
