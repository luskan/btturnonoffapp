package com.example.bluetoothonoff;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.bluetoothonoff.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private final String BLUETOOTH_REQUEST_DISABLE = "android.bluetooth.adapter.action.REQUEST_DISABLE";
    ActivityResultLauncher<Intent> bluetoothResultLauncher;
    private SharedPreferences sharedPreferences;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                    bluetoothOnOff();
                else
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "BT not supported", Toast.LENGTH_SHORT).show();
            viewModel.buttonText.setValue("BT not supported");
            return;
        }

        viewModel.buttonText.setValue(bluetoothAdapter.isEnabled() ? "Turn BT Off" : "Turn BT On");

        viewModel.toggleBluetooth.observe(this, shouldToggle -> {
            if (shouldToggle != null && shouldToggle) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    boolean hasAskedForPermission = sharedPreferences.getBoolean("hasAskedForPermission", false);
                    if (!hasAskedForPermission || shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_CONNECT))
                        showExplanationDialog();
                    else
                       showSettingsDialog();
                    return;
                }
                bluetoothOnOff();
            }
        });

        bluetoothResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    viewModel.buttonText.setValue(bluetoothAdapter.isEnabled() ? "Turn Off" : "Turn On");
                    Toast.makeText(this,
                            "BT result: " + ((result.getResultCode() == RESULT_OK) ? "ok" : "error"), Toast.LENGTH_SHORT).show();
                }
        );

        BluetoothStateLiveData bluetoothStateLiveData = new BluetoothStateLiveData(this);
        bluetoothStateLiveData.observe(this, state -> {
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    viewModel.buttonText.setValue("Turn BT On");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                case BluetoothAdapter.STATE_ON:
                    viewModel.buttonText.setValue("Turn BT Off");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
            }
        });
    }

    private void showExplanationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permission Needed")
                .setMessage("This app needs Bluetooth permission to function.")
                .setPositiveButton("OK", (dialog, which) -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
                    sharedPreferences.edit().putBoolean("hasAskedForPermission", true).apply();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    private void showSettingsDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permission Denied")
                .setMessage("This app needs Bluetooth permission to function. Please enable it from the app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    // Navigate the user to the app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing
                })
                .show();
    }

    @SuppressLint("MissingPermission")
    private void bluetoothOnOff() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled())
            bluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        else
            bluetoothResultLauncher.launch(new Intent(BLUETOOTH_REQUEST_DISABLE));
    }
}
