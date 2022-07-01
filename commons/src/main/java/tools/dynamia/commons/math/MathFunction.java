package tools.dynamia.commons.math;

/**
 * Basic math function parse and evaluator
 */
public class MathFunction {

    public static double evaluate(String expression) {
        return new MathFunction(expression).getF_xo(0);
    }

    /**
     * setup.
     */
    private boolean degree = true;

    /**
     * f(x).
     */
    private String f_x;

    /**
     * FunctionX.
     *
     * @param f_x f(x)
     */
    public MathFunction(final String f_x) {
        this.f_x = f_x.trim().replaceAll(" ", "").toLowerCase();
    }

    public MathFunction(final String f_x, boolean degree) {
        this.f_x = f_x.trim().replaceAll(" ", "").toLowerCase();
        this.degree = degree;
    }

    /**
     * getter f(x).
     *
     * @return the f x
     */
    public String getF_x() {
        return f_x;
    }

    /**
     * setter f(x).
     *
     * @param f_x the new f x
     */
    public void setF_x(final String f_x) {
        this.f_x = f_x;
    }

    /**
     * get f(x0).
     *
     * @param xo point
     * @return the f xo
     * @throws MathCalculationException the calculator exception
     */
    public double getF_xo(final double xo) throws MathCalculationException {
        return eval(f_x, xo);
    }

