package com.app.wallettask.ui.wallet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.app.wallettask.data.Transaction;
import com.app.wallettask.databinding.DialogAddWalletBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.UUID;

public class AddWalletDialog extends DialogFragment {
    private DialogAddWalletBinding binding;
    private FirebaseFirestore db;
    private String uid;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddWalletBinding.inflate(LayoutInflater.from(getContext()));
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String[] methods = {"Money Cash", "Debit Card", "Bank Account", "Credit Card"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, methods);
        binding.methodSpinner.setAdapter(adapter);

        binding.submitButton.setOnClickListener(v -> submit());

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    private void submit() {
        String method = binding.methodSpinner.getSelectedItem().toString();
        String description = binding.descriptionEdit.getText().toString();
        String amountStr = binding.amountEdit.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String id = UUID.randomUUID().toString();
        Transaction transaction = new Transaction(id, uid, "Income", method, description, amount, System.currentTimeMillis());


        db.runTransaction(task -> {
            task.update(db.collection("users").document(uid), "balance", FieldValue.increment(amount));
            task.set(db.collection("transactions").document(id), transaction);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Added successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
