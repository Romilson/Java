package com.cit.logistica.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma rota economica.
 * 
 * @author Romilson
 * 
 */
public class RotaEconomicaDto extends BaseDto<Integer> {

	private static final long serialVersionUID = 1L;

	private List<TrechoMapaDto> trechos = new ArrayList<>();
	
	private List<String> resumoTrechos = new ArrayList<>();
	
	private Double custoTotal;

	public List<TrechoMapaDto> getTrechos() {
		return trechos;
	}

	public void setTrechos(List<TrechoMapaDto> trechos) {
		this.trechos = trechos;
	}

	public void addTrecho(TrechoMapaDto dto) {
		this.trechos.add(dto);
	}
	
	public void addResumoTrecho(String ponto) {
		this.resumoTrechos.add(ponto);
	}

	public Double getCustoTotal() {
		return custoTotal;
	}

	public void setCustoTotal(Double custoTotal) {
		this.custoTotal = custoTotal;
	}
	
	public List<String> getResumoTrechos() {
		return resumoTrechos;
	}
	
	public void setResumoTrechos(List<String> resumoTrechos) {
		this.resumoTrechos = resumoTrechos;
	}
}

