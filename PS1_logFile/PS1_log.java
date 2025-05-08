package HadoopLogFileProcessor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Log {
	public static class LogMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy H:mm");
        private Text userId = new Text();
        private IntWritable duration = new IntWritable();
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            try {
                String[] fields = value.toString().split(",");
                if (fields.length >= 8) {
                    String uid = fields[0].trim();
                    String loginTimeStr = fields[5].trim();
                    String logoutTimeStr = fields[7].trim();
                    Date loginTime = dateFormat.parse(loginTimeStr);
                    Date logoutTime = dateFormat.parse(logoutTimeStr);
                    long diffMillis = logoutTime.getTime() - loginTime.getTime();
                    int diffMinutes = (int) (diffMillis / (1000 * 60));

                    if (diffMinutes > 0) {
                    	userId.set(uid);
                        duration.set(diffMinutes);
                        context.write(userId, duration);
                    }
                }
            } catch (Exception e) {
                // Skip invalid entries
            }
        }
    }

    public static class MaxReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    	private IntWritable totalTime = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int total = 0;
            for (IntWritable val : values) {
                total += val.get();
            }
            totalTime.set(total);
            context.write(key, totalTime);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Max Login Duration");

        job.setJarByClass(Log.class);
        job.setMapperClass(LogMapper.class);
        job.setReducerClass(MaxReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
            



