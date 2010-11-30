/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SettingsDialog.java
 *
 * Created on 30-Oct-2009, 13:31:23
 */

package net.onlydreaming.musicman.dialogs;

/**
 *
 * @author Ian
 */
public class SettingsDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form SettingsDialog */
    public SettingsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        getRootPane().setDefaultButton(okButton);
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scanLibraryOnStartup = new javax.swing.JCheckBox();
        checkForUpdates = new javax.swing.JCheckBox();
        scrobble = new javax.swing.JCheckBox();
        lastfmUser = new javax.swing.JTextField();
        lastfmPass = new javax.swing.JPasswordField();
        lastfmUserLabel = new javax.swing.JLabel();
        lastfmPassLabel = new javax.swing.JLabel();

        setName("SettingsDialog"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(net.onlydreaming.musicman.MusicManApp.class).getContext().getResourceMap(SettingsDialog.class);
        okButton.setText(resourceMap.getString("okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        scanLibraryOnStartup.setSelected(true);
        scanLibraryOnStartup.setText(resourceMap.getString("scanLibraryOnStartup.text")); // NOI18N
        scanLibraryOnStartup.setEnabled(false);
        scanLibraryOnStartup.setName("scanLibraryOnStartup"); // NOI18N

        checkForUpdates.setText(resourceMap.getString("checkForUpdates.text")); // NOI18N
        checkForUpdates.setEnabled(false);
        checkForUpdates.setName("checkForUpdates"); // NOI18N

        scrobble.setText(resourceMap.getString("scrobble.text")); // NOI18N
        scrobble.setEnabled(false);
        scrobble.setName("scrobble"); // NOI18N

        lastfmUser.setText(resourceMap.getString("lastfmUser.text")); // NOI18N
        lastfmUser.setEnabled(false);
        lastfmUser.setName("lastfmUser"); // NOI18N

        lastfmPass.setText(resourceMap.getString("lastfmPass.text")); // NOI18N
        lastfmPass.setEnabled(false);
        lastfmPass.setName("lastfmPass"); // NOI18N

        lastfmUserLabel.setText(resourceMap.getString("lastfmUserLabel.text")); // NOI18N
        lastfmUserLabel.setEnabled(false);
        lastfmUserLabel.setName("lastfmUserLabel"); // NOI18N

        lastfmPassLabel.setText(resourceMap.getString("lastfmPassLabel.text")); // NOI18N
        lastfmPassLabel.setEnabled(false);
        lastfmPassLabel.setName("lastfmPassLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(162, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scanLibraryOnStartup)
                .addContainerGap(101, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkForUpdates)
                .addContainerGap(143, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrobble)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lastfmUser, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lastfmUserLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lastfmPassLabel)
                            .addComponent(lastfmPass, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scanLibraryOnStartup)
                .addGap(18, 18, 18)
                .addComponent(checkForUpdates)
                .addGap(18, 18, 18)
                .addComponent(scrobble)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastfmUserLabel)
                    .addComponent(lastfmPassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastfmUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastfmPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox checkForUpdates;
    private javax.swing.JPasswordField lastfmPass;
    private javax.swing.JLabel lastfmPassLabel;
    private javax.swing.JTextField lastfmUser;
    private javax.swing.JLabel lastfmUserLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox scanLibraryOnStartup;
    private javax.swing.JCheckBox scrobble;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;
}