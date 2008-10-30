/**
 * 
 */
package alphabetsoup.simulators.greedytaskallocation;

import alphabetsoup.framework.Updateable;
import alphabetsoup.framework.Word;
import alphabetsoup.framework.WordList;
import alphabetsoup.framework.WordStation;

/**Basic simple implementation of a WordOrderManager, which dispenses incoming words to Word
 * stations as they have space.
 * @author Chris Hazard
 */
public class WordOrderManager implements Updateable {

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#getNextEventTime(double)
	 */
	public double getNextEventTime(double cur_time) {
		return Double.POSITIVE_INFINITY;
	}

	/* (non-Javadoc)
	 * @see alphabetsoup.framework.Updateable#update(double, double)
	 */
	public void update(double last_time, double cur_time) 
	{
		// GET THE LIST OF WORD IN SIMULATION WORLD
		WordList wl = SimulationWorldGreedyTaskAllocation.getSimulationWorld().getWordList();
		LetterManager lm = ((LetterManager)((SimulationWorldGreedyTaskAllocation)SimulationWorldGreedyTaskAllocation.getSimulationWorld()).letterManager);
		
		//see if all done
		if(wl.getAvailableWords().size() == 0) return;
		
		// IF THERE ARE INCOMPLETE WORDS IN THE WORD LIST, ASSIGN THEM TO STATION THAT IS BELOW CAPACITY 
		for(WordStation s : SimulationWorldGreedyTaskAllocation.getSimulationWorld().getWordStations()) 
		{
			//give the station words if it needs them
			// ASSIGN WORD TO STATION THAT IS BELOW CAPACITY (NUMBER OF BOXES THAT A PICKER CAN HOLD
			if(s.getAssignedWords().size() < s.getCapacity()) 
			{
				Word w = wl.takeAvailableWord(0);
				s.assignWord(w);					// ASSIGN WORD TO THE STATION THAT IS BELOW CAPACITY 
				// WHY DO NEED TO ASSIGN TO BUCKET BOT MANAGER???
				SimulationWorldGreedyTaskAllocation.getSimulationWorld().bucketbotManager.newWordAssignedToStation(w, s);
				lm.newWordAssignedToStation(s, w);
			}
			
			//can't continue if out of words
			if(wl.getAvailableWords().size() == 0) return;
		}
	}
}
