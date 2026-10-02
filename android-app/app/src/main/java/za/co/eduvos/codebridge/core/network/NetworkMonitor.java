package za.co.eduvos.codebridge.core.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isOnline = new MutableLiveData<>();
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        isOnline.setValue(checkCurrentConnection());

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) { isOnline.postValue(true); }

            @Override
            public void onLost(@NonNull Network network) { isOnline.postValue(false); }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    public LiveData<Boolean> getIsOnline() { return isOnline; }

    public void unregister() {
        if (networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException ignored) {
                // Already unregistered
            }
            networkCallback = null;
        }
    }

    private boolean checkCurrentConnection() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
