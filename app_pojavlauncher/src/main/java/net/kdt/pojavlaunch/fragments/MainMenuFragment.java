package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.dialogOnUiThread;
import static net.kdt.pojavlaunch.Tools.hasMods;
import static net.kdt.pojavlaunch.Tools.hasNoOnlineProfileDialog;
import static net.kdt.pojavlaunch.Tools.hasOnlineProfile;
import static net.kdt.pojavlaunch.Tools.openPath;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.Tools.shareLog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.modloaders.LWJGL3ifyUtils;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.util.List;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        applyTheme(view);
        Button mDiscordButton = view.findViewById(R.id.discord_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenDirectoryButton = view.findViewById(R.id.open_files_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mDiscordButton.setOnClickListener(v -> Tools.openURL(requireActivity(), getString(R.string.discord_invite)));
        view.findViewById(R.id.kollegen_social_button).setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), KollegenSocialFragment.class, KollegenSocialFragment.TAG, null));
        mCustomControlButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        if (hasOnlineProfile()) {
            mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
            mInstallJarButton.setOnLongClickListener(v -> {
                runInstallerWithConfirmation(true);
                return true;
            });
        } else mInstallJarButton.setOnClickListener(v -> hasNoOnlineProfileDialog(requireActivity()));
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> {
            if (Tools.hasMods("sodium") && !(LauncherPreferences.DEFAULT_PREF.getBoolean("sodium_override", false))) {
                AlertDialog sodiumWarningDialog = new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.sodium_warning_title)
                        .setMessage(R.string.sodium_warning_message)
                        .setNeutralButton(R.string.delete_sodium, (d,w)-> {
                            Tools.deleteSodiumMods();
                            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
                        })
                        .create();
                sodiumWarningDialog.show();
            } else ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });

        mShareLogsButton.setOnClickListener((v) -> shareLog(requireContext()));

        mOpenDirectoryButton.setOnClickListener((v)-> {
            if (Tools.isDemoProfile(v.getContext())){ 
                hasNoOnlineProfileDialog(getActivity(), getString(R.string.demo_unsupported), getString(R.string.change_account));
            } else if (!hasOnlineProfile()) { 
                hasNoOnlineProfileDialog(requireActivity());
            } else openPath(v.getContext(), getCurrentProfileDirectory(), false);

        });
    }

    private File getCurrentProfileDirectory() {
        String currentProfile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, null);
        if(!Tools.isValidString(currentProfile)) return new File(Tools.DIR_GAME_NEW);
        LauncherProfiles.load();
        MinecraftProfile profileObject = LauncherProfiles.mainProfileJson.profiles.get(currentProfile);
        if(profileObject == null) return new File(Tools.DIR_GAME_NEW);
        return Tools.getGameDirPath(profileObject);
    }

    private void applyTheme(View view){
        int[] pal = net.kdt.pojavlaunch.KollegenTheme.palette();
        view.setBackgroundColor(pal[net.kdt.pojavlaunch.KollegenTheme.BG]);
        View bottomBar = view.findViewById(R.id._background_display_view);
        if(bottomBar != null) bottomBar.setBackgroundColor(pal[net.kdt.pojavlaunch.KollegenTheme.PANEL2]);
        View versionSpinner = view.findViewById(R.id.mc_version_spinner);
        if(versionSpinner instanceof mcVersionSpinner) ((mcVersionSpinner) versionSpinner).setTextColor(pal[net.kdt.pojavlaunch.KollegenTheme.TEXT]);
        Button playButton = view.findViewById(R.id.play_button);
        if(playButton != null){
            playButton.setBackground(net.kdt.pojavlaunch.KollegenTheme.buttonBackground(net.kdt.pojavlaunch.KollegenTheme.ACCENT, net.kdt.pojavlaunch.KollegenTheme.ACCENT2));
            playButton.setTextColor(pal[net.kdt.pojavlaunch.KollegenTheme.BG]);
        }
        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        if(mEditProfileButton != null) mEditProfileButton.setColorFilter(pal[net.kdt.pojavlaunch.KollegenTheme.TEXT]);
        Button[] menuButtons = new Button[]{view.findViewById(R.id.discord_button), view.findViewById(R.id.kollegen_social_button), view.findViewById(R.id.custom_control_button), view.findViewById(R.id.install_jar_button), view.findViewById(R.id.share_logs_button), view.findViewById(R.id.open_files_button)};
        for(Button button : menuButtons){
            if(button != null) button.setTextColor(pal[net.kdt.pojavlaunch.KollegenTheme.TEXT]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
