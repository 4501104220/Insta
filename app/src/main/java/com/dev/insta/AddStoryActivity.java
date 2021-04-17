package com.dev.insta;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {


    private Uri mImageUrl;
    String myUrl ="" ;
    private StorageTask mStorageTask;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

         storageReference = FirebaseStorage.getInstance().getReference("Story");

        CropImage.activity()
                .setAspectRatio(9,16)
                .start(com.dev.insta.AddStoryActivity.this);
    }

    private String getFileExtensions(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private  void publishStory(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();

        if(mImageUrl != null){
            final StorageReference imageReferance = storageReference.child(System.currentTimeMillis()
            +"."+ getFileExtensions(mImageUrl));

            mStorageTask = imageReferance.putFile(mImageUrl);
            mStorageTask.continueWithTask((Continuation) task -> {

                if(!task.isSuccessful())
                {
                    throw task.getException();

                }
                return imageReferance.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if(task.isSuccessful()){

                    Uri downloadUri = task.getResult();
                    myUrl = downloadUri.toString();

                    String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                            .child(myid);
                    String storyid = reference.push().getKey();
                    long timend = System.currentTimeMillis()+86400000;

                    HashMap<String, Object> hashMap = new HashMap<>();

                    hashMap.put("imageurl",myUrl);
                    hashMap.put("timestart", ServerValue.TIMESTAMP);
                    hashMap.put("timeend",timend);
                    hashMap.put("storyid",storyid);
                    hashMap.put("userid",myid);

                    reference.child(storyid).setValue(hashMap);
                    pd.dismiss();
                    finish();

                }else {
                    Toast.makeText(AddStoryActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());


        }else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            mImageUrl = result.getUri();

            publishStory();
        }else {
            Toast.makeText(com.dev.insta.AddStoryActivity.this,"Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(com.dev.insta.AddStoryActivity.this,MainActivity.class));
            finish();

        }
    }
}
