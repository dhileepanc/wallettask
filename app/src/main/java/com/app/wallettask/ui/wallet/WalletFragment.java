package com.app.wallettask.ui.wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.wallettask.R;
import com.app.wallettask.databinding.FragmentWalletBinding;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.app.wallettask.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment {
    private FragmentWalletBinding binding;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        checkAndInitializeUser();
        setupRecyclerView();
        listenToBalance();

        binding.addWalletButton.setOnClickListener(v -> {
            new AddWalletDialog().show(getChildFragmentManager(), "AddWallet");
        });

        binding.settingsIcon.setOnClickListener(v -> {

            androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_profile);
        });

        return binding.getRoot();
    }

    private void checkAndInitializeUser() {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                User user = new User(uid, email, 0.0, uid);
                db.collection("users").document(uid).set(user);
            }
        });
    }

    private com.google.firebase.firestore.ListenerRegistration txListener;

    private void setupRecyclerView() {
        binding.walletMethodsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        
        txListener = db.collection("transactions")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<com.app.wallettask.data.Transaction> list = value.toObjects(com.app.wallettask.data.Transaction.class);
                        java.util.Map<String, Double> subtotals = new java.util.HashMap<>();
                        
                        for (com.app.wallettask.data.Transaction t : list) {
                            if ("Transfer".equals(t.getMethod())) continue;
                            
                            double amount = t.getAmount();
                            if ("Expend".equals(t.getType())) amount = -amount;
                            subtotals.put(t.getMethod(), subtotals.getOrDefault(t.getMethod(), 0.0) + amount);
                        }

                        List<WalletMethodAdapter.WalletMethod> methods = new ArrayList<>();
                        for (String methodName : subtotals.keySet()) {
                            double total = subtotals.get(methodName);
                            String prefix = total >= 0 ? "+ $" : "- $";
                            methods.add(new WalletMethodAdapter.WalletMethod(methodName, "Total via " + methodName, prefix + String.format("%.2f", Math.abs(total))));
                        }
                        
                        if (binding != null) {
                            binding.walletMethodsRecycler.setAdapter(new WalletMethodAdapter(methods));
                        }
                    }
                });
    }

    private com.google.firebase.firestore.ListenerRegistration balanceListener;

    private void listenToBalance() {
        balanceListener = db.collection("users").document(uid).addSnapshotListener((value, error) -> {
            if (value != null && value.exists()) {
                User user = value.toObject(User.class);
                if (user != null && binding != null) {
                    binding.balanceText.setText("$" + String.format("%.2f", user.getBalance()));
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (txListener != null) txListener.remove();
        if (balanceListener != null) balanceListener.remove();
        binding = null;
    }
}
