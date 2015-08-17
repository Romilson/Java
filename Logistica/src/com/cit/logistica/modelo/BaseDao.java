package com.cit.logistica.modelo;

import java.util.Arrays;
import java.util.Set;

/**
 * Classe base para objetos de acesso a banco de dados.
 * 
 * @author Romilson
 * 
 */
public class BaseDao {

	/**
	 * Calcula o proximo Id disponivel.
	 * 
	 * @param ids
	 *            Lista de ids para verificar.
	 */
	protected Integer nextId(Set<Integer> ids) {
		if (ids.isEmpty()) {
			return 1;
		}
		Integer[] idsArray = ids.toArray(new Integer[ids.size()]); 
		Arrays.sort(idsArray);
		return idsArray[idsArray.length - 1] + 1;
	}
}
