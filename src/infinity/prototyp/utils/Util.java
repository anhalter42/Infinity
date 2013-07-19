package infinity.prototyp.utils;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 15.07.13
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    public static String dumpObject(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        sb.append(object.getClass().getSimpleName()).append('{');

        boolean firstRound = true;

        for (Field field : fields) {
            if (!firstRound) {
                sb.append(", ");
            } else {
                firstRound = false;
            }
            field.setAccessible(true);
            try {
                final Object fieldObj = field.get(object);
                final Class type = field.getType();
                sb.append(field.getName()).append('=');
                if (null == fieldObj) {
                    sb.append("null");
                } else {
                    if (type.equals(String.class)) {
                        sb.append("\"").append(fieldObj.toString()).append("\"");
                    } else if (type.isPrimitive()) {
                        sb.append(fieldObj.toString());
                    } else {
                        sb.append(dumpObject(fieldObj));
                    }
                }
            } catch (IllegalAccessException ignore) {
                //this should never happen
            }
        }

        sb.append('}');
        return sb.toString();
    }
}
