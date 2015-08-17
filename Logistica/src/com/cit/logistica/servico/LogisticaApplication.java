package com.cit.logistica.servico;

import javax.ws.rs.ApplicationPath;

import com.sun.jersey.api.core.PackagesResourceConfig;

@ApplicationPath("resources")
public class LogisticaApplication extends PackagesResourceConfig {
	public LogisticaApplication() {
		super("com.cit.logistica.servico");
	}
}