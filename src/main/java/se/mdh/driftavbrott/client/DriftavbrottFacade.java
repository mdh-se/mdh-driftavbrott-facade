package se.mdh.driftavbrott.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import se.mdh.driftavbrott.modell.Driftavbrott;

/**
 * En facade f�r att kunna h�mta information om driftavbrott fr�n en central
 * MDH-tj�nst.
 *
 * @author Dennis Lundberg
 * @version $Id: DriftavbrottFacade.java 49125 2018-02-20 09:15:04Z dlg01 $
 */
public class DriftavbrottFacade {
  /**
   * Den log som ska anv�ndas.
   */
  private static final Log log = LogFactory.getLog(DriftavbrottFacade.class);
  private static final String PROPERTIES_FILE = "se.mdh.driftavbrott.properties";
  private Properties properties;

  /**
   * Skapa en ny instans.
   */
  public DriftavbrottFacade() throws IOException {
    FileInputStream inputStream = null;
    try {
      URL resource = getClass().getClassLoader().getResource(PROPERTIES_FILE);
      if(resource == null) {
        throw new IOException("Hittade inte properties-filen '" + PROPERTIES_FILE + "' p� classpath.");
      }
      inputStream = FileUtils.openInputStream(new File(resource.getFile()));
      properties = new Properties();
      properties.load(inputStream);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * H�mta ett p�g�ende driftavbrott f�r en samling med kanaler.
   *
   * @param kanaler De kanaler som du �r intresserad av
   * @param system Det system som fr�gar om driftavbrott, dvs ditt system
   * @return Ett p�g�ende driftavbrott
   * @throws WebServiceException Om n�got g�r fel i kommunikationen med web servicen
   */
  public Driftavbrott getPagaendeDriftavbrott(final List<String> kanaler,
                                              final String system)
      throws WebServiceException {
    try {
      WebClient client = WebClient.create(properties.getProperty("se.mdh.driftavbrott.service.url"))
          .path("/driftavbrott/pagaende")
          .query("kanal", kanaler.toArray())
          .query("system", system);
      client.accept(MediaType.APPLICATION_XML);
      log.debug("Ska h�mta driftavbrott fr�n: "
                    + client.getCurrentURI().toString());
      return client.get(Driftavbrott.class);
    }
    catch(WebApplicationException wae) {
      // Hantera standard JAX-RS exception
      if(wae.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
        log.info("Det finns inga driftavbrott f�r n�gon av kanalerna " + kanaler + ".");
        return null;
      }
      else {
        String message = "Det gick inte att h�mta driftavbrott f�r kanalerna " + kanaler + " (JAX-RS).";
        log.error(message, wae);
        throw new WebServiceException("", wae);
      }
    }
    catch(Throwable t) {
      // Hantera ok�nt fel
      String message = "Det gick inte att h�mta driftavbrott f�r kanalerna " + kanaler + " (ok�nt fel).";
      log.error(message, t);
      throw new WebServiceException(message, t);
    }
  }
}
