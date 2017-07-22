import org.monome.pages.api.GroovyAPI
import org.monome.pages.configuration.PatternBank

class MIDIFingersPage extends GroovyAPI {

    int baseMidiChannel = 0
    def notes = []

    void init() {
        log("MIDIFingersPage starting up")
        for (int x = 0; x < sizeX(); x++) {
            patterns().ignore(x, sizeY() - 1)
            notes[x] = []
            for (int y = 0; y < sizeY(); y++) {
                notes[x][y] = 0
            }
        }
    }

    void stop() {
        log("MIDIFingersPage shutting down")
        patterns().clearIgnore()
    }

    void press(int x, int y, int val) {
        if (y == sizeY() - 1 && val == 1) {
            if (x < sizeX() / 2) {
                monome().switchPage(x)
            } else if (x == sizeX() - 1) {
                return
            } else {
                int patternNum = x - sizeX() / 2
                patterns().handlePress(patternNum);
                redraw()
            }
        } else if (y == sizeY() - 1 && val == 0) {
            return
        } else {
            int note = ((y * sizeY()) + x)
            int channel = baseMidiChannel + (note / 128)
            if (channel > 15) channel = 0
            note = note % 128
            noteOut(note, 127, channel, val)
        }
    }

    void redraw() {
        for (int x = 0; x < sizeX() / 2; x++) {
            led(x, sizeY() - 1, monome().curPage == x ? 1 : 0)
        }

        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY() - 1; y++) {
                led(x, y, notes[x][y])
            }
        }
        for (int patternNum = 0; patternNum < sizeX() / 2; patternNum++) {
            int x = patternNum + sizeX() / 2
            if (patterns().getPatternState(patternNum) != PatternBank.PATTERN_STATE_EMPTY) {
                led(x, sizeY() - 1, 1)
            } else {
                led(x, sizeY() - 1, 0)
            }
        }
    }

    void note(int num, int velo, int chan, int on) {
        int baseChan = baseMidiChannel
        if (baseChan == 15 && chan == 0) {
            baseChan = -1
        }
        num += (chan - baseChan) * 128
        if (num < 0 || num > 255) {
            return
        }
        int x = num % sizeX()
        int y = num / sizeY()
        if (y > sizeY() - 3) {
            return
        }
        led(x, y, on)
        notes[x][y] = on
    }
}
