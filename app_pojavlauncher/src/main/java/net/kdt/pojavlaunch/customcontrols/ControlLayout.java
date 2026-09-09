package net.kdt.pojavlaunch.customcontrols;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import static org.lwjgl.glfw.CallbackBridge.isGrabbing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonSyntaxException;
import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlButton;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlDrawer;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlJoystick;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlSubButton;
import net.kdt.pojavlaunch.customcontrols.handleview.ActionRow;
import net.kdt.pojavlaunch.customcontrols.handleview.ControlHandleView;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlSideDialog;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ControlLayout extends FrameLayout {
	protected CustomControls mLayout;
	
	private MinecraftGLSurface mGameSurface = null;

	
	private List<ControlInterface> mButtons;
	private boolean mModifiable = false;
	private boolean mIsModified;
	private boolean mControlVisible = false;

	private EditControlSideDialog mControlDialog = null;
	private ControlHandleView mHandleView;
	private ControlButtonMenuListener mMenuListener;
	public ActionRow mActionRow = null;
	public String mLayoutFileName;

	public ControlLayout(Context ctx) {
		super(ctx);
	}

	public ControlLayout(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}


	public void loadLayout(String jsonPath) throws IOException, JsonSyntaxException {
		CustomControls layout = LayoutConverter.loadAndConvertIfNecessary(jsonPath);
		if(layout != null) {
			loadLayout(layout);
			updateLoadedFileName(jsonPath);
			return;
		}

		throw new IOException("Unsupported control layout version");
	}

	public void loadLayout(CustomControls controlLayout) {
		boolean sanitizedModified = false;
		if(controlLayout != null) {
			sanitizedModified = LayoutSanitizer.sanitizeLayout(controlLayout);
		}
		if(mActionRow == null){
			mActionRow = new ActionRow(getContext());
			addView(mActionRow);
		}

		removeAllButtons();
		if(mLayout != null) {
			mLayout.mControlDataList = null;
			mLayout = null;
		}

		System.gc();
		mapTable.clear();

		
		if (controlLayout == null) return;

		mLayout = controlLayout;
		

		
		for(ControlJoystickData joystick : mLayout.mJoystickDataList){
			addJoystickView(joystick);
		}

		
		for (ControlData button : controlLayout.mControlDataList) {
			addControlView(button);
		}

		
		for(ControlDrawerData drawerData : controlLayout.mDrawerDataList){
			ControlDrawer drawer = addDrawerView(drawerData);
			if(mModifiable) drawer.areButtonsVisible = true;
		}

		mLayout.scaledAt = LauncherPreferences.PREF_BUTTONSIZE;

		setModified(sanitizedModified);
		mButtons = null;
		getButtonChildren(); 
	} 

	
	public void addControlButton(ControlData controlButton) {
		mLayout.mControlDataList.add(controlButton);
		addControlView(controlButton);
	}

	private void addControlView(ControlData controlButton) {
		final ControlButton view = new ControlButton(this, controlButton);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);

		setModified(true);
	}

	
	public void addDrawer(ControlDrawerData drawerData){
		mLayout.mDrawerDataList.add(drawerData);
		addDrawerView();
	}

	private void addDrawerView(){
		addDrawerView(null);
	}

	private ControlDrawer addDrawerView(ControlDrawerData drawerData){

		final ControlDrawer view = new ControlDrawer(this,drawerData == null ? mLayout.mDrawerDataList.get(mLayout.mDrawerDataList.size()-1) : drawerData);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);
		
		for (ControlData subButton : view.getDrawerData().buttonProperties) {
			addSubView(view, subButton);
		}

		setModified(true);
		return view;
	}

	
	public void addSubButton(ControlDrawer drawer, ControlData controlButton){
		
		drawer.getDrawerData().buttonProperties.add(controlButton);
		addSubView(drawer, drawer.getDrawerData().buttonProperties.get(drawer.getDrawerData().buttonProperties.size()-1 ));
	}

	private void addSubView(ControlDrawer drawer, ControlData controlButton){
		final ControlSubButton view = new ControlSubButton(this, controlButton, drawer);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}else{
			view.setVisible(true);
		}

		addView(view);
		drawer.addButton(view);


		setModified(true);
	}

	
	public void addJoystickButton(ControlJoystickData data){
		mLayout.mJoystickDataList.add(data);
		addJoystickView(data);
	}

	private void addJoystickView(ControlJoystickData data){
		ControlJoystick view = new ControlJoystick(this, data);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);

	}


	private void removeAllButtons() {
		for(ControlInterface button : getButtonChildren()){
			removeView(button.getControlView());
		}

		System.gc();
		
		
	}

	public void saveLayout(String path) throws Exception {
		mLayout.save(path);
		setModified(false);
	}

	public void toggleControlVisible(){
		mControlVisible = !mControlVisible;
		setControlVisible(mControlVisible);
	}

	public float getLayoutScale(){
		return mLayout.scaledAt;
	}

	public CustomControls getLayout(){
		return mLayout;
	}

	public void setControlVisible(boolean isVisible) {
		if (mModifiable) return; 

		mControlVisible = isVisible;
		for(ControlInterface button : getButtonChildren()){
			button.setVisible(((button.getProperties().displayInGame && isGrabbing()) || (button.getProperties().displayInMenu && !isGrabbing())) && isVisible);
		}
	}

	public void setModifiable(boolean isModifiable) {
		if(!isModifiable && mModifiable){
			removeEditWindow();
		}
		mModifiable = isModifiable;
		if(isModifiable){
			
			for(ControlInterface button : getButtonChildren()){
				button.setVisible(true);
			}
		}
	}

	public boolean getModifiable(){
		return mModifiable;
	}

	public void setModified(boolean isModified) {
		mIsModified = isModified;
	}

	public List<ControlInterface> getButtonChildren(){
		if(mModifiable || mButtons == null){
			mButtons = new ArrayList<>();
			for(int i=0; i<getChildCount(); ++i){
				View v = getChildAt(i);
				if(v instanceof ControlInterface)
					mButtons.add(((ControlInterface) v));
			}
		}

		return mButtons;
	}

	public void refreshControlButtonPositions(){
		for(ControlInterface button : getButtonChildren()){
			button.setDynamicX(button.getProperties().dynamicX);
			button.setDynamicY(button.getProperties().dynamicY);
		}
	}

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if(child instanceof ControlInterface && mControlDialog != null){
			mControlDialog.disappearColor();
            mControlDialog.disappear(false);
        }
    }

    
	public void editControlButton(ControlInterface button){
		if(mControlDialog == null){
			
			
			mControlDialog = new EditControlSideDialog(getContext(), this);
			post(() -> editControlButton(button));
			return;
		}

		mControlDialog.internalChanges = true;
		mControlDialog.setCurrentlyEditedButton(button);

		mControlDialog.appear(button.getControlView().getX() + button.getControlView().getWidth()/2f < currentDisplayMetrics.widthPixels/2f);
		button.loadEditValues(mControlDialog);

		mControlDialog.internalChanges = false;

		mControlDialog.disappearColor();

		if(mHandleView == null){
			mHandleView = new ControlHandleView(getContext());
			addView(mHandleView);
		}
		mHandleView.setControlButton(button);

		
	}

	
	public void adaptPanelPosition(){
		if(mControlDialog != null) mControlDialog.adaptPanelPosition();
	}


	final HashMap<View, ControlInterface> mapTable = new HashMap<>();

	
	public void onTouch(View v, MotionEvent ev) {
		ControlInterface lastControlButton = mapTable.get(v);

		
		ev.offsetLocation(v.getX(), v.getY());


		
		if (ev.getActionMasked() == MotionEvent.ACTION_UP
				|| ev.getActionMasked() == MotionEvent.ACTION_CANCEL
				|| ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
			if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
			mapTable.put(v, null);
			return;
		}

		if (ev.getActionMasked() != MotionEvent.ACTION_MOVE) return;


		
		if (lastControlButton != null) {
			System.out.println("last control button check" + ev.getX() + "-" + ev.getY() + "-" + lastControlButton.getControlView().getX() + "-" + lastControlButton.getControlView().getY());
			if (ev.getX() > lastControlButton.getControlView().getX()
					&& ev.getX() < lastControlButton.getControlView().getX() + lastControlButton.getControlView().getWidth()
					&& ev.getY() > lastControlButton.getControlView().getY()
					&& ev.getY() < lastControlButton.getControlView().getY() + lastControlButton.getControlView().getHeight()) {
				return;
			}
		}

		
		if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
		mapTable.remove(v);

		
		for (ControlInterface button : getButtonChildren()) {
			if (!button.getProperties().isSwipeable) continue;

			if (ev.getX() > button.getControlView().getX()
					&& ev.getX() < button.getControlView().getX() + button.getControlView().getWidth()
					&& ev.getY() > button.getControlView().getY()
					&& ev.getY() < button.getControlView().getY() + button.getControlView().getHeight()) {

				
				if (!button.equals(lastControlButton)) {
					button.sendKeyPresses(true);
					mapTable.put(v, button);
					return;
				}

			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mModifiable && event.getActionMasked() != MotionEvent.ACTION_UP || mControlDialog == null)
			return true;

		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		
		if(!imm.hideSoftInputFromWindow(getWindowToken(), 0)){
			if(mControlDialog.disappearLayer()){
				mActionRow.setFollowedButton(null);
				mHandleView.hide();
			}
		}
		return true;
	}

	public void removeEditWindow() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
		if(mControlDialog != null) {
			mControlDialog.disappearColor();
			mControlDialog.disappear(true);
		}

		if(mActionRow != null) mActionRow.setFollowedButton(null);
		if(mHandleView != null) mHandleView.hide();
	}

	public void save(String path){
		try {
			mLayout.save(path);
		} catch (IOException e) {Log.e("ControlLayout", "Failed to save the layout at:" + path);}
	}


	public boolean hasMenuButton() {
		for(ControlInterface controlInterface : getButtonChildren()){
			for (int keycode : controlInterface.getProperties().keycodes) {
				if (keycode == ControlData.SPECIALBTN_MENU) return true;
			}
		}
		return false;
	}

	public void setMenuListener(ControlButtonMenuListener menuListener) {
		this.mMenuListener = menuListener;
	}

	public void notifyAppMenu() {
		if(mMenuListener != null) mMenuListener.onClickedMenu();
	}

	
	public MinecraftGLSurface getGameSurface(){
		if(mGameSurface == null){
			mGameSurface = findViewById(R.id.main_game_render_view);
		}
		return mGameSurface;
	}

	public void askToExit(EditorExitable editorExitable) {
		if(mIsModified) {
			openSaveDialog(editorExitable);
		}else{
			openExitDialog(editorExitable);
		}
	}

	public void updateLoadedFileName(String path) {
		path = path.replace(Tools.CTRLMAP_PATH, ".");
		path = path.substring(0, path.length() - 5);
		mLayoutFileName = path;
	}

	public String saveToDirectory(String name) throws Exception{
		String jsonPath = Tools.CTRLMAP_PATH + "/" + name + ".json";
		saveLayout(jsonPath);
		return jsonPath;
	}

	class OnClickExitListener implements View.OnClickListener {
		private final AlertDialog mDialog;
		private final EditText mEditText;
		private final EditorExitable mListener;

		public OnClickExitListener(AlertDialog mDialog, EditText mEditText, EditorExitable mListener) {
			this.mDialog = mDialog;
			this.mEditText = mEditText;
			this.mListener = mListener;
		}

		@Override
		public void onClick(View v) {
			Context context = v.getContext();
			if (mEditText.getText().toString().isEmpty()) {
				mEditText.setError(context.getString(R.string.global_error_field_empty));
				return;
			}
			try {
				String jsonPath = saveToDirectory(mEditText.getText().toString());
				Toast.makeText(context, context.getString(R.string.global_save) + ": " + jsonPath, Toast.LENGTH_SHORT).show();
				mDialog.dismiss();
				if(mListener != null) mListener.exitEditor();
			} catch (Throwable th) {
				Tools.showError(context, th, mListener != null);
			}
		}
	}

	public void openSaveDialog(EditorExitable editorExitable) {
		final Context context = getContext();
		final EditText edit = new EditText(context);
		edit.setSingleLine();
		edit.setText(mLayoutFileName);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.global_save);
		builder.setView(edit);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNegativeButton(android.R.string.cancel, null);
		if(editorExitable != null) builder.setNeutralButton(R.string.global_save_and_exit, null);
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(dialogInterface -> {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE)
					.setOnClickListener(new OnClickExitListener(dialog, edit, null));
			if(editorExitable != null) dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
					.setOnClickListener(new OnClickExitListener(dialog, edit, editorExitable));
		});
		dialog.show();
	}

	public void openLoadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.global_load);
		builder.setPositiveButton(android.R.string.cancel, null);

		final AlertDialog dialog = builder.create();
		FileListView flv = new FileListView(dialog, "json");
		if(Build.VERSION.SDK_INT < 29)flv.listFileAt(new File(Tools.CTRLMAP_PATH));
		else flv.lockPathAt(new File(Tools.CTRLMAP_PATH));
		flv.setFileSelectedListener(new FileSelectedListener(){

			@Override
			public void onFileSelected(File file, String path) {
				try {
					loadLayout(path);
				}catch (IOException e) {
					Tools.showError(getContext(), e);
				}
				dialog.dismiss();
			}
		});
		dialog.setView(flv);
		dialog.show();
	}

	public void openSetDefaultDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.customctrl_selectdefault);
		builder.setPositiveButton(android.R.string.cancel, null);

		final AlertDialog dialog = builder.create();
		FileListView flv = new FileListView(dialog, "json");
		flv.lockPathAt(new File(Tools.CTRLMAP_PATH));
		flv.setFileSelectedListener(new FileSelectedListener(){

			@Override
			public void onFileSelected(File file, String path) {
				try {
					LauncherPreferences.DEFAULT_PREF.edit().putString("defaultCtrl", path).apply();
					LauncherPreferences.PREF_DEFAULTCTRL_PATH = path;loadLayout(path);
				}catch (IOException|JsonSyntaxException e) {
					Tools.showError(getContext(), e);
				}
				dialog.dismiss();
			}
		});
		dialog.setView(flv);
		dialog.show();
	}

	public void openExitDialog(EditorExitable exitListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.customctrl_editor_exit_title);
		builder.setMessage(R.string.customctrl_editor_exit_msg);
		builder.setPositiveButton(R.string.global_yes, (d,w)->exitListener.exitEditor());
		builder.setNegativeButton(R.string.global_no, (d,w)->{});
		builder.show();
	}

	public boolean areControlVisible(){
		return mControlVisible;
	}
}
