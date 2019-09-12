package edu.hm.eem_library.net;

import androidx.annotation.StringRes;

/**
 * A handler used to sync callback from the {@link ProtocolManager} to the UI thread.
 * Implemented in client's LockedActivity.LockedHandler and host's LockActivity.LockHandler
 */
public interface ProtocolHandler {
    void putToast(@StringRes int resId);
}
