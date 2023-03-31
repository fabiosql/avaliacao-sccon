package com.avaliacao.sccon.controllers;

import com.avaliacao.sccon.model.Pessoa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/person")
public class PessoaController {

    private HashMap<Integer, Pessoa> pessoas = new HashMap<Integer, Pessoa>();
    private String dataAtual = "2023-02-07";

    public PessoaController() {
        this.pessoas.put(1, new Pessoa(1, "José da Silva", "2000-04-06", "2020-05-10", 1558.00));
        this.pessoas.put(3, new Pessoa(3, "Carlos Santos", "2003-10-01", "2022-01-10", 9000.00));
        this.pessoas.put(2, new Pessoa(2, "Gabriela Silva", "2006-05-01", "2023-03-11", 7000.00));
    }

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<ArrayList<Pessoa>> findAll() {
        try {

            var pessoasList = new ArrayList<Pessoa>();
            for (var pessoa : pessoas.values()) {
                pessoasList.add(pessoa);
            }

            Collections.sort(pessoasList);

            return new ResponseEntity<>(pessoasList, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private Integer getUltimoId(ArrayList<Integer> ids) {
        var max = ids.get(0);
        for (var id : ids) {
            if (id > max) max = id;
        }
        return max;
    }

    @PostMapping(value = "", produces = {"application/json"})
    public ResponseEntity<Pessoa> create(@RequestBody Pessoa pessoa) {
        if (pessoas.containsKey(pessoa.id)) throw new ResponseStatusException(HttpStatus.CONFLICT);

        var ids = new ArrayList<Integer>();
        for (Integer id : pessoas.keySet()) ids.add(id);

        if (pessoa.id == null) pessoa.id = this.getUltimoId(ids) + 1;
        return new ResponseEntity<>(pessoa, HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<HashMap<Integer, Pessoa>> delete(@PathVariable Integer id) {
        if (!pessoas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        pessoas.remove(id);
        return new ResponseEntity<>(pessoas, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<HashMap<Integer, Pessoa>> update(@PathVariable Integer id, @RequestBody Pessoa pessoa) {
        if (!pessoas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var pessoaData = pessoas.get(id);
        pessoaData.nome = pessoa.nome;
        pessoaData.dataNascimento = pessoa.dataNascimento;
        pessoaData.dataAdmissao = pessoa.dataAdmissao;
        return new ResponseEntity<>(pessoas, HttpStatus.OK);
    }

    @PatchMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<HashMap<Integer, Pessoa>> updateNome(@PathVariable Integer id, @RequestParam String nome) {
        if (!pessoas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        pessoas.get(id).nome = nome;
        return new ResponseEntity<>(pessoas, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Pessoa> getById(@PathVariable Integer id) {
        if (!pessoas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(pessoas.get(id), HttpStatus.OK);
    }

    private LocalDate dateStringToLocalDate(String date) {
        var datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, datePattern);
    }

    private Period periodoEntreDatas(String d1, String d2) {
        return Period.between(this.dateStringToLocalDate(d1), this.dateStringToLocalDate(d2));
    }

    @GetMapping(value = "/{id}/age", produces = {"application/json"})
    public ResponseEntity<Integer> getAgeById(@PathVariable Integer id, @RequestParam String output) {
        if (!pessoas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var dataNasc = pessoas.get(id).dataNascimento;
        var periodo = this.periodoEntreDatas(dataNasc, this.dataAtual);

        switch (output) {
            case "days":
                var result = (int) ChronoUnit.DAYS.between(this.dateStringToLocalDate(dataNasc), this.dateStringToLocalDate(this.dataAtual));
                return new ResponseEntity<>(result, HttpStatus.OK);
            case "months":
                return new ResponseEntity<>((periodo.getYears() * 12) + periodo.getMonths(), HttpStatus.OK);
            case "years":
                return new ResponseEntity<>(periodo.getYears(), HttpStatus.OK);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    private String toBrl(double valor) {

        Locale ptBr = new Locale("pt", "BR");
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(ptBr);
        formatter.applyPattern("#,##0.00");

        return formatter.format(valor);
    }

    @GetMapping(value = "/{id}/salary")
    public ResponseEntity<String> getSalary(@PathVariable Integer id, @RequestParam String output) {
        if (!pessoas.containsKey(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var salarioMinimoAtual = 1302.00;
        var pessoa = pessoas.get(id);
        var salario = this.salario(pessoa.salario, this.getQtdeAnosEmpresa(id));

        if (output.equals("min")) {
            var result = (salario / salarioMinimoAtual);
            var arredondado = new BigDecimal(result).setScale(2, RoundingMode.CEILING);
            return new ResponseEntity<>(toBrl(arredondado.doubleValue()), HttpStatus.OK);
        } else if (output.equals("full")) {
            var result = this.toBrl(salario);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    private double salario(double salarioBase, int qtdeAnosEmpresa) {
        var salario = salarioBase;
        for (int i = 0; i < qtdeAnosEmpresa; i++) {
            var incrementoAnual = ((0.18 * salario) + 500);
            salario += incrementoAnual;
        }
        return salario;
    }

    private Integer getQtdeAnosEmpresa(Integer id) {
        var pessoa = pessoas.get(id);
        var d1 = pessoa.dataAdmissao;
        var d2 = dataAtual;
        return this.periodoEntreDatas(d1, d2).getYears();
    }

}
