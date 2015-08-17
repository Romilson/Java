package com.cit.logistica.servico;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.cit.logistica.dto.MapaLogisticoDto;
import com.cit.logistica.dto.RotaEconomicaDto;
import com.cit.logistica.negocio.LogisticaBusiness;
import com.google.gson.Gson;

@Path("/logistica")
public class LogisticaWS {

	private Logger log = Logger.getLogger(LogisticaWS.class);
	
	/**
	 * Classe da camada de negócio.
	 */
	private LogisticaBusiness business = new LogisticaBusiness();

	@GET
	@Path("/obterRotaMaisEconomica/{pontoOrigem}/{pontoDestino}/{autonomiaKmPorLitro}/{valorLitroCombustivel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterRotaMaisEconomica(
			@PathParam("pontoOrigem") String pontoOrigem,
			@PathParam("pontoDestino") String pontoDestino, 
			@PathParam("autonomiaKmPorLitro") Double autonomiaKmPorLitro,
			@PathParam("valorLitroCombustivel") Double valorLitroCombustivel) {
		Gson gson = new Gson();
		try {
			RotaEconomicaDto dto = business.obterRotaMaisEconomica(pontoOrigem, pontoDestino, autonomiaKmPorLitro, valorLitroCombustivel);
			
			if (dto == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("logistica.cit_nenhuma.rota.nao.encontrada.para.parametros.informados").build(); 
			}
			
			return Response.ok(gson.toJson(dto), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			log.error("ws error.", e);
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getMessage()).build();
		}
	}

	/**
	 * Grava um novo mapa logístico ou atualiza um existente.
	 * 
	 * @param dto
	 *            Dto contendo informações do mapa logístico.
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/armazenarMapa")
	public Response armazenarMapa(String json) {
		Gson gson = new Gson();
		try {
			MapaLogisticoDto dto = gson.fromJson(json, MapaLogisticoDto.class);
			business.armazenarMapa(dto);
		} catch (Exception e) {
			log.error("ws error.", e);
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(e.getMessage()).build();
		}
		return Response.ok().build();
	}
}
