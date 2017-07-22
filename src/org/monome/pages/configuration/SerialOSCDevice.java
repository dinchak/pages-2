package org.monome.pages.configuration;

public interface SerialOSCDevice {
    
    public int getPort();
    public void setPort(int port);
    public String getSerial();
    public void setSerial(String serial);
    public String getHostName();
    public void setHostName(String hostName);
    public String getDeviceName();
    public void setDeviceName(String deviceName);
    
}
