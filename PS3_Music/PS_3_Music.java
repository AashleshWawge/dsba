package Musicc;

import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Musicc {
	 public static class Map extends Mapper<LongWritable, Text, Text, Text> {
	        private Text trackID = new Text();
	        private Text userShare = new Text();

	        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	            String line = value.toString();
	         // Skip the header row
	            if (line.contains("UserId") && line.contains("TrackId")) {
	                return;
	            }

	            String[] fields = line.split(",");
	            if (fields.length == 5) {
	            	String userId = fields[0];
	                String trackId = fields[1];
	                String shared = fields[2];

	                trackID.set(trackId);
	                userShare.set(userId + "," + shared);  // e.g., "111115,1"
	                context.write(trackID, userShare);
	            }
	        }
	    }

	    public static class Reduce extends Reducer<Text, Text, Text, Text> {
	        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	            HashSet<String> uniqueUsers = new HashSet<>();
	            int shareCount = 0;

	            for (Text val : values) {
	                String[] parts = val.toString().split(",");
	                if (parts.length == 2) {
	                    String userId = parts[0];
	                    String shared = parts[1];

	                    uniqueUsers.add(userId);
	                    if (shared.equals("1")) {
	                        shareCount++;
	                    }
	                }
	            }

	            String result = "UniqueListeners: " + uniqueUsers.size() + ", SharedCount: " + shareCount;
	            context.write(key, new Text(result));
	        }
	    }
	    public static void main(String[] args) throws Exception {
	        Configuration conf = new Configuration();

	        Job job = new Job(conf, "Music Unique Listeners and Shares");
	        job.setJarByClass(Musicc.class);
	        job.setMapperClass(Map.class);
	        job.setReducerClass(Reduce.class);

	        job.setOutputKeyClass(Text.class);
	        job.setOutputValueClass(Text.class);

	        FileInputFormat.addInputPath(job, new Path(args[0]));
	        FileOutputFormat.setOutputPath(job, new Path(args[1]));

	        System.exit(job.waitForCompletion(true) ? 0 : 1);
	    }
	}


