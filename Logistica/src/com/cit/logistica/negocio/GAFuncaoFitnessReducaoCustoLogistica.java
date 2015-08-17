package com.cit.logistica.negocio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

import com.cit.logistica.dto.MapaLogisticoDto;
import com.cit.logistica.dto.TrechoMapaDto;

/**
 * Função fitness para algoritmo genético que tem como objetivo definir o custo
 * de logística de uma rota candidata (à solução).
 * 
 * @author Romilson
 */
public class GAFuncaoFitnessReducaoCustoLogistica extends FitnessFunction {

	private static final long serialVersionUID = 1L;

	/**
	 * Mapa de trechos da rota.
	 */
	private MapaLogisticoDto mapa;

	/**
	 * Mapa que liga pontos a trechos que utilizam este ponto como origem.
	 */
	private Map<String, List<Integer>> mapaLigacaoTrechos;

	/**
	 * A maior quantidade de ligações encontrada para o mesmo ponto.
	 */
	private Integer maiorQuantidadeLigacoes;

	private String pontoOrigem;
	private String pontoDestino;
	private Double autonomiaKmPorLitro;
	private Double valorLitroCombustivel;

	/**
	 * Constrói o objeto configurando os valores necessários à execução da
	 * função objetivo.
	 * 
	 * @param mapa
	 *            Mapa de trechos da rota.
	 * @param pontoOrigem
	 * @param pontoDestino
	 * @param autonomiaKmPorLitro
	 *            A autonomia em km por litro. Exemplo: 10 Km/L.
	 * @param valorLitroCombustivel
	 *            O custo do litro de combustível. Exemplo: R$3,10.
	 */
	public GAFuncaoFitnessReducaoCustoLogistica(MapaLogisticoDto mapa,
			String pontoOrigem, String pontoDestino,
			Double autonomiaKmPorLitro, Double valorLitroCombustivel) {
		this.mapa = mapa;
		this.pontoOrigem = pontoOrigem;
		this.pontoDestino = pontoDestino;
		this.autonomiaKmPorLitro = autonomiaKmPorLitro;
		this.valorLitroCombustivel = valorLitroCombustivel;
		this.carregarMapaLigacoesTrechos();
	}

	/**
	 * Esta função atribui um valor para a solução candidata (chromossome).
	 * Obs.:
	 * <ol>
	 * <li>
	 * Cromossomo: Representa uma solução, a qual é candidata a ser a melhor do
	 * ponto de vista que está sendo verificado (no caso o custo da logística da
	 * rota). O Cromossomo é composto por Genes.</li>
	 * <li>
	 * Gene: Representa uma parte da solução. Ou seja, representa um trecho da
	 * rota. Ex.: A - B.</li>
	 * <li>
	 * Alelo: Representa um valor para o Gene. Neste caso utiliza o índice de ligação
	 * de um ponto origem ao seu destino.
	 * Exemplo:
	 * O ponto de origem A possui duas ligações para pontos destinos:
	 * índice 0 -> para o ponto B (Trecho A-B)
	 * índice 1 -> para o ponto C (Trecho A-C)
	 * O gene guardaria como alelo, para este exemplo, o valor 0 ou 1. 
	 * </ol>
	 * 
	 * @return Um valor em função do custo de logística da solução. O custo da
	 *         logística é calculado como sendo o custo do litro de combustível
	 *         multiplicado pela soma das distâncias dos trechos da rota. O
	 *         melhor custo é aquele que se aproxima de zero. Ou seja, a melhor
	 *         rota é aquela que possui o menor custo. Quanto menor o custo da
	 *         solução, maior o valor retornado. <br/>
	 *         Para isto, utiliza a fórmula: 1 - (custoTotal / Math.pow(10, 12))
	 * 
	 */
	@Override
	protected double evaluate(IChromosome solucaoCandidata) {

		Gene[] genesValidos = this.getGenesValidos(solucaoCandidata.getGenes());

		// a partir do mapa de ligação, obtém os trechos da rota.
		List<TrechoMapaDto> trechos = new ArrayList<>();
		String pontoAtual = this.pontoOrigem;

		for (Gene gene : genesValidos) {

			Integer idTrecho = this.obterIdTrecho(pontoAtual, (Integer) gene.getAllele());

			if (idTrecho == null) {
				// chegamos em um ponto final. Nao existe ligacoes para outros pontos a partir daqui.
				// Fazemos com este gene e os restantes do cromossomo sejam desprezados
				gene.setAllele(-1);
				break; 
			}
			
			// guarda o id do trecho como dado adicional do gene. Assim nao
			// precisamos mais calcular
			// o indice de ligação para obter o id do trecho.
			gene.setApplicationData(idTrecho);

			TrechoMapaDto dto = mapa.getTrechoById(idTrecho);

			trechos.add(dto);

			pontoAtual = dto.getPontoDestino();
		}

		TrechoMapaDto trechoFinal = trechos.get(trechos.size() - 1);

		// se a solução candidata nao possuir o destino conforme
		// solicitado, será descartada
		if (!trechoFinal.getPontoDestino().equals(this.pontoDestino)) {
			return 0;
		}
		
		double custoTotal = this.getCustoTotalRota(trechos);

		// a função sempre tem que retornar um valor positivo. E quanto maior
		// este valor, mais bem colocada fica a solução, no ranking de melhores
		// soluções.
		// Gera um valor fitness de forma que o menor custo resulte em um maior
		// valor para a função fitness.
		return 1 - (custoTotal / Math.pow(10, 12));
	}

