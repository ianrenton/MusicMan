/*
 * MusicManTreeCellRenderer.java
 * Part of the MusicMan application by Ian Renton.  For information, please visit:
 * http://www.onlydreaming.net/software/musicman
 * This code is licenced under the GNU GPL v3 (http://www.gnu.org/licenses/).
 */
package net.onlydreaming.musicman.renderers;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.jdesktop.application.ResourceMap;

/**
 *
 */
public class MusicManTreeCellRenderer extends DefaultTreeCellRenderer {
    private final ResourceMap resourceMap;

    public MusicManTreeCellRenderer(ResourceMap resourceMap) {
            this.resourceMap = resourceMap;
            resourceMap.getIcon("playPause.icon");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (isNamed(value, "Library")) {
            setIcon(resourceMap.getIcon("library.icon"));
        } else if (isNamed(value, "Playlists")) {
            setIcon(resourceMap.getIcon("folder.icon"));
        } else if (isNamed(value, "Shoutcast Radio")) {
            setIcon(resourceMap.getIcon("shoutcast.icon"));
        } else if (leaf) {
            setIcon(resourceMap.getIcon("playlist.icon"));
        }

        return this;
    }

    public static boolean isNamed(Object value, String name) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        String title = (String) node.getUserObject();
        return (title.equals(name));
    }

}
