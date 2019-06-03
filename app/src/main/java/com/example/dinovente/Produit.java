package com.example.dinovente;

public class Produit {
    String catégorie, nom;
    Integer quantité;
    double valeur;

    public Produit(String nom, String catégorie, Integer quantité, double valeur) {
        this.nom = nom;
        this.catégorie = catégorie;
        this.quantité = quantité;
        this.valeur = valeur;
    }

    public String getCatégorie() {
        return catégorie;
    }

    public String getNom() {
        return nom;
    }

    public Integer getQuantité() {
        return quantité;
    }

    public double getValeur() {
        return valeur;
    }

    public void setQuantité(Integer nvlQtt){
        this.quantité = nvlQtt;
    }

}
