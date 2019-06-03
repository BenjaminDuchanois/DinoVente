package com.example.dinovente;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class FragmentSortieStock extends Fragment {

    public static FragmentSortieStock newInstance() {
        return (new FragmentSortieStock());
    }


    private Integer rangProduit, quantité;
    private Integer nvlQuantité;
    private ArrayList<Produit> listeProduit = new ArrayList<>();
    private ArrayList<String> listeNom = new ArrayList<>();
    private ImageView imageView;
    private String userID, nom, prenom;
    private FirebaseAuth auth;
    private Spinner spinNom;
    private DatabaseReference donnee;
    private Button boutonSortie;
    private EditText inputQuantité;
    private boolean estInitialisé = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sortie_stock, container, false);
        String Tag = getTag();
        ((MainActivity)getActivity()).setTagFragmentSortie(Tag);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
        rangProduit = ((MainActivity) getActivity()).getRangProduit();

        donnee = FirebaseDatabase.getInstance().getReference(userID);


        donnee.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                prenom = dataSnapshot.child("Prenom").getValue().toString();
                nom = dataSnapshot.child("Nom").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final Query query = FirebaseDatabase.getInstance().getReference(userID).child("Catégories");
        query.addListenerForSingleValueEvent(valueEventListener);

        spinNom = getView().findViewById(R.id.sortie_NomProduit);
        imageView = getView().findViewById(R.id.sortie_image);
        boutonSortie = getView().findViewById(R.id.sortie_sortir);
        inputQuantité = getView().findViewById(R.id.sortie_quantité);

        spinNom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rangProduit = i;
                ((MainActivity) getActivity()).setRangProduit(rangProduit);
                if(i!=0)
                    ((MainActivity) getActivity()).lanceViewPager(2);
                query.addListenerForSingleValueEvent(valueEventListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        boutonSortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputQuantité.getText().toString().equals(""))
                {
                    quantité = parseInt(inputQuantité.getText().toString());

                    nvlQuantité = listeProduit.get(rangProduit).getQuantité();

                    if(quantité > nvlQuantité)
                    {
                        Toast.makeText(getContext(), "Vous ne pouvez pas sortir plus de produit que ce qu'il y a en stock !", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    nvlQuantité = nvlQuantité - quantité;
                    listeProduit.get(rangProduit).setQuantité(nvlQuantité);


                    donnee.child("Catégories").child(listeProduit.get(rangProduit).catégorie)
                            .child(listeProduit.get(rangProduit).nom).child("QttProduit").setValue(nvlQuantité);

                    if(quantité == 0)
                        Toast.makeText(getContext(), "C'est inutile de sortir 0 produit..", Toast.LENGTH_SHORT).show();
                    else
                        if(quantité == 1)
                            Toast.makeText(getContext(), "1 " + listeProduit.get(rangProduit).getNom() + " a été sorti !", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), quantité + " " + listeProduit.get(rangProduit).getNom() + " ont été sortis !", Toast.LENGTH_SHORT).show();

                    TextView nvlQttMax = getView().findViewById(R.id.sortie_quantitéMax);
                    TextView nvlValStock = getView().findViewById(R.id.sortie_valeurTotale);
                    nvlQttMax.setText("/" + String.valueOf(nvlQuantité));
                    nvlValStock.setText("Valeur totale en stock: " + String.valueOf((nvlQuantité)*listeProduit.get(rangProduit).getValeur()) + "€");
                }
                else
                {
                    Toast.makeText(getContext(), "Oups.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            listeProduit.clear();
            listeNom.clear();

            for (DataSnapshot catégorieSnap : dataSnapshot.getChildren()){
                String catégorie = catégorieSnap.getKey();
                for (DataSnapshot produitSnap : catégorieSnap.getChildren()) {
                    String nom = produitSnap.getKey();
                    Integer quantité = produitSnap.child("QttProduit").getValue(Integer.class);
                    double valeur = produitSnap.child("ValeurProduit").getValue(double.class);

                    Produit produit = new Produit(nom, catégorie, quantité, valeur);

                    listeProduit.add(produit);
                    listeNom.add(nom);
                }
            }

            if (!estInitialisé)
                if(listeProduit.size()>0){
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listeNom);
                    spinNom.setAdapter(adapter);

                    estInitialisé = true;
                    GlideApp.with(imageView.getContext()).clear(imageView);
                }
            changeEntree();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void changeEntree(){

        rangProduit = ((MainActivity) getActivity()).getRangProduit();

        if(rangProduit != null)
            if(listeProduit.size()>0){

            EditText nomCatégorie = getView().findViewById(R.id.sortie_catégorie);
            EditText valeurProduit = getView().findViewById(R.id.sortie_valeur);
            TextView quantitéMax = getView().findViewById(R.id.sortie_quantitéMax);
            TextView valeurTotale = getView().findViewById(R.id.sortie_valeurTotale);



            afficherImage();

            spinNom.setSelection(rangProduit);
            nomCatégorie.setText(listeProduit.get(rangProduit).catégorie);
            valeurProduit.setText(String.valueOf(listeProduit.get(rangProduit).valeur));
            quantitéMax.setText("/" + String.valueOf(listeProduit.get(rangProduit).quantité));
                Double total = listeProduit.get(rangProduit).quantité * listeProduit.get(rangProduit).valeur * 100;
                total = (double) total.intValue() / 100;
                valeurTotale.setText("Valeur totale en stock : " +
                        String.valueOf(total)
                        + "€");
        }
        else {
            Toast.makeText(getContext(), "Veuillez préciser une quantité à sortir.", Toast.LENGTH_SHORT).show();
            }
    }

    private void afficherImage(){

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images/").child(nom + prenom)
                .child(listeProduit.get(rangProduit).catégorie + "/" + listeProduit.get(rangProduit).getNom() + ".png");

        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e){
            e.printStackTrace();
        }

        storageReference.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                GlideApp.with(getContext().getApplicationContext())
                                        .load(uri)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(imageView);

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Glide.with(getContext().getApplicationContext()).clear(imageView);
        Glide.get(getContext().getApplicationContext()).clearMemory();
    }

}