    /**
     * eval.
     *
     * @param f_x the f x
     * @param xi  the xi
     * @return the double
     * @throws MathCalculationException the calculator exception
     */
    private double eval(final String f_x, final double xi) throws MathCalculationException {
        double value = 0;
        String number = "";
        String function = "";
        boolean hasNumber = false;
        boolean hasFunction = false;

        for (int i = 0; i < f_x.length(); i++) {
            final char character = f_x.charAt(i);

            if (character >= '0' && character <= '9') {

                hasNumber = true;
                number += character;
                if (i == (f_x.length() - 1)) {
                    value = Double.parseDouble(number);
                    number = "";
                    hasNumber = false;
                }

            } else if (character == '+') {

                if (hasNumber) {
                    final Double numb = Double.valueOf(number);
                    final String new_f_x = f_x.substring(i + 1, f_x.length());
                    value = numb + eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasNumber = false;
                    number = "";
                } else if (hasFunction) {
                    final String new_f_x = f_x.substring(i + 1, f_x.length());
                    value = eval(function, xi) + eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasFunction = false;
                    function = "";
                } else {
                    final String new_f_x = f_x.substring(i + 1, f_x.length());
                    value = value + eval(new_f_x, xi);
                    i += new_f_x.length();
                }

            } else if (character == '*') {

                if (hasNumber) {
                    final Double numb = Double.valueOf(number);
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = numb * eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasNumber = false;
                    number = "";
                } else if (hasFunction) {
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = eval(function, xi) * eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasFunction = false;
                    function = "";
                } else {
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = value * eval(new_f_x, xi);
                    i += new_f_x.length();
                }

            } else if (character == '-') {

                if (hasNumber) {
                    final Double numb = Double.valueOf(number);
                    final String new_f_x = nextMinusFunction(f_x.substring(i + 1, f_x.length()));
                    value = numb - eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasNumber = false;
                    number = "";
                } else if (hasFunction) {
                    final String new_f_x = nextMinusFunction(f_x.substring(i + 1, f_x.length()));
                    value = eval(function, xi) - eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasFunction = false;
                    function = "";
                } else {
                    final String new_f_x = nextMinusFunction(f_x.substring(i + 1, f_x.length()));
                    value = value - eval(new_f_x, xi);
                    i += new_f_x.length();
                }

            } else if (character == '/') {

                if (hasNumber) {
                    final Double numb = Double.valueOf(number);
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = numb / eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasNumber = false;
                    number = "";
                } else if (hasFunction) {
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = eval(function, xi) / eval(new_f_x, xi);
                    i += new_f_x.length();
                    hasFunction = false;
                    function = "";
                } else {
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = value / eval(new_f_x, xi);
                    i += new_f_x.length();
                }

            } else if (character == '^') {

                if (hasNumber) {
                    final Double numb = Double.valueOf(number);
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = StrictMath.pow(numb.doubleValue(), eval(new_f_x, xi));
                    i += new_f_x.length();
                    hasNumber = false;
                    number = "";
                } else if (hasFunction) {
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = StrictMath.pow(eval(function, xi), eval(new_f_x, xi));
                    i += new_f_x.length();
                    hasFunction = false;
                    function = "";
                } else {
                    final String new_f_x = nextFunction(f_x.substring(i + 1, f_x.length()));
                    value = StrictMath.pow(value, eval(new_f_x, xi));
                    i += new_f_x.length();
                }

            } else if (character == '.') {

                if (i == (f_x.length() - 1)) {
                    throw new MathCalculationException("The function is not well-formed");
                }
                if (hasNumber && (number.length() > 0)) {
                    number += character;
                }

            } else if (character == '(') {
                if (i == (f_x.length() - 1)) {
                    throw new MathCalculationException("The function is not well-formed");
                }

                final String new_f_x = f_x.substring(i + 1, nextBracket(f_x));
                if (hasFunction) {
                    if (Math.SIN.equals(function)) {

                        if (degree) {
                            value = StrictMath.sin(StrictMath.toRadians(eval(new_f_x, xi)));
                        } else {
                            value = StrictMath.sin(eval(new_f_x, xi));
                        }

                    } else if (Math.COS.equals(function)) {

                        if (degree) {
                            value = StrictMath.cos(StrictMath.toRadians(eval(new_f_x, xi)));
                        } else {
                            value = StrictMath.cos(eval(new_f_x, xi));
                        }

                    } else if (Math.TAN.equals(function)) {

                        if (degree) {
                            value = StrictMath.tan(StrictMath.toRadians(eval(new_f_x, xi)));
                        } else {
                            value = StrictMath.tan(eval(new_f_x, xi));
                        }

                    } else if (Math.SINH.equals(function)) {
                        value = StrictMath.sinh(eval(new_f_x, xi));

                    } else if (Math.COSH.equals(function)) {
                        value = StrictMath.cosh(eval(new_f_x, xi));

                    } else if (Math.TANH.equals(function)) {
                        value = StrictMath.tanh(eval(new_f_x, xi));

                    } else if (Math.ASIN.equals(function)) {
                        if (degree) {
                            value = StrictMath.asin(eval(new_f_x, xi)) * (180 / StrictMath.PI);
                        } else {
                            value = StrictMath.asin(eval(new_f_x, xi));
                        }
                    } else if (Math.ACOS.equals(function)) {
                        if (degree) {
                            value = StrictMath.acos(eval(new_f_x, xi)) * (180 / StrictMath.PI);
                        } else {
                            value = StrictMath.acos(eval(new_f_x, xi));
                        }
                    } else if (Math.ATAN.equals(function)) {
                        if (degree) {
                            value = StrictMath.atan(eval(new_f_x, xi)) * (180 / StrictMath.PI);
                        } else {
                            value = StrictMath.atan(eval(new_f_x, xi));
                        }
                    } else if (Math.LN.equals(function)) {
                        value = StrictMath.log(eval(new_f_x, xi));
                    } else if (Math.LOG.equals(function)) {
                        value = StrictMath.log10(eval(new_f_x, xi));
                    } else if (Math.SQRT.equals(function)) {
                        value = StrictMath.sqrt(eval(new_f_x, xi));
                    } else if (Math.CBRT.equals(function)) {
                        value = StrictMath.cbrt(eval(new_f_x, xi));
                    } else {
                        throw new MathCalculationException("The function is not well-formed");
                    }

                    hasFunction = false;
                    function = "";

                } else {
                    value = eval(new_f_x, xi);
                }
                i += new_f_x.length() + 1;

            } else if (isValidCharacter(character)) {
                function = function + character;
                hasFunction = true;

                if (i == (f_x.length() - 1)) {
                    if (Math.E.equals(function)) {
                        value = StrictMath.E;
                    } else if (Math.PI.equals(function)) {
                        value = StrictMath.PI;
                    } else if (function.length() == 1) {
                        value = xi;
                    } else {
                        throw new MathCalculationException("function is not well defined");
                    }
                }

            } else if (character == ')') {
                throw new MathCalculationException(" '(' is not finished ");

            } else if (character == ' ') {

            } else {
                throw new MathCalculationException("Invalid character:" + character);
            }
        }
        return value;
    }

