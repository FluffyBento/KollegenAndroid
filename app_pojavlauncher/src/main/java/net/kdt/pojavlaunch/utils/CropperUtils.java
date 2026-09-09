package net.kdt.pojavlaunch.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.imgcropper.BitmapCropBehaviour;
import net.kdt.pojavlaunch.imgcropper.CropperBehaviour;
import net.kdt.pojavlaunch.imgcropper.CropperView;
import net.kdt.pojavlaunch.imgcropper.RegionDecoderCropBehaviour;

import java.io.IOException;
import java.io.InputStream;

public class CropperUtils {
    public static ActivityResultLauncher<?> registerCropper(Fragment fragment, final CropperListener cropperListener) {
        return fragment.registerForActivityResult(new ActivityResultContracts.OpenDocument(), (result)->{
            Context context = fragment.getContext();
            if(context == null) return;
            if (result == null) {
                Toast.makeText(context, R.string.cropper_select_cancelled, Toast.LENGTH_SHORT).show();
                return;
            }
            openCropperDialog(context, result, cropperListener);
        });
    }

    private static void openCropperDialog(Context context, Uri selectedUri,
                                          final CropperListener cropperListener) {
        ContentResolver contentResolver = context.getContentResolver();
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.cropper_title)
                .setView(R.layout.dialog_cropper)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        CropperView cropImageView = dialog.findViewById(R.id.crop_dialog_view);
        View finishProgressBar = dialog.findViewById(R.id.crop_dialog_progressbar);
        assert cropImageView != null;
        assert finishProgressBar != null;
        bindViews(dialog, cropImageView);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            dialog.dismiss();
            
            
            cropperListener.onCropped(cropImageView.crop((int) Tools.dpToPx(70)));
        });
        PojavApplication.sExecutorService.execute(()->{
            CropperBehaviour cropperBehaviour = null;
            try {
                 cropperBehaviour = createBehaviour(cropImageView, contentResolver, selectedUri);
            }catch (Exception e) {
                cropperListener.onFailed(e);
            }
            CropperBehaviour finalBehaviour = cropperBehaviour;
            Tools.runOnUiThread(()->finishSetup(dialog, finishProgressBar, cropImageView, finalBehaviour));
        });
    }

    
    private static void fixDialogHeight(AlertDialog dialog) {
        Window dialogWindow = dialog.getWindow();
        if(dialogWindow != null)
            dialogWindow.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT, 
                    WindowManager.LayoutParams.WRAP_CONTENT  
            );
    }

    private static void finishSetup(AlertDialog dialog, View progressBar,
                                    CropperView cropImageView, CropperBehaviour cropperBehaviour) {
        if(cropperBehaviour == null) {
            dialog.dismiss();
            return;
        }
        progressBar.setVisibility(View.GONE);
        cropImageView.setCropperBehaviour(cropperBehaviour);
        cropperBehaviour.applyImage();
        cropImageView.post(()->{
            fixDialogHeight(dialog);
            cropImageView.requestLayout();
        });
    }


    private static CropperBehaviour createBehaviour(CropperView cropImageView,
                                      ContentResolver contentResolver,
                                      Uri selectedUri) throws Exception {
        try (InputStream inputStream = contentResolver.openInputStream(selectedUri)) {
            if(inputStream == null) return null;
            try {
                BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                RegionDecoderCropBehaviour cropBehaviour = new RegionDecoderCropBehaviour(cropImageView);
                cropBehaviour.setRegionDecoder(regionDecoder);
                return cropBehaviour;
            }catch (IOException e) {
                
                
                Log.w("CropperUtils", "Failed to load image into BitmapRegionDecoder", e);
            }
        }
        
        
        try (InputStream inputStream = contentResolver.openInputStream(selectedUri)) {
            if(inputStream == null) return null;
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            BitmapCropBehaviour cropBehaviour = new BitmapCropBehaviour(cropImageView);
            cropBehaviour.setBitmap(originalBitmap);
            return cropBehaviour;
        }
    }

    private static void bindViews(AlertDialog alertDialog, CropperView imageCropperView) {
        ToggleButton horizontalLock = alertDialog.findViewById(R.id.crop_dialog_hlock);
        ToggleButton verticalLock = alertDialog.findViewById(R.id.crop_dialog_vlock);
        View reset = alertDialog.findViewById(R.id.crop_dialog_reset);
        assert horizontalLock != null;
        assert verticalLock != null;
        assert reset != null;
        horizontalLock.setOnClickListener(v->
                imageCropperView.horizontalLock = horizontalLock.isChecked()
        );
        verticalLock.setOnClickListener(v->
                imageCropperView.verticalLock = verticalLock.isChecked()
        );
        reset.setOnClickListener(v->
            imageCropperView.resetTransforms()
        );
    }

    @SuppressWarnings("unchecked")
    public static void startCropper(ActivityResultLauncher<?> resultLauncher) {
        ActivityResultLauncher<String[]> realResultLauncher =
                (ActivityResultLauncher<String[]>) resultLauncher;
        realResultLauncher.launch(new String[]{"image/*"});
    }
    public interface CropperListener {
        void onCropped(Bitmap contentBitmap);
        void onFailed(Exception exception);
    }
}
