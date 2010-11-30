/*
 * TagUpdateBundle.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.objects;

/**
 *
 * @author tsuki
 */
public class TagUpdateBundle {
    private String artist;
    private String album;
    private String title;
    private String track;
    private String genre;
    private boolean writeArtist;
    private boolean writeAlbum;
    private boolean writeTitle;
    private boolean writeTrack;
    private boolean writeGenre;

    public TagUpdateBundle(String artist, String album, String title, String track, String genre, boolean writeArtist, boolean writeAlbum, boolean writeTitle, boolean writeTrack, boolean writeGenre) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.track = track;
        this.genre = genre;
        this.writeArtist = writeArtist;
        this.writeAlbum = writeAlbum;
        this.writeTitle = writeTitle;
        this.writeTrack = writeTrack;
        this.writeGenre = writeGenre;
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
     * @return the track
     */
    public String getTrack() {
        return track;
    }

    /**
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @return the writeArtist
     */
    public boolean isWriteArtist() {
        return writeArtist;
    }

    /**
     * @return the writeAlbum
     */
    public boolean isWriteAlbum() {
        return writeAlbum;
    }

    /**
     * @return the writeTitle
     */
    public boolean isWriteTitle() {
        return writeTitle;
    }

    /**
     * @return the writeTrack
     */
    public boolean isWriteTrack() {
        return writeTrack;
    }

    /**
     * @return the writeGenre
     */
    public boolean isWriteGenre() {
        return writeGenre;
    }
}
