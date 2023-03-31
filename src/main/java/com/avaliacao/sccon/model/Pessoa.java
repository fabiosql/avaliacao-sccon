package com.avaliacao.sccon.model;

public class Pessoa implements Comparable<Pessoa> {
    public Integer id;
    public String nome;
    public String dataNascimento;
    public String dataAdmissao;
    public double salario;

    public Pessoa() {

    }

    public Pessoa(Integer id, String nome, String dataNascimento, String dataAdmissao, double salario) {
        this.id = id;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.dataAdmissao = dataAdmissao;
        this.salario = salario;
    }

    @Override
    public int compareTo(Pessoa outra) {
        return this.nome.compareTo(outra.nome);
    }

}
