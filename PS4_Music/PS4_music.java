package Musical;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class Musical {
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable played = new IntWritable(1);
		private final static IntWritable skipped = new IntWritable(-1);
		private Text trackID = new Text();
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
		    
		    // Skip the header row (column names)
		    if (line.contains("UserId") && line.contains("TrackId") && line.contains("Shared") && line.contains("Radio") && line.contains("Skip")) {
		        return; // Skip this line if it's the header
		    }
            String[] fields = value.toString().split(",");
            if (fields.length == 5) {
                trackID.set(fields[1]); // TrackId
                try {
                    // Check if "Radio" and "Skip" are numeric before parsing them
                	int radio = Integer.parseInt(fields[3]);
                    int skip = Integer.parseInt(fields[4]);

                    if (radio == 1) {
                        context.write(trackID, played);
                    }
                    if (skip == 1) {
                        context.write(trackID, skipped);
                    }
                } catch (NumberFormatException e) {
                    // Log the error and skip this record if there's an issue
                    System.err.println("Skipping invalid record: " + value.toString());
                }
            }
        }
	}
	
	public static class Reduce extends Reducer <Text, IntWritable, Text, IntWritable> {
		public IntWritable result = new IntWritable();
		
		 public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
	            int sum = 0;
	            for (IntWritable val : values) {
	                sum += val.get();
	            }
	            result.set(sum);
	            context.write(key, result);
	        }
	    }
	
	public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	   
	    Job job = new Job(conf, "Music 4");
	    job.setJarByClass(Musical.class);
	    job.setMapperClass(Map.class);
	    job.setCombinerClass(Reduce.class);
	    job.setReducerClass(Reduce.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  }
}



