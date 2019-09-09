package edu.hm.eem_client.net;

import android.content.Context;

import com.github.druk.dnssd.BaseListener;
import com.github.druk.dnssd.BrowseListener;
import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;
import com.github.druk.dnssd.QueryListener;
import com.github.druk.dnssd.ResolveListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;
import edu.hm.eem_library.net.NsdService;
import edu.hm.eem_library.net.ServiceManager;

/**
 * The client side implementation of the service manager. It uses the DNSSD library, as it is more
 * reliable with discovering services than the {@link android.net.nsd.NsdManager}.
 * The DNSSDBindable is used, as DNSSDEMbedded was buggy at the time this program was written.
 */
public class ClientServiceManager extends ServiceManager {
    private final SelectableSortableItemLiveData<NsdService, SelectableSortableItem<NsdService>> selectableSortableMapLiveData;
    private final DNSSDBindable dnssd;
    private final ExamBrowseListener examBrowseListener = new ExamBrowseListener();
    private final ExamResolverListener examResolverListener = new ExamResolverListener();
    private final ExamQueryListener examQueryListener = new ExamQueryListener();
    private final ServiceReadyListener serviceReadyListener;
    private DNSSDService browseService = null, resolveService = null, queryService = null;
    private NsdService nsdService;

    /**
     * Constructior
     *
     * @param context                       The activity context where this service manager is used
     * @param selectableSortableMapLiveData A lifedata object, which feeds the UI list of found services
     * @param serviceReadyListener          A listener with a method to be called when a service was fully resolved
     */
    public ClientServiceManager(Context context, SelectableSortableItemLiveData<NsdService, SelectableSortableItem<NsdService>> selectableSortableMapLiveData, ServiceReadyListener serviceReadyListener) {
        this.dnssd = new DNSSDBindable(context);
        this.selectableSortableMapLiveData = selectableSortableMapLiveData;
        this.serviceReadyListener = serviceReadyListener;
    }

    /**
     * Method to be called to enable DNSSD scanning (DNS based zeroconf). Its callbacks can be
     * found in the {@link ClientServiceManager.ExamBrowseListener}:
     *
     * @param on turns it on or off
     */
    public void discover(boolean on) {
        if (on) {
            try {
                browseService = dnssd.browse(SERVICE_TYPE, examBrowseListener);
            } catch (DNSSDException e) {
                e.printStackTrace();
            }
        } else quit();
    }

    /**
     * After a discovered DNS Service has been selected from the user, its port can be resolved
     * with the resolver started from this method. The resolve callbacks are in
     * {@link ClientServiceManager.ExamResolverListener}.
     *
     * @param nsdService A "struct" of parameters needed
     */
    public void resolve(NsdService nsdService) {
        this.nsdService = nsdService;
        try {
            resolveService = dnssd.resolve(nsdService.flags, nsdService.ifIndex, nsdService.serviceName, nsdService.regType, nsdService.domain, examResolverListener);
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    /**
     * In order to get the IP-address of a resolved service, its TXT records have to be queried.
     * This is done using this method. The callbacks can be found in
     * {@link ClientServiceManager.ExamQueryListener}.
     *
     * @param hostName only param needed for IP query
     */
    private void queryRecords(String hostName) {
        try {
            //Query for record type 1: Ipv4 https://en.wikipedia.org/wiki/List_of_DNS_record_types
            queryService = dnssd.queryRecord(0, nsdService.ifIndex, hostName, 1, 1, examQueryListener);
        } catch (DNSSDException e) {
            e.printStackTrace();
        }
    }

    /**
     * Needs to be called for cleanup purposes
     */
    @Override
    public void quit() {
        if (browseService != null) {
            browseService.stop();
            browseService = null;
        }
        if (resolveService != null) {
            resolveService.stop();
            resolveService = null;
        }
        if (queryService != null) {
            queryService.stop();
            queryService = null;
        }
    }

    /**
     * Is called from the query listener, when all three steps: Discover, Resolve & Query are done
     */
    public interface ServiceReadyListener extends BaseListener {
        void onServiceReady(NsdService nsdService);
    }

    /**
     * Discovery Callbacks
     */
    private class ExamBrowseListener implements BrowseListener {

        @Override
        public void serviceFound(DNSSDService browser, int flags, int ifIndex, String serviceName,
                                 String regType, String domain) {
            NsdService service = new NsdService(flags, ifIndex, serviceName, regType, domain);
            selectableSortableMapLiveData.add(new SelectableSortableItem<>(serviceName, service), true);
        }

        @Override
        public void serviceLost(DNSSDService browser, int flags, int ifIndex, String serviceName,
                                String regType, String domain) {

        }

        @Override
        public void operationFailed(DNSSDService service, int errorCode) {

        }
    }

    /**
     * Resolve callbacks
     */
    private class ExamResolverListener implements ResolveListener {

        @Override
        public void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName,
                                    String hostName, int port, Map<String, String> txtRecord) {
            nsdService.port = port;
            queryRecords(hostName);
        }

        @Override
        public void operationFailed(DNSSDService service, int errorCode) {
            serviceReadyListener.operationFailed(service, errorCode);
        }
    }

    /**
     * Query callbacks
     */
    private class ExamQueryListener implements QueryListener {

        @Override
        public void queryAnswered(DNSSDService query, int flags, int ifIndex, String fullName,
                                  int rrtype, int rrclass, byte[] rdata, int ttl) {
            try {
                nsdService.address = InetAddress.getByAddress(rdata);
                serviceReadyListener.onServiceReady(nsdService);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void operationFailed(DNSSDService service, int errorCode) {
            serviceReadyListener.operationFailed(service, errorCode);
        }
    }
}
