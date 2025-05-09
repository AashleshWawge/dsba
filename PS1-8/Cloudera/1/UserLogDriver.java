import java.io.IOException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class UserLogDriver extends Configured implements Tool {
    public int run(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Please provide input and output paths");
            return -1;
        }
	JobConf conf = new JobConf(UserLogDriver.class);
        conf.setJobName("IP Log Count");

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        conf.setMapperClass(UserLogMapper.class);
        conf.setReducerClass(UserLogReducer.class);

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        JobClient.runJob(conf);
        return 0;
	}

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new UserLogDriver(), args);
        System.out.println(exitCode);
    }
}
