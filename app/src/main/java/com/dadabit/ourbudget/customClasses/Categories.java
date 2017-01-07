package com.dadabit.ourbudget.customClasses;

import android.util.SparseArray;


public class Categories {

    private SparseArray categoryNames;
    private SparseArray categoryImages;

    public Categories(SparseArray categoryNames, SparseArray categoryImages) {
        this.categoryNames = categoryNames;
        this.categoryImages = categoryImages;
    }


    public SparseArray getCategoryNames() {
        return categoryNames;
    }

    public SparseArray getCategoryImages() {
        return categoryImages;
    }

}
