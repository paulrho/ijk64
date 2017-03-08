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
/**
 * Handles playing, stoping, and looping of sounds for the game.
 * @author Tyler Thomas
 *
 */
public class PlaySound {
  PlaySound(String fileName) {
    try {
    ClipTest a = new ClipTest(fileName);
    a.run();
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
    public void run() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        //InputStream inRequest = this.getClass().getResourceAsStream(fileName);
        //InputStream inRequest = this.getClass().getResourceAsStream("noise.wav");

      AudioInputStream sound=AudioSystem.getAudioInputStream(
      new BufferedInputStream(new FileInputStream(fileName)));

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
