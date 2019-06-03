package com.example.dinovente;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentAccueil extends Fragment {

    private TextView txt_nbProduit, txt_valeurTotale, txt_nb;
    private DatabaseReference donnee;
    private String userID;
    private FirebaseAuth auth;
    private Double valeurTotale;
    private Integer nbProduit, nb;

    public static FragmentAccueil newInstance() {
        return (new FragmentAccueil());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accueil, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();

        if(userID != null)
            donnee = FirebaseDatabase.getInstance().getReference(userID).child("Catégories");

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txt_nbProduit = getView().findViewById(R.id.accueil_nbProduit);
        txt_valeurTotale = getView().findViewById(R.id.accueil_valeurProduit);
        txt_nb = getView().findViewById(R.id.accueil_nb);

        if(userID != null)
            donnee.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    nbProduit = 0;
                    valeurTotale = 0.0;
                    nb = 0;
                    for(DataSnapshot catégorieSnap : dataSnapshot.getChildren())
                    {
                        for (DataSnapshot produitSnap : catégorieSnap.getChildren()) {
                            Integer Qtt = produitSnap.child("QttProduit").getValue(Integer.class);
                            double Val = produitSnap.child("ValeurProduit").getValue(double.class);
                            if(Qtt != 0)
                                nb++;
                            nbProduit = nbProduit + Qtt;
                            valeurTotale = valeurTotale + Qtt*Val;
                        }
                    }
                    valeurTotale =(valeurTotale*100);
                    valeurTotale = (double) valeurTotale.intValue()/100;
                    txt_nbProduit.setText(String.valueOf(nbProduit));
                    txt_valeurTotale.setText(String.valueOf(valeurTotale));
                    txt_nb.setText("(" + String.valueOf(nb) + ")");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
}
