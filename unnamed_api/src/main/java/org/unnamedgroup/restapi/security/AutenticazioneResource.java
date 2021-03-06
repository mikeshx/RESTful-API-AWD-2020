package org.unnamedgroup.restapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.dbutils.DbUtils;
import org.mindrot.jbcrypt.BCrypt;


import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.*;
import org.unnamedgroup.restapi.model.PalinsestoCanaliProgrammi;
import org.unnamedgroup.restapi.resources.CanaliDB;
import org.unnamedgroup.restapi.resources.PalinsestoDB;

@Path("auth")
public class AutenticazioneResource {

    /** ADD A NEW CHANNEL */
    // POST: http://localhost:8080/unnamed_api-1.0-SNAPSHOT/rest/auth/canali/asd

    // TODO: change the url to 'auth/{SID}/canali' istead of 'auth/canali'
    @Logged
    @Path("canali/{nome_canale: [a-z0-9]+}")
    @POST
    @Produces("application/json")
    public Response addChannel(ContainerRequestContext requestContext, @PathParam("nome_canale") String nome_canale) throws SQLException, ParseException {

        // Check if a username cookie exists, otherwise we return unauthorized
        if (!requestContext.getCookies().containsKey("username")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // If the username is empty we return unauthorized
        String username = requestContext.getCookies().get("username").getValue();
        if (username.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Now we check if the current user is admin
        if (!DBManager.checkAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if the channel exists
        // According to RFC 7231, a 303 See Other MAY be used If the result of processing
        // a POST would be equivalent to a representation of an existing resource.
        if (DBManager.checkIfChannelExists(nome_canale)) {
            return Response.status(303).build();
        }

        // Add the new channel
        if (DBManager.addChannel(nome_canale)) {
            return Response.ok().build();
        } else return Response.status(409).build();
    }

    /** EDIT A CHANNEL BY ID */
    // POST: http://localhost:8080/unnamed_api-1.0-SNAPSHOT/rest/auth/canali/asd

    // TODO: change the url to 'auth/{SID}/canali' istead of 'auth/canali'
    @Logged
    @Path("canali/{nome_canale: [a-z0-9]+}/{id: [0-9]+}/{nuovo_nome: [a-z0-9]+}")
    @PUT
    @Produces("application/json")
    public Response editChannel(ContainerRequestContext requestContext, @PathParam("nome_canale") String nome_canale,
                               @PathParam("id") int id_canale, @PathParam("nuovo_nome") String nuovo_nome) throws SQLException, ParseException {

        // Check if a username cookie exists, otherwise we return unauthorized
        if (!requestContext.getCookies().containsKey("username")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // If the username is empty we return unauthorized
        String username = requestContext.getCookies().get("username").getValue();
        if (username.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Now we check if the current user is admin
        if (!DBManager.checkAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if the channel exists
        // In this case we return a 404 instead of 309 because the resource...
        // ...we want to edit it's not available
        if (!DBManager.checkIfChannelExists(nome_canale)) {
            return Response.status(404).build();
        }

        // Update the channel with the new name
        // returns 200 (ok)
        if (DBManager.updateChannelInfo(id_canale, nuovo_nome)) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /** ADD A NEW PROGRAM */
    @Logged
    @Path("programmi/{titolo: [a-z0-9]+}/{descrizione: [a-z0-9]+}/{genere: [a-z0-9]+}/{scheda_approfondimento: [a-z0-9]+}/{is_serie: [0-9]+}/{num_stagione_serie: [0-9]+}/{num_episodio_serie: [0-9]+}")
    @POST
    @Produces("application/json")
    public Response addProgram(ContainerRequestContext requestContext,
                               @PathParam("titolo") String titolo,
                               @PathParam("descrizione") String descrizione,
                               @PathParam("genere") String genere,
                               @PathParam("scheda_approfondimento") String scheda_approfondimento,
                               @PathParam("is_serie") int is_serie,
                               @PathParam("num_stagione_serie") int num_stagione_serie,
                               @PathParam("num_episodio_serie") int num_episodio_serie
                               ) throws SQLException, ParseException {

        // convert the int value in boolean
        boolean is_serie_real = false;
        if (is_serie == 1) is_serie_real = true;

        // Check if a username cookie exists, otherwise we return unauthorized
        if (!requestContext.getCookies().containsKey("username")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // If the username is empty we return unauthorized
        String username = requestContext.getCookies().get("username").getValue();
        if (username.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Now we check if the current user is admin
        if (!DBManager.checkAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Check if the channel exists
        // According to RFC 7231, a 303 See Other MAY be used If the result of processing
        // a POST would be equivalent to a representation of an existing resource.
        if (DBManager.checkIfProgramExists(titolo)) {
            return Response.status(303).build();
        }

        // Add the new channel

        if (DBManager.addProgram(titolo, descrizione, genere, scheda_approfondimento, is_serie_real, num_stagione_serie, num_episodio_serie)) {
            return Response.ok().build();
        } else return Response.status(409).build();
    }

    /** EDIT A PROGRAM BY TITLE */
    // PUT:

    // TODO: change the url to 'auth/{SID}/canali' istead of 'auth/canali'
    @Logged
    @Path("programmi/edit/{titolo: [a-z0-9]+}/{descrizione: [a-z0-9]+}/{genere: [a-z0-9]+}/{scheda_approfondimento: [a-z0-9]+}/{is_serie: [0-9]+}/{num_stagione_serie: [0-9]+}/{num_episodio_serie: [0-9]+}/{id_programma: [0-9]+}")
    @PUT
    @Produces("application/json")
    public Response editProgram(ContainerRequestContext requestContext,
                                @PathParam("titolo") String titolo,
                                @PathParam("descrizione") String descrizione,
                                @PathParam("genere") String genere,
                                @PathParam("scheda_approfondimento") String scheda_approfondimento,
                                @PathParam("is_serie") int is_serie,
                                @PathParam("num_stagione_serie") int num_stagione_serie,
                                @PathParam("num_episodio_serie") int num_episodio_serie,
                                @PathParam("id_programma") int id_programma) throws SQLException, ParseException {

        // Check if a username cookie exists, otherwise we return unauthorized
        if (!requestContext.getCookies().containsKey("username")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // If the username is empty we return unauthorized
        String username = requestContext.getCookies().get("username").getValue();
        if (username.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Now we check if the current user is admin
        if (!DBManager.checkAdmin(username)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        boolean is_serie_real = false;
        if (is_serie == 1) is_serie_real = true;

        // Update the channel with the new name
        // returns 200 (ok)
        if (DBManager.updateProgramInfo(titolo, descrizione, genere, scheda_approfondimento, is_serie_real, num_stagione_serie, num_episodio_serie, id_programma)) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }



    // POST ==> /rest/auth/login?username=admin&password=admin
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)

    public Response doLogin(@Context UriInfo uriinfo,
            //un altro modo per ricevere e iniettare i parametri con JAX-RS...
            @FormParam("username") String username,
            @FormParam("password") String password) {

        try {
            if (authenticate(username, password)) {
                System.out.println("true");

                //Gerenate a random id for the issueToken function
                UUID random_id = UUID.randomUUID();

                String authToken = issueToken(uriinfo, username);

                //old issue_token
                //String authToken = issueToken(random_id.toString(), uriinfo, username, 800000);

                // return the token on response

                return Response.ok(authToken)
                        .cookie(new NewCookie("token", authToken))
                        .cookie(new NewCookie("username", username))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer" + authToken).build();

            } else {

                return Response.status(Response.Status.UNAUTHORIZED).build();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Response resp = null;
            return resp;
        }
    }

    @Logged
    @DELETE
    @Path("/logout")
    public Response doLogout(@Context HttpServletRequest request) {
        try {
            //estraiamo i dati inseriti dal nostro LoggedFilter...
            String token = (String) request.getAttribute("token");
            if (token != null) {
                revokeToken(token);
            }
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    /* User authentication */
    public static boolean authenticate(String username, String password) throws SQLException {

        Connection dbConnection = DBManager.getDBConenction();
        PreparedStatement pst = null;
        boolean result = false;

        try {
            pst = dbConnection.prepareStatement("SELECT password FROM login WHERE username = ?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                //Check if the provided plain text password and the hashed one are equal
                if (BCrypt.checkpw(password, rs.getString("password")))
                    result = true;
            }
        } catch (Exception e) {
                e.printStackTrace();
        } finally {
            DbUtils.close(dbConnection);
        }

        return result;
    }

    // The secret key. This should be in a property file NOT under source
    // control and not hard coded in real life. We're putting it here for
    // simplicity.
    private static String SECRET_KEY = "oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";

    private String issueToken(UriInfo context, String username) {
        /* registrare il token e associarlo all'utenza */
        String token = username + UUID.randomUUID().toString();
        /* per esempio */

//        JWT
//        Key key = JWTHelpers.getInstance().getJwtKey();
//        String token = Jwts.builder()
//                .setSubject(username)
//                .setIssuer(context.getAbsolutePath().toString())
//                .setIssuedAt(new Date())
//                .setExpiration(Date.from(LocalDateTime.now().plusMinutes(15L).atZone(ZoneId.systemDefault()).toInstant()))
//                .signWith(key)
//                .compact();
        return token;
    }

    //Sample method to construct a JWT
    public static String issueToken_old(String id, UriInfo context, String subject, long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(context.getAbsolutePath().toString())
                .signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    private void revokeToken(String token) {
        /* invalidate il token */
    }

}
