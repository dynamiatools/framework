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
 * The Class SpanishNumberFormat.
 *
 * @author Mario Serrano Leones
 */
public class SpanishNumberFormat {

    /**
     * The _grupos.
     */
    private static final String[] _grupos = {"", "millon", "billon", "trillon"};

    /**
     * The _unidades.
     */
    private static final String[] _unidades = {"", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"};

    /**
     * The _decena1.
     */
    private static final String[] _decena1 = {"", "once", "doce", "trece", "catorce", "quince",
        "dieciseis", "diecisiete", "dieciocho", "diecinueve"
    };

    /**
     * The _decenas.
     */
    private static final String[] _decenas = {"", "diez", "veinte", "treinta", "cuarenta", "cincuenta",
        "sesenta", "setenta", "ochenta", "noventa"
    };

    /**
     * The _centenas.
     */
    private static final String[] _centenas = {"", "cien", "doscientos", "trescientos", "cuatrocientos",
        "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    /**
     * Millar a texto.
     *
     * @param n the n
     * @return the string
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
     * Convert.
     *
     * @param n the n
     * @return the string
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

    private SpanishNumberFormat() {
    }
}
