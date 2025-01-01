package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.example.myapplication.databinding.ActivitySignupBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity {


    ActivitySignupBinding signupBinding;
    ActivityResultLauncher<String[]> permissionResultlauncher;
    int deniedpermissionCount=0;

    ArrayList<String> permissionList=new ArrayList<>();
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


        signupBinding.imageViewProfileSignup.setOnClickListener(v->{

            if(hasPermission()){
                openPhotoPicker();
            }
            else {
                shouldShowPermissionRationaleIfNeeded();
            }

        });

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








