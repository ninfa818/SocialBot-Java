package com.hostcart.socialbot.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.MoreSectionAdapter;
import com.hostcart.socialbot.model.MoreSectionModel;
import com.hostcart.socialbot.utils.FireConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends BaseFragment {

    private List<MoreSectionModel> sectionModels = new ArrayList<>();
    private MoreSectionAdapter adapter;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        GridView grd_moresection = view.findViewById(R.id.grd_moresection);
        adapter = new MoreSectionAdapter(getContext(), sectionModels);
        grd_moresection.setAdapter(adapter);
        initData();
    }

    private void initData() {
        FireConstants.moreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionModels.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    MoreSectionModel model = dataSnapshot.getValue(MoreSectionModel.class);
                    sectionModels.add(model);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean showAds() {
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}
