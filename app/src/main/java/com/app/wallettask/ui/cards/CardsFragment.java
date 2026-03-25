package com.app.wallettask.ui.cards;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.wallettask.R;
import com.app.wallettask.databinding.FragmentCardsBinding;

import android.widget.Toast;
import com.app.wallettask.data.Card;
import com.app.wallettask.data.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import androidx.activity.result.ActivityResultLauncher;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.UUID;

public class CardsFragment extends Fragment {
    private FragmentCardsBinding binding;
    private FirebaseFirestore db;
    private String currentUid;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    processTransfer(result.getContents());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCardsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupInputFormatters();

        binding.finalAddCardButton.setOnClickListener(v -> saveCard());
        binding.scanQrIcon.setOnClickListener(v -> startScanning());

        loadExistingCard();

        return binding.getRoot();
    }

    private void setupInputFormatters() {

        binding.cardNumberEdit.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isDeleting = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCardTypeLogo(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isDeleting) return;
                int len = s.length();
                if (len > 0 && (len == 4 || len == 9 || len == 14)) {
                    s.append("-");
                }
                if (len > 19) {
                    s.delete(19, len);
                }
            }
        });


        binding.cardExpiryEdit.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isDeleting = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isDeleting) return;
                int len = s.length();
                if (len == 2) {
                    try {
                        int month = Integer.parseInt(s.toString());
                        if (month < 1 || month > 12) {
                            s.clear();
                            Toast.makeText(getContext(), "Invalid month (01-12)", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        s.clear();
                        return;
                    }
                    s.append("/");
                }
                if (len > 5) {
                    s.delete(5, len);
                }
            }
        });


        binding.cardCvvEdit.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(3)});
    }

    private void loadExistingCard() {
        db.collection("cards").document(currentUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.app.wallettask.data.Card card = documentSnapshot.toObject(com.app.wallettask.data.Card.class);
                        if (card != null) {
                            binding.cardNumberEdit.setText(card.getCardNumber());
                            binding.cardHolderEdit.setText(card.getHolderName());
                            binding.cardExpiryEdit.setText(card.getExpiryDate());
                            binding.cardCvvEdit.setText(card.getCvv());
                            updateCardTypeLogo(card.getCardNumber());
                            binding.savedCardNumberTop.setText(card.getCardNumber());
                            binding.savedCardNumberBottom.setText(card.getCardNumber());
                            binding.savedCardTop.setVisibility(View.VISIBLE);
                            binding.savedCardBottom.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void saveCard() {
        String number = binding.cardNumberEdit.getText().toString();
        String holder = binding.cardHolderEdit.getText().toString();
        String expiry = binding.cardExpiryEdit.getText().toString();
        String cvv = binding.cardCvvEdit.getText().toString();

        if (number.length() < 19) {
            Toast.makeText(getContext(), "Invalid Card Number (XXXX-XXXX-XXXX-XXXX)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (holder.isEmpty()) {
            Toast.makeText(getContext(), "Please enter card holder name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (expiry.length() < 5) {
            Toast.makeText(getContext(), "Invalid Expiry Date (MM/YY)", Toast.LENGTH_SHORT).show();
            return;
        } else {
            int month = Integer.parseInt(expiry.substring(0, 2));
            if (month < 1 || month > 12) {
                Toast.makeText(getContext(), "Invalid Month (01-12)", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (cvv.length() < 3) {
            Toast.makeText(getContext(), "Invalid CVV (3 digits)", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();
        com.app.wallettask.data.Card card = new com.app.wallettask.data.Card(id, currentUid, number, holder, expiry, cvv);

        db.collection("cards").document(currentUid).set(card)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Card updated", Toast.LENGTH_SHORT).show();
                    updateCardTypeLogo(number);
                    binding.savedCardNumberTop.setText(number);
                    binding.savedCardNumberBottom.setText(number);
                    binding.savedCardTop.setVisibility(View.VISIBLE);
                    binding.savedCardBottom.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCardTypeLogo(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            binding.cardTypeLogo.setImageResource(R.drawable.ic_visa_white);
        } else {
            binding.cardTypeLogo.setImageResource(R.drawable.ic_mastercard);
        }
    }

    private void startScanning() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan another user's QR code");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void processTransfer(String scannedData) {
        String targetUid;
        String targetEmail;
        try {
            org.json.JSONObject qrJson = new org.json.JSONObject(scannedData);
            targetUid = qrJson.getString("uid");
            targetEmail = qrJson.getString("email");
        } catch (Exception e) {
            targetUid = scannedData.trim();
            targetEmail = "Unknown User";
        }

        String amountStr = binding.transferAmountEdit.getText().toString();
        if (amountStr.isEmpty()) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setTitle("Transfer to: " + targetEmail);
            builder.setMessage("Enter Amount:");
            final android.widget.EditText input = new android.widget.EditText(getContext());
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(input);
            String finalTargetUid = targetUid;
            builder.setPositiveButton("Transfer", (dialog, which) -> {
                String amtObj = input.getText().toString();
                if (!amtObj.isEmpty()) {
                    executeTransfer(finalTargetUid, Double.parseDouble(amtObj));
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        } else {
            executeTransfer(targetUid, Double.parseDouble(amountStr));
        }
    }

    private void executeTransfer(String rawTargetUid, double amount) {
        String targetUid = rawTargetUid.trim();
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot senderDoc = transaction.get(db.collection("users").document(currentUid));
            com.google.firebase.firestore.DocumentSnapshot receiverDoc = transaction.get(db.collection("users").document(targetUid));

            if (!receiverDoc.exists() && (targetUid.length() < 20 || targetUid.length() > 35)) {
                throw new RuntimeException("Invalid QR Code FORMAT");
            }
            if (currentUid.equals(targetUid)) throw new RuntimeException("Cannot transfer to yourself");

            Double sBalance = senderDoc.getDouble("balance");
            double senderBalance = sBalance != null ? sBalance : 0.0;
            
            if (senderBalance < amount) throw new RuntimeException("Insufficient balance");

            Double rBalance = receiverDoc.getDouble("balance");
            double receiverBalance = rBalance != null ? rBalance : 0.0;

            java.util.Map<String, Object> updateSender = new java.util.HashMap<>();
            updateSender.put("balance", senderBalance - amount);
            updateSender.put("uid", currentUid);
            transaction.set(db.collection("users").document(currentUid), updateSender, com.google.firebase.firestore.SetOptions.merge());
            
            java.util.Map<String, Object> updateTarget = new java.util.HashMap<>();
            updateTarget.put("balance", receiverBalance + amount);
            updateTarget.put("uid", targetUid);
            transaction.set(db.collection("users").document(targetUid), updateTarget, com.google.firebase.firestore.SetOptions.merge());

            String senderEmail = senderDoc.getString("email");
            String receiverEmail = receiverDoc.getString("email");
            if (senderEmail == null) senderEmail = currentUid;
            if (receiverEmail == null) receiverEmail = targetUid;

            String t1Id = UUID.randomUUID().toString();
            String t2Id = UUID.randomUUID().toString();
            
            Transaction tSender = new Transaction(t1Id, currentUid, "Expend", "Transfer", "Sent to " + receiverEmail, amount, System.currentTimeMillis());
            Transaction tReceiver = new Transaction(t2Id, targetUid, "Income", "Transfer", "Received from " + senderEmail, amount, System.currentTimeMillis());

            transaction.set(db.collection("transactions").document(t1Id), tSender);
            transaction.set(db.collection("transactions").document(t2Id), tReceiver);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Transfer successful", Toast.LENGTH_SHORT).show();
            binding.transferAmountEdit.setText("");
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Transfer failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
