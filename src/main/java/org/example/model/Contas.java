package org.example.model;

public class Contas {
    private Integer id;
    
    // Novos campos baseados no seu SQL
    private String mesReferencia;
    private String contaContabil;
    private String percentual2024;
    private double valorReajuste2024;
    private String unidadeGd; // unidade_gd
    private String cnpjFilial;
    private String fornecedor;
    private String conta; // Campo 'conta'
    private String servicoProduto; // servico_produto
    private double valorNF;
    private double valorBoleto;
    private String vencimento;
    private String centroCusto;
    private String dataFaturamento;
    private String dataLancamento; // data_lancamento
    private boolean lancada;
    private boolean vencida;
    private int atividade;
    private String PN;

    public Contas() {}

    // Getters e Setters (Essenciais para o funcionamento)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(String mesReferencia) { this.mesReferencia = mesReferencia; }

    public String getContaContabil() { return contaContabil; }
    public void setContaContabil(String contaContabil) { this.contaContabil = contaContabil; }

    public String getPercentual2024() { return percentual2024; }
    public void setPercentual2024(String percentual2024) { this.percentual2024 = percentual2024; }

    public double getValorReajuste2024() { return valorReajuste2024; }
    public void setValorReajuste2024(double valorReajuste2024) { this.valorReajuste2024 = valorReajuste2024; }

    public String getUnidadeGd() { return unidadeGd; }
    public void setUnidadeGd(String unidadeGd) { this.unidadeGd = unidadeGd; }

    public String getCnpjFilial() { return cnpjFilial; }
    public void setCnpjFilial(String cnpjFilial) { this.cnpjFilial = cnpjFilial; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public String getConta() { return conta; }
    public void setConta(String conta) { this.conta = conta; }

    public String getServicoProduto() { return servicoProduto; }
    public void setServicoProduto(String servicoProduto) { this.servicoProduto = servicoProduto; }

    public double getValorNF() { return valorNF; }
    public void setValorNF(double valorNF) { this.valorNF = valorNF; }

    public double getValorBoleto() { return valorBoleto; }
    public void setValorBoleto(double valorBoleto) { this.valorBoleto = valorBoleto; }

    public String getVencimento() { return vencimento; }
    public void setVencimento(String vencimento) { this.vencimento = vencimento; }

    public String getCentroCusto() { return centroCusto; }
    public void setCentroCusto(String centroCusto) { this.centroCusto = centroCusto; }

    public String getDataFaturamento() { return dataFaturamento; }
    public void setDataFaturamento(String dataFaturamento) { this.dataFaturamento = dataFaturamento; }

    public String getDataLancamento() { return dataLancamento; }
    public void setDataLancamento(String dataLancamento) { this.dataLancamento = dataLancamento; }

    public boolean isLancada() { return lancada; }
    public void setLancada(boolean lancada) { this.lancada = lancada; }

    public boolean isVencida() { return vencida; }
    public void setVencida(boolean vencida) { this.vencida = vencida; }

    public int getAtividade() { return atividade; }
    public void setAtividade(int atividade) { this.atividade = atividade; }

    public String getPN() { return PN; }
    public void setPN(String PN) { this.PN = PN; }
}