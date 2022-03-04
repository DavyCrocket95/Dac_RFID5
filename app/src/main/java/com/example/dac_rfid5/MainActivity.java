package com.example.dac_rfid5;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ImageView ivAvatar;
    private VideoView vvAvatar;

    private EditText etTitre, etIdProd, etResume, etCategorie;
    private RadioGroup rbgFormat;
    private RadioButton rbAudio, rbPhoto, rbText, rbVideo;

    private Button btnCaptureP;
    ActivityResultLauncher<Intent> startCamera;
    private Uri cam_uri;

    ActivityResultLauncher<Intent> someActivityResultLauncher;

    /**
     * 1.3 Variables globales pour les URI
     **/
    private Uri localFileUri, serverFileUri;
    private StorageReference contenuRef;
    private String extension_file;
    private static String urlStoragecontenu;

    private void init() {
        etCategorie = findViewById(R.id.etCategorie);
        etTitre = findViewById(R.id.etTitre);
        etIdProd = findViewById(R.id.etIdProd);
        etResume = findViewById(R.id.etResume);
        rbgFormat = findViewById(R.id.rbgFormat);
        rbAudio = findViewById(R.id.rbAudio);
        rbPhoto = findViewById(R.id.rbPhoto);
        rbText = findViewById(R.id.rbText);
        rbVideo = findViewById(R.id.rbVideo);
        rbPhoto.setChecked(true);

        ivAvatar = findViewById(R.id.ivAvatar);
        vvAvatar = findViewById(R.id.vvAvatar);

        btnCaptureP = findViewById(R.id.btnCaptureP);
    }

    private void permission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, " 1R ");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
            Log.i(TAG, "2R ");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, " 1W ");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            Log.i(TAG, "2W ");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, " 1C ");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
            Log.i(TAG, "2C ");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        permission();

        //Choix Format -> photo
        rbgFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int idx) {
                affichebtnCapture(radioGroup, idx);
            }
        });

        //Recherche du repertoire pour les types de fichiers
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        // doSomeOperations();
                        Intent data = result.getData();
                        localFileUri = Objects.requireNonNull(data).getData();

                        /*
                        String s2 = localFileUri.getPath();
                        String s3 = localFileUri.getUserInfo();
                        localFileUri.
                        Log.i(TAG, " s3 =  " + s3);
                        Log.i(TAG, " s2 =  " + s2);
                        Log.i(TAG, " localFileUri=  " + localFileUri);*/


                        int idx = rbgFormat.getCheckedRadioButtonId();
                        RadioButton rbChoix = (RadioButton) findViewById(idx);
                        String format = rbChoix.getText().toString();
                        Log.i(TAG, " Format " + format);

                        localFileUri.getPath();
                        if (format.equals("Photo")) {
                            ivAvatar.setImageURI(localFileUri);
                        } else if (format.equals("Video")) {
                            Log.i(TAG, " Video 2 " + localFileUri);
                            vvAvatar.setVideoURI(localFileUri);
                        } else if (format.equals("Txt")) {
                            Log.i(TAG, " Txt " + localFileUri);
                        }

                    }
                });

        startCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // There are no request codes

                            ivAvatar.setImageURI(cam_uri);
                            localFileUri = cam_uri;

                        }
                    }
                });
    }

    private void affichebtnCapture(RadioGroup radioGroup, int idx) {
        int checkedRadioId = radioGroup.getCheckedRadioButtonId();

        btnCaptureP.setVisibility(View.INVISIBLE);
        if (checkedRadioId == R.id.rbPhoto) {
            btnCaptureP.setVisibility(View.VISIBLE);
        }
    }

    public void btnRepertoire(View v1) {
        pickContenu();
    }

    public void pickContenu() {
        Intent contenuPickerIntent = new Intent(Intent.ACTION_PICK);

        int idx = rbgFormat.getCheckedRadioButtonId();
        RadioButton rbChoix = (RadioButton) findViewById(idx);
        String format = rbChoix.getText().toString();

        contenuPickerIntent.setType("image/*");       //Par défaut

        if (format.equals("Audio")) {
            contenuPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
            contenuPickerIntent.setType("application/mp3");
        } else if (format.equals("Video")) {
            contenuPickerIntent.setType("video/*");
        } else if (format.equals("Text")) {
            contenuPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
            Log.i(TAG, "Text ");
            contenuPickerIntent.setType("application/pdf");
        }


        someActivityResultLauncher.launch(contenuPickerIntent);
    }


    public void btnCaptureP(View v1) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

        //startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE); // OLD WAY
        startCamera.launch(cameraIntent);                // VERY NEW WAY

    }

    public void btnAjouter(View v1) {
        addDoc();
    }

    public void addDoc() {
        String titre = etTitre.getText().toString().trim();
        Log.i(TAG, "Titre : " + titre);

        if (!titre.isEmpty()) {


            CollectionReference docRef = FirebaseFirestore.getInstance().collection("Doc");

            int idx = rbgFormat.getCheckedRadioButtonId();
            RadioButton rbChoix = (RadioButton) findViewById(idx);
            String format = rbChoix.getText().toString();

            contenuRef = FirebaseStorage.getInstance().getReference("Photo_Doc");
            extension_file = ".jpg";

            if (format.equals("Audio")) {
                //photoPickerIntent.setType("audio/*");
            } else if (format.equals("Video")) {
                contenuRef = FirebaseStorage.getInstance().getReference("Video_Doc");
                extension_file = ".mp4";
            }

            Log.i(TAG, "localFileUri : " + localFileUri);

//                    // On ajoute le type de chacun des fichiers ici pour plus de simplicité on utilise des jpg
            StorageReference fileReference = contenuRef.child(System.currentTimeMillis() + extension_file);
            Log.i(TAG, "addDatasToFireBase: " + fileReference);


            // On envoi l'image vers le storage de Firebase
            fileReference.putFile(localFileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { // Ajout du listener de réussite
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Si tout c'est bien passé alors on demande au storage de nous renvoyer l'addresse de stockage
                            fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    // Quand on récupére l'URL on la transforme en texte pour l'insérer dans Firestore avec les autres données
                                    urlStoragecontenu = task.getResult().toString();

                                    // Puis on récupère les infos de texte
                                    String titre = etTitre.getText().toString().trim();
                                    String idProduit = etIdProd.getText().toString().trim();
                                    String resume = etResume.getText().toString().trim();
                                    //String format = etFormat.getText().toString().trim();
                                    String categorie = etCategorie.getText().toString().trim();

                                    // Et on les préparent pour les envoyer
                                    Map<String, Object> datas = new HashMap<>();
                                    datas.put("titre", titre);
                                    datas.put("resume", resume);
                                    datas.put("categorie", categorie);
                                    datas.put("format", format);
                                    datas.put("idProd", idProduit);
                                    datas.put("archive", "false");

                                    datas.put("contenuDoc", urlStoragecontenu);

                                    docRef.add(datas)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.i("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("TAG", "Error adding document", e);
                                                }
                                            });
                                }
                            });
                        }
                    });
        }
    }

}