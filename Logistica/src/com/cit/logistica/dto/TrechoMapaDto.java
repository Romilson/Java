package com.cit.logistica.dto;

/**
 * Representa um trecho de mapa. Exemplo: A-B
 * 
 * @author Romilson
 * 
 */
public class TrechoMapaDto extends BaseDto<Integer> {

	private static final long serialVersionUID = 1L;

	private String pontoOrigem;
	private String pontoDestino;
	private Double distanciaKm;

	public TrechoMapaDto(Long id, String pontoOrigem, String pontoDestino,
			Double distanciaKm) {
		this.pontoOrigem = pontoOrigem;
		this.pontoDestino = pontoDestino;
		this.distanciaKm = distanciaKm;
	}

	public String getPontoOrigem() {
		return pontoOrigem;
	}

	public void setPontoOrigem(String pontoOrigem) {
		this.pontoOrigem = pontoOrigem;
	}

	public String getPontoDestino() {
		return pontoDestino;
	}

	public void setPontoDestino(String pontoDestino) {
		this.pontoDestino = pontoDestino;
	}

	public Double getDistanciaKm() {
		return distanciaKm;
	}

	public void setDistanciaKm(Double distanciaKm) {
		this.distanciaKm = distanciaKm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((pontoDestino == null) ? 0 : pontoDestino.hashCode());
		result = prime * result
				+ ((pontoOrigem == null) ? 0 : pontoOrigem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrechoMapaDto other = (TrechoMapaDto) obj;
		if (pontoDestino == null) {
			if (other.pontoDestino != null)
				return false;
		} else if (!pontoDestino.equals(other.pontoDestino))
			return false;
		if (pontoOrigem == null) {
			if (other.pontoOrigem != null)
				return false;
		} else if (!pontoOrigem.equals(other.pontoOrigem))
			return false;
		return true;
	}
	
}
