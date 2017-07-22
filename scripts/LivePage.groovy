import org.monome.pages.api.GroovyAPI
import org.monome.pages.api.Command
import org.monome.pages.configuration.PatternBank
import org.monome.pages.pages.ArcPage
import org.monome.pages.pages.arc.GroovyPage
import org.monome.pages.configuration.ArcConfiguration
import org.monome.pages.configuration.ArcConfigurationFactory

class LivePage extends GroovyAPI {

    int song = 0
    int recTrack = 0
    int tickNum = 0
    int measure = -1
    int velocity = 127
    int[][] velocities = new int[768][128]
    int[][] originalVelocities = new int[768][128]
    float[] bpms = [ 133.0f, 132.0f, 165.0f, 120.0f, 120.0f, 120.0f, 120.0f, 120.0f ]
    String prefix = "/m0000226"

    void init() {
        log("LivePage starting up")
        for (int x = 0; x < sizeX(); x++) {
            patterns().ignore(x, sizeY() - 1)
            patterns().ignore(x, sizeY() - 2)
        }
    }

    void stop() {
        log("LivePage shutting down")
        patterns().clearIgnore()
    }

    void recordedPress(int x, int y, int val, int pattNum) {
        int note = ((y * sizeX()) + x + (song * 24)) % 128
        int channel = 0
        if (val == 1) {
            PatternBank patterns = monome().patternBanks.get(0)
            int pos = patterns.patternPosition[pattNum]
            noteOut(note, velocities[pos][note], channel, val)
        } else {
            PatternBank patterns = monome().patternBanks.get(0)
            int pos = patterns.patternPosition[pattNum]
            noteOut(note, 0, channel, val)
        }
    }

    void press(int x, int y, int val) {
        PatternBank pagePatterns = monome().patternBanks.get(0)
        int pos = pagePatterns.patterns.get(pagePatterns.curPattern).lastPosition
        if (val == 0 && y == sizeY() - 1) return
        if (y == sizeY() - 1) {
            if (x == sizeX() - 1) {
                return
            } else {
                patterns().handlePress(x);
                sendCommandToArc(new Command("activePattern", x))
                redraw()
            }
        } else if (y == sizeY() - 2) {
            led(song, y, 0)
            song = x
            abletonOut().setTempo(bpms[song])
            led(song, y, 1)
        } else {
            int note = ((y * sizeX()) + x + (song * 24)) % 128
            int channel = 0
            if (val == 1) {
                velocities[pos][note] = velocity
                originalVelocities[pos][note] = velocity
                noteOut(note, velocity, channel, val)
            } else {
                noteOut(note, 0, channel, val)
            }
        }
    }

    void redraw() {
        clear(0)
        for (int patternNum = 0; patternNum < sizeX(); patternNum++) {
            if (patterns().getPatternState(patternNum) != PatternBank.PATTERN_STATE_EMPTY) {
                led(patternNum, sizeY() - 1, 1)
            }
        }
        for (int x = 0; x < sizeX(); x++) {
            if (song == x) {
                led(x, sizeY() - 2, 1)
            } else {
                led(x, sizeY() - 2, 0)
            }
        }
    }

    void note(int num, int velo, int chan, int on) {
        num -= song * 24
        int x = (num) % sizeX()
        int y = (num) / sizeX()
        if (y == sizeY() - 1 || y == sizeY() - 2) {
            return
        }
        led(x, y, on)
    }

    void clock() {
        if (tickNum % 24 > 0 && measure != 0) {
            led(sizeX() - 1, sizeY() - 1, 0)
        }
        if (tickNum % 24 > 12 && measure == 0) {
            led(sizeX() - 1, sizeY() - 1, 0)
        }
        if (tickNum % 24 == 0) { 
            led(sizeX() - 1, sizeY() - 1, 1)
            measure++
            if (measure == 4) measure = 0
        }
        tickNum++
        if (tickNum == 96) {
            tickNum = 0
        }
    }

    void clockReset() {
        tickNum = 0
        measure = -1
    }

    void sendCommand(Command cmd) {
        if (cmd.getCmd().equalsIgnoreCase("velocity")) {
            velocity = (Integer) cmd.getParam()
        }
        if (cmd.getCmd().equalsIgnoreCase("resetPlayhead")) {
            for (int step = 0; step < 768; step++) {
                for (int note = 0; note < 128; note++) {
                    velocities[step][note] = originalVelocities[step][note]
                }
            }
        }
        if (cmd.getCmd().equalsIgnoreCase("offsetPattern")) {
            ArrayList<Integer> args = (ArrayList<Integer>) cmd.getParam()
            int delta = args.get(0)
            int patternNum = args.get(1)
            PatternBank patterns = monome().patternBanks.get(0)
            int pos = patterns.patternPosition[patternNum]
            int length = patterns.patternLengths[patternNum]
            int newPos = pos + delta
            if (newPos < 0) {
                newPos = length + delta
            } else if (newPos >= length) {
                newPos -= length
            }
            for (int note = 0; note < 128; note++) {
                if (velocities[pos][note] != 0) {
                    velocities[newPos][note] = originalVelocities[pos][note]
                }
            }
        }
    }

    void sendCommandToArc(Command command) {
        ArcConfiguration arc = getMyArc()
        if (arc == null) return
        ArcPage page = arc.pages.get(arc.curPage)
        if (page instanceof GroovyPage) {
            ((GroovyPage) page).theApp.sendCommand(command)
        }
    }

    ArcConfiguration getMyArc() {
        ArcConfiguration arc = ArcConfigurationFactory.getArcConfiguration(prefix)
        return arc
    }
}
