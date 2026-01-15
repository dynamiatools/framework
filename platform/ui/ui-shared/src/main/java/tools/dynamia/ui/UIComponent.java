package tools.dynamia.ui;

import java.io.Serializable;

/**
 * Basic framework agnostic UI Component interface
 */
public interface UIComponent extends Serializable {


    /**
     * Set vertical flex
     *
     * @param vflex the vflex
     */
    void setVflex(String vflex);

    /**
     * Get vertical flex
     *
     * @return the string
     */
    String getVflex();

    /**
     * Set horizontal flex
     *
     * @param hflex the hflex
     */
    void setHflex(String hflex);

    /**
     * Get horizontal flex
     *
     * @return the string
     */
    String getHflex();

    /**
     * Set width
     *
     * @param width the width
     */
    void setWidth(String width);

    /**
     * Get width
     *
     * @return the string
     */
    String getWidth();

    /**
     * Set height
     *
     * @param height the height
     * @param height
     */
    void setHeight(String height);

    /**
     * Get height
     *
     * @return the string
     */
    String getHeight();

    /**
     * Add child component
     *
     * @param child the child
     */
   default void add(UIComponent child){
       //do nothing by default
   }
}
