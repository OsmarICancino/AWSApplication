package com.dalvikmx.awsapplication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.PutPostWithPhotoMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.UUID;

import javax.annotation.Nonnull;

import type.S3ObjectInput;

public class AddPostActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        /*mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();*/

    }

    public void choosePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            // String picturePath contains the path of selected Image
            photoPath = picturePath;
        }
    }
    // Actual mutation code
    public void save(View view) {

        S3ObjectInput s3ObjectInput = S3ObjectInput.builder()
                .bucket("aws-android-dev123")
                .key("public/"+ UUID.randomUUID().toString())
                .region("us-east-1")
                .localUri(photoPath)
                .mimeType("image/jpg").build();
        PutPostWithPhotoMutation addPostMutation = PutPostWithPhotoMutation.builder()
                .id(UUID.randomUUID().toString())
                .title("Test")
                .author("Osmar ICancino")
                .url("https://www.google.com")
                .content("Image")
                .ups(0)
                .downs(0)
                .photo(s3ObjectInput)
                .build();
        ClientFactory.getInstance(this).mutate(addPostMutation).enqueue(postsCallback);
    }
    // Mutation callback code
    private GraphQLCall.Callback<PutPostWithPhotoMutation.Data> postsCallback = new GraphQLCall.Callback<PutPostWithPhotoMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<PutPostWithPhotoMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddPostActivity.this, "Added post", Toast.LENGTH_SHORT).show();
                    //AddPostActivity.this.finish();
                }
            });
        }
        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddPostMutation", e);
                    Toast.makeText(AddPostActivity.this, "Failed to add post", Toast.LENGTH_SHORT).show();
                    //AddPostActivity.this.finish();
                }
            });
        }
    };
}
