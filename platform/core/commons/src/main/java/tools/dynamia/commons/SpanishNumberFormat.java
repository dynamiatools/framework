/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.commons;


/**
 * <p>
 * SpanishNumberFormat is a utility class for converting numeric values into their textual representation in Spanish.
 * It supports conversion of integers and long values, handling millions, billions, and trillions, and provides correct grammar for Spanish number formatting.
 * </p>
 *
 * <p>
 * This class is useful for applications that require displaying numbers in Spanish words, such as financial, educational, or reporting systems.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     String texto = SpanishNumberFormat.convert(1234567);
 *     // Output: "un millon doscientos treinta y cuatro mil quinientos sesenta y siete"
 * </pre>
 * </p>
 *
 * <p>
 * This class is not instantiable and only provides static methods.
 * </p>
 *
 * @author Mario Serrano Leones
 * @since 2023
 */
public class SpanishNumberFormat {

    /**
     * Array of group names for large numbers (million, billion, trillion, etc.).
     * Used to format numbers in groups of six digits.
     */
    private static final String[] _grupos = {"", "millon", "billon", "trillon"};

    /**
     * Array of unit names (0-9) in Spanish.
     */
    private static final String[] _unidades = {"", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"};

    /**
     * Array of special names for numbers 11-19 in Spanish.
     */
    private static final String[] _decena1 = {"", "once", "doce", "trece", "catorce", "quince",
        "dieciseis", "diecisiete", "dieciocho", "diecinueve"
    };

    /**
     * Array of tens names (10, 20, 30, ...) in Spanish.
     */
    private static final String[] _decenas = {"", "diez", "veinte", "treinta", "cuarenta", "cincuenta",
        "sesenta", "setenta", "ochenta", "noventa"
    };

    /**
     * Array of hundreds names (100, 200, ...) in Spanish.
     */
    private static final String[] _centenas = {"", "cien", "doscientos", "trescientos", "cuatrocientos",
        "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    /**
     * Converts a number from 0 to 999 into its Spanish textual representation.
     * Handles special cases for hundreds, tens, and units, including grammar rules for Spanish.
     *
     * @param n the number to convert (0-999)
     * @return the Spanish text representation of the number, or empty string if n is 0
     */
    public static String millarATexto(int n) {
        if (n == 0) {
            return "";
        }
        int centenas = n / 100;
        n = n % 100;
        int decenas = n / 10;
        int unidades = n % 10;

        String sufijo = "";

        if (decenas == 0 && unidades != 0) {
            sufijo = _unidades[unidades];
        }
        if (decenas == 1 && unidades != 0) {
            sufijo = _decena1[unidades];
        }
        if (decenas == 2 && unidades != 0) {
            sufijo = "veinti" + _unidades[unidades];
        }
        if (unidades == 0) {
            sufijo = _decenas[decenas];
        }
        if (decenas > 2 && unidades != 0) {
            sufijo = _decenas[decenas] + " y " + _unidades[unidades];
        }
        if (centenas != 1) {
            return _centenas[centenas] + " " + sufijo;
        }
        if (unidades == 0 && decenas == 0) {
            return "cien";
        }

        return "ciento " + sufijo;
    }

    /**
     * Converts a long number into its Spanish textual representation, handling millions, billions, and trillions.
     * The conversion is performed in groups of six digits, applying the correct group name and grammar.
     *
     * @param n the number to convert
     * @return the Spanish text representation of the number
     */
    public static String convert(long n) {
        StringBuilder resultado = new StringBuilder();
        int grupo = 0;
        while (n != 0 && grupo < _grupos.length) {
            long fragmento = n % 1000000;
            int millarAlto = (int) (fragmento / 1000);
            int millarBajo = (int) (fragmento % 1000);
            n = n / 1000000;

            String nombreGrupo = _grupos[grupo];
            if (fragmento > 1 && grupo > 0) {
                nombreGrupo += "es";
            }
            if ((millarAlto != 0) || (millarBajo != 0)) {
                if (millarAlto > 1) {
                    resultado.insert(0, millarATexto(millarAlto) + " mil "
                            + millarATexto(millarBajo) + " "
                            + nombreGrupo + " ");
                }
                if (millarAlto == 0) {
                    resultado.insert(0, millarATexto(millarBajo) + " "
                            + nombreGrupo + " ");
                }
                if (millarAlto == 1) {
                    resultado.insert(0, "mil " + millarATexto(millarBajo) + " "
                            + nombreGrupo + " ");
                }
            }
            grupo++;
        }
        return resultado.toString();
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SpanishNumberFormat() {
    }
}
