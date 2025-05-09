import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class MusicMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
    public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter rep) throws IOException {
        // Ignore the header row
        String line = value.toString();
        if (line.contains("UserId")) return; // Skip header

        String[] fields = line.split(",");
	// Extract TrackId, Radio and Skip columns
        String trackId = fields[1];
        int radio = Integer.parseInt(fields[3]);
        int skip = Integer.parseInt(fields[4]);

        // Emit (trackId_radio) and (trackId_skip)
        output.collect(new Text(trackId + "_radio"), new IntWritable(radio));
        output.collect(new Text(trackId + "_skip"), new IntWritable(skip));
    }
}
