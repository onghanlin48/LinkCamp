package com.um.linkcamp;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Adapter.LearnAdapter;
import Adapter.WorkshopAdapter;
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import model.Learn;
import model.Workshop;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Learn#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Learn extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private LearnAdapter learnAdapter;
    private List<Learn> learnList;
    private DatabaseHelper dbHelper;
    private HashMap<String,Integer> hashMap = new HashMap<>();

    public Fragment_Learn() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Learn.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Learn newInstance(String param1, String param2) {
        Fragment_Learn fragment = new Fragment_Learn();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__learn,container,false);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.RecyclerView);

        dbHelper = new DatabaseHelper(getContext());
        Cursor cursor = dbHelper.getUserData();
        String ic = "";
        if (cursor.moveToFirst()) {
            ic = cursor.getString(cursor.getColumnIndex("ic"));
        }

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        learnList = new ArrayList<>();
        learnAdapter = new LearnAdapter(getContext(),learnList,ic);
        recyclerView.setAdapter(learnAdapter);
        readPosts();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Learning");
        Ref.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", e.getMessage());
                return;
            }

            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.REMOVED) {
                        String postId = change.getDocument().getString("id");
                        if (hashMap.containsKey(postId)) {
                            int index = hashMap.get(postId);
                            learnList.remove(index);
                            learnAdapter.notifyItemRemoved(index);
                            hashMap.remove(postId);
                            for (int i = index; i < learnList.size(); i++) {
                                hashMap.put(learnList.get(i).getId(), i);
                            }
                        }
                    }
                    else if (change.getType() == DocumentChange.Type.ADDED) {
                        int x = learnList.size();
                        String id = change.getDocument().getString("id");
                        String title = change.getDocument().getString("title");
                        String description = change.getDocument().getString("description");
                        Boolean channel = change.getDocument().getBoolean("channel");
                        Timestamp timestamp = change.getDocument().getTimestamp("timestamp");
                        String publisher = change.getDocument().getString("publisher");
                        if(! hashMap.containsKey(id)){
                            Learn learn = new Learn(id,title,description,publisher,channel,timestamp);
                            learnList.add(learn);
                            hashMap.put(id,x);
                            learnAdapter.notifyItemChanged(x);
                        }
                    }
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            readPosts();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }
    private void readPosts() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firebaseFirestore.collection("Learning");
        ref.orderBy("timestamp", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            learnList.clear();
            hashMap.clear();
            int x = 0;
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getString("id");
                    String title = document.getString("title");
                    String description = document.getString("description");
                    Boolean channel = document.getBoolean("channel");
                    Timestamp timestamp = document.getTimestamp("timestamp");
                    String publisher = document.getString("publisher");

                    Learn learn = new Learn(id,title,description,publisher,channel,timestamp);

                    learnList.add(learn);
                    hashMap.put(document.getString("id"),x);
                    x++;
                }

                learnAdapter.notifyDataSetChanged();
            }
        });
    }
}