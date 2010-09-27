package smed.plug.ifc;

import javax.swing.JComponent;


public interface SmedPluggable {

    boolean start();
    boolean stop();
    String getName();
    String getFileName();
    String getInfo();
    JComponent getComponent();
    
    void setPluginManager(SmedPluginManager manager);

}

