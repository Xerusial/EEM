package edu.hm.eem_library.net;

import java.net.InetAddress;

public class NsdService {
    public int flags, ifIndex, port;
    public String serviceName, regType, domain;
    public InetAddress address;

    public NsdService(int flags, int ifIndex, String serviceName, String regType, String domain) {
        this.flags = flags;
        this.ifIndex = ifIndex;
        this.serviceName = serviceName;
        this.regType = regType;
        this.domain = domain;
    }
}
