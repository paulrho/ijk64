/////////////////////////////////////////////////////////////////////////////////
//
//
//
/////////////////////////////////////////////////////////////////////////////////

import javax.sound.sampled.*;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.net.*; // for reading from http
import java.io.InputStreamReader;
import java.io.FileOutputStream;
/**
 * Handles playing, stoping, and looping of sounds for the game.
 * @author Tyler Thomas
 *
 */
public class PlaySound {
  static int maxSound=0; 
  static int highSound=0;
  static String soundFile[];
  static String soundName[];
  static {
	  soundFile=new String[100];
	  soundName=new String[100];
  }

  PlaySound(String fileName) {

    boolean useindex=false;
    int i;
    for (i=0; i<highSound; ++i) if (fileName.equals(soundName[i])) {
	    useindex=true;
	    System.out.printf("Using cached sound %d=%s\n",i,soundFile[i]);
	    break;
	   }
    try {
      ClipTest a;
      if (useindex) { System.out.printf("i=%d file=%s\n",i,soundFile[i]); a = new ClipTest(soundFile[i]); }
      else a = new ClipTest(fileName);
      a.run();
  
      if (!useindex) {
        soundName[maxSound]=fileName;
        soundFile[maxSound++]=a.soundcachename; 
	System.out.printf("setting to %s\n",a.soundcachename);
        if (highSound<100) highSound++; 
        if (maxSound==100) maxSound=0;
      }

      //a.play();
    } catch (java.io.FileNotFoundException e) {
      System.err.println("File Not Found "+e.getMessage());
    } catch (Exception e) {
      System.err.printf("ClipTest fail\n");
      System.err.println(e.getMessage());
 e.printStackTrace();
    }
  }
}

class ClipTest {

    String fileName;
    boolean usesound=false;
    AudioInputStream sound;
    String soundcachename;

    boolean saveCachedFile(URL FILE_URL, String FILE_NAME) {
	    soundcachename=FILE_NAME;
      try {
        BufferedInputStream in = new BufferedInputStream(FILE_URL.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
          fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        return true;
      } catch (IOException e) {
        // handle exception
        return false;
      }
    }

    String deburst(String full) {
      String[] p=full.split("/");
      return "cache.."+p[p.length-1];
    }

    public void run() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        //InputStream inRequest = this.getClass().getResourceAsStream(fileName);
        //InputStream inRequest = this.getClass().getResourceAsStream("noise.wav");

	//if (filename.startsWith("http")) {
	  //URL url = new URL(filename);
          //bgImage = ImageIO.read(url.openStream());
	//} else {
          //bgImage = ImageIO.read(new File(filename));
        //}
      //BufferedReader in = new BufferedReader(
            //new InputStreamReader(
            //url.openStream()));

      //AudioInputStream sound=AudioSystem.getAudioInputStream(
      //new BufferedInputStream(new FileInputStream(fileName)));

	      System.out.printf("About to play sound...%s\n",fileName);
      if (usesound) {
	      System.out.printf("About to play cached sound\n");
      } else if (fileName.startsWith("http")) {
	        URL url = new URL(fileName);
         // bgImage = ImageIO.read(url.openStream());
	soundcachename=deburst(fileName);
        if (saveCachedFile(url,soundcachename)) {
		System.out.printf("playing cached file\n");
			  sound=AudioSystem.getAudioInputStream(
          new BufferedInputStream(new FileInputStream(soundcachename)));
	} else {
		System.out.printf("playing url direct\n");
	   sound=AudioSystem.getAudioInputStream(
           new BufferedInputStream(
           //new InputStreamReader(
           url.openStream()));
	}
      } else {
			  sound=AudioSystem.getAudioInputStream(
          new BufferedInputStream(new FileInputStream(fileName)));
      }


        //AudioInputStream sound = AudioSystem.getAudioInputStream(inRequest);

        DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(sound);

        clip.addLineListener(new LineListener() {

            public void update(LineEvent event) {
                if(event.getType() == LineEvent.Type.STOP) {
//if (verbose) { System.out.printf("Sound STOPped\n"); }
                    event.getLine().close();
                    return;
                }
            }
        });

        clip.start();
//System.out.printf("clip starting\n");

    }

    ClipTest(String filename) throws Exception
    {
      fileName=filename;
    }
}
