package br.com.technexus.model;

public class Produto {
    private final String nome;
    private final String categoria;
    private final double preco;

    public Produto(String nome, String categoria, double preco) {
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
    }

    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public double getPreco() { return preco; }

    @Override
    public String toString() {
        return String.format(java.util.Locale.forLanguageTag("pt-BR"),
                "%s | %s | R$ %.2f", nome, categoria, preco);
    }
}
