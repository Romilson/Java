package com.cit.logistica.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;

import com.cit.logistica.dto.MapaLogisticoDto;
import com.cit.logistica.dto.RotaEconomicaDto;
import com.cit.logistica.dto.TrechoMapaDto;

/**
 * Representa o algoritmo gen�tico que cuidar� da avalia��o das rotas.
 * Respons�vel por configurar os par�metros iniciais e ent�o iniciar a execu��o
 * da engine gen�tica.
 * 
 * @author Romilson
 * 
 */
public class GAReducaoCustoLogistica {

	/**
	 * Configuracao do algoritmo.
	 */
	private Configuration cfg;

	/**
	 * Funcao fitness configurada ap�s a inicializa��o.
	 */
	private GAFuncaoFitnessReducaoCustoLogistica funcao;

	/**
	 * Mapa utilizado pelo algoritmo.
	 */
	private MapaLogisticoDto mapa;

	/**
	 * Inicializa o algoritmo com o mapa informado.
	 * 
	 * @param mapa
	 *            Mapa que ser� processado.
	 */
	public GAReducaoCustoLogistica(MapaLogisticoDto mapa) {
		this.cfg = new DefaultConfiguration(mapa.getId().toString(),
				mapa.getNome());
		this.mapa = mapa;
	}

	/**
	 * Configura os par�metros iniciais para o c�lculo de custo das rotas. Gera
	 * uma popula��o de solu��es candidatas (rotas), que ser� base para a
	 * an�lise do custo das rotas.
	 * 
	 * O melhor custo ser� pesquisado com base nos par�metros deste m�todo, como
	 * por exemplo os pontos de origem e destino.
	 * 
	 * @param pontoOrigem
	 *            O ponto de origem da rota desejada.
	 * @param pontoDestino
	 *            O ponto de destino da rota desejada.
	 * @param autonomiaKmPorLitro
	 *            A autonomia em km por litro do ve�culo. Exemplo: 10 Km/L.
	 * @param valorLitroCombustivel
	 *            O custo do litro de combust�vel. Exemplo: R$3,10.
	 * @throws InvalidConfigurationException
	 *             Caso haja erros na configura��o do algoritmo gen�tico.
	 */
	public void inicializar(String pontoOrigem, String pontoDestino,
			Double autonomiaKmPorLitro, Double valorLitroCombustivel)
			throws InvalidConfigurationException {

		// Configura a fun��o objetivo a ser utilizada.
		this.funcao = new GAFuncaoFitnessReducaoCustoLogistica(mapa,
				pontoOrigem, pontoDestino, autonomiaKmPorLitro,
				valorLitroCombustivel);
		this.cfg.setFitnessFunction(funcao);

		Integer qtdTotalTrechos = this.mapa.getTrechos().size();

		// O cromossomo da solu��o � composto de N genes. Onde N � a quantidade
		// de trechos existentes no mapa.
		// O conte�do do gene (alelo) possui como valor o �ndice da liga��o
		// entre o ponto inicial e o ponto destino.
		// Exemplo:
		// O ponto de origem A possui duas liga��es para pontos destinos:
		// �ndice 0 -> para o ponto B (Trecho A-B)
		// �ndice 1 -> para o ponto C (Trecho A-C)
		// O gene guardaria como alelo, para este exemplo, o valor 0 ou 1.
		// Se o valor gerado para o gene � maior que o �ltimo �ndice, supondo 5
		// para o exemplo acima,
		// ent�o o �ndice a ser considerado � o valor resto de : 5 % [Qtd
		// �ndices].
		// Se o valor gerado para o gene � -1, indica que � final de rota, ou
		// seja, n�o h� mais trechos nos genes restantes do cromossomo.

		List<Gene> genes = new ArrayList<Gene>();

		for (int i = 0; i < qtdTotalTrechos; i++) {
			// Uma rota possui pelo menos 1 trecho. Ent�o o primeiro trecho
			// (gene) possui um m�nimo de 0. Os demais possuem um m�nimo de
			// -1, onde -1 indica que n�o h� trecho.
			Integer lowerBound = i == 0 ? 0 : -1;
			genes.add(new IntegerGene(this.cfg, lowerBound, this.funcao
					.getMaiorQuantidadeLigacoes() - 1));
		}

		Chromosome solucaoExemplo = new Chromosome(this.cfg,
				genes.toArray(new Gene[genes.size()]));

		// configura a solucao exemplo e o tamanho da popula��o a gerar
		this.cfg.setSampleChromosome(solucaoExemplo);
		this.cfg.setPopulationSize(qtdTotalTrechos * qtdTotalTrechos);
	}

	/**
	 * Executa as evolu��es em busca da melhor solu��o para o problema.
	 * 
	 * @param timeoutMs
	 *            Timeout de processamento em milisegundos.
	 * @param qtdMaxEvolucoes
	 *            Quantidade m�xima de evolu��es a realizar.
	 * @return Melhor solu��o encontrada.
	 * @throws InvalidConfigurationException
	 */
	public RotaEconomicaDto executar(int timeoutMs, int qtdMaxEvolucoes)
			throws InvalidConfigurationException {

		long tempoInicial = System.currentTimeMillis();
		boolean tempoEsgotado = false;

		// Gera a popula��o de solu��es candidatas (cromossomos) e evolui
		// quantas vezes puder, dentro do timeout. Tenta cinco vezes encontrar
		// uma
		// solu��o melhor que a anterior. Se n�o conseguir, considera a �ltima
		// encontrada como melhor
		Genotype population = Genotype.randomInitialGenotype(cfg);

		for (int i = 0; i < qtdMaxEvolucoes && !tempoEsgotado; i++) {
			population.evolve();
			tempoEsgotado = (System.currentTimeMillis() - tempoInicial) > timeoutMs;
		}

		IChromosome melhorSolucao = population.getFittestChromosome();

		if (BigDecimal.ZERO.compareTo(BigDecimal.valueOf(melhorSolucao
				.getFitnessValue())) == 0) {
			// n�o encontrou solu��o.
			return null;
		}

		Gene[] genes = this.funcao.getGenesValidos(melhorSolucao.getGenes());

		// gera o dto da rota mais econ�mica.
		RotaEconomicaDto melhorSolucaoDto = new RotaEconomicaDto();

		for (Gene gene : genes) {
			TrechoMapaDto trechoDto = mapa.getTrechoById((Integer) gene
					.getApplicationData());
			melhorSolucaoDto.addTrecho(trechoDto);
			melhorSolucaoDto.addResumoTrecho(trechoDto.getPontoOrigem());
		}

		TrechoMapaDto ultimoTrecho = melhorSolucaoDto.getTrechos().get(
				melhorSolucaoDto.getTrechos().size() - 1);

		melhorSolucaoDto.addResumoTrecho(ultimoTrecho.getPontoDestino());

		melhorSolucaoDto.setCustoTotal(funcao
				.getCustoTotalRota(melhorSolucaoDto.getTrechos()));

		return melhorSolucaoDto;
	}

}
