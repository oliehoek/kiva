/**
 * 
 */
package alphabetsoup.base;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Bucketbot;
import alphabetsoup.framework.LetterStation;
import alphabetsoup.framework.SimulationWorld;
import alphabetsoup.framework.Word;
import alphabetsoup.framework.WordList;
import alphabetsoup.framework.WordStation;

/**SummaryReport is a class of static functions to generate reports of statistics and simulation data.
 * @author Chris Hazard
 */
public class SummaryReport {
	
	/**Generates a report as a List of Strings.
	 * @return a List of Strings in sequential order
	 */
	static public List<String> generateReportText(SimulationWorld sw) {
		//give 4 significant digits
		DecimalFormat four_digits = new DecimalFormat("0.000");
		
		List<String> lines = new ArrayList<String>();
		Bucketbot[] robots = sw.bucketbots;
		double elapsed_time = sw.getCurrentTime() - sw.getStatisticsTime();
		
		lines.add("total time: " + four_digits.format(sw.getCurrentTime()));
		lines.add("current statistics time: " + four_digits.format(elapsed_time));
		
		double total_distance = 0.0;
		for(Bucketbot r : robots) total_distance += r.getDistanceTraveled();
		lines.add("total distance: " + four_digits.format(total_distance));
		
		int num_pickups = 0;
		for(Bucketbot r : robots) num_pickups += r.getNumPickups();
		lines.add("pickups: " + num_pickups);
		
		int num_setdowns = 0;
		for(Bucketbot r : robots) num_setdowns += r.getNumSetdowns();
		lines.add("setdowns: " + num_setdowns);
		
		int num_collisions = 0;
		for(Bucketbot r : robots) num_collisions += r.getNumCollisions();
		lines.add("collisions: " + num_collisions);
		
		//get all robot states
		HashSet<String> states = new HashSet<String>();
		for(Bucketbot r : robots) for(String s : r.getTotalTimes().keySet()) states.add(s);
		
		//write robot state times
		for(String s : states) {
			double time_in_state = 0.0;
			for(Bucketbot r : robots)
				if(r.getTotalTimes().containsKey(s))
					time_in_state += r.getTotalTimes().get(s);
			lines.add("% in " + s + ": " + four_digits.format( (time_in_state / robots.length / elapsed_time) * 100.0)  );
		}

		WordList wl = sw.wordList;
		lines.add("completed words: " + wl.getCompletedWords().size());
		lines.add("word completion rate: " + four_digits.format(wl.getCompletedWords().size() / elapsed_time) );
		int num_letters_completed = 0;
		for(Word w : wl.getCompletedWords()) num_letters_completed += w.getOriginalLetters().length;
		lines.add("completed letters: " + num_letters_completed);
		lines.add("letter completion rate: " + four_digits.format(num_letters_completed / elapsed_time) );
		
		int num_letters_requested = 0;
		double ave_idle_time = 0.0;
		for(LetterStation ls : sw.letterStations) {
			num_letters_requested += ls.getNumLettersRequested();
			ave_idle_time += ls.getIdleTime();
		}
		lines.add("average letters requested per letter station: "
				+ four_digits.format((float)num_letters_requested / sw.letterStations.length) );
		lines.add("average letter station idle time: "
				+ four_digits.format( ave_idle_time / sw.letterStations.length / elapsed_time) );
		
		num_letters_requested = 0;
		ave_idle_time = 0.0;
		for(WordStation ws : sw.wordStations) {
			num_letters_requested += ws.getNumLettersRequested();
			ave_idle_time += ws.getIdleTime();
		}
		lines.add("average letter transfer requests per word station: "
				+ four_digits.format((float)num_letters_requested / sw.wordStations.length) );
		lines.add("average word station idle time: "
				+ four_digits.format(ave_idle_time / sw.wordStations.length / elapsed_time) );
		
		double ave_bucket_capacity = 0.0;
		for(Bucket b : sw.buckets) {
			ave_bucket_capacity += (double)b.getLetters().size() / b.getCapacity();
		}
		lines.add("bucket utilization: " + four_digits.format(ave_bucket_capacity / sw.buckets.length) );

		return lines;
	}

	/**Writes a report to AlphabetSoupReport.txt
	 */
	static public void generateReport(SimulationWorld sw) {
		FileWriter outfile;
		PrintWriter outf;
		try {
			outfile = new FileWriter("AlphabetSoupReport.txt");
			outf = new PrintWriter(outfile);
		}
		catch (Throwable e) {
			System.out.println("Could not open file AlphabetSoup.txt");
			return;
		}
		
		for(String s : generateReportText(sw))
			outf.println(s);
		outf.close();
	}
}
