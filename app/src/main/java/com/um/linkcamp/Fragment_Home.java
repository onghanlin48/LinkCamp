package com.um.linkcamp;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Adapter.PostAdapter;
import data.DatabaseHelper;
import function.SpacingItemDecoration;
import model.Post;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Home extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private HashMap<String,Integer> hashMap = new HashMap<>();
    DatabaseHelper databaseHelper;

    public Fragment_Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Home newInstance(String param1, String param2) {
        Fragment_Home fragment = new Fragment_Home();
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
        View view = inflater.inflate(R.layout.fragment__home,container,false);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.homeRecyclerView);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(postAdapter);
        readPosts();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebaseFirestore.collection("Posts");
        Ref.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", e.getMessage());
                return;
            }

            if (querySnapshot != null) {
                for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                    if (change.getType() == DocumentChange.Type.REMOVED) {
                        String postId = change.getDocument().getString("postId");
                        if (hashMap.containsKey(postId)) {
                            int index = hashMap.get(postId);
                            postList.remove(index);
                            postAdapter.notifyItemRemoved(index);
                            hashMap.remove(postId);
                            for (int i = index; i < postList.size(); i++) {
                                hashMap.put(postList.get(i).getPostId(), i);
                            }
                        }
                    }
                    else if (change.getType() == DocumentChange.Type.ADDED) {
                        int x = postList.size();
                        String id =change.getDocument().getString("postId");
                        if(!hashMap.containsKey(id)){
                            Post post = change.getDocument().toObject(Post.class);
                            postList.add(post);
                            hashMap.put(id,x);
                            postAdapter.notifyItemInserted(x);
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
        CollectionReference ref = firebaseFirestore.collection("Posts");
        ref.orderBy("timestamp", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            postList.clear();
            hashMap.clear();
            int x = 0;
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Post post = document.toObject(Post.class);
                    postList.add(post);
                    hashMap.put(document.getString("postId"),x);
                    x++;
                }

                postAdapter.notifyDataSetChanged();
            }
        });
    }

}