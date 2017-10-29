def radioStationFile = new File("./RadioStations.txt")
if (!radioStationFile.exists()) {
	println("Error: could not find radio station file RadioStations.txt in current folder. Exiting...")
	System.exit(1)	
}

def stationMap = [:]
String currStation

// Initialize station file
radioStationFile.eachLine{line ->
	if (line.startsWith('//')) {
		return
	}
 
	if (line.startsWith('#')) {
		currStation = line.substring(1).trim()
		stationMap.put(currStation, new ArrayList<String>())
	} else if (line.trim().length() != 0) {
		stationMap.get(currStation).add(line)
	}
}

def cli = new CliBuilder(usage: 'Play.groovy -[command]')
cli.with {
	l longOpt: 'list', 'List available stations'
	p longOpt: 'play', 'Play the specified station', args : 1
	h longOpt: 'help', 'Display usage information'
	k longOpt: 'kill', 'Kill the currently playing station'
}

def options = cli.parse(args)

if (!options || options.h) {
	cli.usage()
	return
}

if (options.k) {
	killStream()
	return
}

if (options.l) {
	printStations(stationMap)
	return
}

if (options.p) {
   	def station = options.p

	if (!stationMap.containsKey(station)) {
		println("Did not recognize the specified station: $station")
		printStations(stationMap)		
		return
	}
	// Kill any existing stream
	killStream()
	List stationList = stationMap.get(station)

	// Try a few times in case a bad stream
	int attempts = 0
	while(!isStreamPlaying()) {
		playStream(stationList)
		// Give it a bit to recognize the process is up and running
		Thread.sleep(200)
		attempts++
		if (attempts == 10) {
			println("Looks like no currently running streams for this genre :( ")
			break;
		}
	}

	println('Enjoy! Use the -k flag to stop playing.')
}

def playStream(List stationList) {
	def stationUrl = stationList.get(getRandomStation(stationList.size()))
	"/usr/bin/mpg123 ${stationUrl}".execute()
	return isStreamPlaying()
}

def getCurrentMpgProc() {
	return "pgrep mpg123".execute().text
}

def isStreamPlaying() {
	return (getCurrentMpgProc().length() > 1)
}	

def killStream() {
	if (isStreamPlaying()) {
		def mpgPs = getCurrentMpgProc()
		println("Stopping current playing stream process: $mpgPs")
		"kill -kill $mpgPs".execute()
	}
}

def printStations(stationMap) {
	stationMap.each{k, v -> println "$k"}
}

def getRandomStation(int listSize) {
    return new Random().nextInt(listSize)
}
