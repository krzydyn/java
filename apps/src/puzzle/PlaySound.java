package puzzle;

import java.io.File;

import javax.sound.sampled.Clip;

import snd.Sound;
import sys.Log;

public class PlaySound {

	public static void main(String[] args) {
		Clip clip = null;

		try {
			File f = new File("../resources/vivaldi-spring.mp3");
			Log.prn("url: %s", f.toURI().toURL().toString());
			clip = Sound.play(f.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
			return ;
		}

		if (clip!=null) {
			clip.drain();
			clip.stop();
		}

	}
}
