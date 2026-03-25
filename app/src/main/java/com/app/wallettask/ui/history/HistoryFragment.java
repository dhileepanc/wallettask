package com.app.wallettask.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.app.wallettask.databinding.FragmentHistoryBinding;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.app.wallettask.data.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";
    private FragmentHistoryBinding binding;
    private FirebaseFirestore db;
    private String uid;
    private TransactionAdapter adapter;
    private List<Transaction> transactionsList = new ArrayList<>();
    private Map<String, String> emailCache = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupRecyclerView();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(transactionsList);
        binding.historyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.historyRecycler.setAdapter(adapter);
    }

    private com.google.firebase.firestore.ListenerRegistration userListener;
    private com.google.firebase.firestore.ListenerRegistration txListener;

    private void loadTransactions() {
        // Remove any existing listeners first to prevent duplicates
        if (userListener != null) userListener.remove();
        if (txListener != null) txListener.remove();

        userListener = db.collection("users").document(uid).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                Double balance = snapshot.getDouble("balance");
                if (balance != null && binding != null) {
                    binding.totalBalanceText.setText(String.format("%,.2f $", balance).replace(",", " "));
                }
            }
        });

        txListener = db.collection("transactions")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Transaction listener error: ", error);
                        return;
                    }
                    if (value != null && binding != null) {
                        transactionsList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Transaction t = doc.toObject(Transaction.class);
                            if (t != null) {
                                transactionsList.add(t);
                            }
                        }
                        // Sort locally by timestamp descending
                        transactionsList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                        // Resolve UIDs to emails for Transfer transactions
                        resolveTransactionNames();
                    }
                });
    }

    private void resolveTransactionNames() {
        Set<String> uidsToResolve = new HashSet<>();

        for (Transaction t : transactionsList) {
            if ("Transfer".equals(t.getMethod())) {
                String extractedId = extractUidFromDescription(t.getDescription());
                if (extractedId != null && !emailCache.containsKey(extractedId)) {
                    uidsToResolve.add(extractedId);
                }
            }
        }

        if (uidsToResolve.isEmpty()) {
            // All names already resolved or no transfers, update adapter
            applyResolvedNames();
            return;
        }

        // Look up each unknown UID from Firestore
        final int[] remaining = {uidsToResolve.size()};
        for (String resolveUid : uidsToResolve) {
            db.collection("users").document(resolveUid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            String email = task.getResult().getString("email");
                            if (email != null) {
                                emailCache.put(resolveUid, email);
                            }
                        }
                        remaining[0]--;
                        if (remaining[0] <= 0) {
                            applyResolvedNames();
                        }
                    });
        }
    }

    private void applyResolvedNames() {
        for (Transaction t : transactionsList) {
            if ("Transfer".equals(t.getMethod())) {
                String extractedId = extractUidFromDescription(t.getDescription());
                if (extractedId != null && emailCache.containsKey(extractedId)) {
                    String email = emailCache.get(extractedId);
                    String desc = t.getDescription();
                    t.setDescription(desc.replace(extractedId, email));
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Extracts a UID from descriptions like "Sent to <uid>" or "Received from <uid>".
     * Returns null if the value after the prefix already looks like an email.
     */
    private String extractUidFromDescription(String description) {
        if (description == null) return null;
        String id = null;
        if (description.startsWith("Sent to ")) {
            id = description.substring("Sent to ".length()).trim();
        } else if (description.startsWith("Received from ")) {
            id = description.substring("Received from ".length()).trim();
        }
        // If it contains '@', it's already an email, no need to resolve
        if (id != null && id.contains("@")) {
            return null;
        }
        return id;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (userListener != null) userListener.remove();
        if (txListener != null) txListener.remove();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
