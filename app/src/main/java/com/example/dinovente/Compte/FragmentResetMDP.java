package com.example.dinovente.Compte;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dinovente.R;

public class FragmentResetMDP extends Fragment {

    public static com.example.dinovente.Compte.FragmentResetMDP newInstance() {
        return (new com.example.dinovente.Compte.FragmentResetMDP());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accueil, container, false);
    }
}
