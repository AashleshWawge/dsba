import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MusicReducer extends Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String prefix = key.toString().substring(0, 2);
        String trackId = key.toString().substring(2);
	
	if (prefix.equals("U_")) {
            Set<String> uniqueUsers = new HashSet<>();
            for (Text val : values) {
                uniqueUsers.add(val.toString());
            }
            context.write(new Text(trackId), new Text("UniqueListeners: " + uniqueUsers.size()));
        }else if (prefix.equals("S_")) {
            int count = 0;
            for (Text val : values) {
                count += Integer.parseInt(val.toString());
            }
            context.write(new Text(trackId), new Text("SharedCount: " + count));
        }    
}
}
