package com.javapoint.blogapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private List<User> user_list;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;

    private DocumentSnapshot lastVisible;

    private boolean isFirstpageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

                        Toast.makeText(container.getContext(), "Loading", Toast.LENGTH_LONG).show();

                        loadMorePosts();

                    }

                }
            });

            final Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable final FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    if (isFirstpageFirstLoad) {

                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        blog_list.clear();
                        user_list.clear();

                    }

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            String blogUserId = doc.getDocument().getString("user_id");

                            firebaseFirestore.collection("Users").document(blogUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                if (isFirstpageFirstLoad) {

                                                    user_list.add(user);
                                                    blog_list.add(blogPost);

                                                } else {

                                                    user_list.add(0, user);
                                                    blog_list.add(0, blogPost);
                                                }

                                                blogRecyclerAdapter.notifyDataSetChanged();

                                            } else {

                                                Toast.makeText(container.getContext(), "Firestore Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                            }

                                        }
                                    });


                        }

                    }
                    isFirstpageFirstLoad = false;
                }
                }
            });

        }
        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePosts() {

        Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.ASCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    lastVisible = queryDocumentSnapshots.getDocuments()
                          .get(queryDocumentSnapshots.size() -1 );

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                          if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                              String blogUserId = doc.getDocument().getString("user_id");

                              firebaseFirestore.collection("Users").document(blogUserId).get()
                                      .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                          @Override
                                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                              if (task.isSuccessful()) {

                                                  User user = task.getResult().toObject(User.class);

                                                      user_list.add(user);
                                                      blog_list.add(blogPost);

                                                  blogRecyclerAdapter.notifyDataSetChanged();

                                              }

                                          }
                                      });

                          }

                    }
                }
            }
        });

    }

}
