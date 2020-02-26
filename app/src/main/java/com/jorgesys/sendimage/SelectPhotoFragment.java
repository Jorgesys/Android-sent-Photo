package com.jorgesys.sendimage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.tooltip.Tooltip;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SelectPhotoFragment extends Fragment {

    private static String TAG = "SelectPhotoFragment";
    private static List<Uri> uris;
    private Uri captureImage;
    private boolean isCamera;

    public static final int ACTIVITY_TAKE_PHOTO = 11;
    public static final int ACTIVITY_PICK_MEDIA = 12;
    private static final int GALLERY_INTENT_CALLED = 0;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 1;

    public SelectPhotoFragment() {
        if (uris == null) {
            uris = new ArrayList<Uri>();
        }
    }

    public static SelectPhotoFragment newInstance() {
        SelectPhotoFragment fragment = new SelectPhotoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("¿Do you want to add anothe photo?");
        builder.setPositiveButton("Add more photos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isCamera) {
                    takePictureButtonClicked();
                } else {
                    openGalleryButtonClicked();
                }
            }
        });
        builder.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startEmail();
            }
        });
        builder.create().show();
    }

    private static String externalDir;
    public static String getExternalStoragePath(Context ctx){
         externalDir = ctx.getExternalFilesDir(null).toString();
        return externalDir;
    }

    private File getTempFile() {
        SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd_HHmmssSS");
        String pictureId = format.format(new Date());
        String fileName = "IMG_" +  pictureId + ".jpg";
        File path = new File(getExternalStoragePath(getActivity()));
        if (!path.exists()) {
            Log.w(TAG, "Directory " +path.getAbsoluteFile()+ " doesn´t exits, then must be created!");
            Log.w(TAG, path.mkdir()? "Directory created succesfully!": "Error creating Directory!");
        }else{
            Log.d(TAG, "Directory " +path.getAbsoluteFile()+ " exists!");
        }

        return new File(path, fileName);
    }

    private void startEmail() {
        if (uris == null || uris.isEmpty()) {
            return;
        }

        Activity act = getActivity();
        if (act == null) {
            return;
        }

        String titulo = "SHARE IMAGE";
        String body = "<br><br><br>" + titulo + "</p>";
        Intent sendIntent;

        sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        sendIntent.setType("message/rfc822");
        sendIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{"jorgesys12@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_STREAM, (ArrayList<Uri>) uris);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, titulo + ": Multimedia");
        sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body).toString());
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(sendIntent, "Email:"));
        uris.clear();

    }

    private void takePictureButtonClicked() {
        isCamera = true;
        captureImage = Uri.fromFile(getTempFile());
        captureImage = FileProvider.getUriForFile(getActivity(),
                getActivity().getApplicationContext().getPackageName()+getString(R.string.package_provider),
                getTempFile());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureImage);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, ACTIVITY_TAKE_PHOTO);
    }

    private void openGalleryButtonClicked() {
        isCamera = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_INTENT_CALLED);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
        }
    }

	/*------------------------------- OVERRIDES ---------------------------*/

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            Log.e(TAG, "onActivityResult()  resultCode: " + resultCode);
            return;
        }

        Uri originalUri = null;
        switch (requestCode) {
            case GALLERY_INTENT_CALLED:
                originalUri = data.getData();
                break;
            case GALLERY_KITKAT_INTENT_CALLED:
                originalUri = data.getData();
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getActivity().getContentResolver().
                        takePersistableUriPermission(originalUri, takeFlags);
                break;
            case ACTIVITY_TAKE_PHOTO:
                originalUri = captureImage;
                break;
        }
        uris.add(originalUri);
        createDialog();
    }


    private Tooltip tooltipNombre = null;
    private Tooltip tooltipApellido = null;
    final Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Images & Videos");
        }

        View v = inflater.inflate(R.layout.fragment_select_photo, container, false);
        v.findViewById(R.id.tomarfoto).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                takePictureButtonClicked();
            }
        });


        v.findViewById(R.id.txtFotos).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tooltipNombre!=null){
                    tooltipNombre.dismiss();
                }
                TextView txt = (TextView) v;
                tooltipApellido = new Tooltip.Builder(getContext(), txt)
                        .setText("Take a picture from your gallery!")
                        .setTextColor(Color.parseColor("#ffffff"))
                        .setGravity(Gravity.BOTTOM)
                        .setCornerRadius(8f)
                        .setDismissOnClick(true)
                        .setArrowHeight(18f)
                        .show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tooltipApellido.dismiss();
                    }
                },3000);
            }
        });

        v.findViewById(R.id.txtVideos).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tooltipApellido!=null){
                    tooltipApellido.dismiss();
                }

                Log.i(TAG, "txtVideos ToolTip!");
                TextView txt = (TextView) v;
                  tooltipNombre = new Tooltip.Builder(getContext(), txt)
                        .setText("Record a Video!")
                        .setTextColor(Color.parseColor("#ffffff"))
                        .setGravity(Gravity.BOTTOM)
                        .setCornerRadius(8f)
                        .setDismissOnClick(true)
                        .setArrowHeight(18f)
                        .show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tooltipNombre.dismiss();
                    }
                },3000);
            }
        });


        v.findViewById(R.id.libreriafoto).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View btn) {
                openGalleryButtonClicked();
            }
        });

        return v;
    }


}