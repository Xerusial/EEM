package edu.hm.eem_host.net;

import android.app.Activity;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDEmbedded;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDRegistration;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.RegisterListener;

import java.net.ServerSocket;
import java.util.Map;

import edu.hm.eem_library.net.ServiceManager;

import static android.support.constraint.Constraints.TAG;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HostServiceManager extends ServiceManager implements RegisterListener {
    private final String profName;
    private final DNSSD dnssd;
    private DNSSDService service = null;

    public HostServiceManager(Activity apl, ServerSocket serverSocket, String profName) {
        dnssd = new DNSSDEmbedded(apl);
        this.profName = profName;
        createService(serverSocket.getLocalPort());
    }

    private void createService(int port) {
        try {
            service = dnssd.register(profName, SERVICE_TYPE, port, this);
        } catch (DNSSDException e) {
            Log.e("TAG", "error", e);
        }
    }

    @Override
    public void serviceRegistered(DNSSDRegistration registration, int flags,
                                  String serviceName, String regType, String domain) {
        Log.i("TAG", "Register successfully ");
    }

    @Override
    public void operationFailed(DNSSDService service, int errorCode) {
        Log.e("TAG", "error " + errorCode);
    }

    @Override
    public void quit() {
        if(service!=null) {
            service.stop();
            service = null;
        }
    }
}
