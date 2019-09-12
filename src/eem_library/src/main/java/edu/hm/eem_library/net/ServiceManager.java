package edu.hm.eem_library.net;

/**
 * Base class for the client- and host service managers. As they fundamentally differ, because
 * the host hosts services and uses Android's built-in {@link android.net.nsd.NsdManager} and
 * the client discovers services and uses RxDNSSD for its more reliable performance, this only
 * contains the common service name.
 */
public abstract class ServiceManager {
    protected final static String SERVICE_TYPE = "_exammode._tcp";

    public abstract void quit();
}
