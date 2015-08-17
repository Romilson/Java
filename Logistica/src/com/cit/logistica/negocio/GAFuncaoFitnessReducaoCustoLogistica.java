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
 * Fun��o fitness para algoritmo gen�tico que tem como objetivo definir o custo
 * de log�stica de uma rota candidata (� solu��o).
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
	 * A maior quantidade de liga��es encontrada para o mesmo ponto.
	 */
	private Integer maiorQuantidadeLigacoes;

	private String pontoOrigem;
	private String pontoDestino;
	private Double autonomiaKmPorLitro;
	private Double valorLitroCombustivel;

	/**
	 * Constr�i o objeto configurando os valores necess�rios � execu��o da
	 * fun��o objetivo.
	 * 
	 * @param mapa
	 *            Mapa de trechos da rota.
	 * @param pontoOrigem
	 * @param pontoDestino
	 * @param autonomiaKmPorLitro
	 *            A autonomia em km por litro. Exemplo: 10 Km/L.
	 * @param valorLitroCombustivel
	 *            O custo do litro de combust�vel. Exemplo: R$3,10.
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
	 * Esta fun��o atribui um valor para a solu��o candidata (chromossome).
	 * Obs.:
	 * <ol>
	 * <li>
	 * Cromossomo: Representa uma solu��o, a qual � candidata a ser a melhor do
	 * ponto de vista que est� sendo verificado (no caso o custo da log�stica da
	 * rota). O Cromossomo � composto por Genes.</li>
	 * <li>
	 * Gene: Representa uma parte da solu��o. Ou seja, representa um trecho da
	 * rota. Ex.: A - B.</li>
	 * <li>
	 * Alelo: Representa um valor para o Gene. Neste caso utiliza o �ndice de liga��o
	 * de um ponto origem ao seu destino.
	 * Exemplo:
	 * O ponto de origem A possui duas liga��es para pontos destinos:
	 * �ndice 0 -> para o ponto B (Trecho A-B)
	 * �ndice 1 -> para o ponto C (Trecho A-C)
	 * O gene guardaria como alelo, para este exemplo, o valor 0 ou 1. 
	 * </ol>
	 * 
	 * @return Um valor em fun��o do custo de log�stica da solu��o. O custo da
	 *         log�stica � calculado como sendo o custo do litro de combust�vel
	 *         multiplicado pela soma das dist�ncias dos trechos da rota. O
	 *         melhor custo � aquele que se aproxima de zero. Ou seja, a melhor
	 *         rota � aquela que possui o menor custo. Quanto menor o custo da
	 *         solu��o, maior o valor retornado. <br/>
	 *         Para isto, utiliza a f�rmula: 1 - (custoTotal / Math.pow(10, 12))
	 * 
	 */
	@Override
	protected double evaluate(IChromosome solucaoCandidata) {

		Gene[] genesValidos = this.getGenesValidos(solucaoCandidata.getGenes());

		// a partir do mapa de liga��o, obt�m os trechos da rota.
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
			// o indice de liga��o para obter o id do trecho.
			gene.setApplicationData(idTrecho);

			TrechoMapaDto dto = mapa.getTrechoById(idTrecho);

			trechos.add(dto);

			pontoAtual = dto.getPontoDestino();
		}

		TrechoMapaDto trechoFinal = trechos.get(trechos.size() - 1);

		// se a solu��o candidata nao possuir o destino conforme
		// solicitado, ser� descartada
		if (!trechoFinal.getPontoDestino().equals(this.pontoDestino)) {
			return 0;
		}
		
		double custoTotal = this.getCustoTotalRota(trechos);

		// a fun��o sempre tem que retornar um valor positivo. E quanto maior
		// este valor, mais bem colocada fica a solu��o, no ranking de melhores
		// solu��es.
		// Gera um valor fitness de forma que o menor custo resulte em um maior
		// valor para a fun��o fitness.
		return 1 - (custoTotal / Math.pow(10, 12));
	}

	/**
	 * Recupera o id do trecho a partir do ponto e �ndice de liga��o informados.
	 * 
	 * @param ponto Ponto. Exemplo: A.
	 * @param indiceLigacao �ndice de liga��o do ponto a algum destino.
	 * @return Identificador do trecho. Ou nulo caso o ponto n�o tenha liga��es para destinos.
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
	 *            Trechos que comp�e a rota.
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
	 * Calcula o custo total da rota com base na dist�ncia informada.
	 * 
	 * @param distanciaTotalKm
	 *            Dist�ncia total da rota.
	 * @return O custo total da rota.
	 */
	public Double getCustoTotalRota(Double distanciaTotalKm) {
		return (distanciaTotalKm / this.autonomiaKmPorLitro)
				* this.valorLitroCombustivel;
	}

	/**
	 * Obtem o array de genes v�lidos a partir do array informado. Um array de
	 * genes v�lidos � aquele n�o possui valor -1 em quaisquer de suas posi��es.
	 * A partir do momento que um gene com valor -1 � identificado, todo o
	 * restante do array � ignorado. Pois o valor -1 indica que n�o h� trecho
	 * v�lido de rota definido para o gene.
	 * 
	 * @param genes
	 *            da solu��o.
	 * @return genes v�lidos.
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
	 * Monta um mapa de liga��es entre os pontos. Como chave, guarda o nome do
	 * ponto de origem. Como valor, guarda os ids de trechos em que o ponto de
	 * origem � igual ao ponto de origem chave.
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
	 * @return A quantidade m�xima de liga��es encontradas para um determinado
	 *         ponto.
	 */
	public Integer getMaiorQuantidadeLigacoes() {
		return maiorQuantidadeLigacoes;
	}
}
