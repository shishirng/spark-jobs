import javafx.util.Duration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by shishir on 12/06/17.
 */
public class SparkStreaming {


    static public void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("mesos://10.144.102.128:5050").setAppName("SparkStreaming");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
        JavaDStream<String> words = lines.flatMap(
                new FlatMapFunction<String, String>() {
                    @Override public Iterator<String> call(String x) {
                        return Arrays.asList(x.split(" ")).iterator();
                    }
                });

        JavaPairDStream<String, Integer> pairs = words.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override public Tuple2<String, Integer> call(String s) {
                        return new Tuple2<>(s, 1);
                    }
                });
        JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(
                new Function2<Integer, Integer, Integer>() {
                    @Override public Integer call(Integer i1, Integer i2) {
                        return i1 + i2;
                    }
                });

// Print the first ten elements of each RDD generated in this DStream to the console
        wordCounts.print();
        jssc.start();
        try {
            jssc.awaitTermination();
        }catch (InterruptedException ex) {

        }
    }
}
