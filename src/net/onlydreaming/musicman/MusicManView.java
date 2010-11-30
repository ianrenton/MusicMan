/*
 * MusicManView.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman;

import net.onlydreaming.musicman.dialogs.MusicManAboutBox;
import net.onlydreaming.musicman.dialogs.NewRadioDialog;
import net.onlydreaming.musicman.dialogs.NewPlaylistDialog;
import net.onlydreaming.musicman.dialogs.SettingsDialog;
import net.onlydreaming.musicman.dialogs.MusicFoldersDialog;
import net.onlydreaming.musicman.renderers.MusicManTreeCellRenderer;
import java.util.ArrayList;
import net.onlydreaming.musicman.objects.Song;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Position.Bias;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.onlydreaming.musicman.dialogs.TaggingDialog;

/**
 * The application's main frame.
 */
public class MusicManView extends FrameView {

    MusicManApp app;
    private int itemNowPlaying = 0;
    private boolean playing = false;
    Random random = new Random();
    private final TableRowSorter sorter;

    public MusicManView(MusicManApp app) {
        super(app);
        this.app = app;

        // Mac OS stuff
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MusicMan");

        // JAudioTagger logging off
        System.setProperty("org.jaudiotagger.level", "OFF");

        // Big init functions
        initComponents();
        initStatusBar();
        initKeyboardShortcuts();
        initTreeIcons();

        // Table sorter
        sorter = new TableRowSorter<DefaultTableModel>(((DefaultTableModel) table.getModel()));
        table.setRowSorter(sorter);

        // Select the default things
        tree.setSelectionRow(0);
        if (app.getCurrentPlaylist().size() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
        playPause.requestFocusInWindow();

    }

    /**
     * Shows the "About" box.
     */
    @Action
    public void showAboutBox() {
        JFrame mainFrame = MusicManApp.getApplication().getMainFrame();
        aboutBox = new MusicManAboutBox(mainFrame);
        aboutBox.setLocationRelativeTo(mainFrame);
        MusicManApp.getApplication().show(aboutBox);
    }

    /**
     * Shows the Settings dialog.
     */
    @Action
    public void showSettingsDialog() {
        if (settingsDialog == null) {
            JFrame mainFrame = MusicManApp.getApplication().getMainFrame();
            settingsDialog = new SettingsDialog(mainFrame, false);
            settingsDialog.setLocationRelativeTo(mainFrame);
        }
        MusicManApp.getApplication().show(settingsDialog);
    }

    /**
     * Shows the Tagging dialog.
     */
    @Action
    public void showTaggingDialog() {
        if (table.getSelectedRowCount() > 0) {
            JFrame mainFrame = MusicManApp.getApplication().getMainFrame();
            taggingDialog = new TaggingDialog(mainFrame, true, (String) ((DefaultTableModel) table.getModel()).getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0),
                    (String) ((DefaultTableModel) table.getModel()).getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1),
                    (String) ((DefaultTableModel) table.getModel()).getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 2),
                    (String) ((DefaultTableModel) table.getModel()).getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 3),
                    (String) ((DefaultTableModel) table.getModel()).getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 4),
                    (table.getSelectedRowCount() == 1));
            taggingDialog.setLocationRelativeTo(mainFrame);
            MusicManApp.getApplication().show(taggingDialog);
            if (taggingDialog.getReturnStatus() == NewPlaylistDialog.RET_OK) {
                int[] realRowsSelected = new int[table.getSelectedRowCount()];
                for (int i = 0; i < table.getSelectedRowCount(); i++) {
                    realRowsSelected[i] = table.convertRowIndexToModel(table.getSelectedRows()[i]);
                }
                app.retagFiles(realRowsSelected, taggingDialog.getUpdateBundle());
            }
        } else {
            JOptionPane.showMessageDialog(this.getFrame(), "Please select one or more songs first.", "Tagging", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /**
     * Shows the Music Folders dialog.
     */
    @Action
    public void showMusicFoldersDialog() {
        if (musicFoldersDialog == null) {
            JFrame mainFrame = MusicManApp.getApplication().getMainFrame();
            musicFoldersDialog = new MusicFoldersDialog(mainFrame, app, app.getLibraryDirs());
            musicFoldersDialog.setLocationRelativeTo(mainFrame);
        }
        MusicManApp.getApplication().show(musicFoldersDialog);
    }

    /**
     * Adds a playlist.
     */
    @Action
    public void addPlaylist() {
        JFrame mainFrame = MusicManApp.getApplication().getMainFrame();
        newPlaylistDialog = new NewPlaylistDialog(mainFrame, true);
        newPlaylistDialog.setLocationRelativeTo(mainFrame);
        MusicManApp.getApplication().show(newPlaylistDialog);
        if (newPlaylistDialog.getReturnStatus() == NewPlaylistDialog.RET_OK) {
            TreePath path = tree.getNextMatch("Playlists", 0, Bias.Forward);
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newPlaylistDialog.getNameEntered(), false);
            model.insertNodeInto(newNode, node, node.getChildCount());
            tree.setSelectionPath(getTreePathFromNode(newNode));
        }
    }

    /**
     * Adds a radio station.
     */
    @Action
    public void addRadio() {
        JFrame mainFrame = MusicManApp.getApplication().getMainFrame();
        newRadioDialog = new NewRadioDialog(mainFrame, true);
        newRadioDialog.setLocationRelativeTo(mainFrame);
        MusicManApp.getApplication().show(newRadioDialog);
        if (newRadioDialog.getReturnStatus() == NewRadioDialog.RET_OK) {
            TreePath path = tree.getNextMatch("Shoutcast Radio", 0, Bias.Forward);
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newRadioDialog.getNameEntered(), false);
            model.insertNodeInto(newNode, node, node.getChildCount());
            tree.setSelectionPath(getTreePathFromNode(newNode));
        }
    }

    /**
     * Deletes a playlist or radio station.
     */
    @Action
    public void deletePlaylistItem() {
        int selectedRow = tree.getSelectionRows()[0];
        TreePath selectedPath = tree.getSelectionPath();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        MutableTreeNode node = (MutableTreeNode) selectedPath.getLastPathComponent();
        if (MusicManTreeCellRenderer.isNamed(node.getParent(), "Playlists") || MusicManTreeCellRenderer.isNamed(node.getParent(), "Shoutcast Radio")) {
            model.removeNodeFromParent(node);
            tree.setSelectionRow(selectedRow - 1);
        }
    }

    /**
     * Skips to the next track.
     */
    @Action
    public void next() {
        app.stopPlaying();
        if (shuffle.isSelected()) {
            itemNowPlaying = random.nextInt(table.getRowCount());
        } else {
            try {
                itemNowPlaying = table.convertRowIndexToModel(table.convertRowIndexToView(itemNowPlaying) + 1);
            } catch (IndexOutOfBoundsException ex) {
                // Off the end of the table.
                itemNowPlaying = table.convertRowIndexToModel(0);
                if (!repeat.isSelected()) {
                    playPause(); // stop playing
                }
            }
        }
        updateNowPlayingText();
        app.updateOneTag(itemNowPlaying);
        table.setRowSelectionInterval(table.convertRowIndexToView(itemNowPlaying), table.convertRowIndexToView(itemNowPlaying));
        if (playing) {
            try {
                app.playSong(itemNowPlaying);
            } catch (Exception ex) {
                next();
            }
        }
    }

    /**
     * Skips to the previous track.
     */
    @Action
    public void previous() {
        app.stopPlaying();
        try {
            itemNowPlaying = table.convertRowIndexToModel(table.convertRowIndexToView(itemNowPlaying) - 1);
        } catch (IndexOutOfBoundsException ex) {
            // Off the beginning of the table, loop back to the end
            itemNowPlaying = table.convertRowIndexToModel(table.getRowCount());
        }
        updateNowPlayingText();
        app.updateOneTag(itemNowPlaying);
        table.setRowSelectionInterval(itemNowPlaying, itemNowPlaying);
        if (playing) {
            try {
                app.playSong(itemNowPlaying);
            } catch (Exception ex) {
                previous();
            }
        }
    }

    /**
     * Plays if paused, and pauses if playing.
     */
    @Action
    public void playPause() {
        if (playing) {
            playing = false;
            ResourceMap resourceMap = getResourceMap();
            playPause.setIcon(resourceMap.getIcon("playPause.icon"));
            app.stopPlaying();
        } else {
            try {
                playing = true;
                ResourceMap resourceMap = getResourceMap();
                playPause.setIcon(resourceMap.getIcon("pause.icon"));
                app.playSong(itemNowPlaying);
            } catch (Exception ex) {
                next();
            }
        }
        updateNowPlayingText();
        app.updateSongAndTableWithTags(itemNowPlaying);
    }

    /**
     * If paused, takes the highlighted row and pushes its details up to the
     * Now Playing bar, so the user knows what'll start playing if they click
     * Play.
     */
    public void updateNowPlayingIfPaused() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        app.updateOneTag(row);
        if (!playing) {
            itemNowPlaying = row;
            updateNowPlayingText();
        }
    }

    /**
     * Immediately begins playing the selected item, stopping any playing that's
     * currently in progress.
     */
    public void playSelectedItem() {
        app.stopPlaying();
        itemNowPlaying = table.convertRowIndexToModel(table.getSelectedRow());
        updateNowPlayingText();
        try {
            app.playSong(itemNowPlaying);
            playing = true;
            ResourceMap resourceMap = getResourceMap();
            playPause.setIcon(resourceMap.getIcon("pause.icon"));
        } catch (Exception ex) {
            next();
        }
    }

    /**
     * Blanks the main table, then fills it with song information from a certain
     * playlist.  Sets the current song to the first one in the new list,
     * assuming a song isn't already playing, otherwise it keeps playing the
     * current song.
     * @param playlist The playlist to fill the table with.
     */
    public void fillTable(ArrayList<Song> playlist) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
        for (Song song : playlist) {
            ((DefaultTableModel) table.getModel()).addRow(song.formatForTable());
        }
        itemNowPlaying = 0;
        if (playlist.size() > 0) {
            app.updateOneTag(0);
            if (!playing) {
                updateNowPlayingText();
            }
        }
    }

    /**
     * If the user has "Library" selected in the playlist tree, sets the current
     * playlist to the whole library and fills the table with the whole library.
     */
    public void updatePlaylistIfViewingLibrary() {
        if (tree.getSelectionRows()[0] == 0) {
            app.setCurrentPlaylistToLibrary();
            fillTable(app.getCurrentPlaylist());
        }
    }

    /**
     * Updates the "now playing" text at the top of the window based on the
     * currently-playing song.  If a song is selected but not yet playing, its
     * tags will still be displayed so the user knows what'll play when they
     * click Play.  Only if the playlist is completely empty will it show "not
     * playing".
     */
    protected void updateNowPlayingText() {
        if (app.getCurrentPlaylist().size() > 0) {
            String artist = app.getCurrentPlaylist().get(itemNowPlaying).getArtist();
            String album = app.getCurrentPlaylist().get(itemNowPlaying).getAlbum();
            String title = app.getCurrentPlaylist().get(itemNowPlaying).getTitle();
            artist = artist.substring(0, Math.min(50, artist.length()));
            album = album.substring(0, Math.min(50, album.length()));
            title = title.substring(0, Math.min(60, title.length()));
            nowPlaying.setText("<html>" + artist + "&nbsp;&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;&nbsp;" + album + "<br><b>" + title + "</b></html>");
        } else {
            nowPlaying.setText("Not Playing");
        }
    }

    /**
     * Updates the contents of a single row of the table model based on the song's
     * (presumably changed) tag information.
     * @param row the row of the table to update.
     */
    void updateTableRow(int row) {
        ((DefaultTableModel) table.getModel()).setValueAt(app.getCurrentPlaylist().get(row).getArtist(), row, 0);
        ((DefaultTableModel) table.getModel()).setValueAt(app.getCurrentPlaylist().get(row).getAlbum(), row, 1);
        ((DefaultTableModel) table.getModel()).setValueAt(app.getCurrentPlaylist().get(row).getTrack(), row, 2);
        ((DefaultTableModel) table.getModel()).setValueAt(app.getCurrentPlaylist().get(row).getTitle(), row, 3);
        ((DefaultTableModel) table.getModel()).setValueAt(app.getCurrentPlaylist().get(row).getGenre(), row, 4);
        ((DefaultTableModel) table.getModel()).setValueAt(app.getCurrentPlaylist().get(row).getTime(), row, 5);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        leftButtonBar = new javax.swing.JPanel();
        addPlaylist = new javax.swing.JButton();
        addRadio = new javax.swing.JButton();
        delPlaylist = new javax.swing.JButton();
        rightPanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        rightButtonBar = new javax.swing.JPanel();
        addToPlaylist = new javax.swing.JButton();
        love = new javax.swing.JButton();
        tag = new javax.swing.JButton();
        playerPanel = new javax.swing.JPanel();
        previous = new javax.swing.JButton();
        playPause = new javax.swing.JButton();
        next = new javax.swing.JButton();
        nowPlaying = new javax.swing.JLabel();
        shuffle = new javax.swing.JCheckBox();
        repeat = new javax.swing.JCheckBox();
        search = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        setMusicFoldersItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(140);
        splitPane.setName("splitPane"); // NOI18N

        leftPanel.setName("leftPanel"); // NOI18N
        leftPanel.setLayout(new java.awt.BorderLayout());

        treeScrollPane.setName("treeScrollPane"); // NOI18N
        treeScrollPane.setPreferredSize(new java.awt.Dimension(150, 322));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Library");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Playlists");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Iron Maiden");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Metallica");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Shoutcast Radio");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Gotham Radio");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        tree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        tree.setName("tree"); // NOI18N
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeValueChanged(evt);
            }
        });
        treeScrollPane.setViewportView(tree);

        leftPanel.add(treeScrollPane, java.awt.BorderLayout.CENTER);

        leftButtonBar.setName("leftButtonBar"); // NOI18N
        leftButtonBar.setLayout(new java.awt.GridLayout(1, 0));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(net.onlydreaming.musicman.MusicManApp.class).getContext().getActionMap(MusicManView.class, this);
        addPlaylist.setAction(actionMap.get("addPlaylist")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(net.onlydreaming.musicman.MusicManApp.class).getContext().getResourceMap(MusicManView.class);
        addPlaylist.setIcon(resourceMap.getIcon("addPlaylist.icon")); // NOI18N
        addPlaylist.setText(resourceMap.getString("addPlaylist.text")); // NOI18N
        addPlaylist.setToolTipText(resourceMap.getString("addPlaylist.toolTipText")); // NOI18N
        addPlaylist.setName("addPlaylist"); // NOI18N
        leftButtonBar.add(addPlaylist);

        addRadio.setAction(actionMap.get("addRadio")); // NOI18N
        addRadio.setIcon(resourceMap.getIcon("addRadio.icon")); // NOI18N
        addRadio.setToolTipText(resourceMap.getString("addRadio.toolTipText")); // NOI18N
        addRadio.setName("addRadio"); // NOI18N
        leftButtonBar.add(addRadio);

        delPlaylist.setAction(actionMap.get("deletePlaylistItem")); // NOI18N
        delPlaylist.setIcon(resourceMap.getIcon("delPlaylist.icon")); // NOI18N
        delPlaylist.setText(resourceMap.getString("delPlaylist.text")); // NOI18N
        delPlaylist.setToolTipText(resourceMap.getString("delPlaylist.toolTipText")); // NOI18N
        delPlaylist.setName("delPlaylist"); // NOI18N
        leftButtonBar.add(delPlaylist);

        leftPanel.add(leftButtonBar, java.awt.BorderLayout.PAGE_END);

        splitPane.setLeftComponent(leftPanel);

        rightPanel.setName("rightPanel"); // NOI18N
        rightPanel.setLayout(new java.awt.BorderLayout());

        tableScrollPane.setName("tableScrollPane"); // NOI18N

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Artist", "Album", "#", "Title", "Genre", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        table.setName("table"); // NOI18N
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setShowVerticalLines(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableKeyReleased(evt);
            }
        });
        tableScrollPane.setViewportView(table);
        table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("table.columnModel.title0")); // NOI18N
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("table.columnModel.title1")); // NOI18N
        table.getColumnModel().getColumn(2).setPreferredWidth(1);
        table.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("table.columnModel.title5")); // NOI18N
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("table.columnModel.title2")); // NOI18N
        table.getColumnModel().getColumn(4).setPreferredWidth(30);
        table.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("table.columnModel.title3")); // NOI18N
        table.getColumnModel().getColumn(5).setPreferredWidth(1);
        table.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("table.columnModel.title4")); // NOI18N

        rightPanel.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        rightButtonBar.setName("rightButtonBar"); // NOI18N
        rightButtonBar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        addToPlaylist.setIcon(resourceMap.getIcon("addToPlaylist.icon")); // NOI18N
        addToPlaylist.setText(resourceMap.getString("addToPlaylist.text")); // NOI18N
        addToPlaylist.setToolTipText(resourceMap.getString("addToPlaylist.toolTipText")); // NOI18N
        addToPlaylist.setEnabled(false);
        addToPlaylist.setName("addToPlaylist"); // NOI18N
        rightButtonBar.add(addToPlaylist);

        love.setIcon(resourceMap.getIcon("love.icon")); // NOI18N
        love.setText(resourceMap.getString("love.text")); // NOI18N
        love.setToolTipText(resourceMap.getString("love.toolTipText")); // NOI18N
        love.setEnabled(false);
        love.setName("love"); // NOI18N
        rightButtonBar.add(love);

        tag.setAction(actionMap.get("showTaggingDialog")); // NOI18N
        tag.setIcon(resourceMap.getIcon("tag.icon")); // NOI18N
        tag.setToolTipText(resourceMap.getString("tag.toolTipText")); // NOI18N
        tag.setName("tag"); // NOI18N
        rightButtonBar.add(tag);

        rightPanel.add(rightButtonBar, java.awt.BorderLayout.PAGE_END);

        splitPane.setRightComponent(rightPanel);

        mainPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        playerPanel.setName("playerPanel"); // NOI18N
        playerPanel.setLayout(new java.awt.GridBagLayout());

        previous.setAction(actionMap.get("previous")); // NOI18N
        previous.setIcon(resourceMap.getIcon("previous.icon")); // NOI18N
        previous.setText(resourceMap.getString("previous.text")); // NOI18N
        previous.setToolTipText(resourceMap.getString("previous.toolTipText")); // NOI18N
        previous.setName("previous"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        playerPanel.add(previous, gridBagConstraints);

        playPause.setAction(actionMap.get("playPause")); // NOI18N
        playPause.setIcon(resourceMap.getIcon("playPause.icon")); // NOI18N
        playPause.setText(resourceMap.getString("playPause.text")); // NOI18N
        playPause.setToolTipText(resourceMap.getString("playPause.toolTipText")); // NOI18N
        playPause.setName("playPause"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        playerPanel.add(playPause, gridBagConstraints);

        next.setAction(actionMap.get("next")); // NOI18N
        next.setIcon(resourceMap.getIcon("next.icon")); // NOI18N
        next.setText(resourceMap.getString("next.text")); // NOI18N
        next.setToolTipText(resourceMap.getString("next.toolTipText")); // NOI18N
        next.setName("next"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        playerPanel.add(next, gridBagConstraints);

        nowPlaying.setText(resourceMap.getString("nowPlaying.text")); // NOI18N
        nowPlaying.setName("nowPlaying"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        playerPanel.add(nowPlaying, gridBagConstraints);

        shuffle.setText(resourceMap.getString("shuffle.text")); // NOI18N
        shuffle.setToolTipText(resourceMap.getString("shuffle.toolTipText")); // NOI18N
        shuffle.setName("shuffle"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        playerPanel.add(shuffle, gridBagConstraints);

        repeat.setSelected(true);
        repeat.setText(resourceMap.getString("repeat.text")); // NOI18N
        repeat.setToolTipText(resourceMap.getString("repeat.toolTipText")); // NOI18N
        repeat.setName("repeat"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        playerPanel.add(repeat, gridBagConstraints);

        search.setText(resourceMap.getString("search.text")); // NOI18N
        search.setToolTipText(resourceMap.getString("search.toolTipText")); // NOI18N
        search.setFocusCycleRoot(true);
        search.setName("search"); // NOI18N
        search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 2);
        playerPanel.add(search, gridBagConstraints);

        mainPanel.add(playerPanel, java.awt.BorderLayout.NORTH);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        setMusicFoldersItem.setAction(actionMap.get("showMusicFoldersDialog")); // NOI18N
        setMusicFoldersItem.setText(resourceMap.getString("setMusicFoldersItem.text")); // NOI18N
        setMusicFoldersItem.setName("setMusicFoldersItem"); // NOI18N
        fileMenu.add(setMusicFoldersItem);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        toolsMenu.setText(resourceMap.getString("toolsMenu.text")); // NOI18N
        toolsMenu.setName("toolsMenu"); // NOI18N

        settingsMenuItem.setAction(actionMap.get("showSettingsDialog")); // NOI18N
        settingsMenuItem.setText(resourceMap.getString("settingsMenuItem.text")); // NOI18N
        settingsMenuItem.setName("settingsMenuItem"); // NOI18N
        toolsMenu.add(settingsMenuItem);

        menuBar.add(toolsMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 585, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Updates the contents of the table based on which playlist is selected in
     * the tree.  Currently, this only supports the "library" pseudo-playlist.
     * @param evt
     */
    private void treeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeValueChanged
        TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath == null) {
            tree.setSelectionRow(0);
            updatePlaylistIfViewingLibrary();
        } else if (selectedPath.toString().equals("[root, Library]")) {
            updatePlaylistIfViewingLibrary();
        }
    }//GEN-LAST:event_treeValueChanged

    /**
     * Deals with mouse clicks on the table.  A single click does the same as
     * highlighting a row with the keyboard, so it just fires tableKeyReleased().
     * A double-click stops whatever is currently playing and starts playing the
     * clicked song.
     * @param evt
     */
    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        updateNowPlayingIfPaused();
        if (evt.getClickCount() > 1) {
            playSelectedItem();
        }
    }//GEN-LAST:event_tableMouseClicked

    /**
     * Filters the table rows when the contents of the search box are changed.
     * @param evt
     */
    private void searchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchKeyReleased
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            String s = search.getText();
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                sb.append('[').append(Character.toLowerCase(c)).append(Character.toUpperCase(c)).append(']');
            }
            rf = RowFilter.regexFilter(sb.toString(), 0, 1, 3, 4); // filter on Artist, Album, Title, Genre
        } catch (PatternSyntaxException ex) {
            return;
        }
        sorter.setRowFilter(rf);
    }//GEN-LAST:event_searchKeyReleased

    /**
     * Deals with items being selected in the table.  The mouse listener chains
     * into this, so it handles both mouse and keyboard selections.
     * Updates the tags of the highlighted item, and if not already playing a
     * different song, sets the highlighted song to be current.
     * @param evt
     */
    private void tableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyReleased
        updateNowPlayingIfPaused();
    }//GEN-LAST:event_tableKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPlaylist;
    private javax.swing.JButton addRadio;
    private javax.swing.JButton addToPlaylist;
    private javax.swing.JButton delPlaylist;
    private javax.swing.JPanel leftButtonBar;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JButton love;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton next;
    private javax.swing.JLabel nowPlaying;
    private javax.swing.JButton playPause;
    private javax.swing.JPanel playerPanel;
    private javax.swing.JButton previous;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBox repeat;
    private javax.swing.JPanel rightButtonBar;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JTextField search;
    private javax.swing.JMenuItem setMusicFoldersItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JCheckBox shuffle;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JButton tag;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
    private Timer messageTimer;
    private Timer busyIconTimer;
    private Icon idleIcon;
    private Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private MusicManAboutBox aboutBox;
    private MusicFoldersDialog musicFoldersDialog;
    private SettingsDialog settingsDialog;
    private NewPlaylistDialog newPlaylistDialog;
    private NewRadioDialog newRadioDialog;
    private TaggingDialog taggingDialog;

    private void initStatusBar() {
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    private void initKeyboardShortcuts() {
        // Set keyboard shortcuts
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl K"), "Focus Search Box");
        mainPanel.getActionMap().put("Focus Search Box", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                search.requestFocusInWindow();
            }
        });
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl R"), "Toggle Repeat");
        mainPanel.getActionMap().put("Toggle Repeat", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                repeat.setSelected(!repeat.isSelected());
            }
        });
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl S"), "Toggle Shuffle");
        mainPanel.getActionMap().put("Toggle Shuffle", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                shuffle.setSelected(!shuffle.isSelected());
            }
        });
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl P"), "Previous");
        mainPanel.getActionMap().put("Previous", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                previous();
            }
        });
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl N"), "Next");
        mainPanel.getActionMap().put("Next", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                next();
            }
        });
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl SPACE"), "Play Pause");
        mainPanel.getActionMap().put("Play Pause", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                playPause();
            }
        });
    }

    private void initTreeIcons() {
        tree.setCellRenderer(new MusicManTreeCellRenderer(getResourceMap()));
    }

    public static TreePath getTreePathFromNode(TreeNode node) {
        List list = new ArrayList();
        while (node != null) {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);
        return new TreePath(list.toArray());
    }
}
