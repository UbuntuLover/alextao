package alextao.com.utilslib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Alextao
 * Email : tao_xue@new-mobi.com
 * <p>
 * This is a Handler about to log runtime error and log it to file.
 * We can use it when we customization ourselves.
 * </p>
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    //the default handler.
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    //for not memory leak.
    private WeakReference<Context> mWeakContextRef;

    private Map<String, String> infos = new HashMap<>();

    //for logging time and file name.
    private DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss",
            Locale.CHINESE);


    public void init(Context context) {
        mWeakContextRef = new WeakReference<>(context);
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!handleException(e) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        } else {
            try {
                Thread.sleep(3000);
                //exit. QAQ
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

    }


    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mWeakContextRef.get(), "Logging error...", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();

        collectDeviceInfo(mWeakContextRef.get());
        log2File(ex);
        return true;
    }


    private void log2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "= " + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        String time = formatter.format(new Date());
        String fileName = time + ".txt";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = Environment.getExternalStorageDirectory() + File.separator;
            try {
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes("UTF-8"));
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void collectDeviceInfo(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                String versionName = packageInfo.versionName == null ? "null" : packageInfo.versionName;
                String versionCode = packageInfo.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, "collectDeviceInfo: ");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}
