/*
 * MusicManApp.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.onlydreaming.musicman.objects.Song;
import net.onlydreaming.musicman.objects.TagUpdateBundle;
import net.onlydreaming.musicman.tasks.PlaySong;
import net.onlydreaming.musicman.tasks.RetagFiles;
import net.onlydreaming.musicman.tasks.UpdateAllTags;
import net.onlydreaming.musicman.tasks.UpdateLibrary;
import net.onlydreaming.musicman.tasks.UpdateOneTag;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;

/**
 * The main class of the application.
 */
public class MusicManApp extends SingleFrameApplication {

    private final String DIRECTORIES_FILE = "directories.bin";
    private final String LIBRARY_FILE = "library.bin";
    private ArrayList<File> libraryDirs = new ArrayList<File>();
    private ArrayList<Song> library = new ArrayList<Song>();
    private ArrayList<Song> currentPlaylist = new ArrayList<Song>();
    private MusicManView view;
    private PlaySong playerTask;

    /**
     * At startup load dirs and library information from disk, create and show
     * the main frame of the application, then set the library updater task
     * going.  If the user does not have a saved library or dirs list, spawn
     * the dir entry dialog so that they can specify their dirs.  This dialog
     * will then trigger a library reload itself.
     */
    @Override
    protected void startup() {

        // Logging mostly off, because JAudioTagger spams a load of stuff.
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.SEVERE);

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DIRECTORIES_FILE));
            libraryDirs = (ArrayList<File>) ois.readObject();
            ois.close();
            ois = new ObjectInputStream(new FileInputStream(LIBRARY_FILE));
            library = (ArrayList<Song>) ois.readObject();
            ois.close();
            launchView();
            updateLibrary();
        } catch (Exception ex) {
            launchView();
            // Failed to load dirs or library file, so prompt to enter library
            // dirs and thus regenerate library.
            view.showMusicFoldersDialog();
        }

        getView().updatePlaylistIfViewingLibrary();
    }

    /**
     * General shutdown tasks, like saving the dirs and library to disk.
     */
    @Override
    protected void shutdown() {
        try {
            getContext().getSessionStorage().save(getMainFrame(), "./musicman-session.xml");

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LIBRARY_FILE));
            oos.writeObject(library);
            oos.close();
            oos = new ObjectOutputStream(new FileOutputStream(DIRECTORIES_FILE));
            oos.writeObject(libraryDirs);
            oos.close();
        } catch (Exception ex) {
            // Failed to save library.
            ex.printStackTrace();
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of MusicManApp
     */
    public static MusicManApp getApplication() {
        return Application.getInstance(MusicManApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(MusicManApp.class, args);
    }

    /**
     * Gets the whole library.
     * @return the library.
     */
    public ArrayList<Song> getLibrary() {
        return library;
    }

    /**
     * Gets the current playlist.
     * @return the current playlist
     */
    public ArrayList<Song> getCurrentPlaylist() {
        return currentPlaylist;
    }

    /**
     * Convenience function to copy the entire library into currentPlaylist.
     */
    public void setCurrentPlaylistToLibrary() {
        currentPlaylist = (ArrayList<Song>) library.clone();
    }

    /**
     * Performs a set of actions whenever the library has been updated.  Currently
     * this updates the on-screen table if the user is viewing the library (so it
     * appears quickly on initial load), then fires the ID3 tag updating task.
     * When this task completes, it will trigger another playlist update if the
     * user is still viewing the library.
     */
    public void libraryUpdated() {
        getView().updatePlaylistIfViewingLibrary();
        updateAllTags();
    }

    /**
     * Spawns a task to update the library, bringing the local copy up to date
     * with the music files actually found in the checked locations.
     * @return a reference to the updater task (not used).
     */
    public Task updateLibrary() {
        UpdateLibrary task = new UpdateLibrary(this);
        ApplicationContext C = getApplication().getContext();
        TaskMonitor M = C.getTaskMonitor();
        TaskService S = C.getTaskService();
        S.execute(task);
        M.setForegroundTask(task);

        return task;
    }

    /**
     * Spawns a task to update tags for all items in the database.  This might
     * still be horrifically slow, or that slowness might just have been due
     * to updating the table on every tagging (!).  If this is still too slow,
     * it will be deprecated and the on-access updateOneTag() calls will have
     * to do.
     * @return a reference to the updater task (not used).
     */
    public Task updateAllTags() {
        UpdateAllTags task = new UpdateAllTags(this);
        ApplicationContext C = getApplication().getContext();
        TaskMonitor M = C.getTaskMonitor();
        TaskService S = C.getTaskService();
        S.execute(task);
        M.setForegroundTask(task);

        return task;
    }

    /**
     * Updates the ID3 tag information for a certain track by spawning the
     * appropriate task.
     * @param id the sequential ID number in the current playlist of the track
     * to update tag information on.
     * @return a reference to the updater task (not used).
     */
    public Task updateOneTag(int id) {
        UpdateOneTag task = new UpdateOneTag(this, id);
        ApplicationContext C = getApplication().getContext();
        TaskMonitor M = C.getTaskMonitor();
        TaskService S = C.getTaskService();
        S.execute(task);
        M.setForegroundTask(task);

        return task;
    }

    /**
     * Applies a tag bundle to certain tracks, changing their ID3 tags.
     * @param ids the sequential ID numbers in the current playlist of the tracks
     * to change tag information on.
     * @param bundle The tag bundle to apply.
     * @return a reference to the tagger task (not used).
     */
    public Task retagFiles(int[] ids, TagUpdateBundle bundle) {
        RetagFiles task = new RetagFiles(this, currentPlaylist, ids, bundle);
        ApplicationContext C = getApplication().getContext();
        TaskMonitor M = C.getTaskMonitor();
        TaskService S = C.getTaskService();
        S.execute(task);
        M.setForegroundTask(task);

        return task;
    }

    /**
     * Starts the task that will play a certain song.
     * @param id the sequential ID number in the current playlist of the track
     * to play.
     * @return a reference to the player task (not used).
     */
    public Task playSong(int id) {
        PlaySong task = new PlaySong(this, id);
        ApplicationContext C = getApplication().getContext();
        TaskMonitor M = C.getTaskMonitor();
        TaskService S = C.getTaskService();
        S.execute(task);
        M.setForegroundTask(task);
        playerTask = task;

        return task;
    }

    /**
     * Stops the music player task.
     */
    public void stopPlaying() {
        if (playerTask != null) {
            ((PlaySong) playerTask).stopPlaying();
        }
    }

    /**
     * Updates the list of library directories, then does a full reload from
     * scratch of the library.
     * @param newDirs The new set of directories to use.
     */
    public void updateDirs(ArrayList<File> newDirs) {
        libraryDirs = newDirs;
        updateLibrary();
    }

    /**
     * Gets the list of library directories
     * @return
     */
    public ArrayList<File> getLibraryDirs() {
        return libraryDirs;
    }

    /**
     * Updates the ID3 tag data of a certain song, and pushes the data to the
     * panel (table display and "now playing" bar.
     * @param id the sequential ID number in the current playlist of the track
     * to update.
     */
    public void updateSongAndTableWithTags(int id) {
        currentPlaylist.get(id).updateWithTags();
        getView().updateTableRow(id);
        getView().updateNowPlayingText();
    }

    /**
     * Creates the View and loads session storage parameters.
     */
    private void launchView() {
        view = new MusicManView(this);
        ApplicationContext appContext = getContext();
        try {
            appContext.getSessionStorage().restore(view.getFrame(), "./musicman-session.xml");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        show(getView());
    }

    /**
     * Gets the main frame.
     * @return the view
     */
    public MusicManView getView() {
        return view;
    }
}
