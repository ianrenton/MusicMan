/*
 * PlaySong.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.tasks;

import net.onlydreaming.musicman.MusicManApp;
import org.jdesktop.application.Task;
import javax.sound.sampled.*;
import java.io.*;

/**
 * Task to set a song playing.
 */
public class PlaySong extends Task<Void, Void> {

    private MusicManApp app;
    private final int id;
    private boolean stopPlaying = false;

    public PlaySong(org.jdesktop.application.Application app, int id) {
        super(app);
        this.app = (MusicManApp) app;
        this.id = id;
        this.stopPlaying = false;
    }

    /**
     * Start the task.
     * Starts the song playing, waits until it has finished, then fires a Next
     * action to the MusicManView.
     * @return
     */
    @Override
    public Void doInBackground() {
        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(app.getCurrentPlaylist().get(id).getPath());
            AudioInputStream din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            // Play now.
            rawplay(decodedFormat, din);
            in.close();
            // If stopPlaying is true, something has requested the stop and we'll
            // leave it up to that to decide what to do (e.g. next, prev, pause).
            // If we've reached here and stopPlaying is false, we naturally got
            // to the end of the song, so fire a next.
            if (!stopPlaying) {
                app.getView().next();
            }
        } catch (Exception ex) {
            System.out.println(ex);
            app.getView().next();
        }

        return null;
    }

    /**
     * Stops playing.  Useful for things that need to interrupt, like clicking
     * pause / prev / next.
     */
    public void stopPlaying() {
        stopPlaying = true;
    }

    private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        if (line != null) {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while ((nBytesRead != -1) && (!stopPlaying)) {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) {
                    nBytesWritten = line.write(data, 0, nBytesRead);
                }
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }
}
