package com.cit.logistica.modelo;

import java.util.Map;

import com.cit.logistica.LogisticaException;
import com.cit.logistica.dto.MapaLogisticoDto;

/**
 * Classe de acesso a dados para o sistema Logistica.
 * 
 * @author Romilson
 * 
 */
public class LogisticaDao extends BaseDao {

	private static LogisticaDao INSTANCE = new LogisticaDao();

	private ProvedorModelo provedor = new ProvedorModelo();

	/**
	 * Recupera uma instancia do Dao de Logística.
	 * 
	 * @return Dao de logística.
	 */
	public static LogisticaDao getInstance() {
		return INSTANCE;
	}

	/**
	 * Singleton, para evitar problemas de concorrência.
	 */
	private LogisticaDao() {
	}

	/**
	 * Pesquisa o modelo.
	 * 
	 * @return Modelo de dados.
	 * @throws LogisticaException
	 *             Caso ocorra erros no acesso a dados.
	 */
	public Modelo findModelo() throws LogisticaException {
		return provedor.ler();
	}

	/**
	 * Salva ou atualiza um mapa no banco de dados.
	 * 
	 * @param mapa
	 *            Mapa a ser salvo.
	 * @throws LogisticaException
	 *             Caso ocorra erros no acesso a dados.
	 */
	public void salvarOuAtualizar(MapaLogisticoDto mapa)
			throws LogisticaException {
		Modelo modelo = this.findModelo();
		this.salvarOuAtualizar(mapa, modelo);
	}

	/**
	 * Remove um mapa do sistema.
	 * 
	 * @throws LogisticaException
	 *             Caso ocorra erros no acesso a dados.
	 */
	public void removerMapaPeloNome(String nome) throws LogisticaException {
		Modelo modelo = this.findModelo();
		Integer id = null;
		for (Map.Entry<Integer, MapaLogisticoDto> e : modelo.getMapas()
				.entrySet()) {
			if (e.getValue().getNome().equalsIgnoreCase(nome)) {
				id = e.getKey();
			}
		}
		modelo.getMapas().remove(id);
		this.provedor.gravar(modelo);
	}

	/**
	 * Salva ou atualiza um mapa no banco de dados.
	 * 
	 * @param mapa
	 *            Mapa a ser salvo.
	 * @param modelo
	 *            Modelo de dados em cache.
	 * @throws LogisticaException
	 *             Caso ocorra erros no acesso a dados.
	 */
	public void salvarOuAtualizar(MapaLogisticoDto mapa, Modelo modelo)
			throws LogisticaException {
		if (mapa.getId() == null) {
			mapa.setId(nextId(modelo.getMapas().keySet()));
		}
		modelo.getMapas().put(mapa.getId(), mapa);
		this.provedor.gravar(modelo);
	}

}
