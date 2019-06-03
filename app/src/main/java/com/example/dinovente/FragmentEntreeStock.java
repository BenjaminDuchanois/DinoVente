package com.example.dinovente;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

public class FragmentEntreeStock extends Fragment {

    public static FragmentEntreeStock newInstance() {
        return (new FragmentEntreeStock());
    }

    protected int rangProduit = 0, quantité;
    private Integer nvlQuantité;
    private ArrayList<Produit> listeProduit = new ArrayList<>();
    private ArrayList<String> listeNom = new ArrayList<>();
    private ImageView imageView;
    private String userID, nom, prenom;
    private FirebaseAuth auth;
    private Spinner spinNom;
    private DatabaseReference donnee;
    private Button boutonEntree;
    private EditText inputQuantité;
    private boolean estInitialisé = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entree_stock, container, false);
        String Tag = getTag();
        ((MainActivity)getActivity()).setTagFragmentEntree(Tag);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();

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

        spinNom = getView().findViewById(R.id.entree_NomProduit);
        imageView = getView().findViewById(R.id.entree_image);
        boutonEntree = getView().findViewById(R.id.entree_sortir);
        inputQuantité = getView().findViewById(R.id.entree_quantité);

        spinNom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rangProduit = i;
                ((MainActivity) getActivity()).setRangProduit(rangProduit);
                query.addListenerForSingleValueEvent(valueEventListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        boutonEntree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputQuantité.getText().toString().equals(""))
                {
                    quantité = parseInt(inputQuantité.getText().toString());

                    nvlQuantité = listeProduit.get(rangProduit).getQuantité();

                    nvlQuantité = nvlQuantité + quantité;
                    listeProduit.get(rangProduit).setQuantité(nvlQuantité);


                    donnee.child("Catégories").child(listeProduit.get(rangProduit).catégorie)
                            .child(listeProduit.get(rangProduit).nom).child("QttProduit").setValue(nvlQuantité);

                    if(quantité == 0)
                        Toast.makeText(getContext(), "C'est inutile d'entrer 0 produit..", Toast.LENGTH_SHORT).show();
                    else
                        if(quantité == 1)
                            Toast.makeText(getContext(), "1 " + listeProduit.get(rangProduit).getNom() + " a été ajouté !", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), quantité + " " + listeProduit.get(rangProduit).getNom() + " ont été ajoutés !", Toast.LENGTH_SHORT).show();

                    TextView nvlQttMax = getView().findViewById(R.id.entree_quantitéMax);
                    TextView nvlValStock = getView().findViewById(R.id.entree_valeurTotale);
                    nvlQttMax.setText("/" + String.valueOf(nvlQuantité));
                    nvlValStock.setText("Valeur totale en stock: " + String.valueOf((nvlQuantité)*listeProduit.get(rangProduit).getValeur()) + "€");
                }
                else
                {
                    Toast.makeText(getContext(), "Veuillez préciser une quantité à entrer.", Toast.LENGTH_SHORT).show();
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

            if(!estInitialisé)
            {ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listeNom);
            spinNom.setAdapter(adapter);
            estInitialisé=true;}

            GlideApp.with(imageView.getContext()).clear(imageView);
            changeEntree();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void changeEntree(){

        this.rangProduit = ((MainActivity) getActivity()).getRangProduit();

            EditText nomCatégorie = getView().findViewById(R.id.entree_catégorie);
            EditText valeurProduit = getView().findViewById(R.id.entree_valeur);
            TextView quantitéMax = getView().findViewById(R.id.entree_quantitéMax);
            TextView valeurTotale = getView().findViewById(R.id.entree_valeurTotale);


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

                                imageView.setDrawingCacheEnabled(true);
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

