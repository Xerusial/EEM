package edu.hm.eem_library.net;

import java.net.InetAddress;

/**
 * A struct containing information on an NSD Service. This struct is used for the RxDNSSD library
 * because it does not have Android's handy {@link android.net.nsd.NsdServiceInfo}
 */
public class NsdService {
    public final int flags, ifIndex;
    public int port;
    public final String serviceName, regType, domain;
    public InetAddress address;

    public NsdService(int flags, int ifIndex, String serviceName, String regType, String domain) {
        this.flags = flags;
        this.ifIndex = ifIndex;
        this.serviceName = serviceName;
        this.regType = regType;
        this.domain = domain;
    }
}
