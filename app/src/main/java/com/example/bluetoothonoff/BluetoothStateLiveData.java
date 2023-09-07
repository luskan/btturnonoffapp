package com.example.bluetoothonoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;

import androidx.lifecycle.LiveData;

public class BluetoothStateLiveData extends LiveData<Integer> {
    private final Context context;

    public BluetoothStateLiveData(Context context) {
        this.context = context.getApplicationContext();
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                setValue(state);
            }
        }
    };

    @Override
    protected void onActive() {
        super.onActive();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothStateReceiver, filter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        context.unregisterReceiver(bluetoothStateReceiver);
    }
}
