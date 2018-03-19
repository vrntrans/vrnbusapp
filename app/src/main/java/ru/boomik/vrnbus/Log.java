package ru.boomik.vrnbus;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

@SuppressWarnings("unused")
public final class Log {

    private static final boolean LOGGING = true;
    private static final String TAG = "VrnBus:";

    public static String er(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg);
        return "ТуТу";
    }

    public static void t(Context context, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg);

        Toast.makeText(context, msg,
                Toast.LENGTH_LONG).show();
    }

    public static void t(Context context, CharSequence msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), String.valueOf(msg));

        Toast.makeText(context, msg,
                Toast.LENGTH_LONG).show();
    }

    public static void ts(Context context, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg);

        Toast.makeText(context, msg,
                Toast.LENGTH_SHORT).show();
    }

    public static void td(Context context, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg);

        Toast.makeText(context, TAG + getLocation() + "\n" + msg,
                Toast.LENGTH_LONG).show();
    }

    public static void t(Context context, String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg + "\n" + tr.toString());

        Toast.makeText(context, TAG + getLocation() + "\n" + msg + "\n" + tr.toString(),
                Toast.LENGTH_LONG).show();
    }

    public static void v(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.v(TAG + getLocation(), msg);
    }

    public static void d(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.d(TAG + getLocation(), msg);
    }

    public static void i(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.i(TAG + getLocation(), msg);
    }

    public static void w(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.w(TAG + getLocation(), msg);
    }

    public static void wtf(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.wtf(TAG + getLocation(), msg);
    }

    public static void e(String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg);
    }

    public static void v(String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.v(TAG + getLocation(), msg, tr);
    }

    public static void d(String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.d(TAG + getLocation(), msg, tr);
    }

    public static void i(String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.i(TAG + getLocation(), msg, tr);
    }

    public static void w(String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.w(TAG + getLocation(), msg, tr);
    }

    public static void e(String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG + getLocation(), msg, tr);
    }

    public static void v(String TAG, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.v(TAG, getLocation() + msg);
    }

    public static void d(String TAG, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.d(TAG, getLocation() + msg);
    }

    public static void i(String TAG, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.i(TAG, getLocation() + msg);
    }

    public static void w(String TAG, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.w(TAG, getLocation() + msg);
    }

    public static void e(String TAG, String msg) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG, getLocation() + msg);
    }

    public static void v(String TAG, String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.v(TAG, getLocation() + msg, tr);
    }

    public static void d(String TAG, String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.d(TAG, getLocation() + msg, tr);
    }

    public static void i(String TAG, String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.i(TAG, getLocation() + msg, tr);
    }

    public static void w(String TAG, String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        if (LOGGING)
            android.util.Log.w(TAG, getLocation() + msg, tr);
    }

    public static void e(String TAG, String msg, Throwable tr) {
        if (msg == null || msg.equals("")) {
            msg = "ERROR: SEND NULL MSG TO LOG";
        }
        android.util.Log.e(TAG, getLocation() + msg, tr);
    }

    private static String getLocation() {
        final String className = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread()
                .getStackTrace();
        boolean found = false;

        for (StackTraceElement trace : traces) {
            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":"
                                + trace.getMethodName() + ":"
                                + trace.getLineNumber() + "] ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        return "";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }
}
