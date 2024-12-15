package com.example.sklep_pj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[][] products;

    public ProductAdapter(Context context, String[][] products) {
        super(context, R.layout.product_item);
        this.context = context;
        this.products = products;
    }

    @Override
    public int getCount() {
        return products.length;
    }

    @Override
    public String getItem(int position) {
        return products[position][0];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.product_item, parent, false);
        }

        TextView productName = convertView.findViewById(R.id.product_name);
        TextView productDescription = convertView.findViewById(R.id.product_description);
        TextView productPrice = convertView.findViewById(R.id.product_price);
        ImageView productImage = convertView.findViewById(R.id.product_image);

        productName.setText(products[position][0]);
        productDescription.setText(products[position][1]);
        productPrice.setText(products[position][2] + " PLN");

        int imageResId = context.getResources().getIdentifier(products[position][3], "drawable", context.getPackageName());
        productImage.setImageResource(imageResId);

        return convertView;
    }
}

