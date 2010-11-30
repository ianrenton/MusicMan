/*
 * UpdateOneTag.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.tasks;

import net.onlydreaming.musicman.MusicManApp;
import org.jdesktop.application.Task;

/**
 * Task to update ID3 tags for a song.
 */
public class UpdateOneTag extends Task<Void, Void> {

    private MusicManApp app;
    private final int id;

    /**
     *
     * @param app
     * @param id sequential ID of the song in the current playlist to update
     */
    public UpdateOneTag(org.jdesktop.application.Application app, int id) {
        super(app);
        this.app = (MusicManApp) app;
        this.id = id;
    }

    /**
     * Starts the thread.
     * Updates the song's ID3 tags, and pushes the changes up to the View's table.
     * @return
     */
    @Override
    public Void doInBackground() {
        try {
            if (!app.getCurrentPlaylist().get(id).isTagged()) {
                setMessage("Updating ID3 Tag...");
                app.updateSongAndTableWithTags(id);
            }
            succeeded();
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return null;
    }

    protected void succeeded() {
        setMessage("");
    }
}
