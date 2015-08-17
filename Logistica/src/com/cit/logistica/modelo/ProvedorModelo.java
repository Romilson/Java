package com.cit.logistica.modelo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.cit.logistica.LogisticaException;
import com.google.gson.Gson;

/**
 * Representa didaticamente o provedor de banco de dados da aplicação.
 * 
 * @author Romilson
 * 
 */
public class ProvedorModelo {

	/**
	 * Realiza a operação de gravação do modelo de dados.
	 * 
	 * @param modelo
	 *            Modelo de dados da aplicação.
	 * @throws LogisticaException
	 *             Caso ocorra erros ao acessar o modelo de dados.
	 */
	public void gravar(Modelo modelo) throws LogisticaException {
		try {
			Gson gson = new Gson();
			String json = gson.toJson(modelo);
			Files.write(this.getArquivoBancoDados().toPath(), json.getBytes());
		} catch (Exception e) {
			throw new LogisticaException(
					"Ocorreu erro ao gravar o modelo de dados.", e);
		}
	}

	/**
	 * Realiza a operação de leitura do modelo de dados.
	 * 
	 * @return Modelo de dados da aplicação.
	 * @throws LogisticaException
	 *             Caso ocorra erros ao ler o modelo de dados.
	 */
	public Modelo ler() throws LogisticaException {
		try {
			String json = new String(Files.readAllBytes(this
					.getArquivoBancoDados().toPath()));
			Gson gson = new Gson();
			Modelo modelo = gson.fromJson(json, Modelo.class);
			return modelo != null ? modelo : new Modelo();
		} catch (Exception e) {
			throw new LogisticaException(
					"Ocorreu erro ao ler o modelo de dados.", e);
		}
	}

	/**
	 * Localização do banco de dados da aplicação. Atualmente utiliza arquivo
	 * texto em formato json.
	 * 
	 * @return File representando o diretório e arquivo de banco de dados.
	 * @throws LogisticaException
	 *             Caso ocorra erro na localização/criação do banco de dados.
	 */
	private File getArquivoBancoDados() throws LogisticaException {
		File dir = new File(System.getProperty("catalina.base")
				+ "/db_logistica");

		File file = new File(dir, "db.json");
		
		try {
			if (!Files.exists(dir.toPath())) {
				Files.createDirectory(dir.toPath());
			}
			if (!Files.exists(file.toPath())) {
				Files.createFile(file.toPath());
			}
		} catch (IOException e) {
			throw new LogisticaException(
					"Nao foi possivel criar arquivo de banco de dados.", e);
		}
		
		return file;
	}

}
