package my.company;

import tools.dynamia.commons.Alias;

public class Producto {
    @Alias("name")
    private String nombre;
    @Alias("price")
    private double precio;
    @Alias("sku")
    private String codigo;

    public Producto() {
    }

    public Producto(String nombre, double precio, String codigo) {
        this.nombre = nombre;
        this.precio = precio;
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}

