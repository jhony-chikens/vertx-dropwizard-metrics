package io.vertx.ext.dropwizard.impl;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.test.core.VertxTestBase;
import org.junit.After;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:john.warner@ef.com">John Warner</a>
 */
public class VertxMetricFactoryImplTest extends VertxTestBase {

  private VertxMetrics metrics;

  @After
  public void after() throws Exception {
    if (metrics != null) {
      metrics.close();
      metrics = null;
    }
  }

  @Test
  public void testLoadingFromFile() throws Exception {
    String filePath = ClassLoader.getSystemResource("test_metrics_config.json").getFile();
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions().setConfigPath(filePath);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    // Verify our jmx domain isn't there already, just in case.
    assertFalse(Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains()).contains("test-jmx-domain"));

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertTrue(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testLoadingFromFileFromJson() throws Exception {
    String filePath = ClassLoader.getSystemResource("test_metrics_config.json").getFile();
    VertxOptions vertxOptions = new VertxOptions(new JsonObject().
        put("metricsOptions", new JsonObject().put("configPath", filePath)));

    // Verify our jmx domain isn't there already, just in case.
    assertFalse(Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains()).contains("test-jmx-domain"));

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertTrue(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testloadingFileFromClasspath() throws Exception {
    String path = "test_metrics_config.json";
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions().setConfigPath(path);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    // Verify our jmx domain isn't there already, just in case.
    assertFalse(Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains()).contains("test-jmx-domain"));

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertTrue(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testLoadingWithJmxDisabled() throws Exception {
    String filePath = ClassLoader.getSystemResource("test_metrics_config_jmx_disabled.json").getFile();
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions().setConfigPath(filePath);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    // If our file was loaded correctly, then our jmx domain will exist.
    assertFalse(jmxDomains.contains("test-jmx-domain"));
  }

  @Test
  public void testWithNoConfigFile() throws Exception {
    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions()
        .setJmxEnabled(true)
        .setJmxDomain("non-file-jmx");
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    assertFalse(jmxDomains.contains("test-jmx-domain"));
    assertTrue(jmxDomains.contains("non-file-jmx"));
  }

  @Test
  public void testLoadingWithMissingFile() throws Exception {
    String filePath = "/i/am/not/here/missingfile.json";

    DropwizardMetricsOptions dmo = new DropwizardMetricsOptions()
        .setJmxEnabled(true)
        .setJmxDomain("non-file-jmx")
        .setConfigPath(filePath);
    VertxOptions vertxOptions = new VertxOptions().setMetricsOptions(dmo);

    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    metrics = vmfi.metrics(vertx, vertxOptions);

    List<String> jmxDomains = Arrays.asList(ManagementFactory.getPlatformMBeanServer().getDomains());

    assertFalse(jmxDomains.contains("test-jmx-domain"));
    assertTrue(jmxDomains.contains("non-file-jmx"));
  }

  @Test
  public void testFromJson() throws Exception {
    VertxMetricsFactoryImpl vmfi = new VertxMetricsFactoryImpl();
    VertxMetricsImpl metrics = (VertxMetricsImpl) vmfi.metrics(vertx, new VertxOptions(
        new JsonObject().put("metricsOptions", new JsonObject().put("enabled", true).put("monitoredClientUris", new JsonArray().add(new JsonObject().put("value", "http://www.foo.com"))))
    ));
    DropwizardMetricsOptions options = metrics.getOptions();
    assertEquals(1, options.getMonitoredHttpClientUris().size());
    assertEquals("http://www.foo.com", options.getMonitoredHttpClientUris().get(0).getValue());
    assertEquals(MatchType.EQUALS, options.getMonitoredHttpClientUris().get(0).getType());
  }
}
