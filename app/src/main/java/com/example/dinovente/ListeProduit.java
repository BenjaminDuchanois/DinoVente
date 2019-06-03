package com.example.dinovente;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ListeProduit extends ArrayAdapter<Produit> {

    private Activity context;
    private List<Produit> produitList;
    private String userID, nom, prenom;
    private FirebaseAuth auth;
    private DatabaseReference donnee;

    public ListeProduit(Activity context, List<Produit> produitList){
        super(context, R.layout.list_produit, produitList);
        this.context = context;
        this.produitList = produitList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_produit, null, true);

        TextView quantité = listViewItem.findViewById(R.id.list_quantite);
        TextView catégorie = listViewItem.findViewById(R.id.list_catégorie);
        TextView nom = listViewItem.findViewById(R.id.list_produit);
        TextView valeur = listViewItem.findViewById(R.id.list_valeur);
        TextView valeurTotale = listViewItem.findViewById(R.id.list_valeurTotale);
        ImageView image = listViewItem.findViewById(R.id.list_image);

        Produit produit = produitList.get(position);


        quantité.setText(Integer.toString(produit.getQuantité()) + " x");
        catégorie.setText(produit.getCatégorie());
        nom.setText(produit.getNom());
        valeur.setText(Double.toString(produit.getValeur()) + "€");
        Double v_valeurTotale = produit.getValeur()*produit.getQuantité()*100;
        v_valeurTotale = (double) v_valeurTotale.intValue() / 100;
        valeurTotale.setText(Double.toString(v_valeurTotale) + "€");
        afficherImage(produit.getCatégorie(), produit.getNom(), image);

        return listViewItem;
    }

    private void afficherImage(String catégorie, String produit, final ImageView image){
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

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images/").child(nom + prenom)
                .child(catégorie + "/" + produit + ".png");

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

                                GlideApp.with(getContext()).load(uri).into(image);

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
}
