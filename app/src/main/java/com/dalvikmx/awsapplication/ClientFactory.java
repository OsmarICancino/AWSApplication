package com.dalvikmx.awsapplication;

import android.content.Context;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.S3ObjectManagerImplementation;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;

public class ClientFactory {
    // ...other code...
    private static volatile AWSAppSyncClient client;
    private static volatile S3ObjectManagerImplementation s3ObjectManager;
    public static AWSAppSyncClient getInstance(Context context) {
        if (client == null) {
            client = AWSAppSyncClient.builder()
                    .context(context)
                    .awsConfiguration(new AWSConfiguration(context))
                    .s3ObjectManager(getS3ObjectManager(context)) // Here we initialize the s3 object manager.
                    .build();
        }
        return client;
    }
    // Copy the below two methods and add the .s3ObjectManager builder parameter
    // initialize and fetch the S3 Client
    public static final S3ObjectManagerImplementation getS3ObjectManager(final Context context) {
        if (s3ObjectManager == null) {
            AmazonS3Client s3Client = new AmazonS3Client(getCredentialsProvider(context));
            s3Client.setRegion(Region.getRegion("us-east-1")); // you can set the region of bucket here
            s3ObjectManager = new S3ObjectManagerImplementation(s3Client);
        }
        return s3ObjectManager;
    }
    // initialize and fetch cognito credentials provider for S3 Object Manager
    public static final AWSCredentialsProvider getCredentialsProvider(final Context context){
        final CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                Constants.COGNITO_IDENTITY, // Identity pool ID
                Constants.COGNITO_REGION // Region
        );
        return credentialsProvider;
    }
}