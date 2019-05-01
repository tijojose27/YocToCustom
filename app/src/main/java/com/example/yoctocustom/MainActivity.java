package com.example.yoctocustom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoctocustom.Yocto.YAPI;
import com.example.yoctocustom.Yocto.YAPI_Exception;
import com.example.yoctocustom.Yocto.YAccelerometer;
import com.example.yoctocustom.Yocto.YCompass;
import com.example.yoctocustom.Yocto.YGyro;
import com.example.yoctocustom.Yocto.YModule;
import com.example.yoctocustom.Yocto.YSensor;
import com.example.yoctocustom.Yocto.YTilt;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private ArrayAdapter<String> aa;
    private String serial = "";
    private Handler handler = null;

    TextView statusTV;
    String status="";

    private static final String ACTION_USB_ATTACHED  = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED  = "android.hardware.usb.action.USB_DEVICE_DETACHED";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner my_spin =  findViewById(R.id.spinner1);
        my_spin.setOnItemSelectedListener(this);
        aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        my_spin.setAdapter(aa);
        handler = new Handler();

        statusTV = findViewById(R.id.status);

        status = "\nONCREATE\n";
        statusTV.setText(status);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equalsIgnoreCase(ACTION_USB_ATTACHED)){

                }

                if(action.equalsIgnoreCase(ACTION_USB_DETACHED)){
                    Toast.makeText(getApplicationContext(), "DETTACHED", Toast.LENGTH_SHORT).show();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_ATTACHED);
        intentFilter.addAction(ACTION_USB_DETACHED);

        registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        doYoktoStuff();
    }

    public void doYoktoStuff(){
        status = "\nON START"+status;
        statusTV.setText(status);
        try {
            aa.clear();

            YAPI.EnableUSBHost(this);

            status = "\nENABLE HOST"+status;
            statusTV.setText(status);

            YAPI.RegisterHub("usb");

            status = "\nregistered HOST"+status;
            statusTV.setText(status);


            YModule module = YModule.FirstModule();
            while (module != null) {
                if (module.get_productName().equals("Yocto-3D-V2")) {
                    serial = module.get_serialNumber();
                    aa.add(serial);
                }
                module = module.nextModule();
            }
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
        aa.notifyDataSetChanged();
        handler.postDelayed(r, 500);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        handler.removeCallbacks(r);
        YAPI.FreeAPI();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        serial = parent.getItemAtPosition(pos).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }

    final Runnable r = new Runnable()
    {
        public void run()
        {
            if (serial != null) {
                status= "\n RUNNABLE CHANGED STATUS"+status;
                statusTV.setText(status);
                YSensor tilt1 = YTilt.FindTilt(serial + ".tilt1");
                try {
                    TextView view =  findViewById(R.id.tilt1field);
                    view.setText(String.format("%.1f %s", tilt1.getCurrentValue(), tilt1.getUnit()));
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
                YTilt tilt2 = YTilt.FindTilt(serial + ".tilt2");
                try {
                    TextView view =  findViewById(R.id.tilt2field);
                    view.setText(String.format("%.1f %s", tilt2.getCurrentValue(), tilt2.getUnit()));
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
                YCompass compass = YCompass.FindCompass(serial + ".compass");
                try {
                    TextView view = findViewById(R.id.compassfield);
                    view.setText(String.format("%.1f %s", compass.getCurrentValue(), compass.getUnit()));
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
                YAccelerometer accelerometer = YAccelerometer.FindAccelerometer(serial + ".accelerometer");
                try {
                    TextView view = findViewById(R.id.accelfield);
                    view.setText(String.format("%.1f %s", accelerometer.getCurrentValue(), accelerometer.getUnit()));
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
                YGyro gyro = YGyro.FindGyro(serial + ".gyro");
                try {
                    TextView view = findViewById(R.id.gyrofield);
                    view.setText(String.format("%.1f %s", gyro.getCurrentValue(), gyro.getUnit()));
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
            }
            handler.postDelayed(this, 200);
        }
    };

}
