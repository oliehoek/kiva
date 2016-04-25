
To run Alphabet Soup
The included library directory must be added to the VM to run with graphics (LWJGL),
as well as the library within it.  The parameters are;

-Djava.library.path=./libraries -classpath .;libraries/lwjgl.jar 

For running long simulations, it is useful to add the following parameters for speed (make sure you have the server
version of the JVM installed):
	-server
	-XX:CompileThreshold=100   (can try this...)

With Eclipse, -Djava.library.path=libraries should be added to the VM arguements, and lwjgl should be added
as a library in the Classpath.

Getting started:

To get started, copy either alphabetsoup.simulators.graphexample or alphabetsoup.simulators.simple example into a new package with
a name of your choice.  The 3 managers and their structure (WordManager, LetterManager, BucketbotManager),
is based on the authors' notion of the problem, but can easily be changed (including adding or removing managers).
SimulationWorldXXXExample is a good point to start if you wish to change the waypoint map, and also to make
sure everything is parameratized the way you intend it.

From there, Bucketbot and the managers follow.  Bucketbot is implemented
as with both a task and state queue.  Whenever a task is assigned, Bucketbot breaks the task into the state
sequence required to complete the task.  All of the states extend the inner class BucketbotBase$BucketbotState.
As BucektbotBase performs all of the physical interactions, the corresponding BucketbotBase$BucketbotState.act
function is called whenever BucketbotBase is not doing a blocking action such as picking up a bucket.

The other packages in AlphabetSoup are as follows:

-alphabetsoup.framework:
	This package contains the basic immutable entities of AlphabetSoup.
	It provides Map to keep track of locations and collisions, as well as interfaces for all of the
	"physical" entities, which include the Bucketbots, Buckets, WordStations, and LetterStations.
	Circle is an important class.  While it mostly provides basic obvious functionality for dealing with
	circles, it is very useful for abstractly dealing with circle physical entities (which is used
	very frequently in alphabetsoup.base).
	The Updateable interface should be implemented by anything that is dynamic (operates on its own).
	The Renderable niterface should be implemented by anything that should be drawn to the screen.
	Word, Letter, LetterColor, and WordList are all the objects that deal with words and word completion. 
	SimulationWorld provides basic utilities for implementing your own extension to kick off a simulation.
-alphabetsoup.base:
	This package provides the basic mechanics of AlphabetSoup.  All of the entities are implemented,
	but are completely passive -they will only act when they are told to.  Building a simulation only using
	alphabetsoup.base will technically run, but no actions will occur (nothing will happen).  This minimal
	base allows extensions to only extend what they need, keeping all other entities functional but passive.
	alphabetsoup.base also provides a basic task system for Bucketbots, which contains all of the basic tasks
	Bucketbots will likely perform.
-alphabetsoup.userinterface:
	The class in this package, RenderWindow, contains all of the code that sets up OpenGL and manages
	the user interface.  It has methods that allow Renderable objects to be tagged on to be rendered,
	without the need of modifying the class itself.
-alphabetsoup.waypointgraph:
	This packages contains the classes used to implement a basic waypoint system, such that bucketbots
	use the waypoints to find paths throughout the map.  These classes are used by graphexample and greedytaskallocation.
-alphabetsoup.simulators.simpleexample:
	This is an extremely simple, but fully functional implementation.  The Bucketbots simply go to their target.
	New words are added as the stations need them.  When a new letter is required, a LetterStation retrieves the
	letter.  Each task of transfering each letter to a WordStation is maintained, and are given to the bucketbots
	as they request them (become idle from finishing a previous task).  After every transaction takes place with
	a station, the bucketbots take their bucket to a storage location in the middle portion of the map, set it
	down, and request a new task.  LetterManagerExample and WordManagerExample are simple managers that record
	tasks, and give them to	BucketbotManagerExample, which delivers them to bucketbots.
	SimulationWorldSimpleExample sets up the default layout and kicks everything off.  SimulationWorldSimpleExample
	 uses alphabetsoup.config for configuration parameters.
-alphabetsoup.simulators.graphexample
	This example package is based on alphabetsoup.simpleexample, but also adds the functionality of a waypoint
	graph.  The WaypointGraph implements this functionality by maintaining a graph of Waypoint objects, and the
	Bucketbots traverse this graph using a simple A* algorithm (where bucketbots simply stop and start at each
	waypoint).  This helps the bucketbots navagate the buckets (so they don't always run into them), but the
	simple pathfinding dramatically increases congestion.  Better pathfinding and better graphs should
	greatly better the performance.  SimulationWorldGraphExample produces the initial waypoint graph.
-alphabetsoup.simulators.graphexample
	This example packages is similar to graphexample, but uses a thoroughly greedy heuristics.

	