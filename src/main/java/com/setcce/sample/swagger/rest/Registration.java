package com.setcce.sample.swagger.rest;

import io.swagger.annotations.Api;
import com.setcce.sample.swagger.utils.QLogger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.*;
import javax.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

@Path("/registration")
@Api(description = "Api Gateway")
@RequestScoped
public class Registration {

    @Inject
    @QLogger
    private Logger logger;

    @Context
    HttpServletRequest request;

    //TODO: where do we read config from?
    private String registrationDBApi = "http://private-a8e08a-dbapi5.apiary-mock.com";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegistrationAttributes(@QueryParam("source") String source) {
        String registrationUser = getRegistrationUser(request);
        if(registrationUser == null){
            return prepareResponse(null, 403, "getUserAttributes called without authorization", logger);
        }
        Response response = ClientBuilder.newClient().target(registrationDBApi).path("/registration").queryParam("source", source).request().header("registration_user", registrationUser).get();
        return prepareResponse(response.getEntity(), response.getStatus(), "getUserAttributes", logger);
    }

    @POST @Path("/{raSource}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRegistration(@PathParam("raSource") String raSource, @FormParam("attributes") String attributes, @QueryParam("consent") String consent) {
        try{
            JsonReader jsonReader = Json.createReader(new StringReader(attributes));
            jsonReader.readObject();
            jsonReader.close();
        } catch (Exception ex) {
            logger.severe("postUser attributes input not valid JSON structure: " + attributes);
            return prepareResponse(null, 400, "postUser attributes not valid JSON!", logger);
        }

        String registrationUser = getRegistrationUser(request);
        if(registrationUser == null){
            return prepareResponse(null, 403, "Post Registration called without authorization", logger);
        }

        Response response = ClientBuilder.newClient().target(registrationDBApi).path("/registration/").queryParam("consent", consent).request().header("registration_user", registrationUser).buildPost(Entity.json(attributes)).invoke();
        return prepareResponse(null, response.getStatus(), "postUser", logger);
    }

    private String getRegistrationUser(HttpServletRequest request){
        String casAuth = casAuth(request);
        if(casAuth != null){
            return casAuth;
        }

        String certAuth = certAuth(request);
        if(certAuth != null){
            return certAuth;
        }
        return null;
    }

    private String casAuth(HttpServletRequest request){
        String externalIdIso88591 = request.getHeader("setccecas_external_id");
        if (externalIdIso88591 == null) {
            return null;
        }
        try {
            logger.info("Registration with setccecas_external_id: "+ externalIdIso88591);
            return new String(externalIdIso88591.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.throwing("Registration", "casAuth", ex);
            return null;
        }
    }

    private String certAuth(HttpServletRequest request){
        String externalIdIso88591 = request.getHeader("javax.servlet.request.X509Certificate");
        if (externalIdIso88591 == null) {
            return null;
        }
        try {
            X509Certificate[] x509Certificates = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
            if(x509Certificates.length > 0) {
                String userCertThumbPrint = getThumbPrint(x509Certificates[x509Certificates.length - 1].getEncoded());
                if(!userCertThumbPrint.isEmpty()){
                    logger.info("Registration with cert thumbPrint: "+ userCertThumbPrint);
                    return userCertThumbPrint;
                }
            }
            return null;
        } catch (Exception ex) {
            logger.throwing("Registration", "casAuth", ex);
            return null;
        }
    }

    private static Response prepareResponse(Object entity, int code, String logMsg, Logger log){
        if(logMsg!=null && !logMsg.isEmpty()){
            if (code == 200 || code == 201 || code == 204) {
                log.info(logMsg);
            } else {
                log.severe(logMsg);
            }
        }
        Response.ResponseBuilder response = Response.ok().header("X-Content-Type-Option", "nosniff");
        response.entity(entity);
        response.status(code);
        return response.build();
    }

    private static String getThumbPrint(byte[] der) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    private static String hexify (byte bytes[]) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }
        return buf.toString();
    }
}
