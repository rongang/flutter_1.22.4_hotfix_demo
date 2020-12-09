package com.sk.flutterpatch;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.loader.FlutterApplicationInfo;
import io.flutter.embedding.engine.loader.FlutterLoader;


/**
 * 利用反射把tinker和sophix生成的flutter补丁，打进flutter的so加载流程中
 */
public class FlutterPatch {

    private static final String TAG = "Tinker";
    private static final String STAG = "TinkerS";
    private static String libPathFromSophix = "";

    private static boolean isUseSophix = false;
    private static boolean isUseTinker = true;


    private FlutterPatch() {
    }

    public static String getLibPath(Context context) {
        String libPath = findLibraryFromTinker(context, "lib" + File.separator + getCpuABI(), "libapp.so");
        if (!TextUtils.isEmpty(libPath) && libPath.equals("libapp.so")) {
            return null;
        }
        return libPath;
    }


    public static void reflect(String libPath) {
        TinkerLog.i(TAG, "flutter patch is loading...");
        try {
//
//            Field field = FlutterLoader.class.getDeclaredField("aotSharedLibraryName");
//            field.setAccessible(true);
//            field.set(flutterLoader, libPath);

            Field field = FlutterLoader.class.getDeclaredField("flutterApplicationInfo");
            field.setAccessible(true);
            FlutterApplicationInfo flutterApplicationInfo = (FlutterApplicationInfo) field.get(FlutterInjector.instance().flutterLoader());
            System.out.println(flutterApplicationInfo);
//            Field aotSharedLibraryNameField = FlutterApplicationInfo.class.getDeclaredField("aotSharedLibraryName");
//            aotSharedLibraryNameField.setAccessible(true);
//            System.out.println(aotSharedLibraryNameField.get(flutterApplicationInfo));
//            List<Object> ifs = new ArrayList<>();
//            for (Field f : FlutterApplicationInfo.class.getDeclaredFields()) {
//                f.setAccessible(true);
//                System.out.println(TAG + f.get(flutterApplicationInfo));
//                ifs.add(f.get(flutterApplicationInfo));
//            }
//            TinkerLog.i(TAG, ifs.toString());
            Field aotSharedLibraryNameField = FlutterApplicationInfo.class.getDeclaredField("aotSharedLibraryName");
            Field vmSnapshotDataField = FlutterApplicationInfo.class.getDeclaredField("vmSnapshotData");
            Field isolateSnapshotDataField = FlutterApplicationInfo.class.getDeclaredField("isolateSnapshotData");
            Field flutterAssetsDirField = FlutterApplicationInfo.class.getDeclaredField("flutterAssetsDir");
            Field domainNetworkPolicyField = FlutterApplicationInfo.class.getDeclaredField("domainNetworkPolicy");
            Field nativeLibraryDirField = FlutterApplicationInfo.class.getDeclaredField("nativeLibraryDir");
            Field clearTextPermittedField = FlutterApplicationInfo.class.getDeclaredField("clearTextPermitted");
            aotSharedLibraryNameField.setAccessible(true);
            vmSnapshotDataField.setAccessible(true);
            isolateSnapshotDataField.setAccessible(true);
            flutterAssetsDirField.setAccessible(true);
            domainNetworkPolicyField.setAccessible(true);
            nativeLibraryDirField.setAccessible(true);
            clearTextPermittedField.setAccessible(true);
            String aotSharedLibraryName = (String) aotSharedLibraryNameField.get(flutterApplicationInfo);
            String vmSnapshotData = (String) vmSnapshotDataField.get(flutterApplicationInfo);
            String isolateSnapshotData = (String) isolateSnapshotDataField.get(flutterApplicationInfo);
            String flutterAssetsDir = (String) flutterAssetsDirField.get(flutterApplicationInfo);
            String domainNetworkPolicy = (String) domainNetworkPolicyField.get(flutterApplicationInfo);
            String nativeLibraryDir = (String) nativeLibraryDirField.get(flutterApplicationInfo);
            boolean clearTextPermitted = (boolean) clearTextPermittedField.get(flutterApplicationInfo);
            System.out.println("aotSharedLibraryName："+aotSharedLibraryName);
            System.out.println("vmSnapshotData："+vmSnapshotData);
            System.out.println("isolateSnapshotData："+isolateSnapshotData);
            System.out.println("flutterAssetsDir："+flutterAssetsDir);
            System.out.println("domainNetworkPolicy："+domainNetworkPolicy);
            System.out.println("nativeLibraryDir："+nativeLibraryDir);
            System.out.println("clearTextPermitted："+clearTextPermitted);
            FlutterApplicationInfo flutterApplicationInfoCopy
                    = new FlutterApplicationInfo(
                    libPath,
                    vmSnapshotData,
                    isolateSnapshotData,
                    flutterAssetsDir,
                    domainNetworkPolicy,
                    nativeLibraryDir,
                    clearTextPermitted
            );
            field.set(FlutterInjector.instance().flutterLoader(), flutterApplicationInfoCopy);

            FlutterApplicationInfo flutterApplicationInfoNew = (FlutterApplicationInfo) field.get(FlutterInjector.instance().flutterLoader());
            System.out.println(flutterApplicationInfoNew);
            for (Field f : FlutterApplicationInfo.class.getDeclaredFields()) {
                f.setAccessible(true);
                System.out.println(TAG + f.get(flutterApplicationInfoNew));
            }

            TinkerLog.i(TAG, "flutter patch is loaded successfully");
        } catch (Exception e) {
            TinkerLog.i(TAG, "flutter patch is loaded failed");
            TinkerLog.i(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 插桩方法
     * 此方法不可修改，否则不会成功
     * <p>
     * Tinker和Sophix都集成这种情况是不可能发生的吧？
     *
     * @param obj
     */
    public static void hook(Object obj) {
        TinkerLog.i(STAG, "执行hook");
        if (obj instanceof Context) {

            Context context = (Context) obj;
            TinkerLog.i(TAG, "find FlutterMain");

            if (isUseTinker) {
                TinkerLog.i(STAG, "执行hook-----isUseTinker");
                String libPathFromTinker = getLibPath(context);
                if(libPathFromTinker==null)
                    TinkerLog.i(STAG, "Tinker path is null");
                else
                    TinkerLog.i(STAG, libPathFromTinker);
                if (!TextUtils.isEmpty(libPathFromTinker)) {
                    reflect(libPathFromTinker);
                }
            } else if (isUseSophix) {

                if (!TextUtils.isEmpty(libPathFromSophix)) {
                    reflect(libPathFromSophix);
                }
            } else {
                TinkerLog.i(STAG, "lib path is null");
            }

        } else {

            TinkerLog.i(STAG, "Object: " + obj.getClass().getName());
        }

    }

    /**
     * Sophix 插桩方法，获取sophix so包路径
     * <p>
     * 此方法不可修改，否则不会成功
     *
     * @param obj
     */
    public static void hookSophix(Object obj) {

        if (null != obj) {
            File file = new File(obj.toString() + "/libs/libapp.so");
            if (file.exists() && !file.isDirectory()) {
                libPathFromSophix = file.getAbsolutePath();
                TinkerLog.i(TAG, "path is " + libPathFromSophix);
            } else {
                TinkerLog.i(TAG, "path file is not exist");
            }
        }
    }

    /**
     * Sophix 插桩方法，获取项目是否使用sophix
     * <p>
     * 此方法不可修改，否则不会成功
     */
    public static void hookIsUseSophix() {
        isUseSophix = true;
        TinkerLog.i(TAG, "is use sophix");
    }

    /**
     * Sophix 插桩方法，获取项目是否使用Tinker
     * <p>
     * 此方法不可修改，否则不会成功
     */
    public static void hookIsUseTinker() {
        isUseTinker = true;
        TinkerLog.i(TAG, "is use tinker");
    }


    public static String findLibraryFromTinker(Context context, String relativePath, String libName) throws UnsatisfiedLinkError {
        final Tinker tinker = Tinker.with(context);

        libName = libName.startsWith("lib") ? libName : "lib" + libName;
        libName = libName.endsWith(".so") ? libName : libName + ".so";
        String relativeLibPath = relativePath + File.separator + libName;

        TinkerLog.i(TAG, "flutterPatchInit() called   " + tinker.isTinkerLoaded() + " " + tinker.isEnabledForNativeLib());

        if (tinker.isEnabledForNativeLib() && tinker.isTinkerLoaded()) {
            TinkerLoadResult loadResult = tinker.getTinkerLoadResultIfPresent();
            if (loadResult.libs == null) {
                return libName;
            }
            for (String name : loadResult.libs.keySet()) {
                if (!name.equals(relativeLibPath)) {
                    continue;
                }
                String patchLibraryPath = loadResult.libraryDirectory + "/" + name;
                File library = new File(patchLibraryPath);
                if (!library.exists()) {
                    continue;
                }

                boolean verifyMd5 = tinker.isTinkerLoadVerify();
                if (verifyMd5 && !SharePatchFileUtil.verifyFileMd5(library, loadResult.libs.get(name))) {
                    tinker.getLoadReporter().onLoadFileMd5Mismatch(library, ShareConstants.TYPE_LIBRARY);
                } else {
                    TinkerLog.i(TAG, "findLibraryFromTinker success:" + patchLibraryPath);
                    return patchLibraryPath;
                }
            }
        }

        return libName;
    }

    /**
     * 获取最优abi
     *
     * @return
     */
    public static String getCpuABI() {

        if (Build.VERSION.SDK_INT >= 21) {
            for (String cpu : Build.SUPPORTED_ABIS) {
                if (!TextUtils.isEmpty(cpu)) {
                    TinkerLog.i(TAG, "cpu abi is:" + cpu);
                    return cpu;
                }
            }
        } else {
            TinkerLog.i(TAG, "cpu abi is:" + Build.CPU_ABI);
            return Build.CPU_ABI;
        }

        return "";
    }
}