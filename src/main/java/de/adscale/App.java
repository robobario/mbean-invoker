package de.adscale;

import com.google.common.base.MoreObjects;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class App {

    private String appName;

    private String host;

    private int jmxPort;

    private MBeanServerConnection mBeanServerConnection;

    private JMXConnector connector;


    public int getJmxPort() {
        return jmxPort;
    }


    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }


    public String getAppName() {
        return appName;
    }


    public void setAppName(String appName) {
        this.appName = appName;
    }


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("appName", appName).add("host", host).add("jmxPort", jmxPort).toString();
    }


    public MBeanServerConnection connection() {
        if (mBeanServerConnection != null) {
            return mBeanServerConnection;
        }
        try {
            JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + getHost() + ":" + getJmxPort() + "/jmxrmi");
            connector = JMXConnectorFactory.connect(target);
            mBeanServerConnection = connector.getMBeanServerConnection();
            return mBeanServerConnection;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void close() {
        if (connector != null) {
            try {
                connector.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
