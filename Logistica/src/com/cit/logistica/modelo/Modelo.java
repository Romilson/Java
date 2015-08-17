package com.cit.logistica.modelo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.cit.logistica.dto.MapaLogisticoDto;

/**
 * Representa didaticamente o modelo de dados da aplicação.
 * 
 * @author Romilson
 * 
 */
public class Modelo {

	private BigDecimal versao;
	
	private Map<Integer, MapaLogisticoDto> mapas = new HashMap<Integer, MapaLogisticoDto>();

	public Map<Integer, MapaLogisticoDto> getMapas() {
		return mapas;
	}

	public void setMapas(Map<Integer, MapaLogisticoDto> mapas) {
		this.mapas = mapas;
	}
	
	public BigDecimal getVersao() {
		return versao;
	}
	
	public void setVersao(BigDecimal versao) {
		this.versao = versao;
	}	

}
