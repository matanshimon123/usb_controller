package com.example.electrocal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbConstants;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.usb.UsbInterface;
import android.os.Handler;
import android.os.Looper;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class USBActivity extends AppCompatActivity {

    private boolean connectionEstablished = false;
    private UsbSerialDevice serialPort;
    private boolean shouldReadData = false;
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointIn;
    private TextView usbInfoTextView;
    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private PendingIntent permissionIntent;
    private SerialConnectionManager serialManager;
    private boolean isReading = false;
    private static final String ACTION_USB_PERMISSION = "com.example.electrocal.USB_PERMISSION";
    private Set<Integer> supportedVendorIds = new HashSet<>(Arrays.asList(
            11427, // DJI vendor ID
            6790,
            11427,
            6790

    ));

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            usbDevice = device;
                            displayDeviceInfo();
                            handleUsbDeviceConnection();
                        }
                    } else {
                        // Handle permission denied
                        Log.e("USB Permission", "Permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);

        usbInfoTextView = findViewById(R.id.textView_usb_info);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        permissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);

        // Register receiver for USB permission
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);


        // Button initialization
        Button ce_2400_button = findViewById(R.id.button_ce_2400);
        Button ce_5800_button = findViewById(R.id.button_ce_5800);
        Button ce_dual_button = findViewById(R.id.button_ce_dual);
        Button fcc_2400_button = findViewById(R.id.button_fcc_2400);
        Button fcc_5800_button = findViewById(R.id.button_fcc_5800);
        Button fcc_dual_button = findViewById(R.id.button_fcc_dual);
        Button refresh_usb_connection_button = findViewById(R.id.button_refresh);
        Button read_serial_dji_button = findViewById(R.id.button_read_serial);
        Button read_serial_arduino_button = findViewById(R.id.button_read_serial_arduino);
        Button get_country_code_button = findViewById(R.id.get_country_code);

        // Check for existing devices
        checkForNewDevices();

        // Setup button listeners
        setupButtonListeners(ce_2400_button, ce_5800_button, ce_dual_button,
                fcc_2400_button, fcc_5800_button, fcc_dual_button,
                refresh_usb_connection_button, read_serial_dji_button , read_serial_arduino_button, get_country_code_button);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usbConnection != null) {
            usbConnection.close();
        }
        unregisterReceiver(usbReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for devices when the activity resumes
        checkForNewDevices();
    }

    private void setupDevice(UsbDevice device) {
        clearTextView();
        // Implementation to set up the device communication
        usbInfoTextView.setText("USB device connected: ");
    }


    private void setupButtonListeners(Button ce_2400_button, Button ce_5800_button, Button ce_dual_button,
                                      Button fcc_2400_button, Button fcc_5800_button, Button fcc_dual_button,
                                      Button refresh_usb_connection_button, Button read_serial_dji_button,Button read_serial_arduino_button, Button get_country_code_button) {

        ce_2400_button.setOnClickListener(v -> {
            resetButtonColors();
            Toast.makeText(getApplicationContext(), "CE 2400 Set Successfully", Toast.LENGTH_SHORT).show();
            ce_2400_button.setBackgroundColor(Color.parseColor("#00A300"));
        });

        ce_5800_button.setOnClickListener(v -> {
            resetButtonColors();
            Toast.makeText(getApplicationContext(), "CE 5800 Set Successfully", Toast.LENGTH_SHORT).show();
            ce_5800_button.setBackgroundColor(Color.parseColor("#00A300"));
        });

        ce_dual_button.setOnClickListener(v -> {
            resetButtonColors();
            Toast.makeText(getApplicationContext(), "CE Dual Set Successfully", Toast.LENGTH_SHORT).show();
            ce_dual_button.setBackgroundColor(Color.parseColor("#00A300"));
        });

        fcc_2400_button.setOnClickListener(v -> {
            resetButtonColors();
            Toast.makeText(getApplicationContext(), "FCC 2400 Set Successfully", Toast.LENGTH_SHORT).show();
            fcc_2400_button.setBackgroundColor(Color.parseColor("#00A300"));
        });

        fcc_5800_button.setOnClickListener(v -> {
            resetButtonColors();
            Toast.makeText(getApplicationContext(), "FCC 5800 Set Successfully", Toast.LENGTH_SHORT).show();
            fcc_5800_button.setBackgroundColor(Color.parseColor("#00A300"));
        });

        fcc_dual_button.setOnClickListener(v -> {
            resetButtonColors();
            Toast.makeText(getApplicationContext(), "FCC Dual Set Successfully", Toast.LENGTH_SHORT).show();
            fcc_dual_button.setBackgroundColor(Color.parseColor("#00A300"));
        });

        refresh_usb_connection_button.setOnClickListener(v -> displayDeviceInfo());

        read_serial_arduino_button.setOnClickListener(v -> {
            serialManager = new SerialConnectionManager(this);
            if (!isReading) {
                isReading = true;
                read_serial_arduino_button.setText("Stop Reading");
                Toast.makeText(getApplicationContext(), "Starting continuous reading", Toast.LENGTH_SHORT).show();
                clearTextView();

                new Thread(() -> {
                    boolean connectionEstablished = serialManager.establishConnection(9600);
                    if (connectionEstablished) {
                        while (isReading) {
                            String readData = serialManager.readData();
                            runOnUiThread(() -> {
                                String currentText = usbInfoTextView.getText().toString();
                                String newText = currentText + "\n" + readData;
                                usbInfoTextView.setText(newText);
                                usbInfoTextView.post(() -> {
                                    int scrollAmount = usbInfoTextView.getLayout().getLineTop(usbInfoTextView.getLineCount()) - usbInfoTextView.getHeight();
                                    if (scrollAmount > 0) {
                                        usbInfoTextView.scrollTo(0, scrollAmount);
                                    }
                                });
                            });

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        serialManager.closeConnection();
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Failed to establish connection", Toast.LENGTH_SHORT).show();
                            read_serial_arduino_button.setText("Start Reading");
                            isReading = false;
                        });
                    }
                }).start();
            } else {
                isReading = false;
                read_serial_arduino_button.setText("Start Reading");
                Toast.makeText(getApplicationContext(), "Stopping continuous reading", Toast.LENGTH_SHORT).show();
            }
        });

        read_serial_dji_button.setOnClickListener(v -> {
            Log.d("USB", "Serial read button clicked");
            if (usbConnection != null && endpointIn != null) {
                resetDevice();
                Toast.makeText(getApplicationContext(), "Starting data read", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).post(() -> {
                    new ReadDataThread().start();
                });
            } else {
                Log.e("USB", "USB connection not established yet");
                usbInfoTextView.setText("USB connection not established yet.");
            }
        });

        get_country_code_button.setOnClickListener(v -> {
            String hexString = "550d04330a092d004007191dd2";
            byte[] command = hexStringToByteArray(hexString);
            sendSerialCommand(command);

        });
    }


    private void resetButtonColors() {
        Button[] buttons = {findViewById(R.id.button_ce_2400), findViewById(R.id.button_ce_5800),
                findViewById(R.id.button_ce_dual), findViewById(R.id.button_fcc_2400),
                findViewById(R.id.button_fcc_5800), findViewById(R.id.button_fcc_dual)};
        for (Button button : buttons) {
            button.setBackgroundColor(Color.BLUE);
        }
    }

    private void checkForNewDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            for (UsbDevice device : deviceList.values()) {
                if (supportedVendorIds.contains(device.getVendorId())) {
                    usbDevice = device;
                    Log.d("USB", "Found supported device: VendorId=" + device.getVendorId() + ", ProductId=" + device.getProductId());
                    if (usbManager.hasPermission(device)) {
                        handleUsbDeviceConnection();
                    } else {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                        usbManager.requestPermission(device, pendingIntent);
                    }
                    return; // Exit after finding the first supported device
                }
            }
            usbInfoTextView.setText("No supported USB device found");
        } else {
            usbInfoTextView.setText("No USB devices found");
        }
    }

    private void displayDeviceInfo() {
        clearTextView();
        String info = "USB Device Info:\n";
        info += "Device Name: " + usbDevice.getDeviceName() + "\n";
        info += "Device ID: " + usbDevice.getDeviceId() + "\n";
        info += "Vendor ID: " + usbDevice.getVendorId() + "\n";
        info += "Product ID: " + usbDevice.getProductId() + "\n";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            info += "Serial Number: " + usbDevice.getSerialNumber() + "\n";
        }
        usbInfoTextView.setText(info);
    }

    private void clearTextView() {
        usbInfoTextView.setText("");
    }


    private void readDataFromUsb() {
        if (shouldReadData && !connectionEstablished) {
            if (endpointIn == null) {
                runOnUiThread(() -> usbInfoTextView.setText("Error: No input endpoint"));
                return;
            }

            // Buffer size for reading data
            byte[] buffer = new byte[endpointIn.getMaxPacketSize()];
            int maxAttempts = 3;
            int attempt = 0;
            boolean bulkSuccess = false;

            // Loop to attempt reading data from bulk transfer
            while (attempt < maxAttempts) {
                int bytesRead = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, 5000); // Increased timeout

                if (bytesRead > 0) {
                    // Convert received bytes to hexadecimal format
                    StringBuilder hexData = new StringBuilder();
                    for (int i = 0; i < bytesRead; i++) {
                        hexData.append(String.format("%02X", buffer[i]));
                    }
                    String finalData = hexData.toString();
                    runOnUiThread(() -> usbInfoTextView.append("\nData received: " + finalData)); // Append instead of setText
                    bulkSuccess = true;
                } else if (bytesRead == 0) {
                    Log.d("USB", "No data received on attempt " + (attempt + 1));
                } else {
                    Log.e("USB", "Bulk transfer failed with code: " + bytesRead + " on attempt " + (attempt + 1));
                }

                if (bulkSuccess) {
                    connectionEstablished = true;
                    usbInfoTextView.append("\nBulk transfer establish");
                    break; // Exit the loop if data was successfully read
                }

                attempt++;
                int attemptCopy = attempt;
                runOnUiThread(() -> usbInfoTextView.append("\nAttempt number " + (attemptCopy) + " to read data from bulk transfer."));
                try {
                    Thread.sleep(1000); // Wait for 1 second before next attempt
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // If bulk transfer failed, attempt to establish a serial connection

            if (!bulkSuccess) {
                runOnUiThread(() -> {
                    clearTextView();
                    usbInfoTextView.append("\nBulk transfer failed");
                    usbInfoTextView.append("\nAttempting to establish a serial connection...");
                });
                openSerialConnection(usbDevice);
            }

            shouldReadData = false; // Set to false after attempting to read data
        } else {
            Log.d("USB", "shouldReadData is false");
        }
    }

    private void sendSerialCommand(byte[] command) {
        openSerialConnection(usbDevice);
        // Write the command
        usbInfoTextView.setText("Sending Command....");  // Update UI to indicate command sending
//        serialPort.write(command);

        // Optionally, you can read the response immediately after sending the command
        // Uncomment the line below if you want to read the response immediately
//         serialPort.read(mCallback);
    }

    private void handleUsbDeviceConnection() {
        Log.d("USB", "Handling USB device connection");
        if (usbDevice == null) {
            Log.e("USB", "No USB device selected");
            usbInfoTextView.setText("No USB device selected");
            return;
        }

        Log.d("USB", "Device: " + usbDevice.getDeviceName() + ", VendorId: " + usbDevice.getVendorId() + ", ProductId: " + usbDevice.getProductId());

        UsbInterface usbInterface = null;
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            usbInterface = usbDevice.getInterface(i);
            Log.d("USB", "Interface " + i + ": " + usbInterface.getId() + ", Endpoint Count: " + usbInterface.getEndpointCount());

            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                Log.d("USB", "Endpoint " + j + ": Direction: " + (endpoint.getDirection() == UsbConstants.USB_DIR_IN ? "IN" : "OUT") +
                        ", Type: " + endpoint.getType() + ", Address: " + endpoint.getAddress());
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN &&
                        endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    endpointIn = endpoint;
                    Log.d("USB", "Found IN endpoint");
                    // Exit loop once IN endpoint is found
                    break;
                }
            }
            if (endpointIn != null) {
                break; // Exit loop if IN endpoint is found
            }
        }

        // If no bulk endpoint is found, try to establish a serial connection
        if (endpointIn == null) {
            Log.e("USB", "Could not find IN endpoint");
            usbInfoTextView.setText("Could not find IN endpoint");
            openSerialConnection(usbDevice);
            return;
        }

        usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            Log.e("USB", "Could not open USB connection");
            usbInfoTextView.setText("Could not open USB connection");
            return;
        }

        boolean claimed = usbConnection.claimInterface(usbInterface, true);
        if (!claimed) {
            Log.e("USB", "Failed to claim interface");
            usbInfoTextView.setText("Failed to claim interface");
            usbConnection.close();
            usbConnection = null;
            return;
        }

        Log.d("USB", "USB connection established successfully");
        usbInfoTextView.setText("USB connection established successfully");
    }


    private void openSerialConnection(UsbDevice device) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbSerialDevice serialPort;
        UsbDeviceConnection connection = usbManager.openDevice(device);

        if (connection == null) {
            Log.e("USB", "Cannot open connection to device.");
            runOnUiThread(() -> usbInfoTextView.setText("Cannot open connection to device."));
            return;
        }

        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialPort != null && serialPort.open()) {
            if (serialPort.open()) {
                // Set Serial Connection Parameters
                serialPort.setBaudRate(115200);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                // Set a callback for received data
                serialPort.read(mCallback);
                connectionEstablished = true;
                Log.d("USB", "Serial connection opened.");
                runOnUiThread(() -> usbInfoTextView.append("\nSerial connection opened.\n"));
            } else {
                Log.e("USB", "Could not open serial port.");
                runOnUiThread(() -> usbInfoTextView.append("\nCould not open serial port."));
            }
        } else {
            Log.e("USB", "No driver for the given device.");
            runOnUiThread(() -> usbInfoTextView.append("\nNo driver for the given device."));
        }
    }

    private void closeSerialPort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.close();
            Log.d("Serial", "Serial port closed");
        }
    }

    // Callback for serial data received
    private final UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
            // Convert received bytes to hexadecimal format
            StringBuilder hexData = new StringBuilder();
            for (byte b : data) {
                hexData.append(String.format("%02X", b));
            }
            String receivedData = hexData.toString();
            Log.d("USB", "Received data: " + receivedData);
            runOnUiThread(() -> usbInfoTextView.append("\nReceived data: " + receivedData));

