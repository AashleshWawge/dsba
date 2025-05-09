import java.io.IOException;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;


public class MovieMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, FloatWritable> {

    public void map(LongWritable key, Text value, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {
        String line = value.toString();
	// Skip empty lines or the header row
        if (line.trim().isEmpty() || line.startsWith("userId")) {
            return;
        }
	 String[] fields = line.split(",");
        if (fields.length < 3) {
            return; // Skip malformed lines
        }

        try {
            String movieId = fields[1];
            float rating = Float.parseFloat(fields[2]);
            output.collect(new Text(movieId), new FloatWritable(rating));
        } catch (NumberFormatException e) {
            // Handle lines where rating is not a valid float
            return;
        }
    }
}
