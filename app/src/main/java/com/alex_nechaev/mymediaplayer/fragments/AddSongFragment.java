package com.alex_nechaev.mymediaplayer.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alex_nechaev.mymediaplayer.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.io.File;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.FileProvider.getUriForFile;

public class AddSongFragment extends Fragment {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1101;
    private static final int GALLERY_REQUEST_CODE = 2101;

    private static final int REQUEST_CODE_CAMERA_USE_PERMISSION = 1102;
    private static final int CAMERA_REQUEST_CODE = 2102;

    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private Uri galleryImgUri;
    private File file;

    ImageView albumImage;
    EditText artistsName;
    EditText songName;
    EditText songLink;
    EditText songLyric;

    public interface OnAddSong {
        void onNewAddSong(String songName, String artistName, String imgUri, String songLink, String lyrics);

        void onSnackBarRequest(String message);

        void onCloseFragment();
    }

    OnAddSong callBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callBack = (OnAddSong) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The calling activity must implement OnAddSong");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_song_fragment, container, false);

        albumImage = view.findViewById(R.id.add_img_album);

        ImageButton fromCameraBtn = view.findViewById(R.id.add_from_camera);
        ImageButton fromGalleryBtn = view.findViewById(R.id.add_from_gallery);

        artistsName = view.findViewById(R.id.add_artists_name);
        songName = view.findViewById(R.id.add_song_name);
        songLink = view.findViewById(R.id.add_song_link);
        songLyric = view.findViewById(R.id.add_song_lyric);

        ImageButton onConfirm = view.findViewById(R.id.add_confirm_btn);
        ImageButton onCancel = view.findViewById(R.id.add_denial_btn);

        fromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hasExternalStoragePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });

        fromCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasCameraPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
                    if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                        openCamera();
                    } else {
                        requestPermissions(permissions, REQUEST_CODE_CAMERA_USE_PERMISSION);
                    }
                } else {
                    openCamera();
                }
            }
        });

        onConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sImgUri = "";
                if (galleryImgUri != null) {
                    sImgUri = galleryImgUri.toString();
                }

                String sArtistName = artistsName.getText().toString();
                String sSongName = songName.getText().toString();
                String sSongLink = songLink.getText().toString();
                String sSongLyrics = songLyric.getText().toString();

                if (!sArtistName.equals("")) {
                    if (!sSongName.equals("")) {
                        if (!sSongLink.equals("")) {
                            callBack.onNewAddSong(sSongName, sArtistName, sImgUri, sSongLink, sSongLyrics);
                            callBack.onCloseFragment();
                        } else {
                            callBack.onSnackBarRequest("Enter song's url");
                        }
                    } else {
                        callBack.onSnackBarRequest("Song's name can't be empty");
                    }
                } else {
                    callBack.onSnackBarRequest("Artist's name can't be empty");
                }
            }
        });

        onCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onCloseFragment();
            }
        });
        return view;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    public void openCamera() {
        file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pic" + (System.nanoTime()) + ".jpg");
        galleryImgUri = getUriForFile(getContext(), getActivity().getPackageName(), file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, galleryImgUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            }
        } else if (requestCode == REQUEST_CODE_CAMERA_USE_PERMISSION && grantResults.length > 0) {
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        } else {
            callBack.onSnackBarRequest("Permission denied!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                galleryImgUri = data.getData();
                if (galleryImgUri != null) {
                    Glide
                            .with(this)
                            .load(galleryImgUri)
                            .placeholder(R.drawable.music_album_icon)
                            .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCornersTransformation(30, 5)))
                            .into(albumImage);
                }
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Glide
                    .with(this)
                    .load(galleryImgUri)
                    .placeholder(R.drawable.music_album_icon)
                    .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCornersTransformation(30, 5)))
                    .into(albumImage);
        }
    }


}
