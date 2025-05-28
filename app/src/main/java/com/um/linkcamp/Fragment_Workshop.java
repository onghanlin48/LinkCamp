package com.um.linkcamp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Adapter.WorkshopAdapter;
import function.SpacingItemDecoration;
import model.Workshop;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Workshop#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Workshop extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private RecyclerView recyclerView;
    private WorkshopAdapter workshopAdapter;
    private List<Workshop> postList;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private HashMap<String,Integer> hashMap = new HashMap<>();

    public Fragment_Workshop() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Workshop.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Workshop newInstance(String param1, String param2) {
        Fragment_Workshop fragment = new Fragment_Workshop();
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
        View view = inflater.inflate(R.layout.fragment__workshop,container,false);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.workshopRecyclerView);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        postList = new ArrayList<>();
        workshopAdapter = new WorkshopAdapter(getContext(),postList);
        recyclerView.setAdapter(workshopAdapter);
        readPosts();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Workshop");
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
                            postList.remove(index);
                            workshopAdapter.notifyItemRemoved(index);
                            hashMap.remove(postId);
                            for (int i = index; i < postList.size(); i++) {
                                hashMap.put(postList.get(i).getId(), i);
                            }
                        }
                    }
                    else if (change.getType() == DocumentChange.Type.ADDED) {
                        int x = postList.size();
                        String id = change.getDocument().getString("id");
                        String cover = change.getDocument().getString("Cover");
                        String title = change.getDocument().getString("Title");
                        String date = change.getDocument().getString("Date");
                        String start = change.getDocument().getString("Start");
                        String location = change.getDocument().getString("Location");
                        String publisher = change.getDocument().getString("publisher");
                        if(! hashMap.containsKey(id)){
                            Workshop workshop = new Workshop(id,cover,title,date,start,location,publisher);
                            postList.add(workshop);
                            hashMap.put(id,x);
                            workshopAdapter.notifyItemChanged(x);
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
        CollectionReference ref = firebaseFirestore.collection("Workshop");
        ref.orderBy("timestamp", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            postList.clear();
            hashMap.clear();
            int x = 0;
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getString("id");
                    String cover = document.getString("Cover");
                    String title = document.getString("Title");
                    String date = document.getString("Date");
                    String start = document.getString("Start");
                    String location = document.getString("Location");
                    String publisher = document.getString("publisher");

                    Workshop workshop = new Workshop(id,cover,title,date,start,location,publisher);

                    postList.add(workshop);
                    hashMap.put(document.getString("id"),x);
                    x++;
                }

                workshopAdapter.notifyDataSetChanged();
            }
        });
    }
}