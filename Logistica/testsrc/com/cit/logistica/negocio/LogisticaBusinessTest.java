package com.cit.logistica.negocio;


import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cit.logistica.LogisticaException;
import com.cit.logistica.dto.MapaLogisticoDto;
import com.cit.logistica.dto.RotaEconomicaDto;
import com.cit.logistica.dto.TrechoMapaDto;

/**
 * Classe de teste para a classe negocial Logística Business.
 * 
 * @author Romilson
 */
public class LogisticaBusinessTest {
	
	private LogisticaBusiness logisticaBusiness;
	
	@Before
    public void initialize() throws LogisticaException {
		System.setProperty("catalina.base", System.getProperty("java.io.tmpdir"));    
		this.logisticaBusiness = new LogisticaBusiness();
		this.logisticaBusiness.removerMapa("_MAPA_TESTE_01_");
		this.logisticaBusiness.removerMapa("_MAPA_TESTE_02_");
	}
	
	@Test
	public void armazenarMapa() {
		try {
			logisticaBusiness.armazenarMapa(this.buildMapa("_MAPA_TESTE_01_"));
		} catch (LogisticaException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void obterRotaMaisEconomica() {
		try {
			logisticaBusiness.armazenarMapa(this.buildMapa("_MAPA_TESTE_02_"));
			
			RotaEconomicaDto dto = logisticaBusiness.obterRotaMaisEconomica("Belo Horizonte", "Salvador", 13.5, 3.20);
			
			Assert.assertEquals(3, dto.getResumoTrechos().size());
			Assert.assertEquals("Belo Horizonte", dto.getResumoTrechos().get(0));
			Assert.assertEquals("Montes Claros", dto.getResumoTrechos().get(1));
			Assert.assertEquals("Salvador", dto.getResumoTrechos().get(2));
			Assert.assertEquals(0, toBigDecimalRound(dto.getCustoTotal()).compareTo(toBigDecimalRound(341.33)));

		} catch (LogisticaException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * @return O valor BigDecimal arredondado com duas casas decimais.
	 */
	private BigDecimal toBigDecimalRound(Double valor) {
		return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
	}
	
	private MapaLogisticoDto buildMapa(String nome) {
		MapaLogisticoDto mapa = new MapaLogisticoDto();
		mapa.setNome(nome);
		mapa.addTrecho(new TrechoMapaDto(1L, "Belo Horizonte", "Campinas", 605.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Belo Horizonte", "Brasilia", 735.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Belo Horizonte", "Rio de Janeiro", 441.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Belo Horizonte", "Vila Velha", 522.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Belo Horizonte", "Montes Claros", 426.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Rio de Janeiro", "Porto Seguro", 1105.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Rio de Janeiro", "Montes Claros", 856.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Rio de Janeiro", "Salvador", 1631.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Montes Claros", "Salvador", 1014.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Vila Velha", "Porto Seguro", 588.0));
		mapa.addTrecho(new TrechoMapaDto(1L, "Vila Velha", "Salvador", 1052.0));
		return mapa;
	}
	
}
