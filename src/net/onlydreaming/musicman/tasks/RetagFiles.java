/*
 * RetagFiles.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.tasks;

import java.util.ArrayList;
import java.util.Collections;
import net.onlydreaming.musicman.MusicManApp;
import net.onlydreaming.musicman.objects.Song;
import net.onlydreaming.musicman.objects.TagUpdateBundle;
import org.jdesktop.application.Task;

public class RetagFiles extends Task<Void, Void> {

    private MusicManApp app;
    private final ArrayList<Song> playlist;
    private final int[] indices;
    private final TagUpdateBundle bundle;

    public RetagFiles(org.jdesktop.application.Application app, ArrayList<Song> playlist, int[] indices, TagUpdateBundle bundle) {
        super(app);
        this.app = (MusicManApp) app;
        this.playlist = playlist;
        this.indices = indices;
        this.bundle = bundle;
    }

    @Override
    public Void doInBackground() {
        try {
            setMessage("Saving ID3 Tags...");
            int counter = 0;
            int countmax = indices.length;
            for (counter = 0; counter < indices.length; counter++) {
                playlist.get(indices[counter]).retag(bundle);
                setMessage("Tagging " + playlist.get(indices[counter]).getArtist() + " - " + playlist.get(indices[counter]).getTitle());
                setProgress(counter / (float) countmax);
            }

            setMessage("Sorting...");
            Collections.sort(app.getLibrary());

            succeeded();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    protected void succeeded() {
        setMessage("");
        app.getView().fillTable(app.getCurrentPlaylist());
    }
}
