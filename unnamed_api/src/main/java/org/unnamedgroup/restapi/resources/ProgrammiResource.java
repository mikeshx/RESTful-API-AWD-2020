package org.unnamedgroup.restapi.resources;

import org.unnamedgroup.restapi.model.Palinsesto;
import org.unnamedgroup.restapi.model.PalinsestoCanaliProgrammi;
import org.unnamedgroup.restapi.model.Programma;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
@Path("programmi")
public class ProgrammiResource {
    @Path("{id: [a-z0-9]+}")
    @GET
    @Produces("application/json")
    public Response getProgrammi(@PathParam("id") int id) throws SQLException, ParseException {
        Programma prog; //per esempio

        prog = ProgrammiDB.getProgramma(id);
        return Response.ok(prog).build();
    }

    @Path("{id: [a-z0-9]+}/episodi")
    @GET
    @Produces("application/json")
    public Response getEpisodiProgramma(@PathParam("id") int id) throws SQLException, ParseException {
        ArrayList<PalinsestoCanaliProgrammi> prog; //per esempio

        prog = ProgrammiDB.getEpisodiProgramma(id);
        if (prog.isEmpty()) return Response.ok("error 400").build();
        return Response.ok(prog).build();
    }
}
