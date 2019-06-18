package edu.hm.eem_client.net;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.druk.dnssd.BaseListener;
import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSD;
import com.github.druk.dnssd.DNSSDEmbedded;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.QueryListener;
import com.github.druk.dnssd.ResolveListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.net.NsdService;
import edu.hm.eem_library.net.ServiceManager;

public class ClientServiceManager extends ServiceManager {
    private final SelectableSortableMapLiveData<NsdService, SelectableSortableItem<NsdService>> selectableSortableMapLiveData;
    private final DNSSD dnssd;
    private final ExamBrowseListener examBrowseListener = new ExamBrowseListener();
    private final ExamResolverListener examResolverListener = new ExamResolverListener();
    private final ExamQueryListener examQueryListener = new ExamQueryListener();
    private final ServiceReadyListener serviceReadyListener;
    private DNSSDService dnssdService = null;
    private NsdService nsdService;

    public ClientServiceManager(Context context, SelectableSortableMapLiveData<NsdService, SelectableSortableItem<NsdService>> selectableSortableMapLiveData, ServiceReadyListener serviceReadyListener) {
        this.dnssd = new DNSSDEmbedded(context);
        this.selectableSortableMapLiveData = selectableSortableMapLiveData;
        this.serviceReadyListener = serviceReadyListener;
    }

    public void discover(boolean on) {
        if(on) {
            Log.d("Main", "Started scanning!");
            try {
                dnssdService = dnssd.browse(SERVICE_TYPE, examBrowseListener);
            } catch (DNSSDException e) {
                e.printStackTrace();
            }
        } else if (dnssdService!=null) {
            dnssdService.stop();
            dnssdService = null;
        }
    }

    public void resolve(NsdService nsdService) {
        this.nsdService = nsdService;
        try {
            dnssd.resolve(nsdService.flags, nsdService.ifIndex, nsdService.serviceName, nsdService.regType, nsdService.domain, examResolverListener);
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    private void queryRecords(String hostName) {
        try {
            //Query for record type 1: Ipv4 https://en.wikipedia.org/wiki/List_of_DNS_record_types
            dnssd.queryRecord(0, nsdService.ifIndex, hostName, 1, 1, examQueryListener);
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void quit() {
        discover(false);
    }

    private class ExamBrowseListener implements BrowseListener {

        @Override
        public void serviceFound(DNSSDService browser, int flags, int ifIndex, String serviceName, String regType, String domain) {
            NsdService service = new NsdService(flags, ifIndex, serviceName, regType, domain);
            selectableSortableMapLiveData.add(new SelectableSortableItem<>(serviceName, service), true);
        }

        @Override
        public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName, String regType, String domain) {

        }

        @Override
        public void operationFailed(DNSSDService service, int errorCode) {

        }
    }

    private class ExamResolverListener implements ResolveListener {

        @Override
        public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, Map<String, String> txtRecord) {
            nsdService.port = port;
            queryRecords(hostName);
        }

        @Override
        public void operationFailed(DNSSDService service, int errorCode) {
            serviceReadyListener.operationFailed(service,errorCode);
        }
    }

    private class ExamQueryListener implements QueryListener {

        @Override
        public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName, int rrtype, int rrclass, byte[] rdata, int ttl) {
            try {
                nsdService.address = InetAddress.getByAddress(rdata);
                serviceReadyListener.onServiceReady(nsdService);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void operationFailed(DNSSDService service, int errorCode) {
            serviceReadyListener.operationFailed(service,errorCode);
        }
    }

    public interface ServiceReadyListener extends BaseListener {
        void onServiceReady(NsdService nsdService);
    }
}
