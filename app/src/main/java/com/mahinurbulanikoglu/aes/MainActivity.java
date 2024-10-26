package com.mahinurbulanikoglu.aes;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.mahinurbulanikoglu.aes.databinding.ActivityMainBinding;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_AUDIO_PICK = 2;

    private TextView encryptedTextView, decryptedTextView;
    private String qrCodeValue;
    private Spinner securityLevelSpinner;
    private byte[] selectedImageData;
    private byte[] selectedAudioData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        encryptedTextView = findViewById(R.id.encryptedTextView);
        decryptedTextView = findViewById(R.id.decryptedTextView);
        securityLevelSpinner = findViewById(R.id.securityLevelSpinner);

        Button qrButton = findViewById(R.id.btnScanQR);
        Button encryptButton = findViewById(R.id.btnEncrypt);
        Button decryptButton = findViewById(R.id.btnDecrypt);
        Button selectImageButton = findViewById(R.id.btnSelectImage);
        Button selectAudioButton = findViewById(R.id.btnSelectAudio);

        qrButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QRCodeActivity.class);
            startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
        });

        encryptButton.setOnClickListener(v -> {
            try {
                if (qrCodeValue == null  ) {
                    int bitLength = getSelectedSecurityLevel();
                    SecretKey key = AESUtils.generateKey(qrCodeValue, bitLength);

                    if (selectedImageData != null) {
                        // Görüntü şifreleme
                        String encryptedImageData = AESUtils.encryptBytes(selectedImageData, key);
                        encryptedTextView.setText(encryptedImageData);
                    } else {
                        Toast.makeText(this, "Lütfen önce bir görüntü seçin.", Toast.LENGTH_SHORT).show();
                    }

                    if (selectedAudioData != null) {
                        // Ses şifreleme
                        String encryptedAudioData = AESUtils.encryptBytes(selectedAudioData, key);
                        // Şifrelenmiş ses verisini bir TextView veya başka bir yöntemle gösterebilirsiniz
                    } else {
                        Toast.makeText(this, "Lütfen önce bir ses dosyası seçin.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Lütfen önce QR kodunu tarayın.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        decryptButton.setOnClickListener(v -> {
            try {
                if (qrCodeValue != null) {
                    int bitLength = getSelectedSecurityLevel();
                    SecretKey key = AESUtils.generateKey(qrCodeValue, bitLength);
                    String encryptedData = encryptedTextView.getText().toString();

                    // Görüntü çözme
                    byte[] decryptedImageData = AESUtils.decryptBytes(encryptedData, key);
                    Bitmap decryptedBitmap = BitmapFactory.decodeByteArray(decryptedImageData, 0, decryptedImageData.length);
                    displayImage(decryptedBitmap);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // selectImageButton.setOnClickListener(v -> selectImage());
        // Galeriye erişim için butona tıklama dinleyicisi
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
        selectAudioButton.setOnClickListener(v -> selectAudio());
    }

    private int getSelectedSecurityLevel() {
        String selected = securityLevelSpinner.getSelectedItem().toString();
        switch (selected) {
            case "192-bit":
                return 192;
            case "256-bit":
                return 256;
            default:
                return 128;
        }
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    public void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_AUDIO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_QR_SCAN && resultCode == RESULT_OK) {
            qrCodeValue = data.getStringExtra("qrCodeValue");
        }

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                selectedImageData = bitmapToByteArray(bitmap);
                displayImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_AUDIO_PICK && resultCode == RESULT_OK) {
            Uri selectedAudio = data.getData();
            try {
                selectedAudioData = audioToByteArray(selectedAudio);
                playAudio(selectedAudioData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private byte[] audioToByteArray(Uri audioUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(audioUri);
            byte[] audioBytes = new byte[inputStream.available()];
            inputStream.read(audioBytes);
            inputStream.close();
            return audioBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void playAudio(byte[] audioData) {
        try {
            File tempFile = File.createTempFile("audio", ".mp3", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(audioData);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayImage(Bitmap bitmap) {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }
}



