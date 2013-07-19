package infinity.prototyp.yaml;

import infinity.prototyp.utils.IMetaObjectFieldValueProvider;
import infinity.prototyp.utils.MetaObject;
import infinity.prototyp.utils.MetaObjectHandler;
import infinity.prototyp.utils.Util;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 13.07.13
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
public class YamlMain {
    public static class Point {
        int x;
        int y;
    }

    public static class MyObject extends MetaObject {
        public String a;
        protected String b;
        public int c;
        public int d;
        public float e;
        public Point f;

        public String getB() {
            return b;
        }

        public void setB(String aV) {
            b = aV;
        }
    }

    public static void main(String[] args) {
        MyObject lmyobj = new MyObject();
        Yaml lyaml = new Yaml();
        Object lobj = lyaml.load("a: \"Hello\"\nb: \"World\"\nc: 42\nd: 23\ne: 42.23\nf: 42,23");
        MetaObjectHandler lHandler = new MetaObjectHandler();
        lHandler.registerFieldValueProvider(Point.class,new MetaObjectFieldValueProviderPoint());
        lHandler.deserializeTo(lobj, lmyobj);
        System.out.println(Util.dumpObject(lmyobj));
        System.out.println("Hello World!");
    }

    public static class MetaObjectFieldValueProviderPoint implements IMetaObjectFieldValueProvider {

        @Override
        public Object provideFrom(Object aSourceValue, Object aFieldValue) {
            String lStr = aSourceValue.toString();
            String[] lParts = lStr.split("\\,");
            Point lP;
            if (aFieldValue == null) {
                lP = new Point();
            } else {
                lP = (Point) aFieldValue;
            }
            lP.x = Integer.valueOf(lParts[0]);
            lP.y = Integer.valueOf(lParts[1]);
            return lP;
        }

        @Override
        public String provideTo(Object aSourceValue) {
            return ((Point)aSourceValue).x + "," + ((Point)aSourceValue).y;
        }
    }

}
