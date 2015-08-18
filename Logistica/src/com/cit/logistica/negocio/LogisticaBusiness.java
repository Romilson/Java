package com.cit.logistica.negocio;

import java.util.Map;

import javax.ws.rs.PathParam;

import org.apache.commons.lang.StringUtils;
import org.jgap.InvalidConfigurationException;

import com.cit.logistica.LogisticaException;
import com.cit.logistica.dto.MapaLogisticoDto;
import com.cit.logistica.dto.RotaEconomicaDto;
import com.cit.logistica.modelo.LogisticaDao;
import com.cit.logistica.modelo.Modelo;

/**
 * Classe respons�vel por concentrar todas as regras de neg�cio do sistema de
 * log�stica.
 * 
 * @author Romilson
 */
public class LogisticaBusiness {

	/**
	 * Acesso a dados.
	 */
	private LogisticaDao dao = LogisticaDao.getInstance();

	/**
	 * Inclui o mapa no sistema. Caso um id v�lido tenha sido informado,
	 * substitui o mapa existente pelo novo. Substitui tamb�m se j� existir um
	 * mapa com mesmo nome.
	 * 
	 * @param mapa
	 *            Dto contendo dados do mapa.
	 * @throws LogisticaException
	 */
	public void armazenarMapa(MapaLogisticoDto mapa) throws LogisticaException {
		if (StringUtils.isEmpty(mapa.getNome())) {
			throw new LogisticaException("Nome do mapa � obrigat�rio.");
		}

		// Os trechos da rota sempre recebem id de 1 a N.
		// O id � utilizado como identificador do trecho para o algoritmo
		// genetico.
		for (int i = 0; i < mapa.getTrechos().size(); i++) {
			mapa.getTrechos().get(i).setId(i + 1);
		}

		dao.salvarOuAtualizar(mapa);
	}

	/**
	 * Remove um mapa pelo nome.
	 * @param nome Nome do mapa.
	 * @throws LogisticaException
	 */
	public void removerMapa(String nome) throws LogisticaException {
		dao.removerMapaPeloNome(nome);
	}
	
	/**
	 * Com base nos par�metros de entrada e nos mapas armazenados, verifica qual
	 * a rota mais econ�mica.
	 * 
	 * @param pontoOrigem
	 * @param pontoDestino
	 * @param autonomiaKmPorLitro
	 * @param valorLitroCombustivel
	 * @return Rota mais econ�mica.
	 * @throws LogisticaException
	 */
	public RotaEconomicaDto obterRotaMaisEconomica(
			@PathParam("pontoOrigem") String pontoOrigem,
			@PathParam("pontoDestino") String pontoDestino,
			@PathParam("autonomiaKmPorLitro") Double autonomiaKmPorLitro,
			@PathParam("valorLitroCombustivel") Double valorLitroCombustivel)
			throws LogisticaException {

		Modelo modelo = dao.findModelo();

		RotaEconomicaDto melhorSolucao = null;

		for (Map.Entry<Integer, MapaLogisticoDto> entry : modelo.getMapas()
				.entrySet()) {

			MapaLogisticoDto mapa = entry.getValue();

			GAReducaoCustoLogistica al = new GAReducaoCustoLogistica(mapa);

			try {
				al.inicializar(pontoOrigem, pontoDestino, autonomiaKmPorLitro,
						valorLitroCombustivel);
				RotaEconomicaDto rotaDto = al.executar(90 * 1000, 300);

				if (rotaDto == null) {
					continue; // solucao nao encontrada.
				}
				
				if (melhorSolucao == null
						|| rotaDto.getCustoTotal() < melhorSolucao
								.getCustoTotal()) {
					melhorSolucao = rotaDto;
				}
			} catch (InvalidConfigurationException e) {
				throw new LogisticaException("Erro ao executar c�lculo.", e);
			}
		}

		return melhorSolucao;
	}

}
