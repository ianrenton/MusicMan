/*
 * UpdateLibrary.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import net.onlydreaming.musicman.MusicManApp;
import net.onlydreaming.musicman.objects.Song;
import org.jdesktop.application.Task;

/**
 * Task to update the contents of the library with the actual situation on disk.
 */
public class UpdateLibrary extends Task<Void, Void> {

    private MusicManApp app;

    public UpdateLibrary(org.jdesktop.application.Application app) {
        super(app);
        this.app = (MusicManApp) app;
    }

    /**
     * Starts the task.
     * Checks for files that are in the library but not on disk, and removes
     * them.  Then, checks for files in the dirs on disk but not in the library,
     * and adds them.  Finally, sorts the library with the default sort order.
     * @return
     */
    @Override
    public Void doInBackground() {
        try {
            setMessage("Checking for missing files.");
            int counter = 0;
            int countmax = app.getLibrary().size();
            for (Song song : app.getLibrary()) {
                setMessage("Checking for missing files...");
                if (!song.getPath().exists()) {
                    song.setMissing(true);
                }
                setProgress(counter++ / (float) countmax);
            }

            counter = 0;
            while (true) {
                setMessage("Removing missing files...");
                try {
                    if (app.getLibrary().get(counter).isMissing()) {
                        app.getLibrary().remove(counter);
                    } else {
                        // Only increment the counter if we haven't deleted, since
                        // deleting shifts everything to the left.
                        setProgress(counter++ / (float) countmax);
                    }
                } catch (IndexOutOfBoundsException ex) {
                    // Fallen off the end of the array, this is our exit condition.
                    break;
                }
            }

            counter = 0;
            HashMap<String, Song> libraryHashMap = new HashMap<String, Song>();
            for (Song song : app.getLibrary()) {
                setMessage("Generating library hashes...");
                libraryHashMap.put(song.getPath().getAbsolutePath(), song);
                setProgress(counter++ / (float) countmax);
            }

            // Check for new songs and merge them.
            for (File dir : app.getLibraryDirs()) {
                mergeContentsWithLibrary(libraryHashMap, dir, true);
            }

            setMessage("Sorting...");
            Collections.sort(app.getLibrary());

            succeeded();
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Checks the files in a directory, and adds them to the library if they are
     * not already present.  This calls itself recursively so that it can do
     * a recursive scan all the way down a file hierarchy.
     * @param libraryHashMap HashMap of existing library entries, with the key
     * being their path (so our checking if a file is already in the library
     * is efficient).
     * @param dir The directory to check.
     * @param updateProgress Update the progress bar with a percentage.  Should
     * be called as True at the top level, then false thereafter, otherwise the
     * progress bar will go from 0-100% many times during the scan.
     */
    private void mergeContentsWithLibrary(HashMap<String, Song> libraryHashMap, File dir, boolean updateProgress) {
        setMessage("Checking " + dir);
        File[] contents = dir.listFiles();
        if (contents != null) {
            int counter = 0;
            for (File file : contents) {
                if (file.isDirectory()) {
                    mergeContentsWithLibrary(libraryHashMap, file, false);
                } else {
                    try {
                        if (file.getName().endsWith("mp3")) {
                            if (!libraryHashMap.containsKey(file.getAbsolutePath())) {
                                app.getLibrary().add(new Song(file));
                            }
                        }
                    } catch (IOException ex) {
                        System.out.println(ex);
                        //File didn't exist or wasn't readable
                    }
                }
                if (updateProgress) {
                    setProgress(counter++ / (float) contents.length);
                }
            }
        }
    }

    protected void succeeded() {
        setMessage("");
        app.libraryUpdated();
    }
}
