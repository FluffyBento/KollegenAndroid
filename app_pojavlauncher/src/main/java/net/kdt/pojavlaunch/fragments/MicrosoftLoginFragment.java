package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

public class MicrosoftLoginFragment extends Fragment {
    public static final String TAG = "MICROSOFT_LOGIN_FRAGMENT";
    private WebView mWebview;
    
    private boolean mBlankClient = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWebview = (WebView) inflater.inflate(R.layout.fragment_microsoft_login, container, false);
        setWebViewSettings();
        if(savedInstanceState == null) startNewSession();
        else restoreWebViewState(savedInstanceState);
        return mWebview;
    }

    
    
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewSettings() {
        WebSettings settings = mWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewTrackClient());
        mBlankClient = false;
    }

    private void startNewSession() {
        CookieManager.getInstance().removeAllCookies((b)->{
            mWebview.clearHistory();
            mWebview.clearCache(true);
            mWebview.clearFormData();
            mWebview.clearHistory();
            mWebview.loadUrl("https://login.live.com/oauth20_authorize.srf" +
                    "?client_id=00000000402b5328" +
                    "&response_type=code" +
                    "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL" +
                    "&redirect_url=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf");
        });
    }

    private void restoreWebViewState(Bundle savedInstanceState) {
        Log.i("MSAuthFragment","Restoring state...");
        if(mWebview.restoreState(savedInstanceState) == null) {
            Log.w("MSAuthFragment", "Failed to restore state, starting afresh");
            
            
            startNewSession();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        
        
        if(mBlankClient) mWebview.setWebViewClient(new WebViewTrackClient());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        
        
        mWebview.setWebViewClient(new WebViewClient());
        
        
        
        mBlankClient = true;
        super.onSaveInstanceState(outState);
        mWebview.saveState(outState);
    }

    
    public boolean canGoBack(){ return mWebview.canGoBack();}
    public void goBack(){ mWebview.goBack();}

    
    class WebViewTrackClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("ms-xal-00000000402b5328")) {
                
                ExtraCore.setValue(ExtraConstants.MICROSOFT_LOGIN_TODO, Uri.parse(url));
                Toast.makeText(view.getContext(), "Login started !", Toast.LENGTH_SHORT).show();
                Tools.backToMainMenu(requireActivity());

                return true;
            }

            
            if(url.contains("res=cancel")){
                requireActivity().onBackPressed();
                return true;
            }


            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {}

        @Override
        public void onPageFinished(WebView view, String url) {}
    }


}
