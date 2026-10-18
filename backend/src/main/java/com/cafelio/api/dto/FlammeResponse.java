package com.cafelio.api.dto;

import java.time.LocalDate;
import java.util.List;

public class FlammeResponse {

    private final int sequenciaAtual;
    private final int maiorSequencia;
    private final int totalDeDias;
    private final List<LocalDate> dias;

    public FlammeResponse(int sequenciaAtual, int maiorSequencia, int totalDeDias, List<LocalDate> dias) {
        this.sequenciaAtual = sequenciaAtual;
        this.maiorSequencia = maiorSequencia;
        this.totalDeDias = totalDeDias;
        this.dias = dias;
    }

    public int getSequenciaAtual() {
        return sequenciaAtual;
    }

    public int getMaiorSequencia() {
        return maiorSequencia;
    }

    public int getTotalDeDias() {
        return totalDeDias;
    }

    public List<LocalDate> getDias() {
        return dias;
    }
}