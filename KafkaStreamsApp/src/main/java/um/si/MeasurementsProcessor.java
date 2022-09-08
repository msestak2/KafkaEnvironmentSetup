package um.si;


import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.*;
public class MeasurementsProcessor {
    private static Properties setupApp() {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ProcessMeasurements");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName());
        props.put("schema.registry.url", "http://0.0.0.0:8081");
        props.put("input.topic.name", "measurements-o");
        props.put("default.deserialization.exception.handler", "org.apache.kafka.streams.errors.LogAndContinueExceptionHandler");

        return props;
    }

    public static void main(String[] args) throws Exception {

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                "http://0.0.0.0:8081");

        final Serde<GenericRecord> valueGenericAvroSerde = new GenericAvroSerde();
        valueGenericAvroSerde.configure(serdeConfig, false);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, GenericRecord> inputStreamO = builder.stream("measurements-o", Consumed.with(Serdes.String(), valueGenericAvroSerde));

        inputStreamO.map((k,v)->new KeyValue<>(v.get("stationLocation").toString(),v.get("dateFrom").toString()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count().toStream().mapValues(value -> value.toString())
                //.print(Printed.toSysOut());
                .to("measurements-o-date", Produced.with(Serdes.String(), Serdes.String()));


        KStream<String, GenericRecord> inputStreamPM = builder.stream("measurements-pm", Consumed.with(Serdes.String(), valueGenericAvroSerde));
        KTable<String, Integer> maxPM =
                inputStreamPM
                        .map((k, v) -> new KeyValue<>(v.get("stationLocation").toString(), Integer.valueOf(v.get("pm10").toString())))
                        .groupByKey(Grouped.with(Serdes.String(), Serdes.Integer())).reduce(Integer::max)
                        .filter((k,v)->Integer.valueOf(v) > 10)
                        .toStream().toTable();
        maxPM.toStream().print(Printed.toSysOut());

        KStream<String, GenericRecord> joined = inputStreamO.join(inputStreamPM,
                (leftValue, rightValue) -> leftValue, /* ValueJoiner */
                JoinWindows.of(Duration.ofMinutes(5)),
                Joined.with(
                        Serdes.String(), /* key */
                        valueGenericAvroSerde,   /* left value */
                        valueGenericAvroSerde  /* right value */
        ));
        joined.print(Printed.toSysOut());
        joined.to("joined-streams");


        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, setupApp());
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}
