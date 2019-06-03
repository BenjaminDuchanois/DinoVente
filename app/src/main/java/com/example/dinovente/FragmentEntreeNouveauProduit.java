package com.example.dinovente;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class FragmentEntreeNouveauProduit extends Fragment {

    private Button btnUpload, btnSend;
    private ImageView imageView;

    private EditText inputNom, inputCatégorie, inputValeur, inputQuantité;
    private String nomProduit, catégorieProduit;
    private Double valeurProduit = null;
    private Integer quantitéProduit = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference entreeProduit, donnee;
    private String userID, nom, prenom;
    private FirebaseAuth auth;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;

    public static FragmentEntreeNouveauProduit newInstance() {
        return (new FragmentEntreeNouveauProduit());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entree_nouveau_produit, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
        entreeProduit = FirebaseDatabase.getInstance().getReference().child(userID);

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

        btnSend = (Button) getView().findViewById(R.id.nvproduit_btn_send);
        btnUpload = (Button) getView().findViewById(R.id.nvproduit_btn_upload);
        imageView = (ImageView) getView().findViewById(R.id.nvproduit_image);

        inputCatégorie = (EditText) getView().findViewById(R.id.nvproduit_catégorie);
        inputNom = (EditText) getView().findViewById(R.id.nvproduit_NomProduit);
        inputQuantité = (EditText) getView().findViewById(R.id.nvproduit_quantité);
        inputValeur = (EditText) getView().findViewById(R.id.nvproduit_valeur);


        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nomProduit = inputNom.getText().toString();
                catégorieProduit = inputCatégorie.getText().toString();
                if (!inputQuantité.getText().toString().equals(""))
                    quantitéProduit = parseInt(inputQuantité.getText().toString());
                if (!inputValeur.getText().toString().equals(""))
                    valeurProduit = parseDouble(inputValeur.getText().toString());

                if (TextUtils.isEmpty(nomProduit)) {
                    Toast.makeText(getContext(), "Veuillez donner un nom au produit", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(catégorieProduit)) {
                    Toast.makeText(getContext(), "Veuillez attribuer une catégorie au produit.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (quantitéProduit == null) {
                    Toast.makeText(getContext(), "Veuillez choisir une quantité à entrer.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (valeurProduit == null) {
                    Toast.makeText(getContext(), "Veuillez préciser la valeur unitaire du produit.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(filePath == null){
                   Toast.makeText(getContext(), "Veuillez ajouter une photo du produit.", Toast.LENGTH_SHORT).show();
                   return;
                }

                uploadImage();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/").child(nom + prenom)
                    .child(inputCatégorie.getText().toString() + "/" + inputNom.getText().toString() + ".png");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                            entreeProduit.child("Catégories").child(catégorieProduit).child(nomProduit).child("QttProduit").setValue(quantitéProduit);
                            entreeProduit.child("Catégories").child(catégorieProduit).child(nomProduit).child("ValeurProduit").setValue(valeurProduit);

                            inputValeur.setText("");
                            inputQuantité.setText("");
                            inputNom.setText("");
                            inputCatégorie.setText("");
                            filePath = null;
                            imageView.setVisibility(View.INVISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
