package com.koudaiai.aiofix;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * 绕过 Android 12/13 上 DisplayManagerService 的包名校验。
 *
 * 背景：BAAS AIO 通过 Shizuku 的 UserService 建 VirtualDisplay，该进程以 root(uid 0)
 * 运行，传的 owner package 却是应用包名。Android 12/13 的 validatePackageName 只认
 * “uid 对应的包”，uid 0 查不到包直接 false，于是抛
 *   SecurityException: packageName must match the calling uid
 * Android 14 才加了 `if (uid == ROOT_UID) return true;` 的特例。
 *
 * 这里把 root / shell 的调用直接放行，保留其他 uid 的原逻辑不变。
 */
public class Hook implements IXposedHookLoadPackage {

    private static final int ROOT_UID = 0;
    private static final int SHELL_UID = 2000;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        // 只在 system_server 里生效
        if (!"android".equals(lpparam.packageName)) {
            return;
        }
        boolean hooked = hookTarget(lpparam, "com.android.server.display.DisplayManagerService$BinderService")
                || hookTarget(lpparam, "com.android.server.display.DisplayManagerService");
        if (!hooked) {
            XposedBridge.log("[AIOFix] validatePackageName not found - hook skipped");
        }
    }

    private boolean hookTarget(LoadPackageParam lpparam, String className) {
        try {
            Class<?> clazz = XposedHelpers.findClass(className, lpparam.classLoader);
            int count = 0;
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (!"validatePackageName".equals(method.getName())) {
                    continue;
                }
                XposedBridge.hookMethod(method, new BypassHook());
                count++;
            }
            if (count > 0) {
                XposedBridge.log("[AIOFix] hooked " + count + " validatePackageName in " + className);
                return true;
            }
        } catch (Throwable t) {
            XposedBridge.log("[AIOFix] " + className + " -> " + t);
        }
        return false;
    }

    private static class BypassHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            if (param.args == null || param.args.length == 0) {
                return;
            }
            Object first = param.args[0];
            if (!(first instanceof Integer)) {
                return;
            }
            int uid = (Integer) first;
            if (uid == ROOT_UID || uid == SHELL_UID) {
                // 只放行 root/shell；普通应用仍走原来的包名匹配
                param.setResult(Boolean.TRUE);
            }
        }
    }
}
