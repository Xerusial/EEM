package edu.hm.eem_host.net;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.ClientItemViewModel;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.net.ClientItem;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.FilePacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

/**
 * An {@link IntentService} subclass for handling TCP packages, which is not killed when being run
 * in background.
 */
public class HostProtocolService extends Service {
    public static final int TO_ALL = -1;
    public static final String EXTRA_EXAM_NAME = "edu.hm.eem_host.net.extra.EXAM_NAME";
    private static final String CHANNEL_ID = "ReceiverService";
    private static ClientItemViewModel model;
    // Binder given to clients
    private final IBinder binder = new HostProtocolBinder();
    public boolean locked;
    private NotificationManager nm;
    private String exam = null;
    private int id = 2; //1 is reserved for the running service
    private LinkedList<HostReceiverThread> threads;

    /**
     * Android basics
     */
    @Override
    public void onCreate() {
        threads = new LinkedList<>();
        model = new ClientItemViewModel(this.getApplication());
        nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        super.onCreate();
    }

    /**
     * Gets called when the service is started. This service is immediately promoted to be a foreground
     * service with a notification to be visible to the user.
     *
     * @param intent  Android basics
     * @param flags   Android basics
     * @param startId Android basics
     * @return Start type
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String newExam = intent.getStringExtra(EXTRA_EXAM_NAME);
        if (exam == null || !exam.equals(newExam)) {
            exam = newExam;
            model.getLivedata().clean(true);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_exam_black)
                .setContentTitle(getString(R.string.exam_server, exam))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.exam_server_text, exam)))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        startForeground(1, builder.build());
        return START_STICKY;
    }


    /**
     * Called when the {@link edu.hm.eem_host.view.LockActivity} binds to this service
     *
     * @param intent The intent with which the service had been bound
     * @return IBind instance
     */
    @Override
    public IBinder onBind(Intent intent) {
        locked = false;
        return binder;
    }


    /**
     * If a client connects, generate a receiver thread for it
     *
     * @param socket the socket for the client
     */
    void genReceiverThread(Socket socket) {
        ProtocolManager.ReceiverThread receiverThread = new HostReceiverThread(socket);
        receiverThread.start();
    }

    @NonNull
    public ClientItemViewModel getViewModelInstance() {
        return model;
    }

    /**
     * Create a notification channel. This is used to push notifications if a student disconnects
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    /**
     * Create a notification if a student disconnected.
     *
     * @param name name of the student
     */
    private void notifyStudentLeft(String name) {
        if (locked) {
            notify(getString(R.string.student_left, name), getString(R.string.student_left_text, name));
            model.getLivedata().disconnected(name);
        } else
            model.getLivedata().remove(name, true);
    }

    /**
     * Create a notification if the student pulled his/her notification drawer
     *
     * @param name name of the student
     */
    private void notifyStudentPulledDrawer(String name) {
        model.getLivedata().incrCountNotificationDrawer(name);
        notify(getString(R.string.student_pulled, name), getString(R.string.student_pulled_text, name));
    }

    /**
     * Create a notification to the applications channel.
     *
     * @param title   title of the notification
     * @param message message of the notification
     */
    private void notify(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_student_black)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        nm.notify(++id, builder.build());
    }

    /**
     * Sending a {@link SignalPacket} using TCP to one or all clients using TO_ALL
     *
     * @param signal Signal code to be sent
     * @param index  receiving client: index in the livedata list
     */
    public void sendSignal(SignalPacket.Signal signal, int index) {
        if (index >= 0) {
            ClientItem device = Objects.requireNonNull(model.getLivedata().getValue()).get(index).item;
            ProtocolManager.sendSignal(signal, device.socket);
        } else {
            for (SelectableSortableItem<ClientItem> device : Objects.requireNonNull(model.getLivedata().getValue())) {
                ProtocolManager.sendSignal(signal, device.item.socket);
            }
        }
    }

    /**
     * Send the lighthouse signal
     *
     * @param index receiving client
     */
    public void sendLightHouse(int index) {
        ClientItem device = Objects.requireNonNull(model.getLivedata().getValue()).get(index).item;
        ProtocolManager.sendSignal(device.lighthoused ? SignalPacket.Signal.LIGHTHOUSE_ON : SignalPacket.Signal.LIGHTHOUSE_OFF, device.socket);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    public void quit() {
        sendSignal(SignalPacket.Signal.LOGOFF, TO_ALL);
        for (HostReceiverThread thread : threads) {
            thread.interrupt();
        }
        threads.clear();
        stopSelf();
    }

    /**
     * The TCP receiver thread. Used to handle incoming messages.
     */
    class HostReceiverThread extends ProtocolManager.ReceiverThread {
        private String name;
        private boolean loggedIn = false;

        HostReceiverThread(Socket inputSocket) {
            super(inputSocket);
            threads.add(this);
        }

        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean terminate = false;
            if (!loggedIn) {
                if (type == DataPacket.Type.LOGIN) {
                    terminate = true;
                    DataPacket dataPacket = null;
                    Object[] loginData = LoginPacket.readData(is);
                    int version = (int) loginData[0];
                    if (version > DataPacket.PROTOCOL_VERSION) {
                        dataPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN_VERS_HIGH);
                    } else if (version < DataPacket.PROTOCOL_VERSION) {
                        dataPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN_VERS_LOW);
                    } else {
                        name = (String) loginData[1];
                        if (name != null) {
                            if (model.getLivedata().contains(name)) {
                                dataPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN_NAME);
                            } else {
                                loggedIn = true;
                                terminate = false;
                                model.getLivedata().add(new SelectableSortableItem<>(name, new ClientItem(socket)), true);
                                dataPacket = new FilePacket(getApplication().getFilesDir(), exam);
                                setName("HostProtocolService@" + name);
                            }
                        }
                    }
                    DataPacket.SenderThread sender = new DataPacket.SenderThread(socket, dataPacket);
                    sender.start();
                }
            } else {
                if (type == DataPacket.Type.SIGNAL) {
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal) {
                        case LOGOFF:
                            notifyStudentLeft(name);
                            terminate = true;
                            break;
                        case ALL_DOC_ACCEPTED:
                            model.getLivedata().setSelected(name, true);
                            break;
                        case NOTIFICATIONDRAWER_PULLED:
                            notifyStudentPulledDrawer(name);
                            break;
                    }
                }
            }
            return terminate;
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class HostProtocolBinder extends Binder {
        public HostProtocolService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HostProtocolService.this;
        }
    }

}
