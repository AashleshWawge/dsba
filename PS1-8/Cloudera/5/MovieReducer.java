import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class MovieReducer extends MapReduceBase implements Reducer<Text, FloatWritable, Text, FloatWritable> {

    public void reduce(Text key, Iterator<FloatWritable> values, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {
        float sum = 0;
        int count = 0;

        while (values.hasNext()) {
            sum += values.next().get();
            count++;
        }
	// Calculate the average rating for the movie
        float averageRating = sum / count;
        output.collect(key, new FloatWritable(averageRating));
    }
}
