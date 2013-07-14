package infinity.prototyp.utils;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 11.07.13
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public interface IMetaObject {
    public Object getValue(String aName);
    public void setValue(String aName, Object aValue);
}
