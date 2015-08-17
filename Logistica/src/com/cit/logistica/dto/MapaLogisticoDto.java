package com.cit.logistica.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a malha logística. Guarda informações de todos os trechos que
 * compõe um mapa de malha logística.
 * 
 * @author Romilson
 * 
 */
public class MapaLogisticoDto extends BaseDto<Integer> {

	private static final long serialVersionUID = 1L;
		
	private String nome;
	
	private List<TrechoMapaDto> trechos = new ArrayList<>();
		
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public void addTrecho(TrechoMapaDto trecho) {
		trechos.add(trecho);
	}
	
	public List<TrechoMapaDto> getTrechos() {
		return trechos;
	}
	
	public void setTrechos(List<TrechoMapaDto> trechos) {
		this.trechos = trechos;
	}
	
	public TrechoMapaDto getTrechoById(Integer id) {
		for (TrechoMapaDto dto : this.getTrechos()) {
			if (id.equals(dto.getId())) {
				return dto;
			}
		}
		return null;
	}
}
