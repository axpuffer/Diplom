package ru.mirea.grigoriev.watchtest;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.healthdata.HealthDataClient;

import ru.mirea.grigoriev.watchtest.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private HealthDataClient healthDataClient;
    private SensorManager sensorManager;
    private Sensor heartRateSensor;

    private TextView mTextView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Получаем доступ к датчику пульса
        if (sensorManager != null) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }

        // Проверка и запрос разрешений
        checkAndRequestPermissions();


        mTextView = binding.text;

    }

    private void checkAndRequestPermissions() {
        int permissionState = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BODY_SENSORS);
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            // Разрешение уже предоставлено, регистрируем слушателя
            registerHeartRateListener();
        }
    }

    private void registerHeartRateListener() {
        // Регистрируем слушателя для датчика пульса
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, регистрируем слушателя
                registerHeartRateListener();
            } else {
                // Пользователь отказал в предоставлении разрешения
                // Обработка ситуации отсутствия разрешения
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            // Получаем данные от датчика пульса
            float heartRate = event.values[0];

            updateUI((int) heartRate);

        }
    }

    private void updateUI(int heartRate) {
        mTextView.setText("Heart Rate: " + heartRate);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Отменяем регистрацию слушателя при приостановке активности
        if (heartRateSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}