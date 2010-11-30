/*
 * UpdateAllTags.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.tasks;

import java.util.Collections;
import net.onlydreaming.musicman.MusicManApp;
import net.onlydreaming.musicman.objects.Song;
import org.jdesktop.application.Task;

public class UpdateAllTags extends Task<Void, Void> {

    private MusicManApp app;

    public UpdateAllTags(org.jdesktop.application.Application app) {
        super(app);
        this.app = (MusicManApp) app;
    }

    @Override
    public Void doInBackground() {
        try {
            setMessage("Updating ID3 Tags...");
            int counter = 0;
            int countmax = app.getLibrary().size();
            for (Song song : app.getLibrary()) {
                if (!song.isTagged()) {
                    song.updateWithTags();
                    setMessage("Reading tags for " + song.getArtist() + " - " + song.getTitle());
                }
                setProgress(counter++ / (float) countmax);
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
        app.getView().updatePlaylistIfViewingLibrary();
    }
}
