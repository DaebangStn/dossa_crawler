package com.example.dossa_crawler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dossa_crawler.databinding.FragmentFirstBinding;

import java.util.ArrayList;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.switchCrawl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                EditText editText = (EditText) getView().findViewById(R.id.L1);

                Intent intent = new Intent(getContext(), crawler_service.class);
                if(checked) {
                    editText.setFocusable(false);
                    editText.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
                    editText.setClickable(false); // user navigates with wheel and selects widget

                    QueryStringViewModel queryStringViewModel = new ViewModelProvider(requireActivity()).get(QueryStringViewModel.class);
                    LinearLayout linearLayout = queryStringViewModel.linearLayout;

                    Log.d("first_frag", "received linear layout");

                    ArrayList<CharSequence> chrs = new ArrayList<>();
                    for(int i=0; i<linearLayout.getChildCount(); i++){
                        View tv = linearLayout.getChildAt(i);
                        if(tv instanceof TextView){
                            chrs.add(((TextView) tv).getText().toString());
                        }
                        Log.d("first_frag", "added " + ((TextView) tv).getText().toString());
                    }

                    intent.putExtra("period", editText.getText().toString());
                    intent.putExtra("query", chrs);

                    getContext().startForegroundService(intent);
                }
                else {
                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true); // user touches widget on phone with touch screen
                    editText.setClickable(true); // user navigates with wheel and selects widget
                    getContext().stopService(intent);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}