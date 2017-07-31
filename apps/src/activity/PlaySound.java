package activity;

import java.io.File;

import javax.sound.sampled.Clip;

import sys.Log;
import sys.Sound;

public class PlaySound {

	public static void main(String[] args) {
		Clip clip = null;

		try {
			//File f = new File("res/SampleAudio_0.5mb.mp3");
			File f = new File("../resources/vivaldi-spring.mp3");
			clip = Sound.play(f.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (clip!=null) {
			clip.drain();
			clip.stop();
			clip=null;
		}

		try {
			File f = new File("res/alarm.wav");
			Log.prn("url: %s", f.toURI().toURL().toString());
			clip = Sound.play(f.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (clip!=null) {
			clip.drain();
			clip.stop();
			clip=null;
		}

	}
}
