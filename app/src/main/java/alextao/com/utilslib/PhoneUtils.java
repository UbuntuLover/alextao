package alextao.com.utilslib;

import java.lang.reflect.Method;

/**
 * Created by Alextao on 2018/8/3.
 * Email : tao_xue@new-mobi.com
 * This class is some Utils about Phone.
 */
public class PhoneUtils {


    /**
     * This is a util method about to get system properties.
     *
     * @param key          key
     * @param defaultValue defaultValue
     * @return
     * @see android.os.Build
     */
    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) get.invoke(c, key, "unknown");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    /**
     * This is a util method about how to set the system properties.
     *
     * @param key   key
     * @param value value
     * @see android.os.Build
     */
    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
