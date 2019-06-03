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

public class Connexion extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        auth = FirebaseAuth.getInstance();

        //Si il y a un utilisateur connecté, redirige vert le Main
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(Connexion.this, MainActivity.class));
            finish();
        }

        //Formulaire
        inputEmail = (EditText) findViewById(R.id.ConnEmail);
        inputPassword = (EditText) findViewById(R.id.ConnMotPasse);
        progressBar = (ProgressBar) findViewById(R.id.ConnProgressBar);
        btnSignup = (Button) findViewById(R.id.ConnVersInscription);
        btnLogin = (Button) findViewById(R.id.ConnConnexion);
        btnReset = (Button) findViewById(R.id.ConnMDPOublie);

        //Redirections
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Connexion.this, Inscription.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Connexion.this, FragmentResetMDP.class));
            }
        });

        //Vérifie que tout soit bien entrer puis vérifie les identifiants
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Veuillez entrer votre adresse mail.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Veuillez entrer votre mot de passe.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Connexion.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {

                                    if (password.length() < 6) {
                                        inputPassword.setError("Le mot de passe doit avoir au moins 6 caractères.");
                                    } else {
                                        Toast.makeText(Connexion.this, "La connexion a échouée", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Intent intent = new Intent(Connexion.this, MainActivity.class);
                                    startActivity(intent);
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
