package com.example.bluetoothonoff;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public final MutableLiveData<Boolean> toggleBluetooth = new MutableLiveData<>();
    public final MutableLiveData<String> buttonText = new MutableLiveData<>();

    public void onToggleClicked() {
        toggleBluetooth.setValue(true);
    }
}
