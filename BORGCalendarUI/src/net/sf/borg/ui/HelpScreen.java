/*
This file is part of BORG.

    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Copyright 2003 by ==Quiet==
*/
/*
 * helpscrn.java
 *
 * Created on October 5, 2003, 8:55 AM
 */

package net.sf.borg.ui;

import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Version;

/**
 *
 * @author  mberger
 */
// helpscrn just displays an HTML page in a window
class HelpScreen extends View {
    
    static
    {
        Version.addVersion("$Id$");
    }

    /** Creates new form helpscrn */
    HelpScreen(String file) {
        initComponents();
        try {
            jEditorPane1.setPage(getClass().getResource(file));
        } catch (java.io.IOException e1) {
            e1.printStackTrace();
        }
        
        manageMySize(PrefName.HELPVIEWSIZE);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("BORG");
        this.setSize(165, 65);
        this.setContentPane(jScrollPane1);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jEditorPane1.setEditable(false);
        jEditorPane1.setPreferredSize(new java.awt.Dimension(700, 500));
        jScrollPane1.setViewportView(jEditorPane1);

        //getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm
    
 
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    /* (non-Javadoc)
     * @see net.sf.borg.ui.View#refresh()
     */
    public void refresh() {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see net.sf.borg.ui.View#destroy()
     */
    public void destroy() {
        this.dispose();       
    }
    
}  //  @jve:decl-index=0:visual-constraint="10,10"
