import org.monome.pages.api.GroovyAPI
import org.monome.pages.configuration.PatternBank
import org.monome.pages.ableton.AbletonTrack;
import org.monome.pages.ableton.AbletonLooper;

class MIDIChannelooperPage extends GroovyAPI {

  int baseMidiChannel = 0
  int song = 0
  int tickLed = 1
  int tickNum = 0
  def notes = []
  float[] bpms = [ 109.0f, 132.0f, 165.0f, 120.0f, 120.0f, 120.0f, 120.0f, 120.0f ]

  void init() {
    log("MIDIChannelooperPage starting up")
    int length = 0;
    for (int x = 0; x < sizeX(); x++) {
      patterns().ignore(x, sizeY() - 1)
      patterns().ignore(x, sizeY() - 2)
      patterns().ignore(x, sizeY() - 3)
      patterns().ignore(x, sizeY() - 4)
      notes[x] = []
      for (int y = 0; y < sizeY(); y++) {
        notes[x][y] = 0
      }
      if (x % 2 == 0) {
        length += 2
      }
      patterns().patternLengths[x] = length * 96
    }
    abletonOut().setTempo(bpms[0])
  }

  void stop() {
    log("MIDIChannelooperPage shutting down")
    patterns().clearIgnore()
  }

  void clock() {
    if (tickNum % 24 <= 6) {
      tickLed = 1
    } else {
      tickLed = 0
    }
    redrawLooperState()
    tickNum++
    if (tickNum == 96) {
      tickNum = 0
    }
  }

  void clockReset() {
    tickNum = 0
    tickLed = 1
  }

  void press(int x, int y, int val) {
    if (y == sizeY() - 1 && val == 1) {
      if (x == sizeX() - 1) {
        return
      }
      patterns().handlePress(x)
      redraw()
    } else if (y == sizeY() - 1 && val == 0) {
      return
    } else if (y == sizeY() - 2 && val == 1) {
      // change chain
      ccOut(77, x * 16, 0)
      song = x
      abletonOut().setTempo(bpms[song])
      redraw()
    } else if (y == sizeY() - 2 && val == 0) {
      return
    } else {
      int note = ((y * sizeY()) + x)
      int channel = baseMidiChannel
      if (y == sizeY() - 3 || y == sizeY() - 4) {
        channel = 10
      }
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
      led(x, sizeY() - 2, x == song ? 1 : 0)            
      led(x, sizeY() - 1, patterns().getPatternState(x) == PatternBank.PATTERN_STATE_EMPTY ? 0 : 1)
    }
  }

  void redrawLooperState() {
    def tracks = ableton().getTracks();
    def keys = tracks.keySet();
    def looperNum = 0
    for (Integer i : keys) {
      def track = ableton().getTrack(i)
      if (!track) {
        continue
      }
      def loopers = track.getLoopers()
      def loopKeys = loopers.keySet();
      for (Integer j : loopKeys) {
        def looper = track.getLooper(j)
        def state = looper.getState()
        if (state == AbletonLooper.STATE_STOPPED) {
          led((looperNum * 4), 4, 0);
        } else if (state == AbletonLooper.STATE_RECORDING) {
          led((looperNum * 4), 4, tickLed);
        } else if (state == AbletonLooper.STATE_PLAYING) {
          led((looperNum * 4), 4, 1);
        } else if (state == AbletonLooper.STATE_OVERDUB) {
          led((looperNum * 4), 4, tickLed);
        }
        looperNum++
      }
    }
  }

  void note(int num, int velo, int chan, int on) {
    int x = num % sizeX()
    int y = num / sizeY()
    if (y > sizeY() - 3) {
      return
    }
    led(x, y, on)
    notes[x][y] = on
  }

  void handleAbletonEvent() {
    redrawLooperState()
  }
}
      
