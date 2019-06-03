package com.example.dinovente;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.dinovente.Compte.Connexion;
import com.example.dinovente.Compte.Inscription;
import com.example.dinovente.Compte.FragmentResetMDP;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public boolean AccueilIsVisible;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private ViewPager pager;
    private TabLayout tabLayout;
    private FrameLayout frameLayout;

    private FirebaseAuth auth;
    private DatabaseReference donnee;
    private String userID;
    public int rangProduitGeneral = 0;
    private String tagFragmentEntree, tagFragmentSortie, tagFragmentModif;

    private Fragment fragmentAccueil, fragmentResetMDP, fragmentListe, fragmentSortie;

    private TextView nomPrenom, mail;

    private static final int FRAGMENT_ACCUEIL = 0;
    private static final int FRAGMENT_ENTREE = 1;
    private static final int FRAGMENT_SORTIE = 2;
    private static final int FRAGMENT_RESETMDP = 3;
    private static final int FRAGMENT_LISTE = 4;
    private static final int FRAGMENT_NOUVEAU = 5;
    private static final int FRAGMENT_MODIFPRODUIT = 6;

    public String getTagFragmentEntree() {
        return tagFragmentEntree;
    }

    public String getTagFragmentModif() {
        return tagFragmentModif;
    }

    public String getTagFragmentSortie() {
        return tagFragmentSortie;
    }

    public void setTagFragmentEntree(String FragmentEntree) {
        tagFragmentEntree = FragmentEntree;
        return;
    }

    public void setTagFragmentSortie(String FragmentSortie) {
        tagFragmentSortie = FragmentSortie;
        return;
    }

    public void setTagFragmentModif(String FragmentModif) {
        tagFragmentModif = FragmentModif;
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final String prenom, nom;
        final Intent getDonnee = getIntent();
        prenom = getDonnee.getStringExtra("Prenom");
        nom = getDonnee.getStringExtra("Nom");

        tabLayout = findViewById(R.id.tabLayout);
        pager = findViewById(R.id.view_pager);
        frameLayout = findViewById(R.id.frame_layout);

        this.lanceViewPager(0);
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();

        this.showFirstFragment();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null) {

            userID = auth.getCurrentUser().getUid();

            donnee = FirebaseDatabase.getInstance().getReference(userID);

            donnee.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.child("Prenom").getValue() == null) && (prenom != null))
                        donnee.child("Prenom").setValue(prenom);
                    if ((dataSnapshot.child("Nom").getValue() == null) && (nom != null))
                        donnee.child("Nom").setValue(nom);
                    if (dataSnapshot.child("Mail").getValue() == null)
                        donnee.child("Mail").setValue(auth.getCurrentUser().getEmail());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    public int getRangProduit(){
        return rangProduitGeneral;
    }

    public void setRangProduit(int nvRang){
        rangProduitGeneral = nvRang;
    }

    public void lanceViewPager(int page){
        tabSwitchAdapter tabSwitchAdapter = new tabSwitchAdapter(getSupportFragmentManager());

        if((!pager.isShown())) {
            pager.setAdapter(tabSwitchAdapter);
            pager.setOffscreenPageLimit(4);
            tabLayout.setupWithViewPager(pager);
        }
        if (rangProduitGeneral!=0) {
            Fragment fragment1 = tabSwitchAdapter.getItem(1);
            ((FragmentEntreeStock) fragment1).changeEntree();
            Fragment fragment2 = tabSwitchAdapter.getItem(2);
            ((FragmentSortieStock)fragment2).changeEntree();
            Fragment fragment3 = tabSwitchAdapter.getItem(3);
            ((FragmentModifStock)fragment3).changeEntree();
        }

        pager.setCurrentItem(page);
    }

    public void EntreeDeStock(View v){
        this.showFragment(FRAGMENT_ENTREE);
    }

    public void SortieDeStock(View v){
        this.showFragment(FRAGMENT_SORTIE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(!AccueilIsVisible)
                showFragment(FRAGMENT_ACCUEIL);
            else
                super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        if (auth.getCurrentUser() != null) {
            navigationView.getMenu().getItem(1).setVisible(true);
            navigationView.getMenu().getItem(2).setVisible(false);
            navigationView.getMenu().getItem(3).setVisible(true);
        }
        else
        {
            navigationView.getMenu().getItem(1).setVisible(false);
            navigationView.getMenu().getItem(2).setVisible(true);
            navigationView.getMenu().getItem(3).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_parametres) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_connexion:
                startActivity(new Intent(MainActivity.this, Connexion.class));
                break;
            case R.id.nav_inscription:
                startActivity(new Intent(MainActivity.this, Inscription.class));
                break;
            case R.id.nav_deconnexion:
                auth.signOut();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                break;
            case R.id.nav_accueil:
                this.showFragment(FRAGMENT_ACCUEIL);
                break;
            case R.id.nav_nouveau:
                this.showFragment(FRAGMENT_NOUVEAU);
                break;
            case R.id.nav_entrer:
                this.showFragment(FRAGMENT_ENTREE);
                break;
            case R.id.nav_sortir:
                this.showFragment(FRAGMENT_SORTIE);
                break;
            case R.id.nav_liste:
                this.showFragment(FRAGMENT_LISTE);
                break;
            case R.id.nav_modif_produit:
                this.showFragment(FRAGMENT_MODIFPRODUIT);
                break;
            default: break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(int fragmentIdentifier){
        switch (fragmentIdentifier){
            case FRAGMENT_ACCUEIL:
                AccueilIsVisible = true;
                this.showAccueil();
                break;
            case FRAGMENT_NOUVEAU:
                AccueilIsVisible = false;
                this.showNouveau();
                break;
            case FRAGMENT_ENTREE:
                AccueilIsVisible = false;
                this.showEntreeStock();
                break;
            case FRAGMENT_SORTIE:
                AccueilIsVisible = false;
                this.showSortieStock();
                break;
            case FRAGMENT_RESETMDP:
                AccueilIsVisible = false;
                this.showResetMDP();
                break;
            case FRAGMENT_LISTE:
                AccueilIsVisible = false;
                this.showListeProduits();
                break;
            case FRAGMENT_MODIFPRODUIT:
                AccueilIsVisible = false;
                this.showModifProduit();
                break;
            default:
                break;
        }
    }

    private void showAccueil(){
        pager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        if (this.fragmentAccueil == null) this.fragmentAccueil = FragmentAccueil.newInstance();
        this.startTransactionFragment(this.fragmentAccueil);
    }

    private void showNouveau(){
        showViewPager();
        lanceViewPager(0);
    }

    private void showEntreeStock(){
        showViewPager();
        lanceViewPager(1);
    }

    private void showSortieStock(){
        showViewPager();
        lanceViewPager(2);
    }

    private void showModifProduit(){
        showViewPager();
        lanceViewPager(3);
    }

    public void showViewPager(){
        tabLayout.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
    }

    private void showResetMDP(){
        pager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        if (this.fragmentResetMDP == null) this.fragmentResetMDP = FragmentResetMDP.newInstance();
        this.startTransactionFragment(this.fragmentResetMDP);
    }

    private void showListeProduits(){
        pager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        if (this.fragmentListe == null) this.fragmentListe = FragmentListeProduits.newInstance();
        this.startTransactionFragment(this.fragmentListe);
    }

    private void showFirstFragment(){
        pager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        Fragment visibleFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (visibleFragment == null){
            this.showFragment(FRAGMENT_ACCUEIL);
        }
    }


    private void startTransactionFragment(Fragment fragment){
        if (!fragment.isVisible()){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, fragment).commit();
        }
    }

    public void configureToolBar(){
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void configureDrawerLayout(){
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mail = findViewById(R.id.nav_mail);
                nomPrenom = findViewById(R.id.nav_nomprenom);
                if (userID!=null){
                    donnee.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mail.setText(auth.getCurrentUser().getEmail());
                            nomPrenom.setText(dataSnapshot.child("Nom").getValue().toString() + " " + dataSnapshot.child("Prenom").getValue());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
}


    public void configureNavigationView(){
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void setAccueilVisible(Boolean Visibilité){
        AccueilIsVisible = Visibilité;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}
