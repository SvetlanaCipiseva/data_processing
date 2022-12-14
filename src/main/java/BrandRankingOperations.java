import com.opencsv.CSVReader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BrandRankingOperations {

    public static void main(String[] args) {
        //Connecting to Redis Database with Jedis client
        try (JedisPool pool = new JedisPool("localhost", 6379)) {
            try (Jedis jedis = pool.getResource()) {
                jedis.flushAll(); //Deleting everything from Data Base

                //1. Izveidot skriptu, kas datu bāzē ievieto vismaz 500 ierakstus (var arī no faila).
                System.out.println("1. Izveidot skriptu, kas datu bāzē ievieto vismaz 500 ierakstus\n");
                //Uploading records to DB from csv file
                List<String> keys = create(jedis, getCsvFileByName("BrandRanking"));
                //Checking that 500 records in Data Base
                assert jedis.dbSize() == 500 : "Unexpected number of rows in Database";

                //2. Izveidot skriptu, kas maina vairākus ierakstus (10-100).
                System.out.println("2. Izveidot skriptu, kas maina vairākus ierakstus (10-100).\n");
                //Updating brand value for brands form 100 till 200 position
                update(jedis, keys.subList(100, 200));
                //Checking that 500 records in Data Base
                assert jedis.dbSize() == 500 : "Unexpected number of rows in Database";

                //3. Izveidot skriptu, kas atlasa vairākus ierakstus (10-100).
                System.out.println("3. Izveidot skriptu, kas atlasa vairākus ierakstus (10-100).\n");
                //Selecting 10 records with mget() command
                System.out.println("10 records selected with mget() command: " + jedis.mget("Apple", "Amazon"
                        , "Google", "Microsoft", "Walmart", "Samsung Group"
                        , "Facebook", "ICBC", "Huawei", "Verizon") + "\n");
                //Selecting records, which contains 's' letter in brand name
                System.out.println("All records selected which contains 's' letter in brand name: "
                        + jedis.mget(jedis.keys("*s*").toArray(new String[0])) + "\n");

                //4. Izveidot skriptu, kas dzēš vairākus ierakstus (10-100).
                System.out.println("4. Izveidot skriptu, kas dzēš vairākus ierakstus (10-100).\n");
                // All records for brands with names starting with 'A' will be deleted
                String pattern = "A*";
                int recordCountWithPattern = jedis.keys(pattern).size();
                deleteRecordsByKeyPattern(jedis, pattern);
                //Checking that records with pattern have been deleted from Data Base
                assert jedis.keys(pattern).size() == 0 : "Records with key '" + pattern + "' exist";
                assert jedis.dbSize() == (500 - recordCountWithPattern) : "Unexpected number of rows in Database";

            }
        }
    }

    public static File getCsvFileByName(String name) {
        ClassLoader loader = BrandRankingOperations.class.getClassLoader();
        return new File(Objects.requireNonNull(loader.getResource(name + ".csv")).getFile());
    }

    public static List<String> create(Jedis jedis, File file) {
        List<String> keys = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(file))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                BrandRanking record = new BrandRanking(values);
                jedis.set(record.brand, record.toString());
                keys.add(record.brand);
            }
            System.out.println("Database is created. Records count: " + jedis.dbSize() + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    public static void update(Jedis jedis, List<String> keys) {
        Random random = new Random();
        int i = 1;
        for (String key : keys) {
            BrandRanking brandRanking = new BrandRanking(jedis.get(key));
            System.out.println(i + " Record before update: " + brandRanking);
            brandRanking.brandValue = String.valueOf(random.nextInt(2000000));
            brandRanking.previousBrandValue = String.valueOf(random.nextInt(2000000));
            brandRanking.rating = "AAA";
            brandRanking.previousRating = "AA";
            jedis.set(key, brandRanking.toString());
            System.out.println(i + " Record after update: " + new BrandRanking(jedis.get(key)));
            i++;
        }
        System.out.println("\nDatabase is updated (4 attributes). Records count: " + keys.size() + "\n");
    }

    public static void deleteRecordsByKeyPattern(Jedis jedis, String pattern) {
        Set<String> keys = jedis.keys(pattern);
        if (keys.size() == 0) {
            System.out.println("Records with '" + pattern + "' pattern do not exist");
            return;
        }
        System.out.println(keys.size() + " records with keys pattern: '" + pattern + "' deleted");
        for (String key : keys) {
            jedis.del(key);
        }
        System.out.println("Data base contains " + jedis.dbSize() + " records");
    }

}
