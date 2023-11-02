package se.mdh.driftavbrott.facade;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import se.mdh.driftavbrott.modell.Driftavbrott;

/**
 * En facade för att kunna hämta information om driftavbrott från en central
 * tjänst.
 *
 * @author Dennis Lundberg
 * @version $Id: DriftavbrottFacade.java 49125 2018-02-20 09:15:04Z dlg01 $
 */
public class DriftavbrottFacade {
  /**
   * Den log som ska användas.
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
        throw new IOException("Hittade inte properties-filen '" + PROPERTIES_FILE + "' på classpath.");
      }
      properties = new Properties();
      properties.load(inputStream);
    }
    finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Hämta ett pågående driftavbrott för en samling med kanaler.
   *
   * @param kanaler De kanaler som du är intresserad av
   * @param system Det system som frågar om driftavbrott, dvs ditt system
   * @return Ett pågående driftavbrott
   * @throws WebApplicationException Om något går fel i kommunikationen med web servicen
   */
  public Driftavbrott getPagaendeDriftavbrott(final List<String> kanaler,
                                              final String system)
      throws WebApplicationException {
    return getPagaendeDriftavbrott(kanaler, system, 0);
  }

  /**
   * Hämta ett pågående driftavbrott för en samling med kanaler.
   *
   * @param kanaler De kanaler som du är intresserad av
   * @param system Det system som frågar om driftavbrott, dvs ditt system
   * @param marginal Marginaler för driftavbrottet i minuter
   * @return Ett pågående driftavbrott
   * @throws WebApplicationException Om något går fel i kommunikationen med web servicen
   */
  public Driftavbrott getPagaendeDriftavbrott(final List<String> kanaler,
                                              final String system,
                                              final int marginal)
      throws WebApplicationException {
    String url = "";
    try {
      WebClient client = WebClient.create(properties.getProperty("se.mdh.driftavbrott.service.url"))
          .path("/driftavbrott/pagaende")
          .query("kanal", kanaler.toArray())
          .query("system", system)
          .query("marginal", marginal);

      // Måste acceptera json och text/html för fel som inte tjänsten klarar av att returnera i xml-format, t.ex. 400 (text/html) och 404 (json).
      client.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML);

      url = client.getCurrentURI().toString();

      log.debug("Ska hämta driftavbrott från: "
                    + url);
      return client.get(Driftavbrott.class);
    }
    catch(WebApplicationException wae) {
      String message = "Det gick inte att hämta driftavbrott för kanalerna " + kanaler + " (url = " + url + "). Statuskod " + wae.getResponse().getStatus();
      log.error(message, wae);
      throw new WebApplicationException(message, wae);
    }
    catch(Throwable t) {
      // Hantera okänt fel
      String message = "Det gick inte att hämta driftavbrott för kanalerna " + kanaler + " (okänt fel).";
      log.error(message, t);
      throw new WebApplicationException(message, t);
    }
  }
}
