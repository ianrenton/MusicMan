/*
 * Song.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.objects;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;

/**
 * A song.
 */
public class Song implements Comparable<Song>, Serializable {

    private File path;
    private String artist = "";
    private String album = "";
    private String title = "";
    private String track = "";
    private String genre = "";
    private String time = "0:00";
    private boolean tagged = false;
    private boolean missing = false;
    private String searchMatcher = "";  // Lowercase mash of artist, album and
    // title (we match search strings against
    // this.

    /**
     * Makes a Song object from a given path to a file.  Assumes the path is
     * something like Artist/Album/Title, and sets the internal artist/album/
     * title tags based on the directory structure.  ID3 reading is slow, so
     * doesn't happen in the constructor.
     * @param path the path to the MP3 file.
     * @throws IOException if the file couldn't be found.
     */
    public Song(File path) throws IOException {
        this.path = path;
        String splitChar = (File.separator.equals("\\")) ? "\\\\" : File.separator;
        String[] pathBits = path.getPath().split(splitChar);
        title = pathBits[pathBits.length - 1].substring(0, pathBits[pathBits.length - 1].length() - 4);
        album = pathBits[pathBits.length - 2];
        artist = pathBits[pathBits.length - 3];
        //updateWithTags();
        regenLowercaseFields();
    }

    /**
     * Updates the internal artist/album/title fields based on the ID3 tag of
     * the file.
     */
    public void updateWithTags() {
        try {
            AudioFile f = AudioFileIO.read(path);
            Tag tag = f.getTag();
            AudioHeader audioHeader = f.getAudioHeader();
            artist = tag.getFirstArtist();
            album = tag.getFirstAlbum();
            title = tag.getFirstTitle();
            track = tag.getFirstTrack();
            genre = tag.getFirstGenre();
            int timeSec = audioHeader.getTrackLength();
            int timeMin = (int) Math.floor(timeSec / 60.0);
            timeSec = timeSec % 60;
            time = String.format("%d", timeMin) + ":" + String.format("%02d", timeSec);

            /*AudioFileFormat baseFileFormat = null;
            AudioFormat baseFormat = null;
            baseFileFormat = AudioSystem.getAudioFileFormat(path);
            baseFormat = baseFileFormat.getFormat();
            // TAudioFileFormat properties
            if (baseFileFormat instanceof TAudioFileFormat) {
            Map properties = ((TAudioFileFormat) baseFileFormat).properties();
            artist = (String) properties.get("author");
            album = (String) properties.get("album");
            title = (String) properties.get("title");
            genre = (String) properties.get("mp3.id3tag.genre");
            Long timeUS = (Long) properties.get("duration");
            int timeSec = (int) (timeUS / 1000000);
            int timeMin = (int) Math.floor(timeSec / 60.0);
            timeSec = timeSec % 60;
            time = String.format("%d", timeMin) + ":" + String.format("%02d", timeSec);
            }*/
            regenLowercaseFields();
            tagged = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Changes the tags of a file.
     * @param bundle the bundle containing new tag data.
     */
    public void retag(TagUpdateBundle bundle) {
        if (bundle.isWriteArtist()) {
            artist = bundle.getArtist();
        }
        if (bundle.isWriteAlbum()) {
            album = bundle.getAlbum();
        }
        if (bundle.isWriteTrack()) {
            track = bundle.getTrack();
        }
        if (bundle.isWriteTitle()) {
            title = bundle.getTitle();
        }
        if (bundle.isWriteGenre()) {
            genre = bundle.getGenre();
        }
        AudioFile f;
        try {
            f = AudioFileIO.read(path);
            Tag tag = f.getTag();
            tag.setArtist(artist);
            tag.setAlbum(album);
            tag.setTrack(track);
            tag.setTitle(title);
            tag.setGenre(genre);
            f.setTag(tag);
            f.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        regenLowercaseFields();
    }

    /**
     * Create lower-case versions of the main fields.  This is so that we don't
     * have to String.toLowerCase() these fields *every* time we do a search and
     * need to compare them to a lowercased search string.
     */
    private void regenLowercaseFields() {
        try {
            searchMatcher = artist.toLowerCase() + " " + album.toLowerCase() + " " + title.toLowerCase();
        } catch (NullPointerException ex) {
            //This happens every now and again.  So the track won't be
            //searchable, meh.
        }
    }

    /**
     * Gets the file path.
     * @return the file path.
     */
    public File getPath() {
        return path;
    }

    /**
     * Creates an array of strings of the main song attributes, for displaying
     * int the main table.
     * @return an array of the main attributes.
     */
    public String[] formatForTable() {
        return new String[]{getArtist(), getAlbum(), getTrack(), getTitle(), getGenre(), getTime()};
    }

    /**
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @return the album
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @return the track number
     */
    public String getTrack() {
        return track;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * Compare alphabetically to another song, prioritising Artist, then Album,
     * then Title.  This is the default sort in the table, though the user can
     * sort however they like.
     * @param other the other song to compare to.
     * @return 1 if alphabetically greater, -1 if lesser, 0 if identical in all
     * three fields.
     */
    public int compareTo(Song other) {
        try {
            int artistCompare = this.getArtist().compareTo(other.getArtist());
            if (artistCompare != 0) {
                return artistCompare;
            } else {
                int albumCompare = this.getAlbum().compareTo(other.getAlbum());
                if (albumCompare != 0) {
                    return albumCompare;
                } else {
                    int trackCompare = this.getTrack().compareTo(other.getTrack());
                    if (trackCompare != 0) {
                        return trackCompare;
                    } else {
                        int titleCompare = this.getTitle().compareTo(other.getTitle());
                        return titleCompare;
                    }
                }
            }
        } catch (NullPointerException ex) {
            // For some reason we get null pointers very occasionally. Let's
            // return a zero, and suffer the slightly wonky sorting.
            return 0;
        }
    }

    /**
     * @return whether or not the Song's attributes are already based on actual
     * ID3 tags rather than just the filename, so we don't need to bother re-
     * loading tags.
     */
    public boolean isTagged() {
        return tagged;
    }

    /**
     * @return if the file has been checked and found not to actually exist on
     * disk.
     */
    public boolean isMissing() {
        return missing;
    }

    /**
     * Sets the "missing" parameter, effectively marking it for deletion from
     * the library.
     * @param missing
     */
    public void setMissing(boolean missing) {
        this.missing = missing;
    }

    /**
     * Checks to see if the given search string can be found in this song's
     * artist, album or title fields (or some combination).
     * @param s the LOWERCASE string to check for.
     * @return whether a match was found or not.
     */
    public boolean contains(CharSequence s) {
        return searchMatcher.contains(s);
    }
}
