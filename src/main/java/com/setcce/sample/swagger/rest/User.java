/*
 * Copyright (C) 2016, 2017 Antonio Goncalves and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.setcce.sample.swagger.rest;

import io.swagger.annotations.Api;
import com.setcce.sample.swagger.utils.QLogger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.*;
import javax.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Path("/user")
@Api(description = "Top CDs Endpoint")
@RequestScoped
public class User {

    @Inject
    @QLogger
    private Logger logger;

    @Context
    HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getTopCDs() {

        JsonArrayBuilder array = Json.createArrayBuilder();
        List<Integer> randomCDs = getRandomNumbers();
        for (Integer randomCD : randomCDs) {
            array.add(Json.createObjectBuilder().add("id", randomCD));
        }
        return array.build().toString();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response user(@FormParam("attributes") String attributes) {
        JsonArray jsonArray = null;
        String selfRegistrationDBApi = "http://private-amnesiac-a8e08a-dbapi5.apiary-proxy.com/self-registration";
        String registrationDBApi = "http://private-amnesiac-a8e08a-dbapi5.apiary-proxy.com/registration";

        logger.info("request: "+ request.toString() );
        logger.info("attributes: "+ attributes );

        String casAuth = casAuth(request);
        if(casAuth != null){

            //TODO: call DB API and get user + roles, check if role is ok. If yes, call api db - registration api
        }

        String certAuth = certAuth(request);
        if(certAuth != null){
            //TODO: call DB API and get user + roles, check if role is ok. If yes, call api db - registration api
        }

        if(casAuth == null && certAuth == null){
            //TODO: no auth user, call api db - self-registration
        }

        try{
            JsonReader jsonReader = Json.createReader(new StringReader(attributes));
            jsonArray = jsonReader.readArray();
            jsonReader.close();
        } catch (Exception ex) {
            logger.severe("User attributes input not valid JSON structure: " + attributes);
            logger.throwing("User", "user", ex);
        }

        return prepareResponse(jsonArray, 200, "registration", logger);
    }

    private String casAuth(HttpServletRequest request){
        String externalIdIso88591 = request.getHeader("setccecas_external_id");
        if (externalIdIso88591 == null) {
            return null;
        }
        try {
            logger.info("User with setccecas_external_id: "+ externalIdIso88591);
            return new String(externalIdIso88591.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.throwing("User", "casAuth", ex);
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
                if(userCertThumbPrint != null && !userCertThumbPrint.isEmpty()){
                    logger.info("User with cert thumbPrint: "+ userCertThumbPrint);
                    return userCertThumbPrint;
                }
            }
            return null;
        } catch (Exception ex) {
            logger.throwing("User", "casAuth", ex);
            return null;
        }
    }

    private static Response prepareResponse(Object entity, int code, String logMsg, Logger log){
        if(logMsg!=null && !logMsg.isEmpty()){
            log.info(logMsg);
        }
        Response.ResponseBuilder response = Response.ok().header("X-Content-Type-Option", "nosniff");
        response.entity(entity);
        response.status(code);
        return response.build();
    }

    public static String getThumbPrint(byte[] der) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    public static String hexify (byte bytes[]) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }
        return buf.toString();
    }

    private List<Integer> getRandomNumbers() {
        List<Integer> randomCDs = new ArrayList<>();
        Random r = new Random();
        randomCDs.add(r.nextInt(100) + 1101);
        randomCDs.add(r.nextInt(100) + 1101);
        randomCDs.add(r.nextInt(100) + 1101);
        randomCDs.add(r.nextInt(100) + 1101);
        randomCDs.add(r.nextInt(100) + 1101);

        logger.info("Top CDs are " + randomCDs);

        return randomCDs;
    }
}
