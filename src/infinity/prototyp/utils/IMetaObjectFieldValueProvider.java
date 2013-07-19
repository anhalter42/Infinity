package infinity.prototyp.utils;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 12.07.13
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */
public interface IMetaObjectFieldValueProvider {
    public Object provideFrom(Object aSourceValue, Object aFieldValue);
    public String provideTo(Object aSourceValue);
}
