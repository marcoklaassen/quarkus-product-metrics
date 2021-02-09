package click.klaassen;

import io.quarkus.scheduler.Scheduled;
import java.util.List;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.Gauge;

import static java.lang.String.*;

@ApplicationScoped
@Slf4j
public class ProductMetricsService {

  @ConfigProperty(name = "metrics.name")
  String metricsName;

  @ConfigProperty(name = "metrics.products")
  List<String> products;

  @ConfigProperty(name = "metrics.locations")
  List<String> locations;

  @ConfigProperty(name = "metrics.increase.min")
  long min;

  @ConfigProperty(name = "metrics.increase.max")
  long max;

  @Inject MetricRegistry registry;

  @Scheduled(every = "10s")
  void updateProductValues() {
    var numberGenerator = new RandomDataGenerator();
    products.forEach(
        product -> {
          var location = locations.get(numberGenerator.nextInt(0, locations.size() - 1));
          var size = numberGenerator.nextLong(min, max);
          var fullMetricsName = format("%s.%s", this.getClass().getCanonicalName(), metricsName);
          registry
              .meter(fullMetricsName, new Tag("product", product), new Tag("location", location))
              .mark(size);
          log.info("add new metric {}: [{}:{} - {}]", fullMetricsName, product, size, location);
        });
  }

  @Scheduled(every = "5s")
  void checkAvailability() {
    availability();
  }

  @Gauge(unit = MetricUnits.NONE)
  long availability() {
    var available = (new Random().nextInt(4) == 0) ? 0 : 1;
    log.info("update availability: [{}]", available == 1);
    return available;
  }
}
