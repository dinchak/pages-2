import org.monome.pages.api.GroovyAPI
import org.monome.pages.configuration.PatternBank

class MIDIChannelerPage extends GroovyAPI {

    boolean overdubButton = false

    int baseMidiChannel = 0
    def notes = []

    void init() {
        log("MIDIChannelerPage starting up")
        int length = 0;
        for (int x = 0; x < sizeX(); x++) {
            patterns().ignore(x, sizeY() - 1)
            notes[x] = []
            for (int y = 0; y < sizeY(); y++) {
                notes[x][y] = 0
            }
            if (x % 2 == 0) {
                length += 2
            }
            patterns().patternLengths[x] = length * 96
        }
        if (overdubButton) {
            patterns().ignore(sizeX() - 1, sizeY() - 2)
        }
    }

    void stop() {
        log("MIDIChannelerPage shutting down")
        patterns().clearIgnore()
    }

    void press(int x, int y, int val) {
        if (y == sizeY() - 1 && val == 1) {
            if (x == sizeX() - 1) {
                return
            }
            patterns().handlePress(x);
            redraw()
        } else if (y == sizeY() - 1 && val == 0) {
            return
        } else if (y == sizeY() - 2 && val == 1) {
            if (x == sizeX() - 1 && overdubButton) {
                abletonOut().setOverdub(Math.abs(ableton().getOverdub() - 1))
            } else {
                baseMidiChannel = x
                redraw()
            }
        } else if (y == sizeY() - 2 && val == 0) {
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
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY() - 1; y++) {
                led(x, y, notes[x][y])
            }
        }
        for (int x = 0; x < sizeX(); x++) {
            if (x == sizeX() - 1 && overdubButton) {
                redrawOverdubButton()
            }
            led(x, sizeY() - 2, x == baseMidiChannel ? 1 : 0)            
            led(x, sizeY() - 1, patterns().getPatternState(x) == PatternBank.PATTERN_STATE_EMPTY ? 0 : 1)
        }
    }

    void redrawOverdubButton() {
        led(sizeX() - 1, sizeY() - 2, ableton().getOverdub())
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

    void handleAbletonEvent() {
        redrawOverdubButton()
    }
}