//             Optionally, close the serial port after receiving data
            closeSerialPort();
        }
    };


    private class ReadDataThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 5 && !connectionEstablished; i++) {
                shouldReadData = true;
                readDataFromUsb();
                if (connectionEstablished) {
                    break; // Exit the loop if a connection is established
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!connectionEstablished) {
                runOnUiThread(() -> usbInfoTextView.append("\nFailed to establish a connection after 5 attempts."));
            }
        }
    }

    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }




//    private void readDataFromUsb() {
//        if (shouldReadData) {
//            if (endpointIn == null) {
//                usbInfoTextView.setText("Error: No input endpoint");
//                return;
//            }
//
//            byte[] buffer = new byte[endpointIn.getMaxPacketSize()];
//            int maxAttempts = 3;
//            int attempt = 0;
//
//            while (attempt < maxAttempts) {
//                int bytesRead = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, 10000); // Increased timeout
//
//                if (bytesRead > 0) {
//                    StringBuilder hexData = new StringBuilder();
//                    for (int i = 0; i < bytesRead; i++) {
//                        hexData.append(String.format("%02X", buffer[i]));
//                    }
//                    String finalData = hexData.toString();
//                    runOnUiThread(() -> usbInfoTextView.setText("Data received: " + finalData));
//                    break;
//                } else if (bytesRead == 0) {
//                    Log.d("USB", "No data received on attempt " + (attempt + 1));
//                } else {
//                    Log.e("USB", "Bulk transfer failed with code: " + bytesRead + " on attempt " + (attempt + 1));
//                }
//
//                attempt++;
//                try {
//                    Thread.sleep(1000); // Wait for 1 second before next attempt
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (attempt == maxAttempts) {
//                runOnUiThread(() -> {
//                    usbInfoTextView.setText("Error: Failed to read data after " + maxAttempts + " attempts\n");
//                    usbInfoTextView.append("Endpoint address: " + endpointIn.getAddress() + "\n");
//                    usbInfoTextView.append("Endpoint direction: " + endpointIn.getDirection() + "\n");
//                    usbInfoTextView.append("Endpoint type: " + endpointIn.getType());
//                });
//            }
//
//            shouldReadData = false;
//        } else {
//            Log.d("USB", "shouldReadData is false");
//        }
//    }
//
//    private void handleUsbDeviceConnection() {
//        Log.d("USB", "Handling USB device connection");
//        if (usbDevice == null) {
//            Log.e("USB", "No USB device selected");
//            usbInfoTextView.setText("No USB device selected");
//            return;
//        }
//
//        Log.d("USB", "Device: " + usbDevice.getDeviceName() + ", VendorId: " + usbDevice.getVendorId() + ", ProductId: " + usbDevice.getProductId());
//
//        UsbInterface usbInterface = null;
//        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
//            usbInterface = usbDevice.getInterface(i);
//            Log.d("USB", "Interface " + i + ": " + usbInterface.getId() + ", Endpoint Count: " + usbInterface.getEndpointCount());
//
//            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
//                UsbEndpoint endpoint = usbInterface.getEndpoint(j);
//                Log.d("USB", "Endpoint " + j + ": Direction: " + (endpoint.getDirection() == UsbConstants.USB_DIR_IN ? "IN" : "OUT") +
//                        ", Type: " + endpoint.getType() + ", Address: " + endpoint.getAddress());
//                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN &&
//                        endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
//                    endpointIn = endpoint;
//                    Log.d("USB", "Found IN endpoint");
//                }
//            }
//            if (endpointIn != null) break;
//        }
//
//        if (endpointIn == null) {
//            Log.e("USB", "Could not find IN endpoint");
//            usbInfoTextView.setText("Could not find IN endpoint");
//            return;
//        }
//
//        usbConnection = usbManager.openDevice(usbDevice);
//        if (usbConnection == null) {
//            Log.e("USB", "Could not open USB connection");
//            usbInfoTextView.setText("Could not open USB connection");
//            return;
//        }
//
//        boolean claimed = usbConnection.claimInterface(usbInterface, true);
//        if (!claimed) {
//            Log.e("USB", "Failed to claim interface");
//            usbInfoTextView.setText("Failed to claim interface");
//            usbConnection.close();
//            usbConnection = null;
//            return;
//        }
//
//        Log.d("USB", "USB connection established successfully");
//        usbInfoTextView.setText("USB connection established successfully");
//    }
//









    private void resetDevice() {
        if (usbConnection != null) {
            UsbInterface usbInterface = usbDevice.getInterface(0);
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
        }

        usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection != null) {
            UsbInterface usbInterface = usbDevice.getInterface(0);
            if (usbConnection.claimInterface(usbInterface, true)) {
                Log.d("USB", "Interface claimed after reset");
            } else {
                Log.e("USB", "Failed to claim interface after reset");
            }
        } else {
            Log.e("USB", "Failed to open device after reset");
        }
    }


    public static class SerialConnectionManager {
        private Context context;
        private UsbSerialPort port;

        public SerialConnectionManager(Context context) {
            this.context = context;
        }

        public boolean establishConnection(int baudRate) {
            UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

            if (availableDrivers.isEmpty()) {
                Log.e("SerialConnectionManager", "No available drivers found");
                return false;
            }

            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

            if (connection == null) {
                if (!manager.hasPermission(driver.getDevice())) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(driver.getDevice(), permissionIntent);
                    Log.e("SerialConnectionManager", "USB Permission requested. Please try again after granting permission.");
                } else {
                    Log.e("SerialConnectionManager", "Unable to open connection. Device might be in use by another app.");
                }
                return false;
            }

            try {
                port = driver.getPorts().get(0);
                port.open(connection);
                port.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                Log.i("SerialConnectionManager", "Serial connection established successfully");
                return true;
            } catch (IOException e) {
                Log.e("SerialConnectionManager", "Error establishing connection: " + e.getMessage());
                return false;
            }
        }

        @Nullable
        public String readData() {
            if (port == null) {
                return "Error: Serial port not open";
            }

            try {
                byte[] buffer = new byte[1024];
                int len = port.read(buffer, 2000); // 2000ms timeout
                return new String(buffer, 0, len);
            } catch (IOException e) {
                return "Error reading: " + e.getMessage();
            }
        }

        public void closeConnection() {
            if (port != null) {
                try {
                    port.close();
                    Log.i("SerialConnectionManager", "Serial connection closed");
                } catch (IOException e) {
                    Log.e("SerialConnectionManager", "Error closing connection: " + e.getMessage());
                }
            }
        }
    }

}
