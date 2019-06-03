package com.example.dinovente;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class tabSwitchAdapter extends FragmentStatePagerAdapter {

    String[] tabArray = new String[]{"Nouveau\nProduit", "Entrée\nde produit", "Sortie\nde produit", "Modification du nom"};
    Integer tabNumber = 4;

    public tabSwitchAdapter(FragmentManager fm)
    {
        super(fm);
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabArray[position];
    }

    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 1:
                return FragmentEntreeStock.newInstance();
            case 2:
                return FragmentSortieStock.newInstance();
            case 0:
                return FragmentEntreeNouveauProduit.newInstance();
            case 3:
                return FragmentModifStock.newInstance();
            default:
                break;
        }

        return null;
    }

    @Override
    public int getCount() {
        return tabNumber;
    }
}
