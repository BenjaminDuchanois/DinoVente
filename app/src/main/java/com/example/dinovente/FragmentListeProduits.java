package com.example.dinovente;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TableLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FragmentListeProduits extends Fragment {

    private ListView listView;
    private ArrayList<Produit> listeProduit = new ArrayList<>();
    private String userID, rangProduit;
    private FirebaseAuth auth;

    public static FragmentListeProduits newInstance() {
        return (new FragmentListeProduits());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liste_produits, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) getView().findViewById(R.id.list_listview);
        listeProduit = new ArrayList<>();


        Query query = FirebaseDatabase.getInstance().getReference(userID).child("Catégories");
        query.addListenerForSingleValueEvent(valueEventListener);

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parentView, View childView, int position, long id) {

                TabLayout tabLayout = parentView.getRootView().findViewById(R.id.tabLayout);
                ViewPager pager = parentView.getRootView().findViewById(R.id.view_pager);
                FrameLayout frameLayout = parentView.getRootView().findViewById(R.id.frame_layout);

                ((MainActivity) getActivity()).setRangProduit(position);
                ((MainActivity) getActivity()).lanceViewPager(2);

                frameLayout.setVisibility(View.GONE);
                pager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
            }
        });
    }

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listeProduit.clear();

                for (DataSnapshot catégorieSnap : dataSnapshot.getChildren()){
                    String catégorie = catégorieSnap.getKey();
                    for (DataSnapshot produitSnap : catégorieSnap.getChildren()) {
                        String nom = produitSnap.getKey();
                        Integer quantité = produitSnap.child("QttProduit").getValue(Integer.class);
                        double valeur = produitSnap.child("ValeurProduit").getValue(double.class);

                        Produit produit = new Produit(nom, catégorie, quantité, valeur);

                        listeProduit.add(produit);
                    }
                }

                ListeProduit adapter = new ListeProduit(getActivity(), listeProduit);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
