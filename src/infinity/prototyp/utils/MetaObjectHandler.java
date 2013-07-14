package infinity.prototyp.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 11.07.13
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class MetaObjectHandler {
    protected HashMap<Class, IMetaObjectDeserializer> deserializers = new HashMap<Class, IMetaObjectDeserializer>();
    protected HashMap<Class, IMetaObjectFieldValueProvider> fieldValueProviders = new HashMap<Class, IMetaObjectFieldValueProvider>();

    public MetaObjectHandler() {
        fieldValueProviders.put(String.class, new MetaObjectFieldValueProviderString());
        fieldValueProviders.put(Integer.class, new MetaObjectFieldValueProviderInteger());
        fieldValueProviders.put(int.class, new MetaObjectFieldValueProviderInteger());
        fieldValueProviders.put(float.class, new MetaObjectFieldValueProviderFloat());
    }

    public void registerFieldValueProvider(Class aClass, IMetaObjectFieldValueProvider aProvider) {
        fieldValueProviders.put(aClass, aProvider);
    }

    public Object deserialize(Object aValue, Class aClass) throws IllegalAccessException, InstantiationException {
        Object lResult = aClass.newInstance();
        assignTo(aValue, lResult);
        return lResult;
    }

    public Object deserializeTo(Object aValue, Object aObject) {
        assignTo(aValue, aObject);
        return aObject;
    }

    private void assignTo(Object aValue, Object aResult) {
        Class lClass = aResult.getClass();
        if (aValue instanceof Map) {
            Map<String, Object> lMap = (Map) aValue;
            for (String lKey : lMap.keySet()) {
                Object lOriginalFieldValue = lMap.get(lKey);
                Object lFieldValue = lOriginalFieldValue;
                Class lDestClass = Object.class;
                String lGetMethodName = lKey.substring(0, 1).toUpperCase() + lKey.substring(1);
                lGetMethodName = "get" + lGetMethodName;
                try {
                    Method lMethod = lClass.getMethod(lGetMethodName);
                    try {
                        lDestClass = lMethod.getReturnType();
                        lFieldValue = lMethod.invoke(aResult);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (NoSuchMethodException exm) {
                    try {
                        Field lField = lClass.getField(lKey);
                        lDestClass = lField.getType();
                        lFieldValue = lField.get(aResult);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                IMetaObjectFieldValueProvider lProvider = fieldValueProviders.get(lDestClass);
                if (lProvider != null) {
                    lFieldValue = lProvider.provide(lOriginalFieldValue, lFieldValue);
                } else {
                    //TODO
                    lFieldValue = lOriginalFieldValue;
                }
                String lSetMethodName = lKey.substring(0, 1).toUpperCase() + lKey.substring(1);
                lSetMethodName = "set" + lSetMethodName;
                try {
                    Method lMethod = lClass.getMethod(lSetMethodName, lDestClass);
                    try {
                        lMethod.invoke(aResult, lFieldValue);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (NoSuchMethodException exm) {
                    try {
                        Field lField = lClass.getField(lKey);
                        lField.set(aResult, lFieldValue);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                if (aResult instanceof IMetaObject) {
                    ((IMetaObject) aResult).setValue(lKey, lFieldValue);
                }
            }
        }
    }

    public static class MetaObjectFieldValueProviderString implements IMetaObjectFieldValueProvider {

        @Override
        public Object provide(Object aSourceValue, Object aFieldValue) {
            return aSourceValue != null ? aSourceValue.toString() : null;
        }
    }

    public static class MetaObjectFieldValueProviderInteger implements IMetaObjectFieldValueProvider {

        @Override
        public Object provide(Object aSourceValue, Object aFieldValue) {
            return aSourceValue != null ? Integer.valueOf(aSourceValue.toString()) : null;
        }
    }

    public static class MetaObjectFieldValueProviderFloat implements IMetaObjectFieldValueProvider {

        @Override
        public Object provide(Object aSourceValue, Object aFieldValue) {
            return aSourceValue != null ? Float.valueOf(aSourceValue.toString()) : null;
        }
    }
}
