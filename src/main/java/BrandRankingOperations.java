import com.opencsv.CSVReader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class BrandRankingOperations {
    public static void main(String[] args) {
        ClassLoader loader = BrandRankingOperations.class.getClassLoader();
        File file = new File(Objects.requireNonNull(loader.getResource("BrandRanking.csv")).getFile());

        try (JedisPool pool = new JedisPool("localhost", 6379)) {

            try (CSVReader csvReader = new CSVReader(new FileReader(file))) {
                String[] values;
                while ((values = csvReader.readNext()) != null) {
                    BrandRanking record = new BrandRanking(values);
                    try (Jedis jedis = pool.getResource()) {
                        jedis.set(record.brand, record.toString());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (Jedis jedis = pool.getResource()) {
                BrandRanking walmart = new BrandRanking(jedis.get("Walmart"));
                System.out.println(walmart);
                walmart.position = "1000";
                jedis.set("Walmart", walmart.toString());
                BrandRanking walmartUpdated = new BrandRanking(jedis.get("Walmart"));
                System.out.println(walmartUpdated);
            }
        }
    }

}
