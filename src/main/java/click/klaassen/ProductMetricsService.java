package click.klaassen;

import io.quarkus.scheduler.Scheduled;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;

@ApplicationScoped
@Slf4j
public class ProductMetricsService {

  @ConfigProperty(name = "metrics.products")
  List<String> products;

  @ConfigProperty(name = "metrics.locations")
  List<String> locations;

  @Inject MetricRegistry registry;

  private static final long MIN = 0;
  private static final long MAX = 100;

  @Scheduled(every = "1s")
  void increment() {
    var numberGenerator = new RandomDataGenerator();
    products.forEach(
        product -> {
          var location = locations.get(numberGenerator.nextInt(0, locations.size() - 1));
          var size = numberGenerator.nextLong(MIN, MAX);
          registry
              .meter("fruit", new Tag("product", product), new Tag("location", location))
              .mark(size);
          log.info("add new metric [{}:{} - {}]", product, size, location);
        });
  }
}
