package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignupBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity {


    ActivitySignupBinding signupBinding;
    ActivityResultLauncher<String[]> permissionResultlauncher;
    int deniedpermissionCount=0;

    ArrayList<String> permissionList=new ArrayList<>();

    ActivityResultLauncher<Intent> phohtoPickerResultLauncher;
    ActivityResultLauncher<Intent> croppedPhotoResultLauncher;

    Uri CroppedImageUri;
    String userName,userEmail,userPassword;

    FirebaseAuth auth= FirebaseAuth.getInstance();

    Uri croppedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signupBinding=ActivitySignupBinding.inflate(getLayoutInflater());

        setContentView(signupBinding.getRoot());

        if(Build.VERSION.SDK_INT> 33){
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        } else if (Build.VERSION.SDK_INT >32) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
        }
        else{
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        registerActivityForMultiplePermission();
        registerActivityForPhotoPicker();
        registerActivityForPhotoCrop();


        signupBinding.imageViewProfileSignup.setOnClickListener(v->{

            if(hasPermission()){
                openPhotoPicker();
            }
            else {
                shouldShowPermissionRationaleIfNeeded();
            }

        });

        signupBinding.buttonSignup.setOnClickListener(v->{
            createNewuser();
        });

    }

    public void createNewuser(){
        userName=signupBinding.editTextUserNameSignup.getText().toString().trim();
        userEmail=signupBinding.editTextEmailSignup.getText().toString().trim();
        userPassword=signupBinding.editTextPasswordSignup.getText().toString().trim();

        if(userName.isEmpty() || userEmail.isEmpty()||userPassword.isEmpty()){
            Toast.makeText(this, "Please fill all the requirement data", Toast.LENGTH_SHORT).show();
        }
        else {
            signupBinding.buttonSignup.setEnabled(false);
            signupBinding.progrgressbarSignup.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(task->{
               if(task.isSuccessful()){
                   uploadPhoto();

               }
               else {
                   Toast.makeText(this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                   signupBinding.buttonSignup.setEnabled(true);
                   signupBinding.progrgressbarSignup.setVisibility(View.INVISIBLE);
               }
            });
        }

    }

    public void uploadPhoto(){

    }

    public void registerActivityForMultiplePermission(){
        permissionResultlauncher= registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),result->{

            boolean allGranted= true;
            for(Boolean isAllowed: result.values()){
                if(!isAllowed){
                    allGranted=false;
                    break;
                }
            }

            if(allGranted){

               openPhotoPicker();

            }

            else {
                deniedpermissionCount++;
                if(deniedpermissionCount<2){
                    shouldShowPermissionRationaleIfNeeded();
                }
                else {
                    AlertDialog.Builder builder=new AlertDialog.Builder(SignupActivity.this);
                    builder.setTitle("Chat App");
                    builder.setMessage("Yoc can grant the necessary permission to access to photos from the applicatio setting");
                  builder.setPositiveButton("Go App Setting",(dialog, which) ->{

                      Intent intent= new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                      Uri uri=Uri.parse("package"+getPackageName());
                      intent.setData(uri);
                      startActivity(intent);

                      dialog.dismiss();

                  });

                  builder.setNegativeButton("Dismiss",(dialog, which) -> {
                     dialog.dismiss();

                  });

                  builder.create().show();
                }

            }

        });
    }



    public  void openPhotoPicker(){

        Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        phohtoPickerResultLauncher.launch(intent);

    }


    public void registerActivityForPhotoPicker(){
        phohtoPickerResultLauncher =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result ->{
           int resultCode= result.getResultCode();
           Intent data= result.getData();
           if(resultCode ==RESULT_OK && data !=null){
               Uri unCroppedImageUri=data.getData();
               cropSelectedImage(unCroppedImageUri);
           }
        });

    }

    public void registerActivityForPhotoCrop(){

        croppedPhotoResultLauncher =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),cropResult->{

            int resultCode=cropResult.getResultCode();
            Intent data =cropResult.getData();
            if(resultCode ==RESULT_OK && data != null){
                croppedImageUri=UCrop.getOutput(data);

                if(croppedImageUri !=null){
                    Picasso.get().load(croppedImageUri)
                            .into(signupBinding.imageViewProfileSignup);
                }
            }
            else if(resultCode ==UCrop.RESULT_ERROR && data !=null){
                Toast.makeText(this,UCrop.getError(data).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

//    public void cropSelectedImage(Uri sourceUri){
//
//        Uri destinationUri = Uri.fromFile(new File(getCacheDir(),"cropped"+ System.currentTimeMillis()));
//        Intent croppedIntent= UCrop.of(sourceUri,destinationUri)
//                .withAspectRatio(1,1)
//                .getIntent(SignupActivity.this);
//
//
//    }


    public void cropSelectedImage(Uri sourceUri) {
        try {
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped" + System.currentTimeMillis()));
            Intent croppedIntent = UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .getIntent(SignupActivity.this);
            croppedPhotoResultLauncher.launch(croppedIntent); // Launch the cropping intent
        } catch (Exception e) {
            Log.e("SignupActivity", "Error creating cropped image file: " + e.getMessage());
            Toast.makeText(this, "Error processing image. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    public void shouldShowPermissionRationaleIfNeeded(){

        ArrayList<String>  deniedPermission =new ArrayList<>();

        for(String permission : permissionList){
          if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
              deniedPermission.add(permission);
          }
        }

        if(deniedPermission.isEmpty()){
            Snackbar.make(signupBinding.mainSignup,"Please grant necessary permissions to add a profile photo",Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK",v->{
                        permissionResultlauncher.launch(deniedPermission.toArray(new String[0]));
                    }).show();
        }

        else {
            permissionResultlauncher.launch((permissionList.toArray(new String[0])));
        }

    }

    public boolean hasPermission(){
        for(String permission : permissionList){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return  false;
            }
        }
        return true;
    }
}








