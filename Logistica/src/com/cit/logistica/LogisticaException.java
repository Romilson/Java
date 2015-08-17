package com.cit.logistica;

/**
 * Exceção para erros ocorridos dentro do sistema.
 * 
 * @author Romilson
 * 
 */
public class LogisticaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Repassa para Exception.
	 * 
	 * @param message
	 *            Mensagem.
	 * @param cause
	 *            Causa.
	 */
	public LogisticaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Repassa para Exception.
	 * 
	 * @param message
	 *            Mensagem.
	 */
	public LogisticaException(String message) {
		super(message);
	}

}
