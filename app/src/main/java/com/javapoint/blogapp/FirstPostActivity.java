package com.javapoint.blogapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class FirstPostActivity extends AppCompatActivity {

    private Toolbar firstToolbar;
    private ImageView firstPostImage;
    private EditText firstPostDesc;
    private Button firstPostBtn;
    private ProgressBar firstProgress;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Uri postImageUri = null;
    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_post);

        firstToolbar = findViewById(R.id.first_post_toolbar);
        setSupportActionBar(firstToolbar);
        getSupportActionBar().setTitle("Add your first post");

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        firstPostImage = findViewById(R.id.first_post_image);
        firstPostDesc = findViewById(R.id.first_post_desc);
        firstPostBtn = findViewById(R.id.first_post_btn);
        firstProgress = findViewById(R.id.first_post_progress);

        firstPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(16, 9)
                        .start(FirstPostActivity.this);
                
            }
        });

        firstPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstProgress.setVisibility(View.VISIBLE);

                final String desc = firstPostDesc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null) {



                    final String random_name = UUID.randomUUID().toString();

                    final StorageReference filepath = storageReference.child("post_images").child(random_name + ".jpg");
                    filepath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {

                                    File newImageFile = new File(postImageUri.getPath());

                                    try {
                                        compressedImageFile = new Compressor(FirstPostActivity.this)
                                                .setMaxHeight(100)
                                                .setMaxWidth(100)
                                                .setQuality(2)
                                                .compressToBitmap(newImageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100, baos);
                                    byte[] thumbData = baos.toByteArray();

                                    UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                            .child(random_name + ".jpg")
                                            .putBytes(thumbData);

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                            storageReference.child("post_images/thumbs").child(random_name + ".jpg")
                                                .getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                    public void onSuccess(Uri uri) {

                                                            Map<String, Object> postMap = new HashMap<>();
                                                            postMap.put("image", String.valueOf(uri));
                                                            postMap.put("image_thumb", String.valueOf(uri));
                                                            postMap.put("desc", desc);
                                                            postMap.put("user_id", current_user_id);
                                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                                        firebaseFirestore.collection("Posts").add(postMap)
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                    if (task.isSuccessful()) {

                                                                        Toast.makeText(FirstPostActivity.this, "Your post is uploaded ", Toast.LENGTH_LONG).show();

                                                                        Intent mainIntent = new Intent(FirstPostActivity.this, MainActivity.class);
                                                                        startActivity(mainIntent);
                                                                        finish();

                                                                    } else {

                                                                        String error = task.getException().getMessage();
                                                                        Toast.makeText(FirstPostActivity.this, "UPLOADING POST ERROR: " + error, Toast.LENGTH_LONG).show();

                                                                    }
                                                                    firstProgress.setVisibility(View.INVISIBLE);
                                                                }
                                                            });

                                                        }

                                                    }).addOnFailureListener(new OnFailureListener() {

                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });                            // didn't resolve the gerDownloadUri() method so used instead of this

                                            /*Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("image", String.valueOf(uri));
                                            postMap.put("image_thumb", downloadThumbUri);
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", current_user_id);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                                    if (task.isSuccessful()) {

                                                        Toast.makeText(FirstPostActivity.this, "Your post is uploaded ", Toast.LENGTH_LONG).show();

                                                        Intent mainIntent = new Intent(FirstPostActivity.this, MainActivity.class);
                                                        startActivity(mainIntent);
                                                        finish();

                                                    } else {

                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(FirstPostActivity.this, "UPLOADING POST ERROR: " + error, Toast.LENGTH_LONG).show();

                                                    }
                                                    firstProgress.setVisibility(View.INVISIBLE);
                                                }
                                            }); */

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                          /*  if (task.isSuccessful()) {

                                File newImageFile = new File(postImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(FirstPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100, baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                        .child(random_name + ".jpg")
                                        .putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                        String downloadThumbUri = taskSnapshot.getUploadSessionUri().toString();                            // didn't resolve the gerDownloadUri() method so used instead of this

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image", download_uri);
                                        postMap.put("image_thumb", downloadThumbUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()) {

                                                    Toast.makeText(FirstPostActivity.this, "Your post is uploaded ", Toast.LENGTH_LONG).show();

                                                    Intent mainIntent = new Intent(FirstPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {

                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(FirstPostActivity.this, "UPLOADING POST ERROR: " + error, Toast.LENGTH_LONG).show();

                                                }
                                                firstProgress.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });



                            } else {

                                firstProgress.setVisibility(View.INVISIBLE);

                                String error = task.getException().getMessage();
                                Toast.makeText(FirstPostActivity.this, "POST UPLOAD ERROR: " + error, Toast.LENGTH_LONG).show();

                            } */

                        }
                    });

                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();

                firstPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
