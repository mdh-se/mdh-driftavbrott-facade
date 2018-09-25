package se.mdh.driftavbrott.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
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
    InputStream inputStream = null;
    try {
      inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
      if(inputStream == null) {
        throw new IOException("Hittade inte properties-filen '" + PROPERTIES_FILE + "' p� classpath.");
      }
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
    return getPagaendeDriftavbrott(kanaler, system, 0);
  }

  /**
   * H�mta ett p�g�ende driftavbrott f�r en samling med kanaler.
   *
   * @param kanaler De kanaler som du �r intresserad av
   * @param system Det system som fr�gar om driftavbrott, dvs ditt system
   * @param marginal Marginaler f�r driftavbrottet i minuter
   * @return Ett p�g�ende driftavbrott
   * @throws WebServiceException Om n�got g�r fel i kommunikationen med web servicen
   */
  public Driftavbrott getPagaendeDriftavbrott(final List<String> kanaler,
                                              final String system,
                                              final int marginal)
      throws WebServiceException {
    String url = "";
    try {
      WebClient client = WebClient.create(properties.getProperty("se.mdh.driftavbrott.service.url"))
          .path("/driftavbrott/pagaende")
          .query("kanal", kanaler.toArray())
          .query("system", system)
          .query("marginal", marginal);

//    M�ste acceptera json och text/html f�r fel som inte tj�nsten klarar av att returnera i xml-format, t.ex. 400 (text/html) och 404 (json).
      client.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML);

      url = client.getCurrentURI().toString();

      log.debug("Ska h�mta driftavbrott fr�n: "
                    + url);
      return client.get(Driftavbrott.class);
    }
    catch(WebApplicationException wae) {
        String message = "Det gick inte att h�mta driftavbrott f�r kanalerna " + kanaler + " (url = " + url + "). Statuskod " + wae.getResponse().getStatus();
        log.error(message, wae);
        throw new WebServiceException("", wae);
    }
    catch(Throwable t) {
      // Hantera ok�nt fel
      String message = "Det gick inte att h�mta driftavbrott f�r kanalerna " + kanaler + " (ok�nt fel).";
      log.error(message, t);
      throw new WebServiceException(message, t);
    }
  }
}