	/**
	 * Recupera o id do trecho a partir do ponto e índice de ligação informados.
	 * 
	 * @param ponto Ponto. Exemplo: A.
	 * @param indiceLigacao Índice de ligação do ponto a algum destino.
	 * @return Identificador do trecho. Ou nulo caso o ponto não tenha ligações para destinos.
	 */
	private Integer obterIdTrecho(String ponto, Integer indiceLigacao) {

		List<Integer> idTrehosLigados = mapaLigacaoTrechos.get(ponto);

		if (idTrehosLigados == null || idTrehosLigados.isEmpty()) {
			return null;
		}
		
		// se o indice ultrapassa a quantidade de trechos, calcula o resto como
		// indice.
		if (indiceLigacao > idTrehosLigados.size() - 1) {
			indiceLigacao = indiceLigacao % idTrehosLigados.size();
		}

		return idTrehosLigados.get(indiceLigacao);
	}

	/**
	 * Calcula o custo total da rota com base nos trechos informados.
	 * 
	 * @param trechos
	 *            Trechos que compõe a rota.
	 * @return O custo total da rota.
	 */
	public Double getCustoTotalRota(List<TrechoMapaDto> trechos) {
		Double distanciaTotalKm = 0.0;
		for (TrechoMapaDto dto : trechos) {
			distanciaTotalKm += (Double) dto.getDistanciaKm();
		}
		return this.getCustoTotalRota(distanciaTotalKm);
	}

	/**
	 * Calcula o custo total da rota com base na distância informada.
	 * 
	 * @param distanciaTotalKm
	 *            Distância total da rota.
	 * @return O custo total da rota.
	 */
	public Double getCustoTotalRota(Double distanciaTotalKm) {
		return (distanciaTotalKm / this.autonomiaKmPorLitro)
				* this.valorLitroCombustivel;
	}

	/**
	 * Obtem o array de genes válidos a partir do array informado. Um array de
	 * genes válidos é aquele não possui valor -1 em quaisquer de suas posições.
	 * A partir do momento que um gene com valor -1 é identificado, todo o
	 * restante do array é ignorado. Pois o valor -1 indica que não há trecho
	 * válido de rota definido para o gene.
	 * 
	 * @param genes
	 *            da solução.
	 * @return genes válidos.
	 */
	public Gene[] getGenesValidos(Gene[] genes) {
		List<Gene> genesValidos = new ArrayList<>();
		for (Gene gene : genes) {
			Integer id = (Integer) gene.getAllele();
			if (id.equals(-1)) {
				return genesValidos.toArray(new Gene[genesValidos.size()]);
			}
			genesValidos.add(gene);
		}
		return genesValidos.toArray(new Gene[genesValidos.size()]);
	}

	/**
	 * Monta um mapa de ligações entre os pontos. Como chave, guarda o nome do
	 * ponto de origem. Como valor, guarda os ids de trechos em que o ponto de
	 * origem é igual ao ponto de origem chave.
	 */
	private void carregarMapaLigacoesTrechos() {
		this.mapaLigacaoTrechos = new HashMap<>();
		this.maiorQuantidadeLigacoes = 0;
		for (TrechoMapaDto trechoChave : mapa.getTrechos()) {

			String nomePontoChave = trechoChave.getPontoOrigem();

			List<Integer> idLigacoes = new ArrayList<>();

			for (TrechoMapaDto trechoLigacao : mapa.getTrechos()) {
				if (nomePontoChave.equals(trechoLigacao.getPontoOrigem())) {
					idLigacoes.add(trechoLigacao.getId());
				}
			}

			this.maiorQuantidadeLigacoes = idLigacoes.size() > this.maiorQuantidadeLigacoes ? idLigacoes
					.size() : this.maiorQuantidadeLigacoes;

			this.mapaLigacaoTrechos.put(nomePontoChave, idLigacoes);
		}
	}

	/**
	 * @return A quantidade máxima de ligações encontradas para um determinado
	 *         ponto.
	 */
	public Integer getMaiorQuantidadeLigacoes() {
		return maiorQuantidadeLigacoes;
	}
}
