import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Producer {
    private final static String TOPIC1 = "measurements-pm";
    private final static String TOPIC2 = "measurements-o";

    private static KafkaProducer createProducer() {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "MeasurementProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());

        // Configure the KafkaAvroSerializer.
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Schema Registry location.
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        return new KafkaProducer(props);
    }

    private static ProducerRecord<Object,Object> generateRecordPM(Schema schema) throws ParseException {
        Random rand = new Random();
        GenericRecord avroRecord = new GenericData.Record(schema);


        Map<Integer, Station> stations = new HashMap<Integer, Station>();
        List<String> locations = Arrays.asList("Kranj", "MB Titova", "Ptuj", "MB Vrbanski", "CE Ljubljanska", "MS Rakičan", "Solkan", "NG Grčna", "Koper", "Trbovlje", "Zagorje");

        for(int i = 0; i < locations.size(); i++){
            String stationId = "E" + rand.nextInt((100 - 0 + 1));
            stations.put(i, new Station(stationId, String.valueOf((Math.random()*(100 - 0 + 1) + 1)/10.0), String.valueOf((Math.random()*(100 - 0 + 1) + 1)/10.0),
                    locations.get(i)));
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date dateFrom = dateFormat.parse("2021");
        long timestampFrom = dateFrom.getTime();
        Date dateTo = dateFormat.parse("2022");
        long timestampTo = dateTo.getTime();
        Random random = new Random();
        long timeRange = timestampTo - timestampFrom;
        long randomTimestamp = timestampFrom + (long) (random.nextDouble() * timeRange);

        Station selectedStation = stations.get(rand.nextInt(stations.size()));
        avroRecord.put("stationId",selectedStation.getStationId());
        avroRecord.put("latitude",selectedStation.getLatitude());
        avroRecord.put("longitude",selectedStation.getLongitude());
        avroRecord.put("stationLocation",selectedStation.getStationLocation());
        avroRecord.put("dateFrom", randomTimestamp);
        avroRecord.put("dateUntil", randomTimestamp + TimeUnit.HOURS.toMillis(1));
        avroRecord.put("pm10",Integer.valueOf(rand.nextInt((30-1)) + 1));

        ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(TOPIC1, selectedStation.getStationId(), avroRecord);
        return producerRecord;
    }

    private static ProducerRecord<Object,Object> generateRecordO(Schema schema) throws ParseException {
        Random rand = new Random();
        GenericRecord avroRecord = new GenericData.Record(schema);


        Map<Integer, Station> stations = new HashMap<Integer, Station>();
        List<String> locations = Arrays.asList("Kranj", "MB Titova", "Ptuj", "MB Vrbanski", "CE Ljubljanska", "MS Rakičan", "Solkan", "NG Grčna", "Koper", "Trbovlje", "Zagorje");

        for(int i = 0; i < locations.size(); i++){
            String stationId = "E" + rand.nextInt((100 - 0 + 1));
            stations.put(i, new Station(stationId, String.valueOf((Math.random()*(100 - 0 + 1) + 1)/10.0), String.valueOf((Math.random()*(100 - 0 + 1) + 1)/10.0),
                    locations.get(i)));
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date dateFrom = dateFormat.parse("2021");
        long timestampFrom = dateFrom.getTime();
        Date dateTo = dateFormat.parse("2022");
        long timestampTo = dateTo.getTime();
        Random random = new Random();
        long timeRange = timestampTo - timestampFrom;
        long randomTimestamp = timestampFrom + (long) (random.nextDouble() * timeRange);

        Station selectedStation = stations.get(rand.nextInt(stations.size()));
        avroRecord.put("stationId",selectedStation.getStationId());
        avroRecord.put("latitude",selectedStation.getLatitude());
        avroRecord.put("longitude",selectedStation.getLongitude());
        avroRecord.put("stationLocation",selectedStation.getStationLocation());
        avroRecord.put("dateFrom", randomTimestamp);
        avroRecord.put("dateUntil", randomTimestamp + TimeUnit.HOURS.toMillis(1));

        avroRecord.put("co",Integer.valueOf(rand.nextInt((100 - 0 + 1)) + 1));
        avroRecord.put("o3",Integer.valueOf(rand.nextInt((50-1)) + 1));
        avroRecord.put("no2",Integer.valueOf(rand.nextInt((40-1)) + 1));

        ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(TOPIC2, selectedStation.getStationId(), avroRecord);
        return producerRecord;
    }


    public static void main(String[] args) throws Exception {
        // avro schema avsc file path.
        String schema1Path = "measurements_pm.avsc";
        String schema2Path = "measurements_o.avsc";

        String schema1 = null, schema2 = null;

        KafkaProducer producer = createProducer();

        InputStream inputStream1 = Producer.class.getResourceAsStream(schema1Path);
        try {
            schema1 = new BufferedReader(new InputStreamReader(inputStream1, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } finally {
            inputStream1.close();
        }

        InputStream inputStream2 = Producer.class.getResourceAsStream(schema2Path);
        try {
            schema2 = new BufferedReader(new InputStreamReader(inputStream2, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } finally {
            inputStream2.close();
        }


        Schema avroSchema1 = new Schema.Parser().parse(schema1);
        Schema avroSchema2 = new Schema.Parser().parse(schema2);


        while(true){
            ProducerRecord record = generateRecordPM(avroSchema1);
            producer.send(record);

            //System.out.println("Input data: " + web3ClientVersion.getRawResponse());
            System.out.println("[RECORD] Sent new measurement PM for station with ID: " + record.key().toString());

            Thread.sleep(2000);

            record = generateRecordO(avroSchema2);
            producer.send(record);

            //System.out.println("Input data: " + web3ClientVersion.getRawResponse());
            System.out.println("[RECORD] Sent new measurement O for station with ID: " + record.key().toString());

            Thread.sleep(5000);
        }
    }

}
