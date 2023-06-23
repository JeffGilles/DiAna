/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DiAna;

import ij.Macro;
import ij.WindowManager;
import ij.plugin.PlugIn;
import java.awt.Component;
import javax.swing.JOptionPane;


/**
 *
 * @author Jean-François Gilles
 */
public class Di_Ana implements PlugIn {
    

    @Override
    public void run(String arg) {
       this.checkForClassInstalled();
       Diana_SegmentGui dia = new Diana_SegmentGui();
       
       if (Macro.getOptions()==null){
           dia.setVisible(true);
        }else{
           dia.imA=WindowManager.getImage(dia.imgASelect);
            dia.imB=WindowManager.getImage(dia.imgBSelect);
           dia.macroInterpreter(Macro.getOptions());
        }
    }
    
    private void checkForClassInstalled() { //thanks to csbdresden
        try {
            Class.forName("mcib3d.image3d.ImageHandler");
        } catch (ClassNotFoundException var2) {
            JOptionPane.showMessageDialog((Component)null, "<html><p>DiAna 3C relies on the MCIB-Core plugin from <b>3D ImageJ Suite</b> as libraries."
                   + "<p>Please install it as below by enabling the update site.<br>Go to <i>Help > Update...</i>, then click on <i>Manage update sites</i></p>"
                   + "<br><br><img src='" + this.getClass().getResource("/3dsuite.png") + "' width='440' height='40'>"
                   + "</html>", "Required MCIB-Core plugin missing", 0);
            
            throw new RuntimeException("3DSuite not installed");
        }
    }
    
}
