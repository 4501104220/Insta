package com.dev.insta;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUrl;
    String myUrl = "";

    StorageTask uploadTask ;
    StorageReference storageReference;

    ImageView close, image_added;

    TextView post ;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        storageReference = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(view -> {
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        });


        post.setOnClickListener(view -> uploadImage());

       CropImage.activity()
               .setAspectRatio(1, 1)
               .start(com.dev.insta.PostActivity.this);

    }

    private String getFileExtensions(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();
        if(imageUrl != null){

            final StorageReference filereferance = storageReference.child(System.currentTimeMillis()
            + "."+ getFileExtensions(imageUrl));

            uploadTask = filereferance.putFile(imageUrl);
            uploadTask.continueWithTask((Continuation) task -> {
                if(!task.isSuccessful()){

                    throw  task.getException();
                }
                return filereferance.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if(task.isSuccessful()){

                    Uri downloadUrl = task.getResult();

                    myUrl = downloadUrl.toString();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                    String postid = reference.push().getKey();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postid", postid);
                    hashMap.put("postimage", myUrl);
                    hashMap.put("description", description.getText().toString());
                    hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    reference.child(postid).setValue(hashMap);

                    progressDialog.dismiss();
                    startActivity(new Intent(PostActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(PostActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUrl = result.getUri();

            image_added.setImageURI(imageUrl);
        }else {
            Toast.makeText(com.dev.insta.PostActivity.this,"Searching gone wrong!", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(com.dev.insta.PostActivity.this, com.dev.insta.MainActivity.class));
            finish();
        }
    }
}
