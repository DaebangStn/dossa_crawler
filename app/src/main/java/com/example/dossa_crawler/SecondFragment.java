package com.example.dossa_crawler;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dossa_crawler.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = (LinearLayout) getView().findViewById((R.id.linearLayout2));
                EditText editText = (EditText) getView().findViewById(R.id.textInputEditText);

                TextView textView = new TextView(getContext());
                textView.setGravity(Gravity.CENTER);

                String str = editText.getText().toString();
                textView.setText(editText.getText());

                linearLayout.addView(textView);
                QueryStringViewModel queryStringViewModel = new ViewModelProvider(requireActivity()).get(QueryStringViewModel.class);
                queryStringViewModel.linearLayout = linearLayout;
                editText.setText(null);
            }
        });

        binding.button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = (LinearLayout) getView().findViewById((R.id.linearLayout2));
                linearLayout.removeViewAt(0);
                QueryStringViewModel queryStringViewModel = new ViewModelProvider(requireActivity()).get(QueryStringViewModel.class);
                queryStringViewModel.linearLayout = linearLayout;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}