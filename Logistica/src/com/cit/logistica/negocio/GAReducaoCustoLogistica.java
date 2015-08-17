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
 * Representa o algoritmo genético que cuidará da avaliação das rotas.
 * Responsável por configurar os parâmetros iniciais e então iniciar a execução
 * da engine genética.
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
	 * Funcao fitness configurada após a inicialização.
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
	 *            Mapa que será processado.
	 */
	public GAReducaoCustoLogistica(MapaLogisticoDto mapa) {
		this.cfg = new DefaultConfiguration(mapa.getId().toString(),
				mapa.getNome());
		this.mapa = mapa;
	}

	/**
	 * Configura os parâmetros iniciais para o cálculo de custo das rotas. Gera
	 * uma população de soluções candidatas (rotas), que será base para a
	 * análise do custo das rotas.
	 * 
	 * O melhor custo será pesquisado com base nos parâmetros deste método, como
	 * por exemplo os pontos de origem e destino.
	 * 
	 * @param pontoOrigem
	 *            O ponto de origem da rota desejada.
	 * @param pontoDestino
	 *            O ponto de destino da rota desejada.
	 * @param autonomiaKmPorLitro
	 *            A autonomia em km por litro do veículo. Exemplo: 10 Km/L.
	 * @param valorLitroCombustivel
	 *            O custo do litro de combustível. Exemplo: R$3,10.
	 * @throws InvalidConfigurationException
	 *             Caso haja erros na configuração do algoritmo genético.
	 */
	public void inicializar(String pontoOrigem, String pontoDestino,
			Double autonomiaKmPorLitro, Double valorLitroCombustivel)
			throws InvalidConfigurationException {

		// Configura a função objetivo a ser utilizada.
		this.funcao = new GAFuncaoFitnessReducaoCustoLogistica(mapa,
				pontoOrigem, pontoDestino, autonomiaKmPorLitro,
				valorLitroCombustivel);
		this.cfg.setFitnessFunction(funcao);

		Integer qtdTotalTrechos = this.mapa.getTrechos().size();

		// O cromossomo da solução é composto de N genes. Onde N é a quantidade
		// de trechos existentes no mapa.
		// O conteúdo do gene (alelo) possui como valor o índice da ligação
		// entre o ponto inicial e o ponto destino.
		// Exemplo:
		// O ponto de origem A possui duas ligações para pontos destinos:
		// índice 0 -> para o ponto B (Trecho A-B)
		// índice 1 -> para o ponto C (Trecho A-C)
		// O gene guardaria como alelo, para este exemplo, o valor 0 ou 1.
		// Se o valor gerado para o gene é maior que o último índice, supondo 5
		// para o exemplo acima,
		// então o índice a ser considerado é o valor resto de : 5 % [Qtd
		// índices].
		// Se o valor gerado para o gene é -1, indica que é final de rota, ou
		// seja, não há mais trechos nos genes restantes do cromossomo.

		List<Gene> genes = new ArrayList<Gene>();

		for (int i = 0; i < qtdTotalTrechos; i++) {
			// Uma rota possui pelo menos 1 trecho. Então o primeiro trecho
			// (gene) possui um mínimo de 0. Os demais possuem um mínimo de
			// -1, onde -1 indica que não há trecho.
			Integer lowerBound = i == 0 ? 0 : -1;
			genes.add(new IntegerGene(this.cfg, lowerBound, this.funcao
					.getMaiorQuantidadeLigacoes() - 1));
		}

		Chromosome solucaoExemplo = new Chromosome(this.cfg,
				genes.toArray(new Gene[genes.size()]));

		// configura a solucao exemplo e o tamanho da população a gerar
		this.cfg.setSampleChromosome(solucaoExemplo);
		this.cfg.setPopulationSize(qtdTotalTrechos * qtdTotalTrechos);
	}

	/**
	 * Executa as evoluções em busca da melhor solução para o problema.
	 * 
	 * @param timeoutMs
	 *            Timeout de processamento em milisegundos.
	 * @param qtdMaxEvolucoes
	 *            Quantidade máxima de evoluções a realizar.
	 * @return Melhor solução encontrada.
	 * @throws InvalidConfigurationException
	 */
	public RotaEconomicaDto executar(int timeoutMs, int qtdMaxEvolucoes)
			throws InvalidConfigurationException {

		long tempoInicial = System.currentTimeMillis();
		boolean tempoEsgotado = false;

		// Gera a população de soluções candidatas (cromossomos) e evolui
		// quantas vezes puder, dentro do timeout. Tenta cinco vezes encontrar
		// uma
		// solução melhor que a anterior. Se não conseguir, considera a última
		// encontrada como melhor
		Genotype population = Genotype.randomInitialGenotype(cfg);

		for (int i = 0; i < qtdMaxEvolucoes && !tempoEsgotado; i++) {
			population.evolve();
			tempoEsgotado = (System.currentTimeMillis() - tempoInicial) > timeoutMs;
		}

		IChromosome melhorSolucao = population.getFittestChromosome();

		if (BigDecimal.ZERO.compareTo(BigDecimal.valueOf(melhorSolucao
				.getFitnessValue())) == 0) {
			// não encontrou solução.
			return null;
		}

		Gene[] genes = this.funcao.getGenesValidos(melhorSolucao.getGenes());

		// gera o dto da rota mais econômica.
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
