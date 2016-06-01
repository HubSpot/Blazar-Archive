package com.hubspot.blazar.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.hubspot.blazar.config.BlazarConfiguration;

import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.views.View;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class UiResource {
    private final String basePath;

    @Inject
    public UiResource(BlazarConfiguration configuration) {
        this.basePath = ((SimpleServerFactory) configuration.getServerFactory()).getApplicationContextPath();
    }

    @Path("/{uiPath:.*}")
    @GET
    public IndexView getUi() {
        return new IndexView();
    }

    @GET
    public IndexView getUiRoot() {
        return new IndexView();
    }

    private class IndexView extends View {
        protected IndexView() {
            super("index.mustache");
        }

        public String getStaticRoot() {
            return basePath + "/static";
        }

        public String getAppRoot() {
            return basePath;
        }

        public String getApiRoot() {
            return basePath;
        }
    }
}
