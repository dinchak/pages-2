import org.monome.pages.api.GroovyAPI

class ChordPage extends GroovyAPI {

    int[] majorScale = [2, 2, 1, 2, 2, 2, 1]
    int key = 3
    int octave = 4
    def keysDown = [:]

    void init() {
        log("ChordPage starting up")
    }

    void stop() {
        log("ChordPage shutting down")
    }

    void press(int x, int y, int val) {
        if (y > 0) {
            led(x, y, val)
            int buttonId = (y * sizeX()) + x
            int note1 = calcNote(((int) buttonId % 14), val)
            note1 = manageNote(buttonId, note1, val)
            int note2 = calcNote(((int) buttonId / 14), val)
            note2 = manageNote(buttonId, note2, val)
            noteOut(note1, 127, 0, val)
            noteOut(note2, 127, 0, val)
        }
    }

    int calcNote(int steps, int val) {
        int note = key + (octave * 12)
        for (int step = 0; step < steps; step++) {
            note += majorScale[step % 7]
        }
        while (keysDown.containsKey(note)) {
            for (int step = steps; step < steps + 2; step++) {
                note += majorScale[step % 7]
            }
        }
        return note
    }

    int manageNote(int buttonId, int note, int val) {
        if (val == 1) {
            keysDown[note] = buttonId
            return note
        } else {
            keysDown.eachWithIndex { it, key ->
                if (keysDown.key == buttonId) {
                    it.remove()
                    return key
                }
            }
        }    
    }

    void redrawDevice() {
        clear(0)
        led(0, 0, 1)
        row(1, 255, 255)
        col(2, 255, 255)
    }

    void note(int num, int velo, int chan, int on) {
        noteOut(num, velo, chan, on)
    }

    void cc(int num, int val, int chan) {
        ccOut(num, val, chan)
    }

    void clock() {
        clockOut()
    }

    void clockReset() {
        clockResetOut()
    }
}