/**
 * 
 */
package alphabetsoup.simulators.greedytaskallocation;

import java.util.*;

import alphabetsoup.framework.Letter;
import alphabetsoup.framework.LetterStation;
import alphabetsoup.framework.SimulationWorld;
import alphabetsoup.framework.Updateable;
import alphabetsoup.framework.Word;
import alphabetsoup.framework.WordStation;
/**Basic simple implementation of a Letter Manager, which dispenses incoming letters to Letter
 * stations as they have space. The Letter Manager notified when a new Word is assigned to a WordStation.
 * @author Chris Hazard
 */
public class LetterManager implements Updateable {
	
	//letters that have been requested but haven't been dispensed
	protected List<Letter> requiredLetters = new ArrayList<Letter>();
	
	//letters that have been dispensed that haven't yet been requested
	protected List<Letter> surplusLetters = new ArrayList<Letter>();

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) {
		return Double.POSITIVE_INFINITY;
	}
	
	/**Called by the system when a new word has been assigned to a Wordstation.
	 * It is useful as it indicates what new letters need to be filled in the system (based on the word).
	 * @param s WordStation the Word was assigned to
	 * @param w Word assigned
	 */
	public void newWordAssignedToStation(WordStation s, Word w) {
		//for every letter in the word
		for(Letter l : w.getOriginalLetters()) {
			
			//see if the letter has already been put into the system as surplus
			boolean has_been_added = false;
			for(Letter m : surplusLetters) {
				//if the letter has already been dispensed, then it's not surplus anymore
				if(l.doesMatch(m)) {
					surplusLetters.remove(m);
					has_been_added = true;
					break;
				}
			}
			
			//don't add a letter that's already been added
			if(has_been_added)
				continue;
			
			requiredLetters.add(l.clone());
			int bundle_size = SimulationWorld.getSimulationWorld().letterStations[0].getBundleSize();
			for(int i = 1; i < bundle_size; i++)
				surplusLetters.add(l.clone());
		}
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) {
		for(LetterStation s : SimulationWorldGreedyTaskAllocation.getSimulationWorld().getLetterStations()) {
			//give the station letters if it needs them
			if(s.getAssignedLetters().size() < s.getCapacity()) {

				//create letters randomly based on the distribution 
				//s.addLetter(SimulationWorld.simulationWorld.wordList.generateRandomLetter());
				
				//create letters based on need
				if(requiredLetters.size() > 0) {
					Letter l = requiredLetters.remove(0);
					//add the letter to the station
					s.addBundle(l);
					SimulationWorldGreedyTaskAllocation.getSimulationWorld().bucketbotManager.newLetterBundleAssignedToStation(l, s);
				}
			}
		}
	}
}
