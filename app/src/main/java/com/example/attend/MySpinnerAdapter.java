package com.example.attend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MySpinnerAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> data;
    LayoutInflater inflater;

    public MySpinnerAdapter(Context appContext, ArrayList<String>data){
        this.context = appContext;
        this.data = data;
        inflater = LayoutInflater.from(appContext);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.attendnote_spinner_dropdown_layout,viewGroup,false);
        TextView names = view.findViewById(R.id.spinnerDropdownTextView);
        try {
            names.setText(data.get(i));
        }catch(IndexOutOfBoundsException e){}
        return view;
    }
}
