package com.mahinurbulanikoglu.aes;


import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.nio.charset.StandardCharsets;

public class QRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        Button qrButton = findViewById(R.id.btnScanQR);
        qrButton.setOnClickListener(v -> scanQRCode());
    }

    private void scanQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("QR Kodunu tarayın");
        integrator.setOrientationLocked(false); // Ekran yönünü kilitlemeyin
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setBeepEnabled(true); // Sesli bildirim açılabilir
        integrator.setTimeout(10000); // Tarama süresini uzatın
        integrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            // QR kodun içeriğini kontrol ediyoruz
            String qrCodeValue = result.getContents();

            if (qrCodeValue != null && !qrCodeValue.isEmpty()) {
                // QR kod başarıyla tarandı, değeri işle
                try {
                    // QR kodun içeriğini direkt olarak kullanabilirsiniz
                    // Burada qrCodeValue bir string olduğu için, onu byte dizisine dönüştürmek için doğrudan kullanabilirsiniz
                    byte[] paddedBytes = qrCodeValue.getBytes(StandardCharsets.UTF_8); // QR kodu doğrudan byte dizisine çevirme

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("paddedQrCodeBytes", paddedBytes);
                    setResult(Activity.RESULT_OK, returnIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "QR kodu işleme hatası. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // QR kod taranmadı veya boş değere sahip
                Toast.makeText(this, "QR kod taratılmadı. Lütfen yeniden deneyin.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // result null, yani QR kod okuyucu açılmamış olabilir
            Toast.makeText(this, "QR kod taratma işlemi başarısız. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private byte[] intToPadded128BitArray(int value) {
        // Integer değeri byte dizisine çeviriyoruz (4 byte)
        byte[] intBytes = new byte[4];
        intBytes[3] = (byte) (value & 0xFF);
        intBytes[2] = (byte) ((value >> 8) & 0xFF);
        intBytes[1] = (byte) ((value >> 16) & 0xFF);
        intBytes[0] = (byte) ((value >> 24) & 0xFF);

        // 128-bit için 16 byte uzunluğunda bir dizi oluşturuyoruz
        byte[] paddedBytes = new byte[16];

        // Integer byte dizisini en sağa yerleştiriyoruz ve geri kalanı 0 ile dolu kalıyor
        System.arraycopy(intBytes, 0, paddedBytes, 16 - intBytes.length, intBytes.length);

        return paddedBytes;
    }

}