    /**
     * Next function.
     *
     * @param f_x the f x
     * @return the string
     * @throws MathCalculationException the calculator exception
     */
    private String nextFunction(final String f_x) throws MathCalculationException {
        String result = "";

        for (int i = 0; i < f_x.length(); i++) {
            final char character = f_x.charAt(i);

            if (isValidNumericAndCharacter(character)) {
                result += character;
            } else if (character == '+' || character == '*' || character == '-' || character == '/') {
                i = f_x.length();
            } else if (character == '.' || character == '^') {
                result += character;
            } else if (character == '(') {
                final String new_f_x = f_x.substring(i, nextBracket(f_x) + 1);
                result += new_f_x;
                i = (i + new_f_x.length()) - 1;
            } else if (character == ')') {
                throw new MathCalculationException(" '(' is not finished ");
            } else if (character == ' ') {
                result += character;
            } else {
                throw new MathCalculationException("Invalid character:" + character);
            }
        }
        return result;
    }

    /**
     * Next minus function.
     *
     * @param f_x the f x
     * @return the string
     * @throws MathCalculationException the calculator exception
     */
    private String nextMinusFunction(final String f_x) throws MathCalculationException {
        String result = "";
        for (int i = 0; i < f_x.length(); i++) {
            final char character = f_x.charAt(i);

            if (isValidNumericAndCharacter(character)) {
                result += character;
            } else if (character == '+' || character == '-') {
                i = f_x.length();
            } else if (character == '*' || character == '/' || character == '.' || character == '^') {
                result += character;
            } else if (character == '(') {
                final String new_f_x = f_x.substring(i, nextBracket(f_x) + 1);
                result += new_f_x;
                i = (i + new_f_x.length()) - 1;
            } else if (character == ')') {
                throw new MathCalculationException(" '(' is not finished ");
            } else if (character == ' ') {
                result += character;
            } else {
                throw new MathCalculationException("Invalid character:" + character);
            }
        }
        return result;
    }

    /**
     * isValidCharacter.
     *
     * @param character the character
     * @return true, if is valid character
     */
    private boolean isValidCharacter(final char character) {
        boolean result = false;
        if ((character >= 'a' && character <= 'z')) {
            result = true;
        }
        return result;
    }

    /**
     * isValidNumericAndCharacter.
     *
     * @param character the character
     * @return true, if is valid numeric and character
     */
    private boolean isValidNumericAndCharacter(final char character) {
        boolean result = false;
        if ((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9')) {
            result = true;
        }
        return result;
    }

    /**
     * nextBracket.
     *
     * @param f_x f(x)
     * @return the int
     * @throws MathCalculationException the calculator exception
     */
    private int nextBracket(final String f_x) throws MathCalculationException {
        int result = 0;
        int count = 0;
        for (int i = 0; i < f_x.length(); i++) {
            final char character = f_x.charAt(i);
            if (character == '(') {
                result = i;
                count++;
            } else if (character == ')') {
                result = i;
                count--;
                if (count == 0) {
                    return i;
                }
            } else {
                result = i;
            }
        }

        if (count != 0) {
            throw new MathCalculationException("( is not finished");
        }
        return result;
    }

}