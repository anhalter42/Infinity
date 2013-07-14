package infinity.prototyp.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 11.07.13
 * Time: 08:21
 * To change this template use File | Settings | File Templates.
 */
public class MetaObject implements IMetaObject {
    protected Map<String, Object> values = new HashMap<String, Object>();

    @Override
    public Object getValue(String aName) {
        return values.get(aName);
    }

    @Override
    public void setValue(String aName, Object aValue) {
        values.put(aName, aValue);
    }

    public MetaObject() {

    }
}
