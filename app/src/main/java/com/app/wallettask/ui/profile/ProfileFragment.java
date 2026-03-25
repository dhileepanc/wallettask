package com.app.wallettask.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.app.wallettask.databinding.FragmentProfileBinding;

import android.graphics.Bitmap;
import android.widget.Toast;
import com.app.wallettask.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        binding.userEmailText.setText(email);

        try {
            org.json.JSONObject qrData = new org.json.JSONObject();
            qrData.put("uid", uid);
            qrData.put("email", email);
            generateQRCode(qrData.toString());
        } catch (Exception e) {
            generateQRCode(uid);
        }

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            android.content.Intent intent = new android.content.Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return binding.getRoot();
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 500, 500);
            binding.profileQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error generating QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
