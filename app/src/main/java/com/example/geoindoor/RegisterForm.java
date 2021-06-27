package com.example.geoindoor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.UUID;

public class RegisterForm extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    TextInputLayout f_name, l_name;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri selectedImage;
    private String generatedFilePath;
    private Task<Uri> downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form);
        getSupportActionBar().setTitle(R.string.register);
        f_name = findViewById(R.id.first_name);
        l_name = findViewById(R.id.last_name);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //Trigger image gallery intent
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


    }


    public void btn_name_registration(View view) {
        if (validateFirstname() && validateLastname()) {
            //startActivity(new Intent(getApplicationContext(), RegisterForm_1.class));
            Intent intent = new Intent(getApplicationContext(), RegisterForm_1.class);
            try {
                generatedFilePath = downloadUri.getResult().toString();
            } catch (Exception e) {
                generatedFilePath = "gs://geoindoor-cbc87.appspot.com/images/profile_pic.PNG";
            }
            intent.putExtra("fullName", f_name.getEditText().getText().toString().trim());
            intent.putExtra("lastName", l_name.getEditText().getText().toString().trim());
            intent.putExtra("profilePicture", generatedFilePath.trim());
            startActivity(intent);
        }


    }

    public boolean validateFirstname() {
        String firstName = f_name.getEditText().getText().toString().trim();

        if (firstName.isEmpty()) {
            f_name.setError(getString(R.string.please_fname));
            return false;
        } else {
            f_name.setError(null);
            f_name.setErrorEnabled(false);
            return true;
        }
    }

    public boolean validateLastname() {
        String lastName = l_name.getEditText().getText().toString().trim();

        if (lastName.isEmpty()) {
            l_name.setError(getString(R.string.please_lname));
            return false;
        } else {
            l_name.setError(null);
            l_name.setErrorEnabled(false);
            return true;
        }


    }

    //getting selected image in register activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
           /* Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) findViewById(R.id.img_profile);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));*/
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ImageView imageView = (ImageView) findViewById(R.id.img_profile);
            imageView.setImageBitmap(bitmap);
            uploadPicture();


        }

    }

    private void uploadPicture() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(R.string.uploading_image);
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child("images/" + randomKey);
        imageRef.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Snackbar.make(findViewById(android.R.id.content), R.string.image_uploaded, Snackbar.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage(getString(R.string.percentage) + (int) progressPercent + "%");
                downloadUri = snapshot.getStorage().getDownloadUrl();
            }
        });
    }
}

