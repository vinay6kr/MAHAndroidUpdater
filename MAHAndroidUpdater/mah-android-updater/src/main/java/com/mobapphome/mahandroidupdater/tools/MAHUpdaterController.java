package com.mobapphome.mahandroidupdater.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;

import com.mobapphome.mahandroidupdater.MAHRestricterDlg;
import com.mobapphome.mahandroidupdater.MAHUpdaterDlg;
import com.mobapphome.mahandroidupdater.types.ProgramInfo;
import com.mobapphome.mahandroidupdater.types.DlgModeEnum;

public class MAHUpdaterController {
    public static String urlService;
    private static SharedPreferences sharedPref;
    private static String fontName = null;
    private static FragmentActivity act;
    private static boolean initCalled = false;


    public static void init(final FragmentActivity act, String urlService) throws NullPointerException {
        if (initCalled) {
            return;
        }

        MAHUpdaterController.urlService = urlService;
        MAHUpdaterController.act = act;
        if (urlService == null) {
            throw new NullPointerException("urlService not set call init(final Activity act, String urlService) constructor");
        }

        sharedPref = act.getPreferences(Context.MODE_PRIVATE);

        Updater updater = new Updater();
        updater.setUpdaterListiner(new UpdaterListener() {

            @Override
            public void onResponse(ProgramInfo programInfo, final String errorStr) {
                if (programInfo == null) {
                    Log.i("Test", "MAhUpdater Program info is null");
                    return;
                } else if (!programInfo.isRunMode()) {
                    Log.i("Test", "MAHUpdter run mode is false");
                    return;
                } else if (programInfo.getUriCurrent() == null) {
                    Log.i("Test", "MAHUpdter uri_current is null in service. check service");
                    return;
                } else if (programInfo.getVersionCodeCurrent() == -1) {
                    Log.i("Test", "MAHUpdter version_code_current is -1 in service. check service");
                    return;
                } else {
					Log.i("Test", "Uri current from service = " + programInfo.getUriCurrent() + "  Uri from package" + act.getApplicationContext().getPackageName());
					Log.i("Test", "Version from service = " + programInfo.getVersionCodeCurrent() + "  Version from package" + Utils.getVersionCode(act));

                    boolean isRestrictedDlg = false;
                    if (Utils.getVersionCode(act) < programInfo.getVersionCodeMin()) {
                        isRestrictedDlg = true;
                    }
                    if (!programInfo.getUriCurrent().equals(act.getApplicationContext().getPackageName())) {
                        if (Utils.checkPackageIfExists(act, programInfo.getUriCurrent())) {
                            showRestricterDlg(act, DlgModeEnum.OPEN_NEW, programInfo);
                        } else if (isRestrictedDlg) {
                            showRestricterDlg(act, DlgModeEnum.INSTALL, programInfo);
                        } else {
                            showUpdaterDlg(act, DlgModeEnum.INSTALL, programInfo);
                        }
                    } else if (Utils.getVersionCode(act) < programInfo.getVersionCodeCurrent()) {
                        if (isRestrictedDlg) {
                            showRestricterDlg(act, DlgModeEnum.UPDATE, programInfo);
                        } else {
                            showUpdaterDlg(act, DlgModeEnum.UPDATE, programInfo);
                        }
                    } else {
                        Log.i("Test", "MAHUpdater: There are not any update in service");
                    }
                }
            }
        });
        updater.updateProgramList(act);
        initCalled = true;
    }

    public static void callUpdate() throws NullPointerException {
        if (MAHUpdaterController.urlService == null) {
            throw new NullPointerException("urlService not set call init(final Activity act, String urlService) constructor");
        }


        Updater updater = new Updater();
        updater.setUpdaterListiner(new UpdaterListener() {

            @Override
            public void onResponse(ProgramInfo programInfo, final String errorStr) {

            }
        });
        updater.updateProgramList(act);
    }


    public static void end() {
        Log.i("Test", "MAHUpdater end called");
        callUpdate();
        initCalled = false;
    }

    static public void testUpdaterDlg(final FragmentActivity act){
        final ProgramInfo prInfoTest = new ProgramInfo();
        prInfoTest.setUpdateInfo("Update info test mode.");
        showUpdaterDlg(act, DlgModeEnum.TEST, prInfoTest);
    }

    static public void testRestricterDlg(final FragmentActivity act){
        final ProgramInfo prInfoTest = new ProgramInfo();
        prInfoTest.setUpdateInfo("Update info test mode. ");
        showRestricterDlg(act, DlgModeEnum.TEST, prInfoTest);
    }

    static private void showUpdaterDlg(final FragmentActivity act, DlgModeEnum mode, final ProgramInfo programInfo) {
        if (!act.isFinishing()) {
            FragmentTransaction transaction = act.getSupportFragmentManager().beginTransaction();
            MAHUpdaterDlg mahUpdaterDlg = MAHUpdaterDlg.newInstance(programInfo, mode);

            //transaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
            // This animation works only when fragment adds to view.
            // If fragment does not add to view then other type animation is available
            transaction.add(mahUpdaterDlg, "fragment_android_updater_dlg");

            transaction.commitAllowingStateLoss();
        }
    }

    static private void showRestricterDlg(final FragmentActivity act, DlgModeEnum mode, final ProgramInfo programInfo) {
        if (!act.isFinishing()) {
            FragmentTransaction transaction = act.getSupportFragmentManager().beginTransaction();
            MAHRestricterDlg mahRestricterDlg = MAHRestricterDlg.newInstance(programInfo, mode);

            //transaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
            // This animation works only when fragment adds to view.
            // If fragment does not add to view then other type animation is available
            transaction.add(mahRestricterDlg, "fragment_restricter_dlg");
            transaction.commitAllowingStateLoss();
        }
    }

    protected static SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public static void setFontTextView(TextView tv) {
        if (fontName == null) {
            return;
        }
        try {
            Typeface font = Typeface.createFromAsset(tv.getContext().getAssets(), fontName);
            tv.setTypeface(font);
        } catch (RuntimeException r) {
            Log.e("test", "Error " + r.getMessage());
        }
    }

    public static String getFontName() {
        return fontName;
    }

    public static void setFontName(String fontName) {
        MAHUpdaterController.fontName = fontName;
    }
}
