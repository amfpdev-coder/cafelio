package com.cafelio.api.dto;

public class AllumezResponse {

    private final int ano;
    private final int meta;
    private final int concluidos;

    public AllumezResponse(int ano, int meta, int concluidos) {
        this.ano = ano;
        this.meta = meta;
        this.concluidos = concluidos;
    }

    public int getAno() {
        return ano;
    }

    public int getMeta() {
        return meta;
    }

    public int getConcluidos() {
        return concluidos;
    }
}
