package com.setcce.sample.swagger.rest;

import com.setcce.sample.swagger.utils.ResourceProducer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@RunAsClient
public class UserTest {
    @ArquillianResource
    private URI baseURL;
    private Client client;
    private WebTarget webTarget;

@Deployment
    public static Archive<?> createDeployment() {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
        return ShrinkWrap.create(WebArchive.class).addClass(RestApplication.class).addClass(User.class)
                .addClass(ResourceProducer.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsLibraries(files);
    }

    @Before
    public void initWebTarget() {
        System.out.println("initWebTarget");
        System.out.println("baseUrl: "+baseURL);
        client = ClientBuilder.newClient();
        webTarget = client.target(baseURL);
    }

    @Test
    public void should_be_deployed() throws URISyntaxException {
    assertEquals(new URI("http://localhost:8080/"), baseURL);
    }
}
