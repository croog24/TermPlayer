File radioStationFile = new File("./RadioStations.txt")
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
}

def options = cli.parse(args)

if (!options || options.h) {
	cli.usage()
	return
}

if (options.l) {
	printStations(stationMap)
}

if (options.p) {
	if (!stationMap.containsKey(options.p)) {
		println("Did not recognize the specified station: ${options.p}")
		printStations(stationMap)		
		System.exit(1)
	}

	def sout = new StringBuilder() 
	def serr = new StringBuilder()
	String station = stationMap.get(options.p).get(0)
	String command = "/usr/bin/mpg123"
	def process = [command, station]
	process.execute()
}

void printStations(stationMap) {
	stationMap.each{k, v -> println "${k}"}
}
