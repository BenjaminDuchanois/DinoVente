package com.example.dinovente.Compte;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.dinovente.MainActivity;
import com.example.dinovente.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Inscription extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPassword2, inputName, inputFirstName;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.InscInscription);
        btnSignUp = (Button) findViewById(R.id.InscVersConnexion);
        inputEmail = (EditText) findViewById(R.id.InscEmail);
        inputPassword = (EditText) findViewById(R.id.InscMotPasse);
        inputPassword2 = (EditText) findViewById(R.id.InscMotPasse2);
        inputFirstName = (EditText) findViewById(R.id.InscPrenom);
        inputName = (EditText) findViewById(R.id.InscNom);
        progressBar = (ProgressBar) findViewById(R.id.InscProgressBar);
        btnResetPassword = (Button) findViewById(R.id.InscMDPOublie);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inscription.this, FragmentResetMDP.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String password2 = inputPassword2.getText().toString().trim();
                final String firstName = inputFirstName.getText().toString().trim();
                final String name = inputName.getText().toString().trim();


                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Veuillez entrer votre adresse mail !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Veuillez entrer un mot de passe !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(getApplicationContext(), "Veuillez indiquer votre prénom !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "Veuillez indiquer votre nom !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Le mot de passe doit avoir plus de 6 caractères.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password2)) {
                    Toast.makeText(getApplicationContext(), "Veuillez retaper le mot de passe !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(password2)) {
                    Toast.makeText(getApplicationContext(), "Vous avez mal retapé le mot de passe !", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Inscription.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(Inscription.this, "Le compte a bien été crée !", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(Inscription.this, "L'inscription a échouée" + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Intent Inscription = new Intent(Inscription.this, MainActivity.class);
                                        Inscription.putExtra("Nom", name);
                                        Inscription.putExtra("Prenom", firstName);
                                        startActivity(Inscription);
                                        finish();
                                    }
                                }
                            });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

}
