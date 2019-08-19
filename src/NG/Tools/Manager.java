package NG.Tools;

import java.util.Arrays;

/**
 * A manager represents some implementation of some interface.
 * Objects implementing Manager should also implement the common interface of the implementations that it represent,
 * and mimic the behaviour of these.
 * Additionally, it is advised to make the initial implementation be the first defined enum, although this is not enforced.
 *
 * Note that the implementation should not extend an abstract class, as overridden methods are ignored in this manner.
 *
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public interface Manager<E extends Enum> {

    /**
     * enum representation of the supported implementations
     * @return all accepted values for the switch method
     */
    E[] implementations();

    /**
     * should select implementations()[n]
     * @param n the ordinal of the required enum.
     * @throws IllegalArgumentException if there are less than n implementations
     */
    default void switchTo(int n){
        switchTo(implementations()[n]);
    }

    /**
     * @return the number of implementations available
     */
    default int nrOfImplementations(){
        return implementations().length;
    }

     /**
      * change the behaviour of this manager to that of the implementation represented by E.
      */
    void switchTo(E implementation);

    /**
     * string representations of the enum constants
     * @return a list of the names representing the accepted implementations
     */
    default String[] names(){
        return Arrays.stream(implementations()).map(Object::toString).toArray(String[]::new);
    }
}
